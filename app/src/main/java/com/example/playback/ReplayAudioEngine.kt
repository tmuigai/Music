package com.example.playback

import android.content.Context
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.model.DjDeckState
import com.example.model.DjSingleDeck
import com.example.model.HardwareNavScreen
import com.example.model.PlaybackState
import com.example.model.RepeatMode
import com.example.model.TapeSide
import com.example.model.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.abs

class ReplayAudioEngine(
    private val context: Context,
    val effectsManager: AudioEffectsManager = AudioEffectsManager()
) {
    companion object {
        private const val TAG = "ReplayAudioEngine"
    }

    private val scope = CoroutineScope(Dispatchers.Main + Job())

    // Player for Main Deck / Deck A
    private val mainPlayer: ExoPlayer = ExoPlayer.Builder(context)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .setUsage(C.USAGE_MEDIA)
                .build(),
            true
        )
        .setHandleAudioBecomingNoisy(true)
        .build()

    // Player for DJ Deck B
    private val djPlayerB: ExoPlayer = ExoPlayer.Builder(context)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .setUsage(C.USAGE_MEDIA)
                .build(),
            true
        )
        .build()

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _djState = MutableStateFlow(DjDeckState())
    val djState: StateFlow<DjDeckState> = _djState.asStateFlow()

    // Cassette Side A / Side B tracks
    private var currentCassetteSide: TapeSide = TapeSide.SIDE_A
    private var cassetteSideATracks: List<Track> = emptyList()
    private var cassetteSideBTracks: List<Track> = emptyList()

    // Recording Booth
    private var mediaRecorder: MediaRecorder? = null
    private var currentRecordingFile: File? = null
    private var recordingStartTimeMs: Long = 0L
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()
    private val _recordingDurationMs = MutableStateFlow(0L)
    val recordingDurationMs: StateFlow<Long> = _recordingDurationMs.asStateFlow()
    private val _recordingAmplitude = MutableStateFlow(0f)
    val recordingAmplitude: StateFlow<Float> = _recordingAmplitude.asStateFlow()

    init {
        mainPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    effectsManager.attachAudioSession(mainPlayer.audioSessionId)
                    _playbackState.value = _playbackState.value.copy(
                        durationMs = mainPlayer.duration.coerceAtLeast(0L)
                    )
                } else if (state == Player.STATE_ENDED) {
                    onTrackEnded()
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _playbackState.value = _playbackState.value.copy(isPlaying = isPlaying)
                updateDjDeckAState()
            }
        })

        djPlayerB.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updateDjDeckBState()
            }
        })

        // Ticker loop for reel rotation, tape counter, visualizer, CD spin, and recording amplitude
        scope.launch {
            while (isActive) {
                delay(33) // ~30fps smooth updates
                tick()
            }
        }
    }

    private fun tick() {
        // Main player update
        if (mainPlayer.isPlaying) {
            val pos = mainPlayer.currentPosition
            val dur = mainPlayer.duration.coerceAtLeast(1L)
            val progress = (pos.toFloat() / dur.toFloat()).coerceIn(0f, 1f)
            val tapeCounter = ((pos / 1000) * 3).toInt() % 10000
            val cdAngle = (_playbackState.value.cdRotationAngle + 6f) % 360f

            _playbackState.value = _playbackState.value.copy(
                positionMs = pos,
                durationMs = dur,
                tapeCounter = tapeCounter,
                tapeReelProgress = progress,
                cdRotationAngle = cdAngle
            )

            effectsManager.updateVisualizerFrame(isPlaying = true, positionMs = pos)
            updateDjDeckAState()
        } else {
            effectsManager.updateVisualizerFrame(isPlaying = false, positionMs = _playbackState.value.positionMs)
        }

        // DJ Deck B update
        if (djPlayerB.isPlaying) {
            updateDjDeckBState()
        }

        // Recording amplitude update
        if (_isRecording.value && mediaRecorder != null) {
            try {
                val maxAmp = mediaRecorder?.maxAmplitude ?: 0
                val normalized = (maxAmp / 32767f).coerceIn(0f, 1f)
                _recordingAmplitude.value = normalized
                _recordingDurationMs.value = System.currentTimeMillis() - recordingStartTimeMs
            } catch (e: Exception) {
                // Ignore transient recording poll errors
            }
        }
    }

    // Playback controls
    fun playTrack(track: Track, queue: List<Track> = listOf(track)) {
        val qIndex = queue.indexOfFirst { it.id == track.id }.let { if (it >= 0) it else 0 }
        _playbackState.value = _playbackState.value.copy(
            currentTrack = track,
            queue = queue,
            queueIndex = qIndex,
            positionMs = 0L,
            durationMs = track.durationMs
        )

        loadMediaToPlayer(mainPlayer, track.contentUri)
        mainPlayer.play()

        // Also sync DJ Deck A
        _djState.value = _djState.value.copy(
            deckA = _djState.value.deckA.copy(track = track, positionMs = 0L)
        )
    }

    fun togglePlayPause() {
        if (mainPlayer.isPlaying) {
            mainPlayer.pause()
        } else {
            if (_playbackState.value.currentTrack != null) {
                mainPlayer.play()
            } else if (_playbackState.value.queue.isNotEmpty()) {
                val first = _playbackState.value.queue[0]
                playTrack(first, _playbackState.value.queue)
            }
        }
    }

    fun pause() {
        mainPlayer.pause()
    }

    fun stop() {
        mainPlayer.stop()
        mainPlayer.seekTo(0)
        _playbackState.value = _playbackState.value.copy(
            isPlaying = false,
            positionMs = 0L
        )
    }

    fun seekTo(positionMs: Long) {
        val clamped = positionMs.coerceIn(0L, _playbackState.value.durationMs)
        mainPlayer.seekTo(clamped)
        _playbackState.value = _playbackState.value.copy(positionMs = clamped)
    }

    fun seekRelative(deltaMs: Long) {
        val current = mainPlayer.currentPosition
        val target = (current + deltaMs).coerceIn(0L, mainPlayer.duration.coerceAtLeast(0L))
        mainPlayer.seekTo(target)
    }

    fun nextTrack() {
        val state = _playbackState.value
        if (state.queue.isEmpty()) return

        var nextIndex = state.queueIndex + 1
        if (nextIndex >= state.queue.size) {
            if (state.repeatMode == RepeatMode.ALL) {
                nextIndex = 0
            } else {
                mainPlayer.stop()
                return
            }
        }
        val nextTrack = state.queue[nextIndex]
        playTrack(nextTrack, state.queue)
    }

    fun previousTrack() {
        val state = _playbackState.value
        if (state.queue.isEmpty()) return

        // If played more than 3 seconds, restart current track
        if (mainPlayer.currentPosition > 3000) {
            seekTo(0)
            return
        }

        var prevIndex = state.queueIndex - 1
        if (prevIndex < 0) {
            prevIndex = if (state.repeatMode == RepeatMode.ALL) state.queue.size - 1 else 0
        }
        val prevTrack = state.queue[prevIndex]
        playTrack(prevTrack, state.queue)
    }

    fun toggleShuffle() {
        val nextShuffle = !_playbackState.value.isShuffle
        val currentQ = _playbackState.value.queue
        val currentTrk = _playbackState.value.currentTrack

        val newQueue = if (nextShuffle) {
            val list = currentQ.toMutableList()
            if (currentTrk != null) list.remove(currentTrk)
            list.shuffle()
            if (currentTrk != null) list.add(0, currentTrk)
            list
        } else {
            currentQ.sortedBy { it.title }
        }

        val newIndex = newQueue.indexOfFirst { it.id == currentTrk?.id }.coerceAtLeast(0)
        _playbackState.value = _playbackState.value.copy(
            isShuffle = nextShuffle,
            queue = newQueue,
            queueIndex = newIndex
        )
    }

    fun cycleRepeatMode() {
        val current = _playbackState.value.repeatMode
        val next = when (current) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        _playbackState.value = _playbackState.value.copy(repeatMode = next)
    }

    fun setPlaybackSpeed(speed: Float) {
        val clamped = speed.coerceIn(0.5f, 2.0f)
        mainPlayer.playbackParameters = PlaybackParameters(clamped)
        _playbackState.value = _playbackState.value.copy(playbackSpeed = clamped)
    }

    private fun onTrackEnded() {
        val state = _playbackState.value
        when (state.repeatMode) {
            RepeatMode.ONE -> {
                seekTo(0)
                mainPlayer.play()
            }
            RepeatMode.ALL -> nextTrack()
            RepeatMode.OFF -> {
                if (state.queueIndex < state.queue.size - 1) {
                    nextTrack()
                } else {
                    _playbackState.value = _playbackState.value.copy(isPlaying = false, positionMs = 0)
                }
            }
        }
    }

    // Cassette Flipping
    fun setCassetteTapes(sideA: List<Track>, sideB: List<Track>) {
        cassetteSideATracks = sideA
        cassetteSideBTracks = sideB
        currentCassetteSide = TapeSide.SIDE_A
        if (sideA.isNotEmpty()) {
            playTrack(sideA[0], sideA)
        }
    }

    fun flipCassetteSide(): TapeSide {
        currentCassetteSide = if (currentCassetteSide == TapeSide.SIDE_A) TapeSide.SIDE_B else TapeSide.SIDE_A
        val targetTracks = if (currentCassetteSide == TapeSide.SIDE_A) cassetteSideATracks else cassetteSideBTracks
        if (targetTracks.isNotEmpty()) {
            playTrack(targetTracks[0], targetTracks)
        }
        return currentCassetteSide
    }

    fun getCurrentCassetteSide(): TapeSide = currentCassetteSide

    // DJ DECK OPERATIONS
    fun setDjTrackA(track: Track) {
        _djState.value = _djState.value.copy(
            deckA = _djState.value.deckA.copy(track = track, positionMs = 0L)
        )
        if (_playbackState.value.currentTrack?.id != track.id) {
            playTrack(track)
        }
    }

    fun setDjTrackB(track: Track) {
        _djState.value = _djState.value.copy(
            deckB = _djState.value.deckB.copy(track = track, positionMs = 0L)
        )
        loadMediaToPlayer(djPlayerB, track.contentUri)
    }

    fun toggleDjDeckAPlay() {
        if (mainPlayer.isPlaying) mainPlayer.pause() else mainPlayer.play()
    }

    fun toggleDjDeckBPlay() {
        if (djPlayerB.isPlaying) djPlayerB.pause() else djPlayerB.play()
    }

    fun setDjCrossfader(value: Float) {
        val clamped = value.coerceIn(-1.0f, 1.0f)
        _djState.value = _djState.value.copy(crossfader = clamped)

        // Equal-power crossfader curve
        val master = _djState.value.masterVolume
        val volA = if (clamped <= 0f) master else master * (1.0f - clamped)
        val volB = if (clamped >= 0f) master else master * (1.0f + clamped)

        mainPlayer.volume = volA
        djPlayerB.volume = volB
    }

    fun setDjPitchA(pitchPercent: Float) {
        val clamped = pitchPercent.coerceIn(-16f, 16f)
        val speed = 1.0f + (clamped / 100f)
        mainPlayer.playbackParameters = PlaybackParameters(speed)
        _djState.value = _djState.value.copy(
            deckA = _djState.value.deckA.copy(pitchPercent = clamped)
        )
    }

    fun setDjPitchB(pitchPercent: Float) {
        val clamped = pitchPercent.coerceIn(-16f, 16f)
        val speed = 1.0f + (clamped / 100f)
        djPlayerB.playbackParameters = PlaybackParameters(speed)
        _djState.value = _djState.value.copy(
            deckB = _djState.value.deckB.copy(pitchPercent = clamped)
        )
    }

    // Real vinyl scratching: scrubbing moves playback position and triggers speed alteration
    fun scratchDeckA(deltaDeg: Float) {
        val deltaMs = (deltaDeg * 25).toLong()
        val target = (mainPlayer.currentPosition + deltaMs).coerceIn(0L, mainPlayer.duration.coerceAtLeast(0L))
        mainPlayer.seekTo(target)

        val scratchSpeed = (1.0f + (deltaDeg / 45f)).coerceIn(0.2f, 3.0f)
        mainPlayer.playbackParameters = PlaybackParameters(scratchSpeed)

        val newPlatter = (_djState.value.deckA.platterRotationDeg + deltaDeg) % 360f
        _djState.value = _djState.value.copy(
            deckA = _djState.value.deckA.copy(platterRotationDeg = newPlatter, positionMs = target)
        )
    }

    fun scratchDeckB(deltaDeg: Float) {
        val deltaMs = (deltaDeg * 25).toLong()
        val target = (djPlayerB.currentPosition + deltaMs).coerceIn(0L, djPlayerB.duration.coerceAtLeast(0L))
        djPlayerB.seekTo(target)

        val scratchSpeed = (1.0f + (deltaDeg / 45f)).coerceIn(0.2f, 3.0f)
        djPlayerB.playbackParameters = PlaybackParameters(scratchSpeed)

        val newPlatter = (_djState.value.deckB.platterRotationDeg + deltaDeg) % 360f
        _djState.value = _djState.value.copy(
            deckB = _djState.value.deckB.copy(platterRotationDeg = newPlatter, positionMs = target)
        )
    }

    fun setDjEq(isDeckA: Boolean, band: String, valueDb: Float) {
        val clamped = valueDb.coerceIn(-12f, 12f)
        if (isDeckA) {
            _djState.value = when (band) {
                "low" -> _djState.value.copy(eqLowA = clamped)
                "mid" -> _djState.value.copy(eqMidA = clamped)
                else -> _djState.value.copy(eqHighA = clamped)
            }
        } else {
            _djState.value = when (band) {
                "low" -> _djState.value.copy(eqLowB = clamped)
                "mid" -> _djState.value.copy(eqMidB = clamped)
                else -> _djState.value.copy(eqHighB = clamped)
            }
        }
    }

    private fun updateDjDeckAState() {
        val platter = (_djState.value.deckA.platterRotationDeg + if (mainPlayer.isPlaying) 5f else 0f) % 360f
        _djState.value = _djState.value.copy(
            deckA = _djState.value.deckA.copy(
                isPlaying = mainPlayer.isPlaying,
                positionMs = mainPlayer.currentPosition,
                platterRotationDeg = platter
            )
        )
    }

    private fun updateDjDeckBState() {
        val platter = (_djState.value.deckB.platterRotationDeg + if (djPlayerB.isPlaying) 5f else 0f) % 360f
        _djState.value = _djState.value.copy(
            deckB = _djState.value.deckB.copy(
                isPlaying = djPlayerB.isPlaying,
                positionMs = djPlayerB.currentPosition,
                platterRotationDeg = platter
            )
        )
    }

    // RECORDING BOOTH
    fun startRecording(title: String): Boolean {
        try {
            val recDir = File(context.filesDir, "recordings")
            if (!recDir.exists()) recDir.mkdirs()
            val file = File(recDir, "REC_${System.currentTimeMillis()}.mp4")
            currentRecordingFile = file

            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }

            recordingStartTimeMs = System.currentTimeMillis()
            _isRecording.value = true
            _recordingDurationMs.value = 0L
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start audio recording", e)
            _isRecording.value = false
            return false
        }
    }

    fun stopRecording(): File? {
        if (!_isRecording.value) return null
        try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping recorder", e)
        } finally {
            mediaRecorder = null
            _isRecording.value = false
        }
        return currentRecordingFile
    }

    private fun loadMediaToPlayer(player: ExoPlayer, pathOrUri: String) {
        try {
            val uri = if (pathOrUri.startsWith("content://") || pathOrUri.startsWith("http://") || pathOrUri.startsWith("https://")) {
                Uri.parse(pathOrUri)
            } else {
                Uri.fromFile(File(pathOrUri))
            }
            val mediaItem = MediaItem.fromUri(uri)
            player.setMediaItem(mediaItem)
            player.prepare()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load media: $pathOrUri", e)
        }
    }

    fun release() {
        mainPlayer.release()
        djPlayerB.release()
        effectsManager.release()
        mediaRecorder?.release()
    }
}
