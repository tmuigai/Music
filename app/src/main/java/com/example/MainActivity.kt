package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.HardwareNavScreen
import com.example.ui.ReplayViewModel
import com.example.ui.components.HardwareHeader
import com.example.ui.components.HardwareNavRack
import com.example.ui.components.HardwareTransportBar
import com.example.ui.screens.CassettePlayerScreen
import com.example.ui.screens.ControlDeckScreen
import com.example.ui.screens.DiscoveryMachineScreen
import com.example.ui.screens.DiscmanPlayerScreen
import com.example.ui.screens.DjDeckScreen
import com.example.ui.screens.LyricsRoomScreen
import com.example.ui.screens.MixtapeLibraryScreen
import com.example.ui.screens.MusicDiaryScreen
import com.example.ui.screens.ObsessionModeScreen
import com.example.ui.screens.RadioModeScreen
import com.example.ui.screens.RecordingBoothScreen
import com.example.ui.screens.SoundLaboratoryScreen
import com.example.ui.screens.VirtualRoomScreen
import com.example.ui.screens.WorkshopScreen
import com.example.ui.theme.MatteBlack
import com.example.ui.theme.ReplayTheme

class MainActivity : ComponentActivity() {

    private val viewModel: ReplayViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ReplayTheme {
                ReplayApp(viewModel)
            }
        }
    }
}

