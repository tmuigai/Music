package com.example.ui.screens

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode as AnimRepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MixtapeEntity
import com.example.model.CuratedCategory
import com.example.model.DuplicateGroup
import com.example.model.FolderEntry
import com.example.model.LibraryHealthReport
import com.example.model.LibraryViewMode
import com.example.model.ScanProgress
import com.example.model.SmartFolderType
import com.example.model.Track
import com.example.model.audioFormat
import com.example.model.genreList
import com.example.ui.components.LibraryTrackRow
import com.example.ui.theme.AcidGreen
import com.example.ui.theme.BevelLight
import com.example.ui.theme.BorderHardware
import com.example.ui.theme.BurntOrange
import com.example.ui.theme.ChakraPetchFont
import com.example.ui.theme.DangerRed
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.GunmetalDark
import com.example.ui.theme.GunmetalLight
import com.example.ui.theme.GunmetalMedium
import com.example.ui.theme.LcdCyan
import com.example.ui.theme.MatteBlack
import com.example.ui.theme.ShareTechMonoFont
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun MixtapeLibraryScreen(
    allTracks: List<Track>,
    allMixtapes: List<MixtapeEntity>,
    currentTrack: Track?,
    isPlaying: Boolean,
    accentColor: Color,
    onTrackSelect: (Track) -> Unit,
    onLoadMixtape: (MixtapeEntity) -> Unit,
    onCreateMixtape: (String, String, String, String, String, Boolean, List<Long>, List<Long>, Int) -> Unit,
    onRescanLibrary: () -> Unit,
    onTriggerFilePicker: () -> Unit,
    selectedCategory: String = "SONGS",
    onCategorySelect: (String) -> Unit = {},
    favoriteTracks: List<Track> = emptyList(),
    selectedSmartFolder: SmartFolderType? = null,
    onSelectSmartFolder: (SmartFolderType?) -> Unit = {},
    libraryViewMode: LibraryViewMode = LibraryViewMode.LIST,
    onSelectViewMode: (LibraryViewMode) -> Unit = {},
    onSetTrackGain: (Long, Float) -> Unit = { _, _ -> },
    scanProgress: ScanProgress = ScanProgress(),
    onIncrementalScan: () -> Unit = {},
    onTriggerFolderPicker: () -> Unit = {},
    currentFolderPath: String = "",
    onOpenFolder: (String) -> Unit = {},
    onNavigateUpFolder: () -> Unit = {},
    onPlayFolder: (String) -> Unit = {},
    selectedCuratedCategory: CuratedCategory = CuratedCategory.RECENTLY_ADDED,
    onSelectCuratedCategory: (CuratedCategory) -> Unit = {},
    onPlayCuratedCategory: (CuratedCategory) -> Unit = {},
    selectedGenre: String? = null,
    onSelectGenre: (String?) -> Unit = {},
    onPlayGenre: (String) -> Unit = {},
    libraryHealthReport: LibraryHealthReport = LibraryHealthReport(),
    onRunDiagnostics: () -> Unit = {},
    onDeduplicateAll: () -> Unit = {},
    onPurgeMissingFiles: () -> Unit = {},
    onAutoRepairLibrary: () -> Unit = {},
    isBulkMode: Boolean = false,
    selectedTrackIds: Set<Long> = emptySet(),
    onToggleBulkMode: () -> Unit = {},
    onToggleTrackSelection: (Long) -> Unit = {},
    onSelectAllTracks: (List<Track>) -> Unit = {},
    onClearTrackSelection: () -> Unit = {},
    onDeleteSelectedTracks: () -> Unit = {},
    onBulkUpdateMetadata: (List<Long>, String?, Int?) -> Unit = { _, _, _ -> },
    onDeleteSingleTrack: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    var searchQuery by remember { mutableStateOf("") }
    var showCreatorModal by remember { mutableStateOf(false) }
    var showDiagnosticsDialog by remember { mutableStateOf(false) }
    var showDuplicatesDialog by remember { mutableStateOf(false) }
    var showBulkTagDialog by remember { mutableStateOf(false) }
    var showConfirmDeleteDialog by remember { mutableStateOf(false) }

    var expandedAlbumName by remember { mutableStateOf<String?>(null) }
    var expandedArtistName by remember { mutableStateOf<String?>(null) }

    // Run health check initially once when tracks change or view opens
    LaunchedEffect(allTracks.size) {
        if (allTracks.isNotEmpty() && libraryHealthReport.totalTracks == 0) {
            onRunDiagnostics()
        }
    }

    // Detected format counts
    val formatCounts = remember(allTracks) {
        allTracks.groupBy { it.audioFormat }.mapValues { it.value.size }
    }

    // Filtered tracks for SONGS tab
    val filteredTracks = remember(searchQuery, allTracks, selectedSmartFolder) {
        var list = if (searchQuery.isBlank()) allTracks
        else allTracks.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.artist.contains(searchQuery, ignoreCase = true) ||
                    it.album.contains(searchQuery, ignoreCase = true) ||
                    it.genre.contains(searchQuery, ignoreCase = true) ||
                    it.path.contains(searchQuery, ignoreCase = true)
        }
        if (selectedSmartFolder != null) {
            list = when (selectedSmartFolder) {
                SmartFolderType.PRE_2000 -> list.filter { it.year in 1..1999 }
                SmartFolderType.DEEP_CUTS -> list.filter { it.durationMs >= 300000L }
                SmartFolderType.NEGLECTED -> {
                    val cutOff = System.currentTimeMillis() - (90L * 24 * 60 * 60 * 1000)
                    list.filter { it.lastPlayedMs < cutOff }
                }
                SmartFolderType.LOSSLESS -> list.filter {
                    val fmt = it.audioFormat
                    fmt == "FLAC" || fmt == "WAV" || fmt == "AIFF"
                }
                SmartFolderType.HEAVY_ROTATION -> list.filter { it.replayCount >= 5 }
                SmartFolderType.MISSING_TAGS -> list.filter {
                    it.albumArtUri.isNullOrEmpty() || it.genre == "Unknown" || it.year == 0
                }
                SmartFolderType.KENYA_ROOTS -> list.filter {
                    it.genre.contains("Afro", ignoreCase = true) ||
                    it.genre.contains("Kenya", ignoreCase = true) ||
                    it.genre.contains("Benga", ignoreCase = true)
                }
            }
        }
        list
    }

    // Albums & Artists Groupings
    val albums = remember(allTracks, searchQuery) {
        val base = if (searchQuery.isBlank()) allTracks else allTracks.filter {
            it.album.contains(searchQuery, ignoreCase = true) || it.artist.contains(searchQuery, ignoreCase = true)
        }
        base.groupBy { it.album.ifBlank { "Unknown Album" } }
    }

    val artists = remember(allTracks, searchQuery) {
        val base = if (searchQuery.isBlank()) allTracks else allTracks.filter {
            it.artist.contains(searchQuery, ignoreCase = true)
        }
        base.groupBy { it.artist.ifBlank { "Unknown Artist" } }
    }

    // Genres & Multi-Genre Breakdown
    val genresMap = remember(allTracks) {
        val map = mutableMapOf<String, MutableList<Track>>()
        for (track in allTracks) {
            for (g in track.genreList) {
                val clean = g.trim().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                if (clean.isNotBlank()) {
                    map.getOrPut(clean) { mutableListOf() }.add(track)
                }
            }
        }
        map
    }

    // Curated Category Tracks
    val categoryTracks = remember(allTracks, selectedCuratedCategory) {
        when (selectedCuratedCategory) {
            CuratedCategory.RECENTLY_ADDED -> allTracks.sortedByDescending { it.dateAdded }
            CuratedCategory.RECENTLY_PLAYED -> allTracks.filter { it.lastPlayedMs > 0 }.sortedByDescending { it.lastPlayedMs }
            CuratedCategory.MOST_PLAYED -> allTracks.filter { it.replayCount > 0 }.sortedByDescending { it.replayCount }
            CuratedCategory.FAVOURITES -> allTracks.filter { it.isFavorite }
            CuratedCategory.FORGOTTEN_TRACKS -> {
                val cutoff = System.currentTimeMillis() - (90L * 24 * 60 * 60 * 1000)
                allTracks.filter { (it.lastPlayedMs < cutoff || it.lastPlayedMs == 0L) && it.replayCount < 3 }
            }
            CuratedCategory.LOSSLESS_AUDIO -> allTracks.filter {
                val fmt = it.audioFormat
                fmt == "FLAC" || fmt == "WAV" || fmt == "AIFF"
            }
            CuratedCategory.EXTENDED_CUTS -> allTracks.filter { it.durationMs >= 300000L }
            CuratedCategory.INTERLUDES -> allTracks.filter { it.durationMs in 1..120000L }
        }
    }

    // Folder entries resolution
    val (folderEntries, folderTracks) = remember(allTracks, currentFolderPath) {
        val subfolders = mutableMapOf<String, MutableList<Track>>()
        val direct = mutableListOf<Track>()
        val normCurrent = currentFolderPath.trimEnd('/')

        for (track in allTracks) {
            val path = track.path
            if (path.isBlank()) {
                if (normCurrent.isEmpty()) {
                    subfolders.getOrPut("Storage Crates") { mutableListOf() }.add(track)
                } else if (normCurrent == "Storage Crates") {
                    direct.add(track)
                }
                continue
            }

            val dir = path.substringBeforeLast('/', "")
            if (normCurrent.isEmpty()) {
                val rootName = if (path.startsWith('/')) {
                    val segments = path.split('/').filter { it.isNotBlank() }
                    if (segments.size >= 2) "/${segments[0]}/${segments[1]}" else "/${segments.getOrElse(0) { "Music" }}"
                } else {
                    path.substringBefore('/')
                }
                subfolders.getOrPut(rootName) { mutableListOf() }.add(track)
            } else {
                if (dir == normCurrent) {
                    direct.add(track)
                } else if (dir.startsWith("$normCurrent/")) {
                    val rem = dir.removePrefix("$normCurrent/")
                    val nextSub = rem.substringBefore('/')
                    val subPath = "$normCurrent/$nextSub"
                    subfolders.getOrPut(subPath) { mutableListOf() }.add(track)
                }
            }
        }

        val entries = subfolders.map { (fPath, trks) ->
            FolderEntry(
                fullPath = fPath,
                displayName = fPath.substringAfterLast('/'),
                isDirectory = true,
                trackCount = trks.size,
                sampleTrack = trks.firstOrNull()
            )
        }.sortedBy { it.displayName }

        entries to direct
    }

    val matchingGenreTracks = remember(allTracks, selectedGenre) {
        if (selectedGenre == null) emptyList()
        else {
            val s = selectedGenre.lowercase()
            allTracks.filter {
                it.genreList.any { g -> g.contains(s, ignoreCase = true) } ||
                it.genre.contains(s, ignoreCase = true) ||
                (s.contains("rave") && (it.year in 1990..1999 || it.genre.contains("techno", true))) ||
                (s.contains("jungle") && (it.genre.contains("drum", true) || it.genre.contains("break", true))) ||
                (s.contains("industrial") && it.genre.contains("dark", true)) ||
                (s.contains("ambient") && it.genre.contains("chill", true)) ||
                (s.contains("hip hop") && it.genre.contains("rap", true))
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MatteBlack),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Hardware Scanner HUD & Control Matrix
            item {
                HardwareScannerHud(
                    scanProgress = scanProgress,
                    onFullScan = onRescanLibrary,
                    onIncrementalScan = onIncrementalScan,
                    onImportFolder = onTriggerFolderPicker,
                    onImportFiles = onTriggerFilePicker,
                    accentColor = accentColor,
                    formatCounts = formatCounts,
                    healthReport = libraryHealthReport,
                    onRunDiagnostics = onRunDiagnostics,
                    onAutoRepair = onAutoRepairLibrary,
                    onOpenDuplicates = { showDuplicatesDialog = true },
                    onOpenDiagnostics = { showDiagnosticsDialog = true }
                )
            }

            // 2. Search & Bulk Select Toolbar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(GunmetalDark)
                            .border(1.dp, BorderHardware, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = TextMuted, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = {
                                    Text(text = "Search tracks, artists, formats, folders...", fontFamily = ShareTechMonoFont, fontSize = 11.sp, color = TextMuted)
                                },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                },
                                textStyle = androidx.compose.ui.text.TextStyle(fontFamily = ShareTechMonoFont, fontSize = 12.sp, color = TextPrimary),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                    }

                    // Bulk selection trigger
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (isBulkMode) BurntOrange else GunmetalDark)
                            .border(1.dp, if (isBulkMode) BevelLight else BorderHardware, RoundedCornerShape(3.dp))
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                onToggleBulkMode()
                            }
                            .padding(horizontal = 8.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = if (isBulkMode) "DONE" else "BULK",
                            fontFamily = ChakraPetchFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            color = if (isBulkMode) MatteBlack else TextSecondary
                        )
                    }
                }
            }

            // 3. Hardware Category Navigation Tabs
            item {
                val tabs = listOf("SONGS", "ALBUMS", "ARTISTS", "GENRES", "FOLDERS", "CATEGORIES", "MIXTAPES")
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(tabs) { cat ->
                        val isSel = cat == selectedCategory
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (isSel) GunmetalLight else GunmetalDark)
                                .border(1.dp, if (isSel) accentColor else BorderHardware, RoundedCornerShape(3.dp))
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    onCategorySelect(cat)
                                }
                                .padding(horizontal = 10.dp, vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cat,
                                fontFamily = ChakraPetchFont,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 10.sp,
                                color = if (isSel) accentColor else TextSecondary
                            )
                        }
                    }
                }
            }

            // 4. Section Renderers
            when (selectedCategory) {
                "GENRES" -> {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "GENRE CRATES // ${genresMap.size} DETECTED",
                                    fontFamily = ChakraPetchFont,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = accentColor
                                )
                                if (selectedGenre != null) {
                                    Text(
                                        text = "[CLEAR FILTER]",
                                        fontFamily = ShareTechMonoFont,
                                        fontSize = 9.sp,
                                        color = BurntOrange,
                                        modifier = Modifier.clickable { onSelectGenre(null) }
                                    )
                                }
                            }

                            // Virtual Genre Folders
                            Text(
                                text = "GENRE-BASED VIRTUAL CRATES",
                                fontFamily = ChakraPetchFont,
                                fontSize = 9.sp,
                                color = TextMuted
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                val virtualCrates = listOf(
                                    "90s Rave & Techno",
                                    "Jungle & DNB",
                                    "Industrial & EBM",
                                    "Ambient & Chillout",
                                    "Hip Hop & Boom Bap",
                                    "Roots & Underground"
                                )
                                items(virtualCrates) { vCrate ->
                                    val isCur = selectedGenre == vCrate
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(if (isCur) BurntOrange else GunmetalDark)
                                            .border(1.dp, if (isCur) BevelLight else BorderHardware, RoundedCornerShape(3.dp))
                                            .clickable {
                                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                                onSelectGenre(if (isCur) null else vCrate)
                                            }
                                            .padding(horizontal = 8.dp, vertical = 5.dp)
                                    ) {
                                        Text(
                                            text = vCrate.uppercase(),
                                            fontFamily = ChakraPetchFont,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 8.5.sp,
                                            color = if (isCur) MatteBlack else TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (selectedGenre == null) {
                        items(genresMap.entries.toList().sortedByDescending { it.value.size }) { (genreName, tracks) ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(GunmetalDark)
                                    .border(1.dp, BorderHardware, RoundedCornerShape(4.dp))
                                    .padding(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = genreName,
                                            fontFamily = ChakraPetchFont,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "${tracks.size} TRACKS // ${tracks.firstOrNull()?.artist ?: ""}",
                                            fontFamily = ShareTechMonoFont,
                                            fontSize = 9.5.sp,
                                            color = TextSecondary
                                        )
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(GunmetalLight)
                                                .border(1.dp, BorderHardware, RoundedCornerShape(3.dp))
                                                .clickable {
                                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                                    onSelectGenre(genreName)
                                                }
                                                .padding(horizontal = 8.dp, vertical = 5.dp)
                                        ) {
                                            Text(text = "BROWSE", fontFamily = ChakraPetchFont, fontSize = 9.sp, color = TextPrimary)
                                        }

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(accentColor)
                                                .clickable {
                                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                                    onPlayGenre(genreName)
                                                }
                                                .padding(horizontal = 8.dp, vertical = 5.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = MatteBlack, modifier = Modifier.size(12.dp))
                                                Text(text = "PLAY", fontFamily = ChakraPetchFont, fontWeight = FontWeight.Bold, fontSize = 9.sp, color = MatteBlack)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "CRATE: $selectedGenre (${matchingGenreTracks.size})",
                                    fontFamily = ChakraPetchFont,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = BurntOrange
                                )
                                if (matchingGenreTracks.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(accentColor)
                                            .clickable {
                                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                                onTrackSelect(matchingGenreTracks.first())
                                            }
                                            .padding(horizontal = 8.dp, vertical = 5.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = MatteBlack, modifier = Modifier.size(12.dp))
                                            Text(text = "PLAY ALL", fontFamily = ChakraPetchFont, fontWeight = FontWeight.Bold, fontSize = 9.sp, color = MatteBlack)
                                        }
                                    }
                                }
                            }
                        }

                        items(matchingGenreTracks) { track ->
                            LibraryTrackRow(
                                index = matchingGenreTracks.indexOf(track) + 1,
                                track = track,
                                isCurrent = currentTrack?.id == track.id,
                                isPlaying = currentTrack?.id == track.id && isPlaying,
                                accentColor = accentColor,
                                onClick = { onTrackSelect(track) },
                                isSelectable = isBulkMode,
                                isSelected = selectedTrackIds.contains(track.id),
                                onToggleSelect = { onToggleTrackSelection(track.id) }
                            )
                        }
                    }
                }

                "FOLDERS" -> {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "NESTED FOLDER BROWSER",
                                    fontFamily = ChakraPetchFont,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = accentColor
                                )

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(accentColor)
                                        .clickable {
                                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                            onPlayFolder(currentFolderPath)
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = MatteBlack, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(text = "PLAY FOLDER", fontFamily = ChakraPetchFont, fontWeight = FontWeight.Bold, fontSize = 9.sp, color = MatteBlack)
                                    }
                                }
                            }

                            // Path Display Bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Color(0xFF0F1215))
                                    .border(1.dp, BorderHardware, RoundedCornerShape(2.dp))
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "DIR: ${currentFolderPath.ifBlank { "/ (ROOT CRATES)" }}",
                                    fontFamily = ShareTechMonoFont,
                                    fontSize = 10.sp,
                                    color = LcdCyan,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Up one level navigation button
                            if (currentFolderPath.isNotBlank()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(GunmetalLight)
                                        .border(1.dp, BorderHardware, RoundedCornerShape(2.dp))
                                        .clickable {
                                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                            onNavigateUpFolder()
                                        }
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.ArrowBack, contentDescription = "Up", tint = BurntOrange, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = "[.. UP ONE LEVEL]", fontFamily = ShareTechMonoFont, fontWeight = FontWeight.Bold, fontSize = 9.5.sp, color = BurntOrange)
                                    }
                                }
                            }
                        }
                    }

                    // Subfolders inside current path
                    items(folderEntries) { entry ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(3.dp))
                                .background(GunmetalDark)
                                .border(1.dp, BorderHardware, RoundedCornerShape(3.dp))
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    onOpenFolder(entry.fullPath)
                                }
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(Icons.Default.Folder, contentDescription = "Folder", tint = ElectricBlue, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = entry.displayName,
                                            fontFamily = ChakraPetchFont,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "${entry.trackCount} AUDIO TRACKS",
                                            fontFamily = ShareTechMonoFont,
                                            fontSize = 9.sp,
                                            color = TextMuted
                                        )
                                    }
                                }

                                Text(
                                    text = "OPEN >",
                                    fontFamily = ShareTechMonoFont,
                                    fontSize = 9.sp,
                                    color = accentColor
                                )
                            }
                        }
                    }

                    // Direct audio tracks inside this folder
                    if (folderTracks.isNotEmpty()) {
                        item {
                            Text(
                                text = "TRACKS IN THIS DIRECTORY (${folderTracks.size})",
                                fontFamily = ChakraPetchFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }

                        items(folderTracks) { track ->
                            LibraryTrackRow(
                                index = folderTracks.indexOf(track) + 1,
                                track = track,
                                isCurrent = currentTrack?.id == track.id,
                                isPlaying = currentTrack?.id == track.id && isPlaying,
                                accentColor = accentColor,
                                onClick = { onTrackSelect(track) },
                                isSelectable = isBulkMode,
                                isSelected = selectedTrackIds.contains(track.id),
                                onToggleSelect = { onToggleTrackSelection(track.id) }
                            )
                        }
                    }
                }

                "CATEGORIES" -> {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "CURATED AUDIO CRATES",
                                fontFamily = ChakraPetchFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = accentColor
                            )

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(CuratedCategory.values()) { cat ->
                                    val isSel = cat == selectedCuratedCategory
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(if (isSel) accentColor else GunmetalDark)
                                            .border(1.dp, if (isSel) BevelLight else BorderHardware, RoundedCornerShape(3.dp))
                                            .clickable {
                                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                                onSelectCuratedCategory(cat)
                                            }
                                            .padding(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Column {
                                            Text(
                                                text = cat.title,
                                                fontFamily = ChakraPetchFont,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp,
                                                color = if (isSel) MatteBlack else TextPrimary
                                            )
                                            Text(
                                                text = cat.badge,
                                                fontFamily = ShareTechMonoFont,
                                                fontSize = 7.5.sp,
                                                color = if (isSel) MatteBlack.copy(alpha = 0.7f) else BurntOrange
                                            )
                                        }
                                    }
                                }
                            }

                            // Selected Crate Header with Play Action
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(GunmetalDark)
                                    .border(1.dp, BorderHardware, RoundedCornerShape(3.dp))
                                    .padding(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${selectedCuratedCategory.title} // ${categoryTracks.size} TRACKS",
                                            fontFamily = ChakraPetchFont,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = selectedCuratedCategory.description,
                                            fontFamily = ShareTechMonoFont,
                                            fontSize = 9.sp,
                                            color = TextSecondary
                                        )
                                    }

                                    if (categoryTracks.isNotEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(BurntOrange)
                                                .clickable {
                                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                                onPlayCuratedCategory(selectedCuratedCategory)
                                            }
                                            .padding(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = MatteBlack, modifier = Modifier.size(12.dp))
                                            Text(text = "PLAY CRATE", fontFamily = ChakraPetchFont, fontWeight = FontWeight.Bold, fontSize = 9.sp, color = MatteBlack)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (categoryTracks.isEmpty()) {
                    item {
                        Text(
                            text = "No tracks currently matched in ${selectedCuratedCategory.title}.",
                            fontFamily = ShareTechMonoFont,
                            fontSize = 10.sp,
                            color = TextMuted,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }
                } else {
                    items(categoryTracks) { track ->
                        LibraryTrackRow(
                            index = categoryTracks.indexOf(track) + 1,
                            track = track,
                            isCurrent = currentTrack?.id == track.id,
                            isPlaying = currentTrack?.id == track.id && isPlaying,
                            accentColor = accentColor,
                            onClick = { onTrackSelect(track) },
                            isSelectable = isBulkMode,
                            isSelected = selectedTrackIds.contains(track.id),
                            onToggleSelect = { onToggleTrackSelection(track.id) }
                        )
                    }
                }
            }

            "ALBUMS" -> {
                item {
                    Text(
                        text = "ALBUMS ARCHIVE // ${albums.size} RELEASES",
                        fontFamily = ChakraPetchFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = accentColor
                    )
                }

                items(albums.entries.toList()) { (albumName, albumTracks) ->
                    val isExpanded = expandedAlbumName == albumName
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(GunmetalDark)
                            .border(1.dp, if (isExpanded) accentColor else BorderHardware, RoundedCornerShape(4.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            expandedAlbumName = if (isExpanded) null else albumName
                                        }
                                ) {
                                    Text(
                                        text = albumName,
                                        fontFamily = ChakraPetchFont,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "${albumTracks.firstOrNull()?.artist ?: "Unknown"} // ${albumTracks.size} TRACKS",
                                        fontFamily = ShareTechMonoFont,
                                        fontSize = 10.sp,
                                        color = BurntOrange
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(GunmetalLight)
                                            .border(1.dp, BorderHardware, RoundedCornerShape(3.dp))
                                            .clickable {
                                                expandedAlbumName = if (isExpanded) null else albumName
                                            }
                                            .padding(horizontal = 6.dp, vertical = 5.dp)
                                    ) {
                                        Text(text = if (isExpanded) "CLOSE" else "TRACKS", fontFamily = ChakraPetchFont, fontSize = 8.5.sp, color = TextPrimary)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(accentColor)
                                            .clickable {
                                                if (albumTracks.isNotEmpty()) {
                                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                                    onTrackSelect(albumTracks.first())
                                                }
                                            }
                                            .padding(horizontal = 8.dp, vertical = 5.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = MatteBlack, modifier = Modifier.size(12.dp))
                                            Text(text = "PLAY", fontFamily = ChakraPetchFont, fontWeight = FontWeight.Bold, fontSize = 9.sp, color = MatteBlack)
                                        }
                                    }
                                }
                            }

                            if (isExpanded) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    albumTracks.forEachIndexed { i, track ->
                                        LibraryTrackRow(
                                            index = i + 1,
                                            track = track,
                                            isCurrent = currentTrack?.id == track.id,
                                            isPlaying = currentTrack?.id == track.id && isPlaying,
                                            accentColor = accentColor,
                                            onClick = { onTrackSelect(track) },
                                            isSelectable = isBulkMode,
                                            isSelected = selectedTrackIds.contains(track.id),
                                            onToggleSelect = { onToggleTrackSelection(track.id) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            "ARTISTS" -> {
                item {
                    Text(
                        text = "ARTISTS DIRECTORY // ${artists.size} ARTISTS",
                        fontFamily = ChakraPetchFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = accentColor
                    )
                }

                items(artists.entries.toList()) { (artistName, artistTracks) ->
                    val isExpanded = expandedArtistName == artistName
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(GunmetalDark)
                            .border(1.dp, if (isExpanded) accentColor else BorderHardware, RoundedCornerShape(4.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            expandedArtistName = if (isExpanded) null else artistName
                                        }
                                ) {
                                    Text(
                                        text = artistName,
                                        fontFamily = ChakraPetchFont,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "${artistTracks.size} TRACKS IN ARCHIVE",
                                        fontFamily = ShareTechMonoFont,
                                        fontSize = 10.sp,
                                        color = TextSecondary
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(GunmetalLight)
                                            .border(1.dp, BorderHardware, RoundedCornerShape(3.dp))
                                            .clickable {
                                                expandedArtistName = if (isExpanded) null else artistName
                                            }
                                            .padding(horizontal = 6.dp, vertical = 5.dp)
                                    ) {
                                        Text(text = if (isExpanded) "CLOSE" else "TRACKS", fontFamily = ChakraPetchFont, fontSize = 8.5.sp, color = TextPrimary)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(accentColor)
                                            .clickable {
                                                if (artistTracks.isNotEmpty()) {
                                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                                    onTrackSelect(artistTracks.first())
                                                }
                                            }
                                            .padding(horizontal = 8.dp, vertical = 5.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = MatteBlack, modifier = Modifier.size(12.dp))
                                            Text(text = "PLAY", fontFamily = ChakraPetchFont, fontWeight = FontWeight.Bold, fontSize = 9.sp, color = MatteBlack)
                                        }
                                    }
                                }
                            }

                            if (isExpanded) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    artistTracks.forEachIndexed { i, track ->
                                        LibraryTrackRow(
                                            index = i + 1,
                                            track = track,
                                            isCurrent = currentTrack?.id == track.id,
                                            isPlaying = currentTrack?.id == track.id && isPlaying,
                                            accentColor = accentColor,
                                            onClick = { onTrackSelect(track) },
                                            isSelectable = isBulkMode,
                                            isSelected = selectedTrackIds.contains(track.id),
                                            onToggleSelect = { onToggleTrackSelection(track.id) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            "MIXTAPES" -> {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "RECORDED TAPES // C-60 & C-90",
                            fontFamily = ChakraPetchFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = accentColor
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(BurntOrange)
                                .border(1.dp, MatteBlack, RoundedCornerShape(3.dp))
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    showCreatorModal = true
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = "+ REC TAPE", fontFamily = ChakraPetchFont, fontWeight = FontWeight.Bold, fontSize = 9.sp, color = MatteBlack)
                        }
                    }
                }

                items(allMixtapes) { tape ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(GunmetalDark)
                            .border(1.dp, BorderHardware, RoundedCornerShape(4.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = tape.title,
                                        fontFamily = ChakraPetchFont,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "${tape.labelText} // C-${tape.durationMinutes}",
                                        fontFamily = ShareTechMonoFont,
                                        fontSize = 10.sp,
                                        color = BurntOrange
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(accentColor)
                                        .clickable {
                                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                            onLoadMixtape(tape)
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = MatteBlack, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "LOAD DECK", fontFamily = ChakraPetchFont, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = MatteBlack)
                                    }
                                }
                            }

                            if (tape.notes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = tape.notes,
                                    fontFamily = ShareTechMonoFont,
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }
            }

            else -> { // "SONGS"
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SMART CRATE FILTERS",
                                fontFamily = ChakraPetchFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = BurntOrange
                            )

                            // View mode switcher: LIST, GRID, WALL
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                LibraryViewMode.values().forEach { mode ->
                                    val isSel = mode == libraryViewMode
                                    Box(
                                        modifier = Modifier
                                            .background(if (isSel) accentColor else GunmetalDark)
                                            .border(1.dp, if (isSel) BevelLight else BorderHardware, RoundedCornerShape(2.dp))
                                            .clickable {
                                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                                onSelectViewMode(mode)
                                            }
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = mode.name,
                                            fontFamily = ShareTechMonoFont,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSel) MatteBlack else TextSecondary
                                        )
                                    }
                                }
                            }
                        }

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            item {
                                val isAll = selectedSmartFolder == null
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(if (isAll) AcidGreen else GunmetalDark)
                                        .border(1.dp, if (isAll) BevelLight else BorderHardware, RoundedCornerShape(2.dp))
                                        .clickable {
                                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                            onSelectSmartFolder(null)
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "ALL AUDIO (${allTracks.size})",
                                        fontFamily = ChakraPetchFont,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp,
                                        color = if (isAll) MatteBlack else TextPrimary
                                    )
                                }
                            }

                            items(SmartFolderType.values()) { folder ->
                                val isSel = folder == selectedSmartFolder
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(if (isSel) ElectricBlue else GunmetalDark)
                                        .border(1.dp, if (isSel) LcdCyan else BorderHardware, RoundedCornerShape(2.dp))
                                        .clickable {
                                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                            onSelectSmartFolder(folder)
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = folder.title,
                                        fontFamily = ChakraPetchFont,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp,
                                        color = if (isSel) MatteBlack else TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                if (filteredTracks.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(GunmetalDark)
                                .border(1.dp, BorderHardware, RoundedCornerShape(4.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (searchQuery.isBlank()) "NO TRACKS IN THIS CRATE" else "NO TRACKS MATCHING \"$searchQuery\"",
                                    fontFamily = ChakraPetchFont,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Run auto rescan, incremental scan, or import music folders directly.",
                                    fontFamily = ShareTechMonoFont,
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = onRescanLibrary,
                                        colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                                    ) {
                                        Text(text = "SCAN STORAGE", fontFamily = ChakraPetchFont, color = MatteBlack, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                    }
                                    Button(
                                        onClick = onTriggerFolderPicker,
                                        colors = ButtonDefaults.buttonColors(containerColor = BurntOrange)
                                    ) {
                                        Text(text = "+ FOLDER", fontFamily = ChakraPetchFont, color = MatteBlack, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    items(filteredTracks) { track ->
                        LibraryTrackRow(
                            index = allTracks.indexOf(track) + 1,
                            track = track,
                            isCurrent = currentTrack?.id == track.id,
                            isPlaying = currentTrack?.id == track.id && isPlaying,
                            accentColor = accentColor,
                            onClick = { onTrackSelect(track) },
                            isSelectable = isBulkMode,
                            isSelected = selectedTrackIds.contains(track.id),
                            onToggleSelect = { onToggleTrackSelection(track.id) }
                        )
                    }
                }
            }
        }
    }

        // Floating Bulk Command Console when in bulk mode
        if (isBulkMode) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(GunmetalDark)
                    .border(1.5.dp, BurntOrange, RoundedCornerShape(4.dp))
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SELECTED: ${selectedTrackIds.size} / ${allTracks.size}",
                            fontFamily = ShareTechMonoFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = BurntOrange
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "[ALL]",
                                fontFamily = ShareTechMonoFont,
                                fontSize = 9.sp,
                                color = AcidGreen,
                                modifier = Modifier.clickable { onSelectAllTracks(allTracks) }
                            )
                            Text(
                                text = "[NONE]",
                                fontFamily = ShareTechMonoFont,
                                fontSize = 9.sp,
                                color = TextMuted,
                                modifier = Modifier.clickable { onClearTrackSelection() }
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (selectedTrackIds.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(ElectricBlue)
                                    .clickable { showBulkTagDialog = true }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(text = "BATCH TAG", fontFamily = ChakraPetchFont, fontWeight = FontWeight.Bold, fontSize = 9.sp, color = MatteBlack)
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(DangerRed)
                                    .clickable { showConfirmDeleteDialog = true }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(text = "DELETE", fontFamily = ChakraPetchFont, fontWeight = FontWeight.Bold, fontSize = 9.sp, color = Color.White)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(GunmetalLight)
                                .clickable { onToggleBulkMode() }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(text = "EXIT", fontFamily = ChakraPetchFont, fontSize = 9.sp, color = TextPrimary)
                        }
                    }
                }
            }
        }
    }

    // Diagnostics Dialog
    if (showDiagnosticsDialog) {
        AlertDialog(
            onDismissRequest = { showDiagnosticsDialog = false },
            title = {
                Text(
                    text = "VAULT HEALTH DIAGNOSTICS",
                    fontFamily = ChakraPetchFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = accentColor
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "HEALTH SCORE: ${libraryHealthReport.healthScore}%",
                        fontFamily = ShareTechMonoFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (libraryHealthReport.healthScore >= 80) AcidGreen else BurntOrange
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF0F1215))
                            .padding(8.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("TOTAL TRACKS: ${libraryHealthReport.totalTracks}", fontFamily = ShareTechMonoFont, fontSize = 10.sp, color = TextPrimary)
                            Text("DUPLICATES: ${libraryHealthReport.duplicateGroups.sumOf { it.duplicateTracks.size }} COPIES IN ${libraryHealthReport.duplicateGroups.size} GROUPS", fontFamily = ShareTechMonoFont, fontSize = 10.sp, color = BurntOrange)
                            Text("GHOST / MISSING FILES: ${libraryHealthReport.missingFiles.size}", fontFamily = ShareTechMonoFont, fontSize = 10.sp, color = DangerRed)
                            Text("STUDIO LOSSLESS: ${libraryHealthReport.losslessCount} TRACKS", fontFamily = ShareTechMonoFont, fontSize = 10.sp, color = ElectricBlue)
                            Text("MISSING ARTWORK: ${libraryHealthReport.missingArtworkCount}", fontFamily = ShareTechMonoFont, fontSize = 10.sp, color = TextMuted)
                            Text("UNTAGGED METADATA: ${libraryHealthReport.missingMetadataCount}", fontFamily = ShareTechMonoFont, fontSize = 10.sp, color = TextMuted)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onAutoRepairLibrary()
                        showDiagnosticsDialog = false
                    }
                ) {
                    Text("REPAIR ARCHIVE", fontFamily = ChakraPetchFont, fontWeight = FontWeight.Bold, color = BurntOrange)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiagnosticsDialog = false }) {
                    Text("CLOSE", color = TextMuted)
                }
            },
            containerColor = GunmetalDark
        )
    }

    // Duplicates Comparison Dialog
    if (showDuplicatesDialog) {
        AlertDialog(
            onDismissRequest = { showDuplicatesDialog = false },
            title = {
                Text(
                    text = "DUPLICATE SONG DETECTOR",
                    fontFamily = ChakraPetchFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = BurntOrange
                )
            },
            text = {
                if (libraryHealthReport.duplicateGroups.isEmpty()) {
                    Text(
                        text = "ZERO DUPLICATES FOUND IN CRATE.\nAll tracks in the archive are unique masters.",
                        fontFamily = ShareTechMonoFont,
                        fontSize = 11.sp,
                        color = AcidGreen
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.height(280.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(libraryHealthReport.duplicateGroups) { group ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color(0xFF0F1215))
                                    .border(1.dp, BorderHardware, RoundedCornerShape(3.dp))
                                    .padding(8.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "${group.title} // ${group.artist}",
                                        fontFamily = ChakraPetchFont,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = group.reason,
                                        fontFamily = ShareTechMonoFont,
                                        fontSize = 9.sp,
                                        color = BurntOrange
                                    )

                                    // Master Track
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "KEEP: [${group.recommendedKeep.audioFormat}] ${group.recommendedKeep.durationMs / 1000}s",
                                            fontFamily = ShareTechMonoFont,
                                            fontSize = 9.5.sp,
                                            color = AcidGreen
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(GunmetalLight)
                                                .clickable { onTrackSelect(group.recommendedKeep) }
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text("PLAY", fontFamily = ChakraPetchFont, fontSize = 8.sp, color = TextPrimary)
                                        }
                                    }

                                    // Duplicates list
                                    group.duplicateTracks.forEach { dup ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "DUP: [${dup.audioFormat}] ${dup.durationMs / 1000}s",
                                                fontFamily = ShareTechMonoFont,
                                                fontSize = 9.sp,
                                                color = DangerRed
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(2.dp))
                                                    .background(GunmetalLight)
                                                    .clickable { onTrackSelect(dup) }
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                            ) {
                                                Text("PLAY", fontFamily = ChakraPetchFont, fontSize = 8.sp, color = TextPrimary)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (libraryHealthReport.duplicateGroups.isNotEmpty()) {
                    TextButton(
                        onClick = {
                            onDeduplicateAll()
                            showDuplicatesDialog = false
                        }
                    ) {
                        Text("DEDUPLICATE ALL", fontFamily = ChakraPetchFont, fontWeight = FontWeight.Bold, color = BurntOrange)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showDuplicatesDialog = false }) {
                    Text("CLOSE", color = TextMuted)
                }
            },
            containerColor = GunmetalDark
        )
    }

    // Bulk Tag Dialog
    if (showBulkTagDialog) {
        var bulkGenre by remember { mutableStateOf("") }
        var bulkYear by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showBulkTagDialog = false },
            title = {
                Text(
                    text = "BATCH TAG ${selectedTrackIds.size} TRACKS",
                    fontFamily = ChakraPetchFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = ElectricBlue
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = bulkGenre,
                        onValueChange = { bulkGenre = it },
                        label = { Text("Genre (e.g. Acid House / Techno)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = bulkYear,
                        onValueChange = { bulkYear = it },
                        label = { Text("Release Year (e.g. 1996)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val yearInt = bulkYear.toIntOrNull()
                        val genreStr = bulkGenre.ifBlank { null }
                        onBulkUpdateMetadata(selectedTrackIds.toList(), genreStr, yearInt)
                        showBulkTagDialog = false
                    }
                ) {
                    Text("APPLY TO ALL", fontFamily = ChakraPetchFont, fontWeight = FontWeight.Bold, color = ElectricBlue)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBulkTagDialog = false }) {
                    Text("CANCEL", color = TextMuted)
                }
            },
            containerColor = GunmetalDark
        )
    }

    // Confirm Delete Dialog
    if (showConfirmDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDeleteDialog = false },
            title = {
                Text(
                    text = "PURGE ${selectedTrackIds.size} TRACK(S)?",
                    fontFamily = ChakraPetchFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = DangerRed
                )
            },
            text = {
                Text(
                    text = "This will remove the selected tracks from the REPLAY 99 library crate.",
                    fontFamily = ShareTechMonoFont,
                    fontSize = 11.sp,
                    color = TextPrimary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteSelectedTracks()
                        showConfirmDeleteDialog = false
                    }
                ) {
                    Text("CONFIRM PURGE", fontFamily = ChakraPetchFont, fontWeight = FontWeight.Bold, color = DangerRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDeleteDialog = false }) {
                    Text("CANCEL", color = TextMuted)
                }
            },
            containerColor = GunmetalDark
        )
    }

    // Mixtape Creator Dialog
    if (showCreatorModal) {
        var tapeTitle by remember { mutableStateOf("") }
        var tapeLabel by remember { mutableStateOf("C-60 / CHROME HIGH BIAS") }
        var tapeNotes by remember { mutableStateOf("") }
        var tapeDuration by remember { mutableStateOf(60) }
        var selectedColorHex by remember { mutableStateOf("#16191C") }

        AlertDialog(
            onDismissRequest = { showCreatorModal = false },
            title = {
                Text(
                    text = "RECORD NEW MIXTAPE",
                    fontFamily = ChakraPetchFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = BurntOrange
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = tapeTitle,
                        onValueChange = { tapeTitle = it },
                        label = { Text("Tape Title (e.g. 1995 Underground Cassette)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = tapeLabel,
                        onValueChange = { tapeLabel = it },
                        label = { Text("Cassette Sticker Label") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = tapeNotes,
                        onValueChange = { tapeNotes = it },
                        label = { Text("J-Card Inlay Notes & Credits") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Tape Type:", fontFamily = ShareTechMonoFont, fontSize = 11.sp, color = TextSecondary)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(60, 90).forEach { dur ->
                                val isSel = tapeDuration == dur
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(if (isSel) BurntOrange else GunmetalDark)
                                        .clickable { tapeDuration = dur }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "C-$dur",
                                        fontFamily = ChakraPetchFont,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = if (isSel) MatteBlack else TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (tapeTitle.isNotBlank()) {
                            val sideA = allTracks.take(10).map { it.id }
                            val sideB = allTracks.drop(10).take(10).map { it.id }
                            onCreateMixtape(
                                tapeTitle,
                                tapeLabel,
                                tapeNotes,
                                selectedColorHex,
                                "#00FF66",
                                true,
                                sideA,
                                sideB,
                                tapeDuration
                            )
                            showCreatorModal = false
                        }
                    }
                ) {
                    Text("RECORD MASTER TAPE", fontFamily = ChakraPetchFont, fontWeight = FontWeight.Bold, color = BurntOrange)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreatorModal = false }) {
                    Text("CANCEL", color = TextMuted)
                }
            },
            containerColor = GunmetalDark
        )
    }
}

@Composable
fun HardwareScannerHud(
    scanProgress: ScanProgress,
    onFullScan: () -> Unit,
    onIncrementalScan: () -> Unit,
    onImportFolder: () -> Unit,
    onImportFiles: () -> Unit,
    accentColor: Color,
    formatCounts: Map<String, Int>,
    healthReport: LibraryHealthReport = LibraryHealthReport(),
    onRunDiagnostics: () -> Unit = {},
    onAutoRepair: () -> Unit = {},
    onOpenDuplicates: () -> Unit = {},
    onOpenDiagnostics: () -> Unit = {}
) {
    val view = LocalView.current
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = AnimRepeatMode.Reverse
        ),
        label = "scanner_led"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(GunmetalDark)
            .border(1.5.dp, BorderHardware, RoundedCornerShape(4.dp))
            .padding(10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Scanner Title & Status LED
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (scanProgress.isScanning) AcidGreen.copy(alpha = pulseAlpha)
                                else AcidGreen
                            )
                    )
                    Text(
                        text = "SCANNER ENGINE // ARCHIVE DIGGER v19.99",
                        fontFamily = ChakraPetchFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = accentColor,
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = if (scanProgress.isScanning) "BUSY [SCAN]" else "ONLINE [IDLE]",
                    fontFamily = ShareTechMonoFont,
                    fontSize = 9.sp,
                    color = if (scanProgress.isScanning) BurntOrange else TextSecondary
                )
            }

            // LCD Status readout
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF0A1210))
                    .border(1.dp, BorderHardware, RoundedCornerShape(2.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = scanProgress.statusMessage.uppercase(),
                            fontFamily = ShareTechMonoFont,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (scanProgress.isScanning) LcdCyan else AcidGreen,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        if (scanProgress.isScanning) {
                            Text(
                                text = "${(scanProgress.progressPercent * 100).toInt()}%",
                                fontFamily = ShareTechMonoFont,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = LcdCyan
                            )
                        }
                    }

                    // Progress Bar
                    if (scanProgress.isScanning) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(1.dp))
                                .background(Color(0xFF14241E))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(scanProgress.progressPercent.coerceIn(0.05f, 1.0f))
                                    .height(4.dp)
                                    .background(LcdCyan)
                            )
                        }
                    }
                }
            }

            // Health & Duplicate Status Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (healthReport.healthScore >= 80) AcidGreen else BurntOrange)
                    )
                    Text(
                        text = "HEALTH: ${healthReport.healthScore}%",
                        fontFamily = ShareTechMonoFont,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (healthReport.healthScore >= 80) AcidGreen else BurntOrange
                    )
                    if (healthReport.duplicateGroups.isNotEmpty()) {
                        Text(
                            text = "// ${healthReport.duplicateGroups.sumOf { it.duplicateTracks.size }} DUPS",
                            fontFamily = ShareTechMonoFont,
                            fontSize = 8.sp,
                            color = BurntOrange,
                            modifier = Modifier.clickable { onOpenDuplicates() }
                        )
                    }
                    if (healthReport.missingFiles.isNotEmpty()) {
                        Text(
                            text = "// ${healthReport.missingFiles.size} GHOSTS",
                            fontFamily = ShareTechMonoFont,
                            fontSize = 8.sp,
                            color = DangerRed
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (healthReport.duplicateGroups.isNotEmpty() || healthReport.missingFiles.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(2.dp))
                                .background(BurntOrange)
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    onAutoRepair()
                                }
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text("REPAIR", fontFamily = ChakraPetchFont, fontSize = 7.5.sp, fontWeight = FontWeight.Bold, color = MatteBlack)
                        }
                    }

                    if (healthReport.duplicateGroups.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(2.dp))
                                .background(GunmetalLight)
                                .border(0.5.dp, BorderHardware, RoundedCornerShape(2.dp))
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    onOpenDuplicates()
                                }
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text("COMPARE", fontFamily = ChakraPetchFont, fontSize = 7.5.sp, color = TextPrimary)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(2.dp))
                            .background(GunmetalLight)
                            .border(0.5.dp, BorderHardware, RoundedCornerShape(2.dp))
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                onOpenDiagnostics()
                            }
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text("REPORT", fontFamily = ChakraPetchFont, fontSize = 7.5.sp, color = TextSecondary)
                    }
                }
            }

            // Formats Telemetry Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "FORMATS:",
                    fontFamily = ShareTechMonoFont,
                    fontSize = 8.sp,
                    color = TextMuted
                )
                listOf("MP3", "FLAC", "WAV", "OGG", "OPUS", "AAC", "M4A").forEach { fmt ->
                    val count = formatCounts[fmt] ?: 0
                    val isPresent = count > 0 || scanProgress.formatsDetected.contains(fmt)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (isPresent) GunmetalLight else MatteBlack)
                            .border(
                                0.5.dp,
                                if (fmt == "FLAC" || fmt == "WAV") BurntOrange else BorderHardware,
                                RoundedCornerShape(2.dp)
                            )
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = if (count > 0) "$fmt:$count" else fmt,
                            fontFamily = ShareTechMonoFont,
                            fontSize = 7.5.sp,
                            color = if (isPresent) (if (fmt == "FLAC" || fmt == "WAV") BurntOrange else TextPrimary) else TextMuted
                        )
                    }
                }
            }

            // Hardware Action Buttons Array
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Full Rescan
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(2.dp))
                        .background(GunmetalLight)
                        .border(1.dp, BorderHardware, RoundedCornerShape(2.dp))
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            onFullScan()
                        }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "AUTO RESCAN",
                        fontFamily = ChakraPetchFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 8.5.sp,
                        color = TextPrimary
                    )
                }

                // Incremental Scan
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(2.dp))
                        .background(GunmetalLight)
                        .border(1.dp, BorderHardware, RoundedCornerShape(2.dp))
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            onIncrementalScan()
                        }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "QUICK REFRESH",
                        fontFamily = ChakraPetchFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 8.5.sp,
                        color = ElectricBlue
                    )
                }

                // Import Folder
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(2.dp))
                        .background(GunmetalLight)
                        .border(1.dp, accentColor, RoundedCornerShape(2.dp))
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            onImportFolder()
                        }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+ FOLDER",
                        fontFamily = ChakraPetchFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 8.5.sp,
                        color = accentColor
                    )
                }

                // Import Files
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(2.dp))
                        .background(BurntOrange)
                        .border(1.dp, MatteBlack, RoundedCornerShape(2.dp))
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            onImportFiles()
                        }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+ FILES",
                        fontFamily = ChakraPetchFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 8.5.sp,
                        color = MatteBlack
                    )
                }
            }
        }
    }
}
