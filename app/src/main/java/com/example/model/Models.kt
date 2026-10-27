package com.example.model

enum class HardwareNavScreen(val title: String, val code: String) {
    CONTROL_DECK("CONTROL DECK", "01"),
    CASSETTE("CASSETTE DECK", "02"),
    DISCMAN("PORTABLE CD", "03"),
    DJ_DECK("DJ MIXER", "04"),
    LYRICS_ROOM("LYRICS ROOM", "05"),
    MIXTAPE_LIBRARY("MIXTAPE VAULT", "06"),
    SOUND_LAB("SOUND LAB", "07"),
    WORKSHOP("CUSTOM WORKSHOP", "08"),
    VIRTUAL_ROOM("COLLECTION ROOM", "09"),
    RECORDING_BOOTH("RECORDING BOOTH", "10"),
    RADIO_MODE("FM TUNER", "11"),
    DISCOVERY_MACHINE("DISCOVERY", "12"),
    OBSESSION_MODE("OBSESSION", "13"),
    MUSIC_DIARY("STATISTICS & DIARY", "14"),
    MINIDISC("MINIDISC DECK", "15"),
    BOOMBOX("GHETTOBLASTER", "16"),
    VINYL("TURNTABLE DECK", "17"),
    FOUR_TRACK("PORTASTUDIO 4-TRK", "18")
}

data class Track(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val contentUri: String,
    val albumArtUri: String? = null,
    val path: String = "",
    val genre: String = "Local Audio",
    val year: Int = 0,
    val dateAdded: Long = System.currentTimeMillis(),
    val replayCount: Int = 0,
    val lastPlayedMs: Long = 0,
    val isFavorite: Boolean = false,
    val lyrics: String? = null
)

enum class RepeatMode {
    OFF, ALL, ONE
}

enum class OutputProfile(val label: String) {
    HEADPHONES("HEADPHONES // PRO"),
    SPEAKER("BUILT-IN SPEAKER"),
    LINE_OUT("LINE OUT // STEREO")
}

data class MasterVolumeState(
    val amplificationPercent: Int = 100, // 0 to 300%, or up to 500%
    val preampGainDb: Float = 0.0f, // -12dB to +18dB
    val balancePan: Float = 0.0f, // -1.0 (Left) to +1.0 (Right)
    val isMuted: Boolean = false,
    val isClipping: Boolean = false,
    val limiterEnabled: Boolean = true,
    val outputProfile: OutputProfile = OutputProfile.HEADPHONES,
    val allowExperimentalCeiling: Boolean = false // 500% ceiling
)

data class TapeDegradationState(
    val tapeHissEnabled: Boolean = false,
    val tapeHissVolume: Float = 0.20f,
    val wowFlutterEnabled: Boolean = false,
    val wowFlutterDepth: Float = 0.35f,
    val saturationWarmth: Float = 0.50f
)

data class TrackBookmark(
    val id: Long,
    val trackId: Long,
    val timestampMs: Long,
    val label: String
)

data class MiniDiscCartridge(
    val id: Long,
    val discTitle: String,
    val labelText: String = "MD-DATA / 74 MIN",
    val colorHex: String = "#0055AA",
    val atracMode: String = "SP", // "SP" (292kbps), "LP2" (132kbps), "LP4" (66kbps)
    val tracks: List<Track> = emptyList(),
    val isShutterOpen: Boolean = false
)

data class PlaybackState(
    val currentTrack: Track? = null,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val queue: List<Track> = emptyList(),
    val queueIndex: Int = -1,
    val isShuffle: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val playbackSpeed: Float = 1.0f,
    val pitchSemiTones: Float = 0.0f,
    val tapeCounter: Int = 0,
    val tapeReelProgress: Float = 0.0f, // 0.0 to 1.0, changes reel radius
    val cdRotationAngle: Float = 0.0f,
    val antiSkipBufferPercent: Float = 1.0f,
    val isCassetteInserted: Boolean = true,
    val activeDeckMode: HardwareNavScreen = HardwareNavScreen.CONTROL_DECK
)

enum class TapeSide {
    SIDE_A, SIDE_B
}

data class StickerItem(
    val id: String,
    val name: String,
    val xPercent: Float,
    val yPercent: Float,
    val rotationDeg: Float = 0f,
    val scale: Float = 1.0f
)

