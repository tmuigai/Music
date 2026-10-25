package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ReplayApplication
import com.example.data.ListeningHistoryEntity
import com.example.data.MixtapeEntity
import com.example.data.ObsessionProfileEntity
import com.example.data.RadioPresetEntity
import com.example.data.RecordingEntity
import com.example.model.EqualizerState
import com.example.model.HardwareMaterial
import com.example.model.HardwareNavScreen
import com.example.model.HardwareSkin
import com.example.model.LyricsLine
import com.example.model.PlaybackState
import com.example.model.StickerItem
import com.example.model.TapeSide
import com.example.model.Track
import com.example.playback.LrcParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.math.abs
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import kotlin.random.Random

class ReplayViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as ReplayApplication
    val repository = app.repository
    val audioEngine = app.audioEngine

    // Navigation State
    private val _currentScreen = MutableStateFlow(HardwareNavScreen.CONTROL_DECK)
    val currentScreen: StateFlow<HardwareNavScreen> = _currentScreen.asStateFlow()

    private val screenBackStack = mutableListOf<HardwareNavScreen>()

    // Current Hardware Skin Profile
    private val _skin = MutableStateFlow(HardwareSkin())
    val skin: StateFlow<HardwareSkin> = _skin.asStateFlow()

    // Radio Tuner State
    private val _radioFreqMhz = MutableStateFlow(99.1f)
    val radioFreqMhz: StateFlow<Float> = _radioFreqMhz.asStateFlow()

    private val _isRadioTuned = MutableStateFlow(true)
    val isRadioTuned: StateFlow<Boolean> = _isRadioTuned.asStateFlow()

    // Lyrics State
    private val _lyricsLines = MutableStateFlow<List<LyricsLine>>(emptyList())
    val lyricsLines: StateFlow<List<LyricsLine>> = _lyricsLines.asStateFlow()

    private val _lyricsOffsetMs = MutableStateFlow(0L)
    val lyricsOffsetMs: StateFlow<Long> = _lyricsOffsetMs.asStateFlow()

    // Cassette Customization for current tape
    private val _tapeShellColor = MutableStateFlow("#16191C")
    val tapeShellColor: StateFlow<String> = _tapeShellColor.asStateFlow()

    private val _tapeReelColor = MutableStateFlow("#00FF66")
    val tapeReelColor: StateFlow<String> = _tapeReelColor.asStateFlow()

    private val _tapeStickers = MutableStateFlow<List<StickerItem>>(
        listOf(
            StickerItem("stk_1", "90s UNDERGROUND", 0.65f, 0.20f, -5f, 1.0f),
            StickerItem("stk_2", "ACID", 0.15f, 0.72f, 8f, 0.9f)
        )
    )
    val tapeStickers: StateFlow<List<StickerItem>> = _tapeStickers.asStateFlow()

    // Discman Jewel Case Modal
    private val _showJewelCase = MutableStateFlow(false)
    val showJewelCase: StateFlow<Boolean> = _showJewelCase.asStateFlow()

    // Mixtape creation modal
    private val _showMixtapeCreator = MutableStateFlow(false)
    val showMixtapeCreator: StateFlow<Boolean> = _showMixtapeCreator.asStateFlow()

    // UI Feedback Message (e.g., "Cassette Ejected", "Skin Profile Saved", "Radio Locked")
    private val _hudMessage = MutableStateFlow<String?>(null)
    val hudMessage: StateFlow<String?> = _hudMessage.asStateFlow()

    // Data streams from repository
    val allTracks: StateFlow<List<Track>> = repository.allTracks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteTracks: StateFlow<List<Track>> = repository.favoriteTracks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mostPlayedTracks: StateFlow<List<Track>> = repository.mostPlayedTracks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentlyPlayedTracks: StateFlow<List<Track>> = repository.recentlyPlayedTracks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMixtapes: StateFlow<List<MixtapeEntity>> = repository.allMixtapes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRecordings: StateFlow<List<RecordingEntity>> = repository.allRecordings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allObsessions: StateFlow<List<ObsessionProfileEntity>> = repository.allObsessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRadioPresets: StateFlow<List<RadioPresetEntity>> = repository.allRadioPresets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val listeningHistory: StateFlow<List<ListeningHistoryEntity>> = repository.recentHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active playback states from audio engine
    val playbackState: StateFlow<PlaybackState> = audioEngine.playbackState
    val djState = audioEngine.djState
    val visualizerBands = audioEngine.effectsManager.visualizerBands
    val vuMeterLeft = audioEngine.effectsManager.vuMeterLeft
    val vuMeterRight = audioEngine.effectsManager.vuMeterRight
    val eqState = audioEngine.effectsManager.eqState
    val isRecording = audioEngine.isRecording
    val recordingDurationMs = audioEngine.recordingDurationMs
    val recordingAmplitude = audioEngine.recordingAmplitude

    init {
        // Observe track changes to auto-parse lyrics
        viewModelScope.launch {
            playbackState.collect { state ->
                val track = state.currentTrack
                if (track != null) {
                    val lrc = track.lyrics ?: ""
                    _lyricsLines.value = LrcParser.parse(lrc)
                } else {
                    _lyricsLines.value = emptyList()
                }
            }
        }
    }

    // Navigation
    fun navigateTo(screen: HardwareNavScreen) {
        if (_currentScreen.value != screen) {
            screenBackStack.add(_currentScreen.value)
            _currentScreen.value = screen
            showHud("MODE: ${screen.title}")
        }
    }

    fun navigateBack(): Boolean {
        if (screenBackStack.isNotEmpty()) {
            _currentScreen.value = screenBackStack.removeAt(screenBackStack.size - 1)
            return true
        } else if (_currentScreen.value != HardwareNavScreen.CONTROL_DECK) {
            _currentScreen.value = HardwareNavScreen.CONTROL_DECK
            return true
        }
        return false
    }

    // Playback
    fun playTrack(track: Track, queue: List<Track> = allTracks.value) {
        audioEngine.playTrack(track, queue)
        viewModelScope.launch {
            repository.recordTrackPlayed(track)
        }
    }

    fun togglePlayPause() = audioEngine.togglePlayPause()
    fun nextTrack() = audioEngine.nextTrack()
    fun previousTrack() = audioEngine.previousTrack()
    fun stopPlayback() = audioEngine.stop()
    fun seekTo(positionMs: Long) = audioEngine.seekTo(positionMs)
    fun seekRelative(deltaMs: Long) = audioEngine.seekRelative(deltaMs)
    fun toggleShuffle() = audioEngine.toggleShuffle()
    fun cycleRepeatMode() = audioEngine.cycleRepeatMode()
    fun setPlaybackSpeed(speed: Float) = audioEngine.setPlaybackSpeed(speed)

    fun toggleFavorite(track: Track) {
        viewModelScope.launch {
            repository.toggleFavorite(track.id, track.isFavorite)
            showHud(if (!track.isFavorite) "ADDED TO FAVORITES" else "REMOVED FROM FAVORITES")
        }
    }

    // Cassette Controls
    fun flipCassetteSide() {
        val newSide = audioEngine.flipCassetteSide()
        showHud("CASSETTE FLIPPED -> ${newSide.name}")
    }

    fun ejectCassette() {
        audioEngine.stop()
        showHud("CASSETTE EJECTED")
    }

    fun setTapeShellColor(hex: String) {
        _tapeShellColor.value = hex
    }

    fun setTapeReelColor(hex: String) {
        _tapeReelColor.value = hex
    }

    fun addSticker(name: String) {
        val newSticker = StickerItem(
            id = "stk_${System.currentTimeMillis()}",
            name = name,
            xPercent = Random.nextFloat() * 0.5f + 0.2f,
            yPercent = Random.nextFloat() * 0.4f + 0.2f,
            rotationDeg = (Random.nextFloat() * 20f) - 10f,
            scale = 1.0f
        )
        _tapeStickers.value = _tapeStickers.value + newSticker
        showHud("STICKER ATTACHED")
    }

    fun removeSticker(id: String) {
        _tapeStickers.value = _tapeStickers.value.filterNot { it.id == id }
    }

    // Discman Controls
    fun toggleJewelCase(show: Boolean) {
        _showJewelCase.value = show
    }

    // DJ Controls
    fun setDjTrackA(track: Track) = audioEngine.setDjTrackA(track)
    fun setDjTrackB(track: Track) = audioEngine.setDjTrackB(track)
    fun toggleDjDeckAPlay() = audioEngine.toggleDjDeckAPlay()
    fun toggleDjDeckBPlay() = audioEngine.toggleDjDeckBPlay()
    fun setDjCrossfader(value: Float) = audioEngine.setDjCrossfader(value)
    fun setDjPitchA(pitchPercent: Float) = audioEngine.setDjPitchA(pitchPercent)
    fun setDjPitchB(pitchPercent: Float) = audioEngine.setDjPitchB(pitchPercent)
    fun scratchDeckA(deltaDeg: Float) = audioEngine.scratchDeckA(deltaDeg)
    fun scratchDeckB(deltaDeg: Float) = audioEngine.scratchDeckB(deltaDeg)
    fun setDjEq(isDeckA: Boolean, band: String, valueDb: Float) = audioEngine.setDjEq(isDeckA, band, valueDb)

    // Lyrics
    fun adjustLyricsOffset(deltaMs: Long) {
        _lyricsOffsetMs.value += deltaMs
    }

    fun saveEditedLyrics(trackId: Long, rawLyrics: String) {
        viewModelScope.launch {
            repository.updateTrackLyrics(trackId, rawLyrics)
            _lyricsLines.value = LrcParser.parse(rawLyrics)
            showHud("LYRICS SAVED")
        }
    }

    // Sound Lab
    fun setEqBand(bandIndex: Int, gainDb: Int) = audioEngine.effectsManager.updateBands(bandIndex, gainDb)
    fun setBassBoost(strength: Int) = audioEngine.effectsManager.setBassBoost(strength)
    fun setVirtualizer(strength: Int) = audioEngine.effectsManager.setVirtualizer(strength)
    fun setEqEnabled(enabled: Boolean) = audioEngine.effectsManager.setEnabled(enabled)
    fun applyEqPreset(name: String) {
        audioEngine.effectsManager.applyPreset(name)
        showHud("EQ PRESET: $name")
    }

    // Mixtape Creation
    fun toggleMixtapeCreator(show: Boolean) {
        _showMixtapeCreator.value = show
    }

    fun createMixtape(
        title: String,
        label: String,
        notes: String,
        shellColor: String,
        reelColor: String,
        isTransparent: Boolean,
        sideATracks: List<Long>,
        sideBTracks: List<Long>,
        durationMinutes: Int
    ) {
        viewModelScope.launch {
            repository.saveMixtape(
                title = title,
                labelText = label,
                notes = notes,
                shellColorHex = shellColor,
                reelColorHex = reelColor,
                isTransparent = isTransparent,
                sideATracks = sideATracks,
                sideBTracks = sideBTracks,
                durationMinutes = durationMinutes
            )
            _showMixtapeCreator.value = false
            showHud("MIXTAPE RECORDED: $title")
        }
    }

    fun loadMixtapeToDeck(mixtape: MixtapeEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val all = allTracks.value.associateBy { it.id }
            val sideAIds = mixtape.sideATrackIds.split(",").mapNotNull { it.trim().toLongOrNull() }
            val sideBIds = mixtape.sideBTrackIds.split(",").mapNotNull { it.trim().toLongOrNull() }

            val sideATracks = sideAIds.mapNotNull { all[it] }
            val sideBTracks = sideBIds.mapNotNull { all[it] }

            _tapeShellColor.value = mixtape.shellColorHex
            _tapeReelColor.value = mixtape.reelColorHex

            audioEngine.setCassetteTapes(sideATracks, sideBTracks)
            navigateTo(HardwareNavScreen.CASSETTE)
            showHud("LOADED: ${mixtape.title}")
        }
    }

    // Workshop & Skins
    fun setHardwareSkin(
        material: HardwareMaterial = _skin.value.material,
        accentColorHex: String = _skin.value.accentColorHex,
        lcdColorHex: String = _skin.value.lcdColorHex
    ) {
        _skin.value = _skin.value.copy(
            material = material,
            accentColorHex = accentColorHex,
            lcdColorHex = lcdColorHex
        )
        showHud("SKIN PROFILE UPDATED")
    }

    // Recording Booth
    fun startRecording(title: String) {
        val success = audioEngine.startRecording(title)
        if (success) {
            showHud("RECORDING ARMED [REC]")
        } else {
            showHud("MIC ACCESS REQUIRED")
        }
    }

    fun stopRecording(title: String, voiceoverTrack: String = "") {
        val file = audioEngine.stopRecording()
        if (file != null && file.exists()) {
            val dur = audioEngine.recordingDurationMs.value
            viewModelScope.launch {
                repository.saveRecording(
                    title = title.ifBlank { "TAPE_SESSION_${System.currentTimeMillis() % 10000}" },
                    filePath = file.absolutePath,
                    durationMs = dur,
                    voiceoverTrackTitle = voiceoverTrack
                )
                showHud("RECORDING MASTER SAVED")
            }
        }
    }

    fun deleteRecording(id: Long) {
        viewModelScope.launch {
            repository.deleteRecording(id)
            showHud("RECORDING DELETED")
        }
    }

    // Radio Tuner
    fun tuneRadio(freqMhz: Float) {
        val clamped = (Math.round(freqMhz * 10f) / 10f).coerceIn(88.0f, 108.0f)
        _radioFreqMhz.value = clamped

        // Check if close to any preset
        val presets = allRadioPresets.value
        val matched = presets.firstOrNull { abs(it.freqMhz - clamped) < 0.25f }

        if (matched != null) {
            _isRadioTuned.value = true
            showHud("SIGNAL LOCKED: ${matched.name}")
            // If local shuffle station and not currently playing, start shuffle
            if (matched.isLocalShuffle && allTracks.value.isNotEmpty()) {
                val randTrack = allTracks.value.random()
                playTrack(randTrack, allTracks.value.shuffled())
            }
        } else {
            _isRadioTuned.value = false
        }
    }

    // Discovery Machine
    fun triggerRouletteTrack() {
        val tracks = allTracks.value
        if (tracks.isNotEmpty()) {
            val randomTrack = tracks.random()
            playTrack(randomTrack, tracks.shuffled())
            showHud("ROULETTE HIT: ${randomTrack.title}")
        }
    }

    // Obsession Mode
    fun createObsession(trackId: Long, title: String, notes: String, target: Int = 99) {
        viewModelScope.launch {
            repository.saveObsessionProfile(trackId, title, notes, target)
            navigateTo(HardwareNavScreen.OBSESSION_MODE)
            showHud("OBSESSION SHRINE CREATED")
        }
    }

    // Library Rescan
    fun refreshLibrary() {
        viewModelScope.launch {
            repository.scanDeviceMusic()
            showHud("LIBRARY RESCANNED")
        }
    }

    private fun showHud(text: String) {
        _hudMessage.value = text
        viewModelScope.launch {
            delay(2400)
            if (_hudMessage.value == text) {
                _hudMessage.value = null
            }
        }
    }
}
