package com.example.model

enum class HardwareNavCategory(val title: String, val code: String) {
    HOME("HOME", "01"),
    MACHINES("MACHINES", "02"),
    LIBRARY("LIBRARY", "03"),
    SOUND("SOUND", "04"),
    WORKSHOP("WORKSHOP", "05")
}

enum class HardwareNavScreen(val title: String, val code: String, val category: HardwareNavCategory) {
    CONTROL_DECK("CONTROL DECK", "01", HardwareNavCategory.HOME),
    CASSETTE("CASSETTE DECK", "02", HardwareNavCategory.MACHINES),
    DISCMAN("PORTABLE CD", "03", HardwareNavCategory.MACHINES),
    DJ_DECK("DJ MIXER", "04", HardwareNavCategory.MACHINES),
    LYRICS_ROOM("LYRICS ROOM", "05", HardwareNavCategory.SOUND),
    MIXTAPE_LIBRARY("LIBRARY CRATE", "06", HardwareNavCategory.LIBRARY),
    SOUND_LAB("SOUND LAB", "07", HardwareNavCategory.SOUND),
    WORKSHOP("CUSTOM WORKSHOP", "08", HardwareNavCategory.WORKSHOP),
    VIRTUAL_ROOM("COLLECTION ROOM", "09", HardwareNavCategory.WORKSHOP),
    RECORDING_BOOTH("RECORDING BOOTH", "10", HardwareNavCategory.SOUND),
    RADIO_MODE("FM TUNER", "11", HardwareNavCategory.WORKSHOP),
    DISCOVERY_MACHINE("DISCOVERY", "12", HardwareNavCategory.SOUND),
    OBSESSION_MODE("OBSESSION", "13", HardwareNavCategory.WORKSHOP),
    MUSIC_DIARY("STATISTICS & DIARY", "14", HardwareNavCategory.WORKSHOP),
    MINIDISC("MINIDISC DECK", "15", HardwareNavCategory.MACHINES),
    BOOMBOX("GHETTOBLASTER", "16", HardwareNavCategory.MACHINES),
    VINYL("TURNTABLE DECK", "17", HardwareNavCategory.MACHINES),
    FOUR_TRACK("PORTASTUDIO 4-TRK", "18", HardwareNavCategory.MACHINES),
    ROTARY_PHONE("ROTARY PHONE", "19", HardwareNavCategory.MACHINES),
    PARTY_JUKEBOX("PARTY JUKEBOX", "20", HardwareNavCategory.MACHINES),
    BUILD_A_MACHINE("BUILD-A-MACHINE", "21", HardwareNavCategory.WORKSHOP)
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
    val lyrics: String? = null,
    val volumeGainOffsetDb: Float = 0.0f
)

data class ScanProgress(
    val isScanning: Boolean = false,
    val totalFound: Int = 0,
    val newlyAdded: Int = 0,
    val currentFileName: String = "",
    val formatsDetected: Set<String> = emptySet(),
    val statusMessage: String = "SCANNER READY",
    val progressPercent: Float = 0f,
    val isIncremental: Boolean = false
)

data class FolderEntry(
    val fullPath: String,
    val displayName: String,
    val isDirectory: Boolean,
    val trackCount: Int = 0,
    val subFolderCount: Int = 0,
    val sampleTrack: Track? = null
)

enum class CuratedCategory(val title: String, val badge: String, val description: String) {
    RECENTLY_ADDED("RECENTLY ADDED", "NEW CRATE", "Freshly acquired & indexed tracks"),
    RECENTLY_PLAYED("RECENTLY PLAYED", "SESSIONS", "Recently loaded on decks"),
    MOST_PLAYED("MOST PLAYED", "ROTATION", "Most spun records in archive"),
    FAVOURITES("FAVOURITES", "GOLD STARS", "Starred essential listening"),
    FORGOTTEN_TRACKS("FORGOTTEN TRACKS", "UNTOUCHED", "Dusty gems unplayed in 90+ days"),
    LOSSLESS_AUDIO("LOSSLESS MASTERS", "STUDIO GRADE", "Audiophile FLAC, WAV & AIFF"),
    EXTENDED_CUTS("EXTENDED 12\"", "LONG PLAYS", "Tracks over 5 minutes duration"),
    INTERLUDES("INTERLUDES", "SHORT CUTS", "Brief skits & intros under 2 minutes")
}

