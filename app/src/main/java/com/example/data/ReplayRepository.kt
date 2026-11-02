package com.example.data

import android.content.ContentUris
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.example.model.Track
import com.example.model.audioFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _scanProgress = MutableStateFlow(com.example.model.ScanProgress())
    val scanProgress: StateFlow<com.example.model.ScanProgress> = _scanProgress.asStateFlow()

    suspend fun scanDeviceMusic(isIncremental: Boolean = false): Int = withContext(Dispatchers.IO) {
        var count = 0
        _scanProgress.value = com.example.model.ScanProgress(
            isScanning = true,
            statusMessage = if (isIncremental) "INCREMENTAL SCANNING STORAGE..." else "SCANNING ALL STORAGE MEDIA...",
            isIncremental = isIncremental
        )
        try {
            val existingIds = if (isIncremental) dao.getAllTrackIds().toHashSet() else hashSetOf()
            val existingUris = if (isIncremental) dao.getAllContentUris().toHashSet() else hashSetOf()

            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.YEAR,
                MediaStore.Audio.Media.MIME_TYPE,
                MediaStore.Audio.Media.DATE_ADDED
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
                val mimeCol = cursor.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE)
                val dateAddedCol = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)

                val scannedTracks = mutableListOf<TrackEntity>()
                val detectedFormats = mutableSetOf<String>()
                val totalInCursor = cursor.count
                var index = 0
                var newCount = 0

                while (cursor.moveToNext()) {
                    index++
                    val id = cursor.getLong(idCol)
                    val title = cursor.getString(titleCol) ?: "Unknown Track"
                    val artist = cursor.getString(artistCol) ?: "Unknown Artist"
                    val album = cursor.getString(albumCol) ?: "Unknown Album"
                    val duration = cursor.getLong(durationCol)
                    val path = if (dataCol >= 0) cursor.getString(dataCol) ?: "" else ""
                    val year = if (yearCol >= 0) cursor.getInt(yearCol) else 0
                    val albumId = if (albumIdCol >= 0) cursor.getLong(albumIdCol) else -1L
                    val mimeType = if (mimeCol >= 0) cursor.getString(mimeCol) else null
                    val dateAddedSec = if (dateAddedCol >= 0) cursor.getLong(dateAddedCol) else System.currentTimeMillis() / 1000

                    val format = com.example.model.detectAudioFormat(path, mimeType)
                    detectedFormats.add(format)

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    ).toString()

                    if (isIncremental && (existingIds.contains(id) || existingUris.contains(contentUri))) {
                        // Already indexed
                        continue
                    }

                    val artUri = if (albumId != -1L) {
                        ContentUris.withAppendedId(
                            Uri.parse("content://media/external/audio/albumart"),
                            albumId
                        ).toString()
                    } else null

                    // Extract genre with fallback
                    var genre = "Underground"
                    if (path.isNotBlank()) {
                        try {
                            val ext = path.substringAfterLast('.', "").lowercase()
                            genre = when {
                                ext == "flac" -> "FLAC / Studio Lossless"
                                ext == "wav" -> "WAV / Uncompressed"
                                ext == "opus" -> "Opus / High Density"
                                ext == "ogg" -> "OGG / Vorbis"
                                else -> "Local Audio / ${format}"
                            }
                        } catch (_: Exception) {}
                    }

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
                            genre = genre,
                            year = year,
                            dateAdded = dateAddedSec * 1000
                        )
                    )
                    newCount++

                    if (index % 5 == 0 || index == totalInCursor) {
                        _scanProgress.value = com.example.model.ScanProgress(
                            isScanning = true,
                            totalFound = totalInCursor,
                            newlyAdded = newCount,
                            currentFileName = title,
                            formatsDetected = detectedFormats.toSet(),
                            statusMessage = "SCANNING [$index / $totalInCursor]: $title",
                            progressPercent = if (totalInCursor > 0) index.toFloat() / totalInCursor else 0f,
                            isIncremental = isIncremental
                        )
                    }
                }

                if (scannedTracks.isNotEmpty()) {
                    dao.insertTracks(scannedTracks)
                    count = scannedTracks.size
                    Log.d(TAG, "Indexed $count tracks. Formats: $detectedFormats")
                }

                _scanProgress.value = com.example.model.ScanProgress(
                    isScanning = false,
                    totalFound = totalInCursor,
                    newlyAdded = newCount,
                    formatsDetected = detectedFormats.toSet(),
                    statusMessage = "COMPLETE // ${totalInCursor} TOTAL (+$newCount NEW)",
                    progressPercent = 1.0f,
                    isIncremental = isIncremental
                )
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "Audio permission not yet granted: ${e.message}")
            _scanProgress.value = com.example.model.ScanProgress(
                isScanning = false,
                statusMessage = "PERMISSION NEEDED FOR LOCAL SCAN"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error querying MediaStore", e)
            _scanProgress.value = com.example.model.ScanProgress(
                isScanning = false,
                statusMessage = "SCAN ERROR: ${e.message}"
            )
        }
        count
    }

    // Manual Folder Import (via Android SAF OpenDocumentTree)
    suspend fun importFolderTree(treeUri: Uri): Int = withContext(Dispatchers.IO) {
        var importedCount = 0
        val detectedFormats = mutableSetOf<String>()
        _scanProgress.value = com.example.model.ScanProgress(
            isScanning = true,
            statusMessage = "OPENING FOLDER CRATE...",
            isIncremental = false
        )

        try {
            try {
                context.contentResolver.takePersistableUriPermission(
                    treeUri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {}

            val rootDocId = android.provider.DocumentsContract.getTreeDocumentId(treeUri)
            val docQueue = ArrayDeque<Pair<String, String>>() // docId to folderPath
            docQueue.add(rootDocId to (treeUri.lastPathSegment?.substringAfterLast(':') ?: "Imported Folder"))

            val supportedExtensions = setOf("mp3", "flac", "wav", "aac", "m4a", "ogg", "opus", "aiff", "wma")
            val batchTracks = mutableListOf<TrackEntity>()

            while (docQueue.isNotEmpty()) {
                val (currentDocId, currentFolderPath) = docQueue.removeFirst()
                val childrenUri = android.provider.DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, currentDocId)

                val projection = arrayOf(
                    android.provider.DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                    android.provider.DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                    android.provider.DocumentsContract.Document.COLUMN_MIME_TYPE,
                    android.provider.DocumentsContract.Document.COLUMN_LAST_MODIFIED
                )

                context.contentResolver.query(childrenUri, projection, null, null, null)?.use { cursor ->
                    val idCol = cursor.getColumnIndexOrThrow(android.provider.DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                    val nameCol = cursor.getColumnIndexOrThrow(android.provider.DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                    val mimeCol = cursor.getColumnIndexOrThrow(android.provider.DocumentsContract.Document.COLUMN_MIME_TYPE)
                    val modCol = cursor.getColumnIndex(android.provider.DocumentsContract.Document.COLUMN_LAST_MODIFIED)

                    while (cursor.moveToNext()) {
                        val childDocId = cursor.getString(idCol)
                        val displayName = cursor.getString(nameCol) ?: "Unknown"
                        val mimeType = cursor.getString(mimeCol) ?: ""
                        val lastMod = if (modCol >= 0) cursor.getLong(modCol) else System.currentTimeMillis()

                        if (mimeType == android.provider.DocumentsContract.Document.MIME_TYPE_DIR) {
                            // Subdirectory in nested folder tree
                            docQueue.add(childDocId to "$currentFolderPath/$displayName")
                        } else {
                            val ext = displayName.substringAfterLast('.', "").lowercase()
                            if (mimeType.startsWith("audio/") || supportedExtensions.contains(ext)) {
                                val fileDocUri = android.provider.DocumentsContract.buildDocumentUriUsingTree(treeUri, childDocId)
                                val format = com.example.model.detectAudioFormat(displayName, mimeType)
                                detectedFormats.add(format)

                                // Extract metadata using retriever
                                var title = displayName.substringBeforeLast('.')
                                var artist = "Unknown Artist"
                                var album = currentFolderPath.substringAfterLast('/')
                                var durationMs = 180000L
                                var genre = "Underground / $format"
                                var year = 0

                                try {
                                    val retriever = MediaMetadataRetriever()
                                    retriever.setDataSource(context, fileDocUri)
                                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)?.let { if (it.isNotBlank()) title = it }
                                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)?.let { if (it.isNotBlank()) artist = it }
                                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)?.let { if (it.isNotBlank()) album = it }
                                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()?.let { durationMs = it }
                                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)?.let { if (it.isNotBlank()) genre = it }
                                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)?.toIntOrNull()?.let { year = it }
                                    retriever.release()
                                } catch (_: Exception) {}

                                val trackId = System.currentTimeMillis() + batchTracks.size + (Math.random() * 1000).toLong()
                                batchTracks.add(
                                    TrackEntity(
                                        id = trackId,
                                        title = title,
                                        artist = artist,
                                        album = album,
                                        durationMs = durationMs,
                                        contentUri = fileDocUri.toString(),
                                        albumArtUri = null,
                                        path = "$currentFolderPath/$displayName",
                                        genre = genre,
                                        year = year,
                                        dateAdded = lastMod
                                    )
                                )
                                importedCount++

                                _scanProgress.value = com.example.model.ScanProgress(
                                    isScanning = true,
                                    totalFound = importedCount,
                                    newlyAdded = importedCount,
                                    currentFileName = displayName,
                                    formatsDetected = detectedFormats.toSet(),
                                    statusMessage = "READING: $displayName",
                                    progressPercent = 0.5f,
                                    isIncremental = false
                                )
                            }
                        }
                    }
                }
            }

            if (batchTracks.isNotEmpty()) {
                dao.insertTracks(batchTracks)
            }

            _scanProgress.value = com.example.model.ScanProgress(
                isScanning = false,
                totalFound = importedCount,
                newlyAdded = importedCount,
                formatsDetected = detectedFormats.toSet(),
                statusMessage = "FOLDER CRATE IMPORTED // $importedCount TRACK(S)",
                progressPercent = 1.0f,
                isIncremental = false
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error importing folder tree", e)
            _scanProgress.value = com.example.model.ScanProgress(
                isScanning = false,
                statusMessage = "IMPORT ERROR: ${e.message}"
            )
        }
        importedCount
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

    suspend fun updateTrackVolumeGainOffset(trackId: Long, gainDb: Float) = withContext(Dispatchers.IO) {
        dao.updateTrackVolumeGainOffset(trackId, gainDb)
    }

    suspend fun bulkUpdateMetadata(trackIds: List<Long>, year: Int, genre: String) = withContext(Dispatchers.IO) {
        dao.bulkUpdateMetadata(trackIds, year, genre)
    }

    suspend fun deleteTrack(trackId: Long) = withContext(Dispatchers.IO) {
        dao.deleteTrack(trackId)
    }

    suspend fun deleteTracks(trackIds: List<Long>) = withContext(Dispatchers.IO) {
        dao.deleteTracks(trackIds)
    }

    suspend fun analyzeLibraryHealth(): com.example.model.LibraryHealthReport = withContext(Dispatchers.IO) {
        val tracks = dao.getAllTracksSnapshot().map { it.toDomain() }
        if (tracks.isEmpty()) {
            return@withContext com.example.model.LibraryHealthReport()
        }

        // 1. Group duplicates
        val duplicateGroups = mutableListOf<com.example.model.DuplicateGroup>()
        val processedIds = mutableSetOf<Long>()

        // Group by normalized title + artist
        val byTitleArtist = tracks.groupBy {
            "${it.title.lowercase().trim()}|${it.artist.lowercase().trim()}"
        }

        for ((_, groupTracks) in byTitleArtist) {
            if (groupTracks.size > 1) {
                // Determine best version to keep: lossless > duration > artwork/lyrics
                val best = groupTracks.maxWithOrNull(
                    compareBy<Track> {
                        val fmt = it.audioFormat
                        if (fmt == "FLAC" || fmt == "WAV") 3 else if (fmt == "M4A" || fmt == "OGG") 2 else 1
                    }.thenBy { if (!it.lyrics.isNullOrBlank()) 1 else 0 }
                    .thenBy { if (!it.albumArtUri.isNullOrBlank()) 1 else 0 }
                    .thenBy { it.durationMs }
                ) ?: groupTracks.first()

                val duplicates = groupTracks.filter { it.id != best.id }
                duplicates.forEach { processedIds.add(it.id) }
                duplicateGroups.add(
                    com.example.model.DuplicateGroup(
                        id = "dup_${best.id}",
                        title = best.title,
                        artist = best.artist,
                        reason = "Identical Title & Artist (${groupTracks.size} copies)",
                        recommendedKeep = best,
                        duplicateTracks = duplicates
                    )
                )
            }
        }

        // Also check by filename for remaining tracks
        val remainingTracks = tracks.filterNot { processedIds.contains(it.id) }
        val byFilename = remainingTracks.filter { it.path.isNotBlank() }.groupBy {
            it.path.substringAfterLast('/').lowercase().trim()
        }
        for ((filename, groupTracks) in byFilename) {
            if (groupTracks.size > 1 && !groupTracks.all { processedIds.contains(it.id) }) {
                val best = groupTracks.first()
                val duplicates = groupTracks.filter { it.id != best.id }
                duplicateGroups.add(
                    com.example.model.DuplicateGroup(
                        id = "file_${best.id}",
                        title = best.title,
                        artist = best.artist,
                        reason = "Duplicate File: $filename",
                        recommendedKeep = best,
                        duplicateTracks = duplicates
                    )
                )
            }
        }

        // 2. Identify missing files
        val missingFiles = mutableListOf<Track>()
        for (track in tracks) {
            var exists = true
            if (track.path.isNotBlank() && track.path.startsWith("/")) {
                val file = java.io.File(track.path)
                if (!file.exists()) {
                    exists = false
                }
            } else if (track.contentUri.isNotBlank()) {
                try {
                    val pfd = context.contentResolver.openAssetFileDescriptor(Uri.parse(track.contentUri), "r")
                    if (pfd != null) {
                        pfd.close()
                    } else {
                        exists = false
                    }
                } catch (_: Exception) {
                    exists = false
                }
            }
            if (!exists) {
                missingFiles.add(track)
            }
        }

        // 3. Missing artwork & metadata counts
        var missingArtworkCount = 0
        var missingMetadataCount = 0
        var losslessCount = 0
        var totalDuration = 0L

        for (track in tracks) {
            totalDuration += track.durationMs
            if (track.albumArtUri.isNullOrBlank()) {
                missingArtworkCount++
            }
            if (track.year == 0 || track.genre == "Local Audio" || track.artist.isBlank() || track.artist == "Unknown Artist") {
                missingMetadataCount++
            }
            val fmt = track.audioFormat
            if (fmt == "FLAC" || fmt == "WAV" || fmt == "AIFF") {
                losslessCount++
            }
        }

        val totalDups = duplicateGroups.sumOf { it.duplicateTracks.size }
        val penalty = (totalDups * 4) + (missingFiles.size * 6) + (missingMetadataCount * 1)
        val score = (100 - penalty).coerceIn(15, 100)

        com.example.model.LibraryHealthReport(
            totalTracks = tracks.size,
            duplicateGroups = duplicateGroups,
            missingFiles = missingFiles,
            missingArtworkCount = missingArtworkCount,
            missingMetadataCount = missingMetadataCount,
            losslessCount = losslessCount,
            totalDurationMs = totalDuration,
            healthScore = score,
            isScanning = false
        )
    }

    suspend fun deduplicateLibrary(groups: List<com.example.model.DuplicateGroup>): Int = withContext(Dispatchers.IO) {
        val idsToDelete = groups.flatMap { it.duplicateTracks.map { t -> t.id } }
        if (idsToDelete.isNotEmpty()) {
            dao.deleteTracks(idsToDelete)
        }
        idsToDelete.size
    }

    suspend fun purgeMissingTracks(missingTracks: List<Track>): Int = withContext(Dispatchers.IO) {
        val idsToDelete = missingTracks.map { it.id }
        if (idsToDelete.isNotEmpty()) {
            dao.deleteTracks(idsToDelete)
        }
        idsToDelete.size
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
        lyrics = customLyrics,
        volumeGainOffsetDb = volumeGainOffsetDb
    )
}
