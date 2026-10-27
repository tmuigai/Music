package com.example.data

import android.content.ContentUris
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.example.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
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
    val allMiniDiscs: Flow<List<MiniDiscEntity>> = dao.getAllMiniDiscs()
    val allBookmarks: Flow<List<TrackBookmarkEntity>> = dao.getAllBookmarks()
    val allRecordings: Flow<List<RecordingEntity>> = dao.getAllRecordings()
    val allObsessions: Flow<List<ObsessionProfileEntity>> = dao.getAllObsessions()
    val allRadioPresets: Flow<List<RadioPresetEntity>> = dao.getAllRadioPresets()
    val recentHistory: Flow<List<ListeningHistoryEntity>> = dao.getRecentHistory()

    fun getBookmarksForTrack(trackId: Long): Flow<List<TrackBookmarkEntity>> =
        dao.getBookmarksForTrack(trackId)

    suspend fun initializeLibrary() = withContext(Dispatchers.IO) {
        // Purge any synthetic test tracks (IDs 9901..9905) to ensure pure real user data
        dao.purgeSyntheticTestTracks()

        // Seed pure local radio stations if empty
        val presets = dao.getAllRadioPresets()
        dao.insertRadioPresets(
            listOf(
                RadioPresetEntity(name = "LOCAL VAULT SHUFFLE", freqMhz = 99.1f, isLocalShuffle = true, genre = "Device Audio"),
                RadioPresetEntity(name = "HEAVY ROTATION AIR", freqMhz = 88.5f, isLocalShuffle = true, genre = "Most Played"),
                RadioPresetEntity(name = "UNDERGROUND DUB FM", freqMhz = 104.3f, isLocalShuffle = true, genre = "Unplayed Gems"),
                RadioPresetEntity(name = "CASSETTE MASTER STATION", freqMhz = 92.7f, isLocalShuffle = true, genre = "Mixtape Shuffle"),
                RadioPresetEntity(name = "MINIDISC OPTICAL STREAM", freqMhz = 107.9f, isLocalShuffle = true, genre = "Favorites")
            )
        )

        // Perform real MediaStore Scan
        scanDeviceMusic()
    }

    suspend fun scanDeviceMusic(): Int = withContext(Dispatchers.IO) {
        var count = 0
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

            val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= 5000"
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
                    val year = if (yearCol >= 0) cursor.getInt(yearCol) else 0
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
                            year = year
                        )
                    )
                }

                if (scannedTracks.isNotEmpty()) {
                    dao.insertTracks(scannedTracks)
                    count = scannedTracks.size
                    Log.d(TAG, "Scanned $count real audio tracks from MediaStore.")
                }
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "Audio permission not yet granted: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Error querying MediaStore", e)
        }
        count
    }

    // Import real audio files directly chosen by user via SAF OpenDocument picker
    suspend fun importAudioUri(uri: Uri): Track? = withContext(Dispatchers.IO) {
        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, uri)

            val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                ?: uri.lastPathSegment?.substringAfterLast('/')?.substringBeforeLast('.')
                ?: "Imported Track"
            val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: "Unknown Artist"
            val album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM) ?: "Imported Album"
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val durationMs = durationStr?.toLongOrNull() ?: 180000L
            val genre = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE) ?: "Audio File"
            val yearStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)
            val year = yearStr?.toIntOrNull() ?: 0

            retriever.release()

            val trackId = System.currentTimeMillis()
            val entity = TrackEntity(
                id = trackId,
                title = title,
                artist = artist,
                album = album,
                durationMs = durationMs,
                contentUri = uri.toString(),
                albumArtUri = null,
                path = uri.path ?: "",
                genre = genre,
                year = year,
                dateAdded = System.currentTimeMillis()
            )

            dao.insertTrack(entity)
            Log.d(TAG, "Successfully imported real user track: $title")
            entity.toDomain()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to import real audio file: $uri", e)
            null
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

    suspend fun saveMiniDisc(discTitle: String, labelText: String, colorHex: String, atracMode: String, trackIds: List<Long>): Long =
        withContext(Dispatchers.IO) {
            dao.insertMiniDisc(
                MiniDiscEntity(
                    discTitle = discTitle,
                    labelText = labelText,
                    colorHex = colorHex,
                    atracMode = atracMode,
                    trackIds = trackIds.joinToString(",")
                )
            )
        }

    suspend fun deleteMiniDisc(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteMiniDisc(id)
    }

    suspend fun saveBookmark(trackId: Long, timestampMs: Long, label: String): Long =
        withContext(Dispatchers.IO) {
            dao.insertBookmark(
                TrackBookmarkEntity(
                    trackId = trackId,
                    timestampMs = timestampMs,
                    label = label
                )
            )
        }

    suspend fun deleteBookmark(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteBookmark(id)
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