fun detectAudioFormat(path: String?, mimeType: String? = null): String {
    val ext = path?.substringAfterLast('.', "")?.uppercase() ?: ""
    return when (ext) {
        "MP3" -> "MP3"
        "FLAC" -> "FLAC"
        "WAV", "WAVE" -> "WAV"
        "AAC" -> "AAC"
        "M4A", "M4B", "M4P" -> "M4A"
        "OGG", "OGA" -> "OGG"
        "OPUS" -> "OPUS"
        "AIFF", "AIF" -> "AIFF"
        "WMA" -> "WMA"
        else -> {
            when {
                mimeType?.contains("flac", ignoreCase = true) == true -> "FLAC"
                mimeType?.contains("wav", ignoreCase = true) == true -> "WAV"
                mimeType?.contains("opus", ignoreCase = true) == true -> "OPUS"
                mimeType?.contains("ogg", ignoreCase = true) == true -> "OGG"
                mimeType?.contains("aac", ignoreCase = true) == true -> "AAC"
                mimeType?.contains("mp4", ignoreCase = true) == true || mimeType?.contains("m4a", ignoreCase = true) == true -> "M4A"
                mimeType?.contains("mpeg", ignoreCase = true) == true || mimeType?.contains("mp3", ignoreCase = true) == true -> "MP3"
                else -> if (ext.isNotBlank() && ext.length in 2..4) ext else "AUDIO"
            }
        }
    }
}

fun parseGenres(rawGenre: String?): List<String> {
    if (rawGenre.isNullOrBlank() || rawGenre.equals("Unknown", ignoreCase = true) || rawGenre.equals("Local Audio", ignoreCase = true)) {
        return listOf("Underground")
    }
    val split = rawGenre.split(Regex("[/,;|&•+]"))
        .map { it.trim() }
        .filter { it.isNotBlank() && it.length > 1 }
    return if (split.isEmpty()) listOf(rawGenre.trim()) else split
}

val Track.genreList: List<String> get() = parseGenres(genre)
val Track.audioFormat: String get() = detectAudioFormat(path)

data class DuplicateGroup(
    val id: String,
    val title: String,
    val artist: String,
    val reason: String,
    val recommendedKeep: Track,
    val duplicateTracks: List<Track>
)

