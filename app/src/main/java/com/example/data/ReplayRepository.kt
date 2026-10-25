package com.example.data

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.example.model.CassetteTape
import com.example.model.TapeSide
import com.example.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ReplayRepository(
    private val context: Context,
    private val dao: ReplayDao
) {
    companion object {
        private const val TAG = "ReplayRepository"
    }

    val allTracks: Flow<List<Track>> = dao.getAllTracks().map { list ->
        list.map { it.toDomain() }
    }

    val favoriteTracks: Flow<List<Track>> = dao.getFavoriteTracks().map { list ->
        list.map { it.toDomain() }
    }

    val mostPlayedTracks: Flow<List<Track>> = dao.getMostPlayedTracks().map { list ->
        list.map { it.toDomain() }
    }

    val recentlyPlayedTracks: Flow<List<Track>> = dao.getRecentlyPlayedTracks().map { list ->
        list.map { it.toDomain() }
    }

    val allMixtapes: Flow<List<MixtapeEntity>> = dao.getAllMixtapes()
    val allRecordings: Flow<List<RecordingEntity>> = dao.getAllRecordings()
    val allObsessions: Flow<List<ObsessionProfileEntity>> = dao.getAllObsessions()
    val allRadioPresets: Flow<List<RadioPresetEntity>> = dao.getAllRadioPresets()
    val recentHistory: Flow<List<ListeningHistoryEntity>> = dao.getRecentHistory()

    suspend fun initializeLibrary() = withContext(Dispatchers.IO) {
        // 1. Check if tracks already exist
        val existingTracks = dao.getAllTracks().first()
        if (existingTracks.isEmpty()) {
            Log.d(TAG, "No existing tracks in database. Seeding 90s underground tracks...")
            val seeded = AudioSeeder.seedTracksIfNecessary(context)
            dao.insertTracks(seeded)

            // Seed default Mixtape
            val trackIds = seeded.map { it.id.toString() }
            val sideA = trackIds.take(3).joinToString(",")
            val sideB = trackIds.drop(3).joinToString(",")
            dao.insertMixtape(
                MixtapeEntity(
                    title = "MIXTAPE 99: ACID & BREAKS",
                    labelText = "C-60 / CHROME HIGH BIAS",
                    notes = "Underground warehouse party masters recorded on dual cassette deck.",
                    shellColorHex = "#16191C",
                    reelColorHex = "#00FF66",
                    isTransparent = true,
                    sideATrackIds = sideA,
                    sideBTrackIds = sideB,
                    durationMinutes = 60
                )
            )

            // Seed Radio Presets
            dao.insertRadioPresets(
                listOf(
                    RadioPresetEntity(name = "99.1 REPLAY FM (NYC)", freqMhz = 99.1f, isLocalShuffle = true, genre = "Breakbeat & Jungle"),
                    RadioPresetEntity(name = "88.5 PIRATE DUB (LDN)", freqMhz = 88.5f, isLocalShuffle = true, genre = "Dub & Reggae"),
                    RadioPresetEntity(name = "104.3 ACID MATRIX (BER)", freqMhz = 104.3f, isLocalShuffle = true, genre = "Acid Techno"),
                    RadioPresetEntity(name = "92.7 BOOM BAP VAULT", freqMhz = 92.7f, isLocalShuffle = true, genre = "Golden Era Hip-Hop"),
                    RadioPresetEntity(name = "107.9 CYBER DISCMAN", freqMhz = 107.9f, isLocalShuffle = true, genre = "Eurodance / Trance")
                )
            )
        }

        // 2. Perform MediaStore Scan
        scanDeviceMusic()
    }

    suspend fun scanDeviceMusic() = withContext(Dispatchers.IO) {
        try {
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.YEAR
            )

            val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= 15000"
            val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

            context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                sortOrder
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val dataCol = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
                val albumIdCol = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
                val yearCol = cursor.getColumnIndex(MediaStore.Audio.Media.YEAR)

                val scannedTracks = mutableListOf<TrackEntity>()

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val title = cursor.getString(titleCol) ?: "Unknown Track"
                    val artist = cursor.getString(artistCol) ?: "Unknown Artist"
                    val album = cursor.getString(albumCol) ?: "Unknown Album"
                    val duration = cursor.getLong(durationCol)
                    val path = if (dataCol >= 0) cursor.getString(dataCol) ?: "" else ""
                    val year = if (yearCol >= 0) cursor.getInt(yearCol) else 1999
                    val albumId = if (albumIdCol >= 0) cursor.getLong(albumIdCol) else -1L

                    val artUri = if (albumId != -1L) {
                        ContentUris.withAppendedId(
                            Uri.parse("content://media/external/audio/albumart"),
                            albumId
                        ).toString()
                    } else null

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    ).toString()

                    scannedTracks.add(
                        TrackEntity(
                            id = id,
                            title = title,
                            artist = artist,
                            album = album,
                            durationMs = duration,
                            contentUri = contentUri,
                            albumArtUri = artUri,
                            path = path,
                            genre = "Local Audio",
                            year = if (year > 0) year else 1999
                        )
                    )
                }

                if (scannedTracks.isNotEmpty()) {
                    dao.insertTracks(scannedTracks)
                    Log.d(TAG, "Scanned and saved ${scannedTracks.size} user tracks from device.")
                }
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "Audio permission not yet granted for device MediaStore scan: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Error querying MediaStore", e)
        }
    }

    suspend fun toggleFavorite(trackId: Long, currentFav: Boolean) = withContext(Dispatchers.IO) {
        dao.toggleFavorite(trackId, !currentFav)
    }

    suspend fun recordTrackPlayed(track: Track, durationPlayedMs: Long = 0L) = withContext(Dispatchers.IO) {
        dao.incrementReplay(track.id)
        dao.insertHistory(
            ListeningHistoryEntity(
                trackId = track.id,
                title = track.title,
                artist = track.artist,
                album = track.album,
                durationPlayedMs = durationPlayedMs
            )
        )
    }

    suspend fun updateTrackLyrics(trackId: Long, lyrics: String) = withContext(Dispatchers.IO) {
        dao.updateLyrics(trackId, lyrics)
    }

    suspend fun saveMixtape(
        id: Long = 0,
        title: String,
        labelText: String,
        notes: String,
        shellColorHex: String,
        reelColorHex: String,
        isTransparent: Boolean,
        sideATracks: List<Long>,
        sideBTracks: List<Long>,
        durationMinutes: Int
    ): Long = withContext(Dispatchers.IO) {
        dao.insertMixtape(
            MixtapeEntity(
                id = id,
                title = title,
                labelText = labelText,
                notes = notes,
                shellColorHex = shellColorHex,
                reelColorHex = reelColorHex,
                isTransparent = isTransparent,
                sideATrackIds = sideATracks.joinToString(","),
                sideBTrackIds = sideBTracks.joinToString(","),
                durationMinutes = durationMinutes
            )
        )
    }

    suspend fun deleteMixtape(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteMixtape(id)
    }

    suspend fun saveRecording(title: String, filePath: String, durationMs: Long, voiceoverTrackTitle: String): Long =
        withContext(Dispatchers.IO) {
            dao.insertRecording(
                RecordingEntity(
                    title = title,
                    filePath = filePath,
                    durationMs = durationMs,
                    voiceoverTrackTitle = voiceoverTrackTitle
                )
            )
        }

    suspend fun deleteRecording(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteRecording(id)
    }

    suspend fun saveObsessionProfile(trackId: Long, title: String, notes: String, target: Int): Long =
        withContext(Dispatchers.IO) {
            dao.insertObsession(
                ObsessionProfileEntity(
                    trackId = trackId,
                    shrineTitle = title,
                    personalNotes = notes,
                    replayTarget = target
                )
            )
        }

    suspend fun deleteObsession(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteObsession(id)
    }

    private fun TrackEntity.toDomain() = Track(
        id = id,
        title = title,
        artist = artist,
        album = album,
        durationMs = durationMs,
        contentUri = contentUri,
        albumArtUri = albumArtUri,
        path = path,
        genre = genre,
        year = year,
        dateAdded = dateAdded,
        replayCount = replayCount,
        lastPlayedMs = lastPlayedMs,
        isFavorite = isFavorite,
        lyrics = customLyrics
    )
}
