package com.example.ui.screens

import android.view.HapticFeedbackConstants
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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
import com.example.model.Track
import com.example.ui.screens.LibraryTrackRow
import com.example.ui.theme.AcidGreen
import com.example.ui.theme.BorderHardware
import com.example.ui.theme.BurntOrange
import com.example.ui.theme.ChakraPetchFont
import com.example.ui.theme.DangerRed
import com.example.ui.theme.GunmetalDark
import com.example.ui.theme.GunmetalLight
import com.example.ui.theme.GunmetalMedium
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
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("MIXTAPES") } // "MIXTAPES", "TRACKS", "ALBUMS", "ARTISTS"
    var showCreatorModal by remember { mutableStateOf(false) }

    // Filter tracks
    val filteredTracks = remember(searchQuery, allTracks) {
        if (searchQuery.isBlank()) allTracks
        else allTracks.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.artist.contains(searchQuery, ignoreCase = true) ||
                    it.album.contains(searchQuery, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MatteBlack),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Vault Header & Creator Action
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CRATE VAULT // ARCHIVES",
                    fontFamily = ChakraPetchFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = accentColor,
                    letterSpacing = 1.sp
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Rescan button
                    Box(
                        modifier = Modifier
                            .background(GunmetalDark)
                            .border(1.dp, BorderHardware, RoundedCornerShape(2.dp))
                            .clickable { onRescanLibrary() }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Text(text = "SCAN MEDIA", fontFamily = ChakraPetchFont, fontSize = 9.sp, color = TextSecondary)
                    }

                    // Record new mixtape button
                    Box(
                        modifier = Modifier
                            .background(BurntOrange)
                            .border(1.dp, MatteBlack, RoundedCornerShape(2.dp))
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                showCreatorModal = true
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "+ REC MIXTAPE",
                            fontFamily = ChakraPetchFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = MatteBlack
                        )
                    }
                }
            }
        }

        // Search Input
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GunmetalDark)
                    .border(1.dp, BorderHardware, RoundedCornerShape(4.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = TextMuted, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(text = "Search tracks, artists, underground crates...", fontFamily = ShareTechMonoFont, fontSize = 11.sp, color = TextMuted)
                        },
                        textStyle = androidx.compose.ui.text.TextStyle(fontFamily = ShareTechMonoFont, fontSize = 12.sp, color = TextPrimary),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        }

        // Category Filter Tabs
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("MIXTAPES", "TRACKS", "ALBUMS", "ARTISTS").forEach { cat ->
                    val isSel = cat == selectedCategory
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (isSel) GunmetalLight else GunmetalDark)
                            .border(1.dp, if (isSel) accentColor else BorderHardware, RoundedCornerShape(3.dp))
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                selectedCategory = cat
                            }
                            .padding(vertical = 6.dp),
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

        // Section Content
        if (selectedCategory == "MIXTAPES") {
            item {
                Text(
                    text = "RECORDED TAPES // C-60 & C-90",
                    fontFamily = ChakraPetchFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = TextSecondary
                )
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

                            // Load Tape Button
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
        } else {
            // Track / Album / Artist List
            items(filteredTracks) { track ->
                LibraryTrackRow(
                    index = allTracks.indexOf(track) + 1,
                    track = track,
                    isCurrent = currentTrack?.id == track.id,
                    isPlaying = currentTrack?.id == track.id && isPlaying,
                    accentColor = accentColor,
                    onClick = { onTrackSelect(track) }
                )
            }
        }
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
                        label = { Text("Tape Title (e.g. Warehouse 1999)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = tapeLabel,
                        onValueChange = { tapeLabel = it },
                        label = { Text("Spine Label Text") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = tapeNotes,
                        onValueChange = { tapeNotes = it },
                        label = { Text("Liner Notes / Dedication") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "DURATION", fontFamily = ChakraPetchFont, fontSize = 11.sp, color = TextSecondary)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(60, 90).forEach { dur ->
                                Box(
                                    modifier = Modifier
                                        .background(if (tapeDuration == dur) BurntOrange else GunmetalLight)
                                        .clickable { tapeDuration = dur }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(text = "C-$dur", fontFamily = ChakraPetchFont, color = if (tapeDuration == dur) MatteBlack else TextPrimary)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trackIds = allTracks.map { it.id }
                        val mid = (trackIds.size / 2).coerceAtLeast(1)
                        val sideA = trackIds.take(mid)
                        val sideB = trackIds.drop(mid)

                        onCreateMixtape(
                            tapeTitle.ifBlank { "UNTITLED MIXTAPE" },
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
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BurntOrange)
                ) {
                    Text(text = "PRESS TAPE", color = MatteBlack, fontFamily = ChakraPetchFont, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreatorModal = false }) {
                    Text(text = "CANCEL", color = TextSecondary, fontFamily = ChakraPetchFont)
                }
            },
            containerColor = GunmetalDark
        )
    }
}
