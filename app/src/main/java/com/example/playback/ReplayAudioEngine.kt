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
import com.example.model.FourTrackState
import com.example.model.PortaChannelState
import com.example.model.RepeatMode
import com.example.model.TapeSide
import com.example.model.Track
import com.example.model.VinylState
import kotlin.math.PI
import kotlin.math.sin
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

    private val _vinylState = MutableStateFlow(VinylState())
    val vinylState: StateFlow<VinylState> = _vinylState.asStateFlow()

    private val _fourTrackState = MutableStateFlow(FourTrackState())
    val fourTrackState: StateFlow<FourTrackState> = _fourTrackState.asStateFlow()

    // AB Repeat and Sleep Timer
    private var sleepTimerJob: Job? = null

    // DJ Hot Cues (Cue 1 to 4)
    private val _hotCues = MutableStateFlow<Map<Int, Long>>(
        mapOf(1 to 0L, 2 to 15000L, 3 to 30000L, 4 to 45000L)
    )
    val hotCues: StateFlow<Map<Int, Long>> = _hotCues.asStateFlow()

    // Tap Tempo BPM
    private val tapTimestamps = mutableListOf<Long>()
    private val _detectedBpm = MutableStateFlow(128)
    val detectedBpm: StateFlow<Int> = _detectedBpm.asStateFlow()

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

            // AB Repeat loop check
            val ab = _playbackState.value.abRepeat
            if (ab.isEnabled && ab.pointA != null && ab.pointB != null && ab.pointB > ab.pointA) {
                if (pos >= ab.pointB) {
                    seekTo(ab.pointA)
                }
            }

            // Wow & Flutter analog tape pitch modulation
            val isWowFlutter = effectsManager.tapeDegradation.value.wowFlutterEnabled
            if (isWowFlutter) {
                val wobble = 1.0f + (0.007f * sin(pos / 250.0)).toFloat()
                mainPlayer.playbackParameters = PlaybackParameters(wobble * _playbackState.value.playbackSpeed)
            }

            effectsManager.updateVisualizerFrame(isPlaying = true, positionMs = pos)
            updateDjDeckAState()

            // Vinyl Platter rotation
            val spinDelta = if (_vinylState.value.rpm == 45) 8.5f else 6.3f
            _vinylState.value = _vinylState.value.copy(
                platterRotationDeg = (_vinylState.value.platterRotationDeg + spinDelta) % 360f
            )

            // Four-track Portastudio VU meter dynamics
            val curVuL = effectsManager.vuMeterLeft.value
            val curVuR = effectsManager.vuMeterRight.value
            val ft = _fourTrackState.value
            val updatedChannels = ft.channels.mapIndexed { idx, ch ->
                val base = if (idx % 2 == 0) curVuL else curVuR
                val dynamicLevel = (base * ch.faderLevel * (0.8f + idx * 0.1f)).coerceIn(0.05f, 0.98f)
                ch.copy(vuLevel = dynamicLevel)
            }
            _fourTrackState.value = ft.copy(channels = updatedChannels)
        } else {
            effectsManager.updateVisualizerFrame(isPlaying = false, positionMs = _playbackState.value.positionMs)
            val ft = _fourTrackState.value
            val decayedChannels = ft.channels.map { ch ->
                ch.copy(vuLevel = (ch.vuLevel * 0.85f).coerceAtLeast(0.05f))
            }
            _fourTrackState.value = ft.copy(channels = decayedChannels)
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
        
        // Apply per-song volume offset to AudioEffectsManager
        effectsManager.setPerSongGainOffset(track.volumeGainOffsetDb)
        val updatedDiag = _playbackState.value.audioDiagnostics.copy(
            perSongGainOffsetDb = track.volumeGainOffsetDb
        )

        _playbackState.value = _playbackState.value.copy(
            currentTrack = track,
            queue = queue,
            queueIndex = qIndex,
            positionMs = 0L,
            durationMs = track.durationMs,
            audioDiagnostics = updatedDiag
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

    // AB REPEAT CONTROL
    fun setAbPointA(posMs: Long = mainPlayer.currentPosition) {
        val curAb = _playbackState.value.abRepeat
        _playbackState.value = _playbackState.value.copy(
            abRepeat = curAb.copy(isEnabled = true, pointA = posMs)
        )
    }

    fun setAbPointB(posMs: Long = mainPlayer.currentPosition) {
        val curAb = _playbackState.value.abRepeat
        val finalPointB = if (curAb.pointA != null && posMs <= curAb.pointA) curAb.pointA + 1000L else posMs
        _playbackState.value = _playbackState.value.copy(
            abRepeat = curAb.copy(isEnabled = true, pointB = finalPointB)
        )
    }

    fun clearAbRepeat() {
        _playbackState.value = _playbackState.value.copy(
            abRepeat = com.example.model.AbRepeatState(isEnabled = false, pointA = null, pointB = null)
        )
    }

    // SLEEP TIMER WITH GENTLE 30-SECOND FADEOUT
    fun startSleepTimer(minutes: Int) {
        cancelSleepTimer()
        val totalSec = minutes * 60
        _playbackState.value = _playbackState.value.copy(
            sleepTimer = com.example.model.SleepTimerState(
                isRunning = true,
                totalMinutes = minutes,
                remainingSeconds = totalSec,
                fadeoutActive = false
            )
        )

        sleepTimerJob = scope.launch {
            var rem = totalSec
            val initialVol = mainPlayer.volume
            while (rem > 0 && isActive) {
                delay(1000L)
                rem--
                val isFade = rem <= 30
                if (isFade) {
                    val fadeFrac = (rem.toFloat() / 30f).coerceIn(0f, 1f)
                    mainPlayer.volume = initialVol * fadeFrac
                }
                _playbackState.value = _playbackState.value.copy(
                    sleepTimer = _playbackState.value.sleepTimer.copy(
                        remainingSeconds = rem,
                        fadeoutActive = isFade
                    )
                )
            }
            if (rem <= 0) {
                stop()
                mainPlayer.volume = initialVol
                _playbackState.value = _playbackState.value.copy(
                    sleepTimer = com.example.model.SleepTimerState(isRunning = false, remainingSeconds = 0)
                )
            }
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        _playbackState.value = _playbackState.value.copy(
            sleepTimer = com.example.model.SleepTimerState(isRunning = false, remainingSeconds = 0)
        )
    }

    // CROSSFADE DURATION (0, 2, 4, 8, 12 seconds)
    fun setCrossfadeDuration(sec: Int) {
        _playbackState.value = _playbackState.value.copy(
            crossfadeDurationSec = sec.coerceIn(0, 15)
        )
    }

    // DJ HOT CUES (1 to 4)
    fun setHotCue(cueIndex: Int, posMs: Long = mainPlayer.currentPosition) {
        val current = _hotCues.value.toMutableMap()
        current[cueIndex] = posMs
        _hotCues.value = current
    }

    fun jumpToHotCue(cueIndex: Int) {
        val target = _hotCues.value[cueIndex] ?: return
        seekTo(target)
    }

    // DJ PITCH BEND NUDGE (+/- 4% momentary nudge)
    fun nudgePitch(deltaPercent: Float) {
        val currentSpeed = _playbackState.value.playbackSpeed
        val nudgedSpeed = (currentSpeed + deltaPercent / 100f).coerceIn(0.5f, 2.0f)
        mainPlayer.playbackParameters = PlaybackParameters(nudgedSpeed)
        scope.launch {
            delay(250)
            mainPlayer.playbackParameters = PlaybackParameters(currentSpeed)
        }
    }

    // TAP TEMPO BPM DETECTOR
    fun tapTempo() {
        val now = System.currentTimeMillis()
        tapTimestamps.add(now)
        if (tapTimestamps.size > 8) {
            tapTimestamps.removeAt(0)
        }
        if (tapTimestamps.size >= 2) {
            val intervals = mutableListOf<Long>()
            for (i in 1 until tapTimestamps.size) {
                val diff = tapTimestamps[i] - tapTimestamps[i - 1]
                if (diff in 200..2000) {
                    intervals.add(diff)
                }
            }
            if (intervals.isNotEmpty()) {
                val avgDiffMs = intervals.average()
                val calculatedBpm = (60000.0 / avgDiffMs).toInt().coerceIn(60, 220)
                _detectedBpm.value = calculatedBpm
            }
        }
    }

    // VINTAGE MPC SAMPLER SOUND GENERATOR (Zero dependencies, pure offline audio synthesis)
    fun triggerSamplerSound(soundType: String) {
        scope.launch(Dispatchers.Default) {
            try {
                val sampleRate = 22050
                val durationSec = when (soundType) {
                    "AIRHORN" -> 0.45f
                    "VINYL_BRAKE" -> 0.50f
                    "TR808_SNARE" -> 0.25f
                    "TAPE_REWIND" -> 0.35f
                    else -> 0.20f
                }
                val numSamples = (sampleRate * durationSec).toInt()
                val buffer = ShortArray(numSamples)
                val random = java.util.Random()

                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    val sampleValue: Double = when (soundType) {
                        "AIRHORN" -> {
                            val env = if (t < 0.05) t / 0.05 else (1.0 - (t - 0.05) / 0.40).coerceAtLeast(0.0)
                            val tone1 = sin(2.0 * PI * 680.0 * t)
                            val tone2 = sin(2.0 * PI * 850.0 * t)
                            (tone1 * 0.5 + tone2 * 0.5) * env
                        }
                        "VINYL_BRAKE" -> {
                            val freq = (600.0 * (1.0 - (t / durationSec))).coerceAtLeast(40.0)
                            val env = (1.0 - (t / durationSec)).coerceAtLeast(0.0)
                            sin(2.0 * PI * freq * t) * env
                        }
                        "TR808_SNARE" -> {
                            val env = (1.0 - t / durationSec).coerceAtLeast(0.0)
                            val tone = sin(2.0 * PI * 180.0 * t) * env * 0.4
                            val noise = (random.nextDouble() * 2.0 - 1.0) * env * 0.6
                            tone + noise
                        }
                        "TAPE_REWIND" -> {
                            val freq = 1200.0 + 800.0 * sin(2.0 * PI * 15.0 * t)
                            val noise = (random.nextDouble() * 0.4 - 0.2)
                            val env = (1.0 - t / durationSec).coerceAtLeast(0.0)
                            (sin(2.0 * PI * freq * t) * 0.7 + noise) * env
                        }
                        else -> sin(2.0 * PI * 440.0 * t) * (1.0 - t / durationSec).coerceAtLeast(0.0)
                    }
                    buffer[i] = (sampleValue.coerceIn(-1.0, 1.0) * 30000).toInt().toShort()
                }

                @Suppress("DEPRECATION")
                val audioTrack = android.media.AudioTrack(
                    android.media.AudioManager.STREAM_MUSIC,
                    sampleRate,
                    android.media.AudioFormat.CHANNEL_OUT_MONO,
                    android.media.AudioFormat.ENCODING_PCM_16BIT,
                    buffer.size * 2,
                    android.media.AudioTrack.MODE_STATIC
                )
                audioTrack.write(buffer, 0, buffer.size)
                audioTrack.play()
                delay((durationSec * 1000).toLong() + 60L)
                audioTrack.release()
            } catch (e: Exception) {
                Log.e(TAG, "Error playing sampler sound $soundType", e)
            }
        }
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

    fun ejectCassette() {
        mainPlayer.pause()
        _playbackState.value = _playbackState.value.copy(
            isCassetteInserted = false,
            isPlaying = false
        )
    }

    fun insertCassette() {
        _playbackState.value = _playbackState.value.copy(
            isCassetteInserted = true
        )
    }

    // VINYL TURNTABLE OPERATIONS
    fun setVinylRpm(rpm: Int) {
        val validRpm = if (rpm == 45) 45 else 33
        _vinylState.value = _vinylState.value.copy(rpm = validRpm)
    }

    fun setVinylPitch(pitchPercent: Float) {
        val clamped = pitchPercent.coerceIn(-8.0f, 8.0f)
        _vinylState.value = _vinylState.value.copy(pitchPercent = clamped)
        val actualSpeed = (1.0f + clamped / 100f).coerceIn(0.5f, 2.0f)
        mainPlayer.playbackParameters = PlaybackParameters(actualSpeed)
        _playbackState.value = _playbackState.value.copy(playbackSpeed = actualSpeed)
    }

    fun cueTonearm(cued: Boolean) {
        _vinylState.value = _vinylState.value.copy(isTonearmCued = cued)
        if (cued) {
            // Needle dropped on vinyl -> resume playback
            if (_playbackState.value.currentTrack != null) {
                mainPlayer.play()
            }
        } else {
            // Tonearm lifted -> pause
            mainPlayer.pause()
        }
    }

    fun scratchVinyl(deltaDeg: Float) {
        val deltaMs = (deltaDeg * 25f).toLong()
        seekRelative(deltaMs)
        _vinylState.value = _vinylState.value.copy(
            platterRotationDeg = (_vinylState.value.platterRotationDeg + deltaDeg) % 360f
        )
    }

    // FOUR-TRACK PORTASTUDIO OPERATIONS
    fun setFourTrackSpeed(isHigh: Boolean) {
        _fourTrackState.value = _fourTrackState.value.copy(isHighSpeed = isHigh)
        // 9.5 cm/s = double speed studio playback; 4.75 cm/s = standard speed
        val speed = if (isHigh) 1.5f else 1.0f
        mainPlayer.playbackParameters = PlaybackParameters(speed)
        _playbackState.value = _playbackState.value.copy(playbackSpeed = speed)
    }

    fun setFourTrackPitch(pitchPercent: Float) {
        val clamped = pitchPercent.coerceIn(-12.0f, 12.0f)
        _fourTrackState.value = _fourTrackState.value.copy(pitchKnob = clamped)
        val baseSpeed = if (_fourTrackState.value.isHighSpeed) 1.5f else 1.0f
        val finalSpeed = (baseSpeed * (1.0f + clamped / 100f)).coerceIn(0.5f, 2.0f)
        mainPlayer.playbackParameters = PlaybackParameters(finalSpeed)
        _playbackState.value = _playbackState.value.copy(playbackSpeed = finalSpeed)
    }

    fun setFourTrackMasterFader(fader: Float) {
        val clamped = fader.coerceIn(0.0f, 1.0f)
        _fourTrackState.value = _fourTrackState.value.copy(masterFader = clamped)
        mainPlayer.volume = clamped
    }

    fun setFourTrackChannelFader(channelNum: Int, level: Float) {
        val ft = _fourTrackState.value
        val updated = ft.channels.map { ch ->
            if (ch.channelNumber == channelNum) ch.copy(faderLevel = level.coerceIn(0f, 1f)) else ch
        }
        _fourTrackState.value = ft.copy(channels = updated)
    }

    fun toggleFourTrackRecArm(channelNum: Int) {
        val ft = _fourTrackState.value
        val updated = ft.channels.map { ch ->
            if (ch.channelNumber == channelNum) ch.copy(isArmedForRec = !ch.isArmedForRec) else ch
        }
        _fourTrackState.value = ft.copy(channels = updated)
    }

    fun toggleFourTrackDbx() {
        val next = !_fourTrackState.value.isDbxNoiseReductionOn
        _fourTrackState.value = _fourTrackState.value.copy(isDbxNoiseReductionOn = next)
        effectsManager.setEnabled(next)
    }

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
