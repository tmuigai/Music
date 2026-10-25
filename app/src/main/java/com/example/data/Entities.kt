package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val contentUri: String,
    val albumArtUri: String? = null,
    val path: String = "",
    val genre: String = "Underground",
    val year: Int = 1999,
    val dateAdded: Long = System.currentTimeMillis(),
    val replayCount: Int = 0,
    val lastPlayedMs: Long = 0,
    val isFavorite: Boolean = false,
    val customLyrics: String? = null
)

@Entity(tableName = "mixtapes")
data class MixtapeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val labelText: String,
    val notes: String = "",
    val shellColorHex: String = "#1E2226",
    val reelColorHex: String = "#FF6B1A",
    val isTransparent: Boolean = false,
    val sideATrackIds: String, // Comma-separated track IDs
    val sideBTrackIds: String, // Comma-separated track IDs
    val durationMinutes: Int = 60,
    val stickerDataJson: String = "[]",
    val createdMs: Long = System.currentTimeMillis()
)

@Entity(tableName = "listening_history")
data class ListeningHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val trackId: Long,
    val title: String,
    val artist: String,
    val album: String,
    val playedAtMs: Long = System.currentTimeMillis(),
    val durationPlayedMs: Long = 0
)

@Entity(tableName = "recordings")
data class RecordingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val filePath: String,
    val durationMs: Long,
    val timestampMs: Long = System.currentTimeMillis(),
    val voiceoverTrackTitle: String = ""
)

@Entity(tableName = "obsession_profiles")
data class ObsessionProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val trackId: Long,
    val shrineTitle: String,
    val personalNotes: String,
    val replayTarget: Int = 99,
    val currentReplays: Int = 0,
    val createdMs: Long = System.currentTimeMillis()
)

@Entity(tableName = "radio_presets")
data class RadioPresetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val freqMhz: Float,
    val streamUrl: String = "",
    val isLocalShuffle: Boolean = false,
    val genre: String = "Underground 90s"
)