data class CassetteTape(
    val id: Long,
    val title: String,
    val labelText: String,
    val notes: String = "",
    val shellColorHex: String = "#1E2226",
    val reelColorHex: String = "#FF6B1A",
    val isTransparent: Boolean = false,
    val sideATracks: List<Track> = emptyList(),
    val sideBTracks: List<Track> = emptyList(),
    val currentSide: TapeSide = TapeSide.SIDE_A,
    val durationMinutes: Int = 60,
    val stickers: List<StickerItem> = emptyList()
)

data class DjSingleDeck(
    val track: Track? = null,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val pitchPercent: Float = 0.0f, // -16% to +16%
    val volume: Float = 1.0f,
    val isCueActive: Boolean = false,
    val cuePositionMs: Long = 0L,
    val platterRotationDeg: Float = 0f,
    val isLoopActive: Boolean = false,
    val loopStartMs: Long = 0L,
    val loopEndMs: Long = 4000L
)

data class DjDeckState(
    val deckA: DjSingleDeck = DjSingleDeck(),
    val deckB: DjSingleDeck = DjSingleDeck(),
    val crossfader: Float = 0.0f, // -1.0 (all A) to +1.0 (all B)
    val eqLowA: Float = 0.0f, // -12dB to +12dB
    val eqMidA: Float = 0.0f,
    val eqHighA: Float = 0.0f,
    val eqLowB: Float = 0.0f,
    val eqMidB: Float = 0.0f,
    val eqHighB: Float = 0.0f,
    val masterVolume: Float = 0.9f
)

data class WordTimestamp(
    val word: String,
    val startMs: Long,
    val endMs: Long
)

data class LyricsLine(
    val timestampMs: Long,
    val text: String,
    val wordTimestamps: List<WordTimestamp>? = null,
    val isSynchronized: Boolean = true
)

data class EqualizerState(
    val isEnabled: Boolean = true,
    val bands: Map<Int, Int> = mapOf(
        0 to 3,   // 60 Hz Bass
        1 to 2,   // 230 Hz Low Mid
        2 to 0,   // 910 Hz Mid
        3 to 4,   // 3 kHz Presence
        4 to 5    // 14 kHz Air
    ),
    val bassBoost: Int = 600, // 0 to 1000
    val virtualizer: Int = 450, // 0 to 1000
    val presetName: String = "Studio Reference"
)

enum class HardwareMaterial(val label: String, val baseColorHex: String, val borderColorHex: String) {
    MATTE_BLACK("Matte Black", "#101214", "#202428"),
    GUNMETAL("Gunmetal Steel", "#24282E", "#3D434D"),
    BRUSHED_ALUMINUM("Brushed Aluminum", "#5C636B", "#8C96A3"),
    WORN_PLASTIC("1994 Worn Plastic", "#1B1E1A", "#2E332B")
}

data class HardwareSkin(
    val id: String = "default_99",
    val name: String = "Studio 99 Industrial",
    val material: HardwareMaterial = HardwareMaterial.MATTE_BLACK,
    val accentColorHex: String = "#00FF66", // Acid green
    val secondaryColorHex: String = "#FF6B1A", // Burnt orange
    val lcdColorHex: String = "#38E1FF", // Electric LCD
    val hapticFeedback: Boolean = true,
    val tapeScratchesEnabled: Boolean = true,
    val highQualityVisualizer: Boolean = true
)

data class VinylState(
    val rpm: Int = 33, // 33 or 45
    val isTonearmCued: Boolean = true, // true = down on record, false = up/cued
    val pitchPercent: Float = 0.0f, // -8.0% to +8.0%
    val antiSkate: Float = 1.5f,
    val platterRotationDeg: Float = 0.0f
)

data class PortaChannelState(
    val channelNumber: Int,
    val trimLevel: Float = 0.5f, // 0 to 1
    val eqHigh: Float = 0.0f, // -12 to +12 dB
    val eqLow: Float = 0.0f, // -12 to +12 dB
    val pan: Float = 0.0f, // -1.0 (L) to +1.0 (R)
    val faderLevel: Float = 0.75f, // 0 to 1
    val isArmedForRec: Boolean = false,
    val vuLevel: Float = 0.2f
)

data class FourTrackState(
    val channels: List<PortaChannelState> = listOf(
        PortaChannelState(1, vuLevel = 0.35f),
        PortaChannelState(2, vuLevel = 0.28f),
        PortaChannelState(3, vuLevel = 0.22f),
        PortaChannelState(4, vuLevel = 0.40f)
    ),
    val masterFader: Float = 0.85f,
    val pitchKnob: Float = 0.0f, // -12% to +12%
    val isHighSpeed: Boolean = false, // false = 4.75 cm/s, true = 9.5 cm/s
    val isDbxNoiseReductionOn: Boolean = true
)