@Composable
fun ReplayApp(viewModel: ReplayViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val playbackState by viewModel.playbackState.collectAsStateWithLifecycle()
    val hudMessage by viewModel.hudMessage.collectAsStateWithLifecycle()
    val skin by viewModel.skin.collectAsStateWithLifecycle()
    val tapeShellColor by viewModel.tapeShellColor.collectAsStateWithLifecycle()
    val tapeReelColor by viewModel.tapeReelColor.collectAsStateWithLifecycle()
    val tapeStickers by viewModel.tapeStickers.collectAsStateWithLifecycle()
    val showJewelCase by viewModel.showJewelCase.collectAsStateWithLifecycle()

    val allTracks by viewModel.allTracks.collectAsStateWithLifecycle()
    val favoriteTracks by viewModel.favoriteTracks.collectAsStateWithLifecycle()
    val mostPlayedTracks by viewModel.mostPlayedTracks.collectAsStateWithLifecycle()
    val allMixtapes by viewModel.allMixtapes.collectAsStateWithLifecycle()
    val allRecordings by viewModel.allRecordings.collectAsStateWithLifecycle()
    val allObsessions by viewModel.allObsessions.collectAsStateWithLifecycle()
    val allRadioPresets by viewModel.allRadioPresets.collectAsStateWithLifecycle()
    val listeningHistory by viewModel.listeningHistory.collectAsStateWithLifecycle()

    val djState by viewModel.djState.collectAsStateWithLifecycle()
    val visualizerBands by viewModel.visualizerBands.collectAsStateWithLifecycle()
    val vuMeterLeft by viewModel.vuMeterLeft.collectAsStateWithLifecycle()
    val vuMeterRight by viewModel.vuMeterRight.collectAsStateWithLifecycle()
    val eqState by viewModel.eqState.collectAsStateWithLifecycle()
    val lyricsLines by viewModel.lyricsLines.collectAsStateWithLifecycle()
    val lyricsOffsetMs by viewModel.lyricsOffsetMs.collectAsStateWithLifecycle()

    val radioFreqMhz by viewModel.radioFreqMhz.collectAsStateWithLifecycle()
    val isRadioTuned by viewModel.isRadioTuned.collectAsStateWithLifecycle()
    val isRecording by viewModel.isRecording.collectAsStateWithLifecycle()
    val recordingDurationMs by viewModel.recordingDurationMs.collectAsStateWithLifecycle()
    val recordingAmplitude by viewModel.recordingAmplitude.collectAsStateWithLifecycle()

    val accentColor = Color(android.graphics.Color.parseColor(skin.accentColorHex))

    // Hardware Back Button handling for non-Home screens
    BackHandler(enabled = currentScreen != HardwareNavScreen.CONTROL_DECK) {
        viewModel.navigateBack()
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        bottomBar = {
            Column(
                modifier = Modifier
                    .background(MatteBlack)
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                HardwareTransportBar(
                    isPlaying = playbackState.isPlaying,
                    onPlayPause = { viewModel.togglePlayPause() },
                    onStop = { viewModel.stopPlayback() },
                    onPrevious = { viewModel.previousTrack() },
                    onNext = { viewModel.nextTrack() },
                    onRewindHold = { viewModel.seekRelative(-10000) },
                    onFastForwardHold = { viewModel.seekRelative(10000) },
                    accentColor = accentColor
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MatteBlack)
        ) {
            // Top Machine Status Bar
            HardwareHeader(
                currentScreen = currentScreen,
                hudMessage = hudMessage,
                tapeCounter = playbackState.tapeCounter,
                accentColor = accentColor,
                onEjectClick = { viewModel.ejectCassette() }
            )

            // Mode Rack Selector (8 primary modes + expansion bays)
            HardwareNavRack(
                currentScreen = currentScreen,
                accentColor = accentColor,
                onNavigate = { viewModel.navigateTo(it) }
            )

            // Active Screen Destination
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (currentScreen) {
                    HardwareNavScreen.CONTROL_DECK -> {
                        ControlDeckScreen(
                            playbackState = playbackState,
                            tracks = allTracks,
                            favoriteTracks = favoriteTracks,
                            mostPlayedTracks = mostPlayedTracks,
                            vuMeterLeft = vuMeterLeft,
                            vuMeterRight = vuMeterRight,
                            accentColor = accentColor,
                            onTrackSelect = { viewModel.playTrack(it) },
                            onNavigate = { viewModel.navigateTo(it) },
                            onFlipCassetteSide = { viewModel.flipCassetteSide() }
                        )
                    }
                    HardwareNavScreen.CASSETTE -> {
                        CassettePlayerScreen(
                            playbackState = playbackState,
                            currentSide = viewModel.audioEngine.getCurrentCassetteSide(),
                            shellColorHex = tapeShellColor,
                            reelColorHex = tapeReelColor,
                            stickers = tapeStickers,
                            accentColor = accentColor,
                            onFlipSide = { viewModel.flipCassetteSide() },
                            onSeekDrag = { delta ->
                                val target = (playbackState.positionMs + (delta * playbackState.durationMs).toLong())
                                    .coerceIn(0L, playbackState.durationMs)
                                viewModel.seekTo(target)
                            },
                            onTrackSelect = { viewModel.playTrack(it) },
                            onSetShellColor = { viewModel.setTapeShellColor(it) },
                            onSetReelColor = { viewModel.setTapeReelColor(it) },
                            onAddSticker = { viewModel.addSticker(it) }
                        )
                    }
                    HardwareNavScreen.DISCMAN -> {
                        DiscmanPlayerScreen(
                            playbackState = playbackState,
                            showJewelCase = showJewelCase,
                            accentColor = accentColor,
                            onToggleJewelCase = { viewModel.toggleJewelCase(it) },
                            onTrackSelect = { viewModel.playTrack(it) },
                            onToggleShuffle = { viewModel.toggleShuffle() },
                            onCycleRepeat = { viewModel.cycleRepeatMode() }
                        )
                    }
                    HardwareNavScreen.DJ_DECK -> {
                        DjDeckScreen(
                            djState = djState,
                            allTracks = allTracks,
                            accentColor = accentColor,
                            onTogglePlayA = { viewModel.toggleDjDeckAPlay() },
                            onTogglePlayB = { viewModel.toggleDjDeckBPlay() },
                            onCrossfaderChange = { viewModel.setDjCrossfader(it) },
                            onPitchAChange = { viewModel.setDjPitchA(it) },
                            onPitchBChange = { viewModel.setDjPitchB(it) },
                            onScratchA = { viewModel.scratchDeckA(it) },
                            onScratchB = { viewModel.scratchDeckB(it) },
                            onSetEq = { isA, band, v -> viewModel.setDjEq(isA, band, v) },
                            onSelectTrackA = { viewModel.setDjTrackA(it) },
                            onSelectTrackB = { viewModel.setDjTrackB(it) }
                        )
                    }
                    HardwareNavScreen.LYRICS_ROOM -> {
                        LyricsRoomScreen(
                            playbackState = playbackState,
                            lyricsLines = lyricsLines,
                            lyricsOffsetMs = lyricsOffsetMs,
                            accentColor = accentColor,
                            onAdjustOffset = { viewModel.adjustLyricsOffset(it) },
                            onSaveLyrics = { id, text -> viewModel.saveEditedLyrics(id, text) }
                        )
                    }
                    HardwareNavScreen.MIXTAPE_LIBRARY -> {
                        MixtapeLibraryScreen(
                            allTracks = allTracks,
                            allMixtapes = allMixtapes,
                            currentTrack = playbackState.currentTrack,
                            isPlaying = playbackState.isPlaying,
                            accentColor = accentColor,
                            onTrackSelect = { viewModel.playTrack(it) },
                            onLoadMixtape = { viewModel.loadMixtapeToDeck(it) },
                            onCreateMixtape = { title, label, notes, shell, reel, transp, sA, sB, dur ->
                                viewModel.createMixtape(title, label, notes, shell, reel, transp, sA, sB, dur)
                            },
                            onRescanLibrary = { viewModel.refreshLibrary() }
                        )
                    }
                    HardwareNavScreen.SOUND_LAB -> {
                        SoundLaboratoryScreen(
                            eqState = eqState,
                            visualizerBands = visualizerBands,
                            vuMeterLeft = vuMeterLeft,
                            vuMeterRight = vuMeterRight,
                            accentColor = accentColor,
                            onSetBand = { idx, g -> viewModel.setEqBand(idx, g) },
                            onSetBassBoost = { viewModel.setBassBoost(it) },
                            onSetVirtualizer = { viewModel.setVirtualizer(it) },
                            onToggleEnabled = { viewModel.setEqEnabled(it) },
                            onApplyPreset = { viewModel.applyEqPreset(it) }
                        )
                    }
                    HardwareNavScreen.WORKSHOP -> {
                        WorkshopScreen(
                            currentSkin = skin,
                            onUpdateSkin = { mat, acc, lcd ->
                                viewModel.setHardwareSkin(mat, acc, lcd)
                            }
                        )
                    }
                    HardwareNavScreen.VIRTUAL_ROOM -> {
                        VirtualRoomScreen(
                            tracks = allTracks,
                            mixtapes = allMixtapes,
                            accentColor = accentColor,
                            onTrackSelect = { viewModel.playTrack(it) },
                            onLoadMixtape = { viewModel.loadMixtapeToDeck(it) }
                        )
                    }
                    HardwareNavScreen.RECORDING_BOOTH -> {
                        RecordingBoothScreen(
                            isRecording = isRecording,
                            recordingDurationMs = recordingDurationMs,
                            recordingAmplitude = recordingAmplitude,
                            recordings = allRecordings,
                            accentColor = accentColor,
                            onStartRecording = { viewModel.startRecording(it) },
                            onStopRecording = { viewModel.stopRecording(it) },
                            onDeleteRecording = { viewModel.deleteRecording(it) },
                            onPlayTrack = { viewModel.playTrack(it) }
                        )
                    }
                    HardwareNavScreen.RADIO_MODE -> {
                        RadioModeScreen(
                            currentFreqMhz = radioFreqMhz,
                            isTuned = isRadioTuned,
                            presets = allRadioPresets,
                            accentColor = accentColor,
                            onTuneFreq = { viewModel.tuneRadio(it) }
                        )
                    }
                    HardwareNavScreen.DISCOVERY_MACHINE -> {
                        DiscoveryMachineScreen(
                            tracks = allTracks,
                            accentColor = accentColor,
                            onTriggerRoulette = { viewModel.triggerRouletteTrack() },
                            onTrackSelect = { viewModel.playTrack(it) }
                        )
                    }
                    HardwareNavScreen.OBSESSION_MODE -> {
                        ObsessionModeScreen(
                            currentTrack = playbackState.currentTrack,
                            obsessions = allObsessions,
                            playbackState = playbackState,
                            accentColor = accentColor,
                            onCreateObsession = { trkId, title, notes, target ->
                                viewModel.createObsession(trkId, title, notes, target)
                            },
                            onTrackSelect = { viewModel.playTrack(it) }
                        )
                    }
                    HardwareNavScreen.MUSIC_DIARY -> {
                        MusicDiaryScreen(
                            tracks = allTracks,
                            history = listeningHistory,
                            accentColor = accentColor
                        )
                    }
                }
            }
        }
    }
}