data class LibraryHealthReport(
    val totalTracks: Int = 0,
    val duplicateGroups: List<DuplicateGroup> = emptyList(),
    val missingFiles: List<Track> = emptyList(),
    val missingArtworkCount: Int = 0,
    val missingMetadataCount: Int = 0,
    val losslessCount: Int = 0,
    val totalDurationMs: Long = 0L,
    val healthScore: Int = 100,
    val isScanning: Boolean = false
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

data class AbRepeatState(
    val isEnabled: Boolean = false,
    val pointA: Long? = null,
    val pointB: Long? = null
)

data class SleepTimerState(
    val isRunning: Boolean = false,
    val totalMinutes: Int = 0,
    val remainingSeconds: Int = 0,
    val fadeoutActive: Boolean = false
)

data class AudioDiagnostics(
    val codec: String = "PCM / 16-Bit FLAC/MP3",
    val sampleRateKhz: Float = 44.1f,
    val bitDepth: Int = 16,
    val channels: String = "Stereo 2.0 (L/R)",
    val bufferHealthPercent: Int = 100,
    val dspPipeline: String = "ExoPlayer -> 10-Band EQ -> Preamp -> LoudnessEnhancer -> Limiter",
    val activeOutput: String = "High Definition Audio Route",
    val perSongGainOffsetDb: Float = 0.0f
)

enum class ListeningEnvironment(
    val title: String,
    val code: String,
    val description: String,
    val ampBoostPercent: Int,
    val bassBoostStrength: Int,
    val tapeHissEnabled: Boolean,
    val eqPreset: String
) {
    BEDROOM("BEDROOM", "ENV-01", "Warm, mellow tape hiss & soft low end", 95, 300, true, "Tape Cassette Warmth"),
    NIGHT_DRIVE("NIGHT DRIVE", "ENV-02", "Deep bass & punchy presence for motion", 130, 750, false, "Underground Bass"),
    HEADPHONES("HEADPHONES", "ENV-03", "Precision stereo imaging & high definition", 110, 450, false, "Discman Crystal"),
    STUDIO("STUDIO", "ENV-04", "Flat reference response, zero coloration", 100, 0, false, "Flat"),
    PARTY("PARTY", "ENV-05", "Max loudness amplification & 3D surround sound", 160, 900, false, "90s Rave Club"),
    QUIET_LISTENING("QUIET", "ENV-06", "Gentle acoustic curve & subtle dynamics", 85, 200, false, "Studio Reference")
}

enum class SmartFolderType(
    val title: String,
    val subtitle: String,
    val ruleCode: String
) {
    PRE_2000("PRE-2000 ARCHIVE", "Tracks released before year 2000", "RULE_YEAR_LT_2000"),
    DEEP_CUTS("DEEP CUTS (> 5 MIN)", "Extended club mixes & epics", "RULE_DUR_GT_5MIN"),
    NEGLECTED("UNPLAYED 90+ DAYS", "Rediscover forgotten crate gems", "RULE_LAST_PLAYED_90D"),
    LOSSLESS("LOSSLESS / HI-RES", "FLAC, WAV & high-fidelity audio", "RULE_FORMAT_LOSSLESS"),
    HEAVY_ROTATION("HEAVY ROTATION", "Most spun tracks (> 5 plays)", "RULE_REPLAY_GT_5"),
    MISSING_TAGS("METADATA DEFECTS", "Missing artwork or album tags", "RULE_MISSING_METADATA"),
    KENYA_ROOTS("KENYA & AFRO GROOVES", "East African & Afro underground", "RULE_GENRE_AFRO")
}

enum class LibraryViewMode {
    LIST, GRID, WALL
}

data class HotCue(
    val cueIndex: Int,
    val timestampMs: Long,
    val label: String
)

data class CustomMachineSlot(
    val slotIndex: Int,
    val moduleName: String,
    val icon: String
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
    val activeDeckMode: HardwareNavScreen = HardwareNavScreen.CONTROL_DECK,
    val crossfadeDurationSec: Int = 0, // 0, 2, 4, 8, 12 sec
    val abRepeat: AbRepeatState = AbRepeatState(),
    val sleepTimer: SleepTimerState = SleepTimerState(),
    val audioDiagnostics: AudioDiagnostics = AudioDiagnostics(),
    val currentEnvironment: ListeningEnvironment = ListeningEnvironment.HEADPHONES
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
    val is10BandMode: Boolean = true,
    val bands: Map<Int, Int> = mapOf(
        0 to 4,   // 31 Hz Sub-bass
        1 to 3,   // 62 Hz Bass
        2 to 2,   // 125 Hz Upper bass
        3 to 1,   // 250 Hz Low mid
        4 to 0,   // 500 Hz Mid
        5 to 2,   // 1 kHz Center mid
        6 to 3,   // 2 kHz Upper mid
        7 to 4,   // 4 kHz Presence
        8 to 5,   // 8 kHz Brilliance
        9 to 4    // 16 kHz Air
    ),
    val bassBoost: Int = 600, // 0 to 1000
    val virtualizer: Int = 450, // 0 to 1000
    val presetName: String = "Studio Reference",
    val parametricFreqHz: Float = 1000f,
    val parametricBandwidthQ: Float = 1.4f,
    val parametricGainDb: Float = 0.0f
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

