package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ReplayApplication
import com.example.data.ListeningHistoryEntity
import com.example.data.MiniDiscEntity
import com.example.data.MixtapeEntity
import com.example.data.ObsessionProfileEntity
import com.example.data.RadioPresetEntity
import com.example.data.RecordingEntity
import com.example.data.TrackBookmarkEntity
import com.example.model.CuratedCategory
import com.example.model.CustomMachineSlot
import com.example.model.DuplicateGroup
import com.example.model.EqualizerState
import com.example.model.HardwareMaterial
import com.example.model.HardwareNavCategory
import com.example.model.HardwareNavScreen
import com.example.model.HardwareSkin
import com.example.model.LibraryHealthReport
import com.example.model.LibraryViewMode
import com.example.model.ListeningEnvironment
import com.example.model.LyricsLine
import com.example.model.MasterVolumeState
import com.example.model.MiniDiscCartridge
import com.example.model.OutputProfile
import com.example.model.PlaybackState
import com.example.model.ScanProgress
import com.example.model.SmartFolderType
import com.example.model.StickerItem
import com.example.model.TapeDegradationState
import com.example.model.TapeSide
import com.example.model.Track
import com.example.model.audioFormat
import com.example.model.genreList
import com.example.playback.LrcParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.random.Random

class ReplayViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as ReplayApplication
    val repository = app.repository
    val audioEngine = app.audioEngine

    // Navigation State
    private val _currentScreen = MutableStateFlow(HardwareNavScreen.CONTROL_DECK)
    val currentScreen: StateFlow<HardwareNavScreen> = _currentScreen.asStateFlow()

    // Sub-category state for Library and Sound Lab
    private val _libraryCategory = MutableStateFlow("SONGS")
    val libraryCategory: StateFlow<String> = _libraryCategory.asStateFlow()

    private val _soundLabSection = MutableStateFlow("EQ")
    val soundLabSection: StateFlow<String> = _soundLabSection.asStateFlow()

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
            StickerItem("stk_2", "CHROME TAPE", 0.15f, 0.72f, 8f, 0.9f)
        )
    )
    val tapeStickers: StateFlow<List<StickerItem>> = _tapeStickers.asStateFlow()

    // MiniDisc Player State
    private val _currentMiniDisc = MutableStateFlow<MiniDiscCartridge?>(null)
    val currentMiniDisc: StateFlow<MiniDiscCartridge?> = _currentMiniDisc.asStateFlow()

    // Discman Jewel Case Modal
    private val _showJewelCase = MutableStateFlow(false)
    val showJewelCase: StateFlow<Boolean> = _showJewelCase.asStateFlow()

    // Mixtape creation modal
    private val _showMixtapeCreator = MutableStateFlow(false)
    val showMixtapeCreator: StateFlow<Boolean> = _showMixtapeCreator.asStateFlow()

    // UI Feedback Message (e.g., "Cassette Ejected", "Skin Profile Saved", "Radio Locked")
    private val _hudMessage = MutableStateFlow<String?>(null)
    val hudMessage: StateFlow<String?> = _hudMessage.asStateFlow()

    // Library Intelligence States
    private val _libraryViewMode = MutableStateFlow(LibraryViewMode.LIST)
    val libraryViewMode: StateFlow<LibraryViewMode> = _libraryViewMode.asStateFlow()

    private val _selectedSmartFolder = MutableStateFlow<SmartFolderType?>(null)
    val selectedSmartFolder: StateFlow<SmartFolderType?> = _selectedSmartFolder.asStateFlow()

    // Scanner & Library Navigation States
    val scanProgress: StateFlow<ScanProgress> = repository.scanProgress

    private val _currentFolderPath = MutableStateFlow("")
    val currentFolderPath: StateFlow<String> = _currentFolderPath.asStateFlow()

    private val _selectedCuratedCategory = MutableStateFlow(CuratedCategory.RECENTLY_ADDED)
    val selectedCuratedCategory: StateFlow<CuratedCategory> = _selectedCuratedCategory.asStateFlow()

    private val _selectedGenre = MutableStateFlow<String?>(null)
    val selectedGenre: StateFlow<String?> = _selectedGenre.asStateFlow()

    // Library Diagnostics & Duplicates
    private val _libraryHealthReport = MutableStateFlow(LibraryHealthReport())
    val libraryHealthReport: StateFlow<LibraryHealthReport> = _libraryHealthReport.asStateFlow()

    // Bulk Track Selection
    private val _isBulkMode = MutableStateFlow(false)
    val isBulkMode: StateFlow<Boolean> = _isBulkMode.asStateFlow()

    private val _selectedTrackIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedTrackIds: StateFlow<Set<Long>> = _selectedTrackIds.asStateFlow()

    // Listening Environment State
    private val _currentEnvironment = MutableStateFlow(ListeningEnvironment.HEADPHONES)
    val currentEnvironment: StateFlow<ListeningEnvironment> = _currentEnvironment.asStateFlow()

    // Modular Build-A-Machine configuration
    private val _customMachineSlots = MutableStateFlow(
        listOf(
            CustomMachineSlot(0, "CASSETTE DECK", "TAPE"),
            CustomMachineSlot(1, "10-BAND EQ", "EQ"),
            CustomMachineSlot(2, "CROSSFADER", "DJ")
        )
    )
    val customMachineSlots: StateFlow<List<CustomMachineSlot>> = _customMachineSlots.asStateFlow()

    // Rotary Telephone State
    private val _telephoneNumber = MutableStateFlow("")
    val telephoneNumber: StateFlow<String> = _telephoneNumber.asStateFlow()

    // Party Jukebox Mode State
    private val _jukeboxCredits = MutableStateFlow(3)
    val jukeboxCredits: StateFlow<Int> = _jukeboxCredits.asStateFlow()

    private val _jukeboxSelectionCode = MutableStateFlow("")
    val jukeboxSelectionCode: StateFlow<String> = _jukeboxSelectionCode.asStateFlow()

    private val _isPartyLockoutActive = MutableStateFlow(false)
    val isPartyLockoutActive: StateFlow<Boolean> = _isPartyLockoutActive.asStateFlow()

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

    val allMiniDiscs: StateFlow<List<MiniDiscEntity>> = repository.allMiniDiscs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBookmarks: StateFlow<List<TrackBookmarkEntity>> = repository.allBookmarks
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
    val volumeState: StateFlow<MasterVolumeState> = audioEngine.effectsManager.volumeState
    val tapeDegradation: StateFlow<TapeDegradationState> = audioEngine.effectsManager.tapeDegradation
    val isRecording = audioEngine.isRecording
    val recordingDurationMs = audioEngine.recordingDurationMs
    val recordingAmplitude = audioEngine.recordingAmplitude
    val vinylState = audioEngine.vinylState
    val fourTrackState = audioEngine.fourTrackState

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

    fun navigateToCategory(category: HardwareNavCategory) {
        when (category) {
            HardwareNavCategory.HOME -> navigateTo(HardwareNavScreen.CONTROL_DECK)
            HardwareNavCategory.MACHINES -> {
                if (_currentScreen.value.category != HardwareNavCategory.MACHINES) {
                    navigateTo(HardwareNavScreen.CASSETTE)
                }
            }
            HardwareNavCategory.LIBRARY -> {
                if (_currentScreen.value != HardwareNavScreen.MIXTAPE_LIBRARY) {
                    navigateTo(HardwareNavScreen.MIXTAPE_LIBRARY)
                }
            }
            HardwareNavCategory.SOUND -> {
                if (_currentScreen.value.category != HardwareNavCategory.SOUND) {
                    navigateTo(HardwareNavScreen.SOUND_LAB)
                }
            }
            HardwareNavCategory.WORKSHOP -> {
                if (_currentScreen.value.category != HardwareNavCategory.WORKSHOP) {
                    navigateTo(HardwareNavScreen.WORKSHOP)
                }
            }
        }
    }

    fun setLibraryCategory(category: String) {
        _libraryCategory.value = category
        if (_currentScreen.value != HardwareNavScreen.MIXTAPE_LIBRARY) {
            navigateTo(HardwareNavScreen.MIXTAPE_LIBRARY)
        }
    }

    fun setSoundLabSection(section: String) {
        _soundLabSection.value = section
        if (_currentScreen.value != HardwareNavScreen.SOUND_LAB) {
            navigateTo(HardwareNavScreen.SOUND_LAB)
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

    // Complete Volume System (0% to 300% & 500% experimental ceiling)
    fun setAmplification(percent: Int) {
        audioEngine.effectsManager.setAmplificationPercent(percent)
        if (percent > 140) {
            showHud("BOOST: $percent% (PREAMP ACTIVE)")
        }
    }

    fun setPreampGain(gainDb: Float) {
        audioEngine.effectsManager.setPreampGain(gainDb)
    }

    fun setBalance(pan: Float) {
        audioEngine.effectsManager.setBalance(pan)
    }

    fun toggleMute() {
        audioEngine.effectsManager.toggleMute()
        showHud(if (volumeState.value.isMuted) "MUTED [MUTE ACTIVE]" else "AUDIO RESTORED")
    }

    fun setOutputProfile(profile: OutputProfile) {
        audioEngine.effectsManager.setOutputProfile(profile)
        showHud("OUTPUT: ${profile.label}")
    }

    fun toggleExperimentalCeiling() {
        audioEngine.effectsManager.toggleExperimentalCeiling()
        val enabled = audioEngine.effectsManager.volumeState.value.allowExperimentalCeiling
        showHud(if (enabled) "EXPERIMENTAL 500% BOOST UNLOCKED" else "300% CEILING RESTORED")
    }

    // Tape Degradation Simulator
    fun toggleTapeHiss() {
        audioEngine.effectsManager.toggleTapeHiss()
        showHud(if (tapeDegradation.value.tapeHissEnabled) "TAPE HISS ON" else "TAPE HISS OFF")
    }

    fun toggleWowFlutter() {
        audioEngine.effectsManager.toggleWowFlutter()
        showHud(if (tapeDegradation.value.wowFlutterEnabled) "WOW & FLUTTER ON" else "WOW & FLUTTER OFF")
    }

    fun setSaturationWarmth(warmth: Float) {
        audioEngine.effectsManager.setSaturationWarmth(warmth)
    }

    // Audio Time Machine (Bookmarks)
    fun addBookmarkAtCurrentPosition(label: String) {
        val curTrack = playbackState.value.currentTrack ?: return
        val pos = playbackState.value.positionMs
        viewModelScope.launch {
            repository.saveBookmark(
                trackId = curTrack.id,
                timestampMs = pos,
                label = label.ifBlank { "MOMENT @ ${pos / 1000}s" }
            )
            showHud("TIME MACHINE BOOKMARK SAVED")
        }
    }

    fun jumpToBookmark(timestampMs: Long) {
        seekTo(timestampMs)
        showHud("JUMPED TO ${timestampMs / 1000}s")
    }

    fun deleteBookmark(id: Long) {
        viewModelScope.launch {
            repository.deleteBookmark(id)
            showHud("BOOKMARK REMOVED")
        }
    }

    // MiniDisc Player Operations
    fun saveMiniDisc(title: String, label: String, colorHex: String, atracMode: String, trackIds: List<Long>) {
        viewModelScope.launch {
            repository.saveMiniDisc(title, label, colorHex, atracMode, trackIds)
            showHud("MINIDISC MASTERED: $title")
        }
    }

    fun loadMiniDisc(disc: MiniDiscEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val all = allTracks.value.associateBy { it.id }
            val ids = disc.trackIds.split(",").mapNotNull { it.trim().toLongOrNull() }
            val tracks = ids.mapNotNull { all[it] }

            _currentMiniDisc.value = MiniDiscCartridge(
                id = disc.id,
                discTitle = disc.discTitle,
                labelText = disc.labelText,
                colorHex = disc.colorHex,
                atracMode = disc.atracMode,
                tracks = tracks,
                isShutterOpen = true
            )

            if (tracks.isNotEmpty()) {
                playTrack(tracks[0], tracks)
            }
            navigateTo(HardwareNavScreen.MINIDISC)
            showHud("LOADED MD: ${disc.discTitle}")
        }
    }

    fun ejectMiniDisc() {
        _currentMiniDisc.value = null
        audioEngine.stop()
        showHud("MINIDISC EJECTED")
    }

    // Real Audio File Importing (via SAF)
    fun importAudioFiles(uris: List<Uri>) {
        viewModelScope.launch {
            var importedCount = 0
            for (uri in uris) {
                val track = repository.importAudioUri(uri)
                if (track != null) importedCount++
            }
            if (importedCount > 0) {
                showHud("IMPORTED $importedCount AUDIO FILE(S)")
            } else {
                showHud("NO VALID AUDIO EXTRACTED")
            }
        }
    }

    // Cassette Controls
    fun flipCassetteSide() {
        val newSide = audioEngine.flipCassetteSide()
        showHud("CASSETTE FLIPPED -> ${newSide.name}")
    }

    fun ejectCassette() {
        audioEngine.ejectCassette()
        showHud("CASSETTE EJECTED [DOOR OPEN]")
    }

    fun insertCassette() {
        audioEngine.insertCassette()
        showHud("CASSETTE INSERTED [DOOR CLOSED]")
    }

    // Vinyl Turntable
    fun setVinylRpm(rpm: Int) {
        audioEngine.setVinylRpm(rpm)
        showHud("TURNTABLE SPEED: $rpm RPM")
    }

    fun setVinylPitch(pitchPercent: Float) {
        audioEngine.setVinylPitch(pitchPercent)
        showHud(String.format("PITCH: %+.1f%%", pitchPercent))
    }

    fun cueTonearm(cued: Boolean) {
        audioEngine.cueTonearm(cued)
        showHud(if (cued) "TONEARM DROPPED [NEEDLE ON GROOVE]" else "TONEARM CUED UP")
    }

    fun scratchVinyl(deltaDeg: Float) {
        audioEngine.scratchVinyl(deltaDeg)
    }

    // Four-Track Portastudio
    fun setFourTrackSpeed(isHigh: Boolean) {
        audioEngine.setFourTrackSpeed(isHigh)
        showHud(if (isHigh) "TAPE SPEED: 9.5 CM/S (HIGH FIDELITY)" else "TAPE SPEED: 4.75 CM/S (STANDARD)")
    }

    fun setFourTrackPitch(pitchPercent: Float) {
        audioEngine.setFourTrackPitch(pitchPercent)
        showHud(String.format("PORTA PITCH: %+.1f%%", pitchPercent))
    }

    fun setFourTrackMasterFader(fader: Float) {
        audioEngine.setFourTrackMasterFader(fader)
    }

    fun setFourTrackChannelFader(channelNum: Int, level: Float) {
        audioEngine.setFourTrackChannelFader(channelNum, level)
    }

    fun toggleFourTrackRecArm(channelNum: Int) {
        audioEngine.toggleFourTrackRecArm(channelNum)
        showHud("CH $channelNum REC ARM TOGGLED")
    }

    fun toggleFourTrackDbx() {
        audioEngine.toggleFourTrackDbx()
        showHud(if (fourTrackState.value.isDbxNoiseReductionOn) "DBX NOISE REDUCTION ON" else "DBX BYPASSED")
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

        val presets = allRadioPresets.value
        val matched = presets.firstOrNull { abs(it.freqMhz - clamped) < 0.25f }

        if (matched != null) {
            _isRadioTuned.value = true
            showHud("SIGNAL LOCKED: ${matched.name}")
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

    // Library Intelligence Controls
    fun setLibraryViewMode(mode: LibraryViewMode) {
        _libraryViewMode.value = mode
        showHud("CRATE VIEW: ${mode.name}")
    }

    fun selectSmartFolder(folder: SmartFolderType?) {
        _selectedSmartFolder.value = folder
        showHud(if (folder != null) "SMART FILTER: ${folder.title}" else "ALL AUDIO CRATE")
    }

    fun getFilteredTracks(folder: SmartFolderType?, baseTracks: List<Track>): List<Track> {
        if (folder == null) return baseTracks
        return when (folder) {
            SmartFolderType.PRE_2000 -> baseTracks.filter { it.year in 1..1999 }
            SmartFolderType.DEEP_CUTS -> baseTracks.filter { it.durationMs >= 300000L }
            SmartFolderType.NEGLECTED -> {
                val cutOff = System.currentTimeMillis() - (90L * 24 * 60 * 60 * 1000)
                baseTracks.filter { it.lastPlayedMs < cutOff }
            }
            SmartFolderType.LOSSLESS -> baseTracks.filter {
                it.path.endsWith(".flac", ignoreCase = true) || it.path.endsWith(".wav", ignoreCase = true)
            }
            SmartFolderType.HEAVY_ROTATION -> baseTracks.filter { it.replayCount >= 5 }
            SmartFolderType.MISSING_TAGS -> baseTracks.filter {
                it.albumArtUri.isNullOrEmpty() || it.genre == "Unknown" || it.year == 0
            }
            SmartFolderType.KENYA_ROOTS -> baseTracks.filter {
                it.genre.contains("Afro", ignoreCase = true) ||
                it.genre.contains("Kenya", ignoreCase = true) ||
                it.genre.contains("Benga", ignoreCase = true)
            }
        }
    }

    fun bulkUpdateMetadata(trackIds: List<Long>, genre: String?, year: Int?) {
        viewModelScope.launch {
            repository.bulkUpdateMetadata(trackIds, year ?: 0, genre ?: "Underground")
            showHud("BULK TAGGED ${trackIds.size} TRACK(S)")
        }
    }

    // Per-Song Volume Memory
    fun setPerSongGain(trackId: Long, gainDb: Float) {
        viewModelScope.launch {
            repository.updateTrackVolumeGainOffset(trackId, gainDb)
            if (audioEngine.playbackState.value.currentTrack?.id == trackId) {
                audioEngine.effectsManager.setPerSongGainOffset(gainDb)
            }
            showHud("GAIN MEMORY: ${if (gainDb >= 0) "+" else ""}${String.format("%.1f", gainDb)} dB")
        }
    }

    // AB Repeat Loop
    fun setAbPointA(timeMs: Long? = null) {
        if (timeMs != null) audioEngine.setAbPointA(timeMs) else audioEngine.setAbPointA()
        showHud("AB REPEAT: [A] MARK SET")
    }

    fun setAbPointB(timeMs: Long? = null) {
        if (timeMs != null) audioEngine.setAbPointB(timeMs) else audioEngine.setAbPointB()
        showHud("AB REPEAT: [B] MARK SET -> LOOP ENGAGED")
    }

    fun clearAbRepeat() {
        audioEngine.clearAbRepeat()
        showHud("AB REPEAT: DISENGAGED")
    }

    // Sleep Timer
    fun startSleepTimer(minutes: Int) {
        audioEngine.startSleepTimer(minutes)
        showHud("SLEEP TIMER: $minutes MIN WITH FADEOUT")
    }

    fun cancelSleepTimer() {
        audioEngine.cancelSleepTimer()
        showHud("SLEEP TIMER: CANCELLED")
    }

    // Crossfade
    fun setCrossfadeDuration(seconds: Int) {
        audioEngine.setCrossfadeDuration(seconds)
        showHud("CROSSFADE: ${seconds}s")
    }

    // Listening Environment Presets
    fun applyListeningEnvironment(env: ListeningEnvironment) {
        _currentEnvironment.value = env
        audioEngine.effectsManager.applyEnvironment(env)
        showHud("ENV ENGAGED: ${env.title}")
    }

    // MPC Sampler Sound FX
    fun triggerSamplerSound(soundType: String) {
        audioEngine.triggerSamplerSound(soundType)
        showHud("SAMPLER: $soundType")
    }

    // DJ Hot Cues & Pitch Bend
    fun setHotCue(index: Int) {
        audioEngine.setHotCue(index)
        showHud("HOT CUE $index SAVED")
    }

    fun jumpToHotCue(index: Int) {
        audioEngine.jumpToHotCue(index)
        showHud("JUMP -> CUE $index")
    }

    fun nudgePitch(deltaPercent: Float) {
        audioEngine.nudgePitch(deltaPercent)
        showHud("PITCH NUDGE: ${if (deltaPercent > 0) "+" else ""}${deltaPercent.toInt()}%")
    }

    fun tapTempo() {
        audioEngine.tapTempo()
        val bpm = audioEngine.detectedBpm.value
        showHud("TAP TEMPO: $bpm BPM")
    }

    // Modular Build-A-Machine
    fun updateMachineSlot(slotIndex: Int, moduleName: String, icon: String) {
        val current = _customMachineSlots.value.toMutableList()
        val idx = current.indexOfFirst { it.slotIndex == slotIndex }
        val newSlot = CustomMachineSlot(slotIndex, moduleName, icon)
        if (idx >= 0) {
            current[idx] = newSlot
        } else {
            current.add(newSlot)
        }
        _customMachineSlots.value = current
        showHud("BAY ${slotIndex + 1} MOUNTED: $moduleName")
    }

    // Underground Rotary Telephone
    fun dialTelephoneDigit(digit: Int) {
        if (_telephoneNumber.value.length < 8) {
            _telephoneNumber.value += digit.toString()
            audioEngine.triggerSamplerSound("AIRHORN")
        }
    }

    fun clearTelephoneNumber() {
        _telephoneNumber.value = ""
        showHud("LINE CLEARED")
    }

    fun callTelephoneHotline() {
        val number = _telephoneNumber.value
        val tracks = allTracks.value
        if (tracks.isEmpty()) {
            showHud("HOTLINE ERROR: NO AUDIO IN CRATE")
            return
        }

        when (number) {
            "99", "1999" -> {
                showHud("HOTLINE CONNECTED: 1999 RAVE LINE")
                val raveTracks = tracks.filter { it.year in 1990..1999 }.ifEmpty { tracks }
                playTrack(raveTracks.random(), raveTracks)
            }
            "411", "00" -> {
                showHud("HOTLINE CONNECTED: DEEP CUTS OPERATOR")
                val deep = tracks.filter { it.durationMs >= 240000L }.ifEmpty { tracks }
                playTrack(deep.random(), deep)
            }
            "808" -> {
                showHud("HOTLINE CONNECTED: TR-808 HIP HOP LINE")
                triggerSamplerSound("TR808_SNARE")
                val hiphop = tracks.filter { it.genre.contains("Hip", ignoreCase = true) }.ifEmpty { tracks }
                playTrack(hiphop.random(), hiphop)
            }
            "254" -> {
                showHud("HOTLINE CONNECTED: NAIROBI GROOVES HOTLINE")
                val afro = tracks.filter {
                    it.genre.contains("Afro", ignoreCase = true) || it.genre.contains("Kenya", ignoreCase = true)
                }.ifEmpty { tracks }
                playTrack(afro.random(), afro)
            }
            else -> {
                showHud("PULSE DIALED: $number -> DISPATCHING TRACK")
                playTrack(tracks.random(), tracks.shuffled())
            }
        }
    }

    // Party Jukebox Mode
    fun insertJukeboxCoin() {
        _jukeboxCredits.value += 3
        triggerSamplerSound("VINYL_BRAKE")
        showHud("COIN INSERTED! +3 CREDITS (TOTAL: ${_jukeboxCredits.value})")
    }

    fun pressJukeboxKey(key: String) {
        if (_isPartyLockoutActive.value) {
            showHud("JUKEBOX LOCKED BY HOST")
            return
        }
        val currentCode = _jukeboxSelectionCode.value
        if (currentCode.length >= 2) {
            _jukeboxSelectionCode.value = key
        } else {
            val newCode = currentCode + key
            _jukeboxSelectionCode.value = newCode
            if (newCode.length == 2) {
                // Code complete (e.g. A1, B3, H8)
                if (_jukeboxCredits.value <= 0) {
                    showHud("INSERT COIN TO SELECT TRACK")
                } else {
                    _jukeboxCredits.value -= 1
                    val tracks = allTracks.value
                    if (tracks.isNotEmpty()) {
                        val rowLetter = newCode[0].uppercaseChar()
                        val colDigit = newCode[1].digitToIntOrNull() ?: 1
                        val codeIndex = ((rowLetter - 'A') * 8 + (colDigit - 1)).coerceIn(0, tracks.size - 1)
                        val chosenTrack = tracks[codeIndex % tracks.size]
                        audioEngine.playTrack(chosenTrack, tracks)
                        showHud("[$newCode] PLAYING: ${chosenTrack.title}")
                    }
                }
            }
        }
    }

    fun togglePartyLockout() {
        val next = !_isPartyLockoutActive.value
        _isPartyLockoutActive.value = next
        showHud(if (next) "PARTY LOCKOUT: GUEST INPUT DISABLED" else "PARTY LOCKOUT: GUEST SELECTION OPEN")
    }

    // Library Scanner Operations
    fun refreshLibrary(isIncremental: Boolean = false) {
        viewModelScope.launch {
            val count = repository.scanDeviceMusic(isIncremental = isIncremental)
            showHud(if (count > 0) "SCAN COMPLETED: +$count TRACKS" else "SCAN COMPLETE: UP TO DATE")
        }
    }

    fun importFolder(treeUri: Uri) {
        viewModelScope.launch {
            val count = repository.importFolderTree(treeUri)
            showHud(if (count > 0) "FOLDER IMPORTED: +$count TRACKS" else "NO COMPATIBLE AUDIO FOUND")
        }
    }

    // Folder Navigation
    fun openFolder(path: String) {
        _currentFolderPath.value = path
        showHud("CRATE: ${path.substringAfterLast('/').ifBlank { "ROOT" }}")
    }

    fun navigateUpFolder() {
        val cur = _currentFolderPath.value
        if (cur.contains('/')) {
            _currentFolderPath.value = cur.substringBeforeLast('/')
        } else {
            _currentFolderPath.value = ""
        }
    }

    fun playFolder(folderPath: String) {
        val tracks = allTracks.value.filter {
            if (folderPath.isBlank()) true else it.path.startsWith(folderPath)
        }
        if (tracks.isNotEmpty()) {
            audioEngine.playTrack(tracks.first(), tracks)
            showHud("PLAYING FOLDER (${tracks.size} TRACKS)")
        }
    }

    // Curated Categories & Crates
    fun selectCuratedCategory(category: CuratedCategory) {
        _selectedCuratedCategory.value = category
        showHud("CATEGORY: ${category.title}")
    }

    fun getTracksForCategory(category: CuratedCategory, baseTracks: List<Track>): List<Track> {
        return when (category) {
            CuratedCategory.RECENTLY_ADDED -> baseTracks.sortedByDescending { it.dateAdded }
            CuratedCategory.RECENTLY_PLAYED -> baseTracks.filter { it.lastPlayedMs > 0 }.sortedByDescending { it.lastPlayedMs }
            CuratedCategory.MOST_PLAYED -> baseTracks.filter { it.replayCount > 0 }.sortedByDescending { it.replayCount }
            CuratedCategory.FAVOURITES -> baseTracks.filter { it.isFavorite }
            CuratedCategory.FORGOTTEN_TRACKS -> {
                val cutoff = System.currentTimeMillis() - (90L * 24 * 60 * 60 * 1000)
                baseTracks.filter { it.lastPlayedMs < cutoff && it.replayCount < 3 }
            }
            CuratedCategory.LOSSLESS_AUDIO -> baseTracks.filter {
                val fmt = it.audioFormat
                fmt == "FLAC" || fmt == "WAV" || fmt == "AIFF"
            }
            CuratedCategory.EXTENDED_CUTS -> baseTracks.filter { it.durationMs >= 300000L }
            CuratedCategory.INTERLUDES -> baseTracks.filter { it.durationMs in 1..120000L }
        }
    }

    fun playCategory(category: CuratedCategory) {
        val tracks = getTracksForCategory(category, allTracks.value)
        if (tracks.isNotEmpty()) {
            audioEngine.playTrack(tracks.first(), tracks)
            showHud("PLAYING CRATE: ${category.title}")
        }
    }

    // Genres & Multi-Genre
    fun selectGenre(genre: String?) {
        _selectedGenre.value = genre
        showHud(if (genre != null) "GENRE: $genre" else "ALL GENRES")
    }

    fun playGenre(genre: String) {
        val tracks = allTracks.value.filter { it.genreList.any { g -> g.equals(genre, ignoreCase = true) } }
        if (tracks.isNotEmpty()) {
            audioEngine.playTrack(tracks.first(), tracks)
            showHud("PLAYING GENRE: $genre (${tracks.size})")
        }
    }

    // Library Health Diagnostics & Duplicate Management
    fun runLibraryHealthDiagnostics() {
        viewModelScope.launch {
            _libraryHealthReport.value = _libraryHealthReport.value.copy(isScanning = true)
            val report = repository.analyzeLibraryHealth()
            _libraryHealthReport.value = report
            showHud("DIAGNOSTICS COMPLETE // HEALTH: ${report.healthScore}%")
        }
    }

    fun deduplicateAll() {
        viewModelScope.launch {
            val count = repository.deduplicateLibrary(_libraryHealthReport.value.duplicateGroups)
            runLibraryHealthDiagnostics()
            showHud("REMOVED $count DUPLICATE COPIES")
        }
    }

    fun purgeMissingFiles() {
        viewModelScope.launch {
            val count = repository.purgeMissingTracks(_libraryHealthReport.value.missingFiles)
            runLibraryHealthDiagnostics()
            showHud("PURGED $count MISSING TRACKS")
        }
    }

    fun autoRepairLibrary() {
        viewModelScope.launch {
            val dupsCount = repository.deduplicateLibrary(_libraryHealthReport.value.duplicateGroups)
            val missingCount = repository.purgeMissingTracks(_libraryHealthReport.value.missingFiles)
            runLibraryHealthDiagnostics()
            showHud("ARCHIVE REPAIRED // -$dupsCount DUPS // -$missingCount GHOSTS")
        }
    }

    fun deleteTrack(trackId: Long) {
        viewModelScope.launch {
            repository.deleteTrack(trackId)
            showHud("TRACK REMOVED FROM VAULT")
        }
    }

    fun deleteSelectedTracks() {
        val ids = _selectedTrackIds.value.toList()
        if (ids.isNotEmpty()) {
            viewModelScope.launch {
                repository.deleteTracks(ids)
                _selectedTrackIds.value = emptySet()
                _isBulkMode.value = false
                showHud("PURGED ${ids.size} TRACK(S)")
            }
        }
    }

    fun toggleBulkMode() {
        _isBulkMode.value = !_isBulkMode.value
        if (!_isBulkMode.value) {
            _selectedTrackIds.value = emptySet()
        }
        showHud(if (_isBulkMode.value) "BULK SELECTION ARMED" else "BULK MODE DISENGAGED")
    }

    fun toggleTrackSelection(trackId: Long) {
        val current = _selectedTrackIds.value.toMutableSet()
        if (current.contains(trackId)) {
            current.remove(trackId)
        } else {
            current.add(trackId)
        }
        _selectedTrackIds.value = current
    }

    fun selectAllTracks(tracks: List<Track>) {
        _selectedTrackIds.value = tracks.map { it.id }.toSet()
        showHud("SELECTED ALL ${tracks.size} TRACKS")
    }

    fun clearTrackSelection() {
        _selectedTrackIds.value = emptySet()
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
