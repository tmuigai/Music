package com.example.ui.screens

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eject
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import com.example.data.MiniDiscEntity
import com.example.model.MiniDiscCartridge
import com.example.model.PlaybackState
import com.example.model.Track
import com.example.ui.theme.AcidGreen
import com.example.ui.theme.BorderHardware
import com.example.ui.theme.BurntOrange
import com.example.ui.theme.ChakraPetchFont
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
fun MiniDiscPlayerScreen(
    currentMiniDisc: MiniDiscCartridge?,
    allMiniDiscs: List<MiniDiscEntity>,
    allTracks: List<Track>,
    playbackState: PlaybackState,
    accentColor: Color,
    onTrackSelect: (Track) -> Unit,
    onLoadMiniDisc: (MiniDiscEntity) -> Unit,
    onEjectMiniDisc: () -> Unit,
    onSaveMiniDisc: (String, String, String, String, List<Long>) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    var showCreateDialog by remember { mutableStateOf(false) }
    val currentTrack = playbackState.currentTrack

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MatteBlack),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Machine Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "MINIDISC SYSTEM // MZ-99",
                        fontFamily = ChakraPetchFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = ElectricBlue,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "MAGNETO-OPTICAL ATRAC RECORDER",
                        fontFamily = ShareTechMonoFont,
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (currentMiniDisc != null) {
                        Box(
                            modifier = Modifier
                                .background(GunmetalDark)
                                .border(1.dp, BurntOrange, RoundedCornerShape(2.dp))
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    onEjectMiniDisc()
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = "EJECT MD", fontFamily = ChakraPetchFont, fontSize = 9.sp, color = BurntOrange)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .background(ElectricBlue)
                            .border(1.dp, MatteBlack, RoundedCornerShape(2.dp))
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                showCreateDialog = true
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = "+ RECORD MD", fontFamily = ChakraPetchFont, fontWeight = FontWeight.Bold, fontSize = 9.sp, color = MatteBlack)
                    }
                }
            }
        }

        // MiniDisc Unit Front Faceplate with 2-Line High-Contrast LCD
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF14191F))
                    .border(2.dp, BorderHardware, RoundedCornerShape(6.dp))
                    .padding(14.dp)
            ) {
                Column {
                    // Dot Matrix Blue-Lit LCD Screen
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0A2230))
                            .border(1.5.dp, ElectricBlue.copy(alpha = 0.5f), RoundedCornerShape(3.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (currentMiniDisc != null) "DISC: ${currentMiniDisc.discTitle}" else "NO DISC INSERTED",
                                    fontFamily = ShareTechMonoFont,
                                    fontSize = 11.sp,
                                    color = LcdCyan,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "ATRAC ${currentMiniDisc?.atracMode ?: "SP"}",
                                    fontFamily = ShareTechMonoFont,
                                    fontSize = 10.sp,
                                    color = if (playbackState.isPlaying) AcidGreen else TextMuted
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = currentTrack?.title ?: "STANDBY // PRESS PLAY",
                                fontFamily = ChakraPetchFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                val min = (playbackState.positionMs / 60000)
                                val sec = (playbackState.positionMs % 60000) / 1000
                                Text(
                                    text = String.format("TRK %02d // %02d:%02d", playbackState.queueIndex + 1, min, sec),
                                    fontFamily = ShareTechMonoFont,
                                    fontSize = 11.sp,
                                    color = LcdCyan
                                )
                                Text(
                                    text = if (playbackState.isPlaying) "► OPTICAL READ" else "■ STANDBY",
                                    fontFamily = ShareTechMonoFont,
                                    fontSize = 10.sp,
                                    color = if (playbackState.isPlaying) AcidGreen else BurntOrange
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // MiniDisc Cartridge Physical Representation
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (currentMiniDisc != null) Color(android.graphics.Color.parseColor(currentMiniDisc.colorHex))
                                else GunmetalDark
                            )
                            .border(2.dp, BorderHardware, RoundedCornerShape(6.dp))
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentMiniDisc?.labelText ?: "INSERT 74/80-MIN CARTRIDGE",
                                    fontFamily = ShareTechMonoFont,
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )
                                Text(
                                    text = currentMiniDisc?.discTitle ?: "EMPTY DRIVE SLOT",
                                    fontFamily = ChakraPetchFont,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "SHOCK RESISTANT MEMORY // 10s BUFFER",
                                    fontFamily = ShareTechMonoFont,
                                    fontSize = 8.sp,
                                    color = ElectricBlue
                                )
                            }

                            // Metallic Sliding Shutter
                            Box(
                                modifier = Modifier
                                    .width(60.dp)
                                    .height(80.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(GunmetalMedium)
                                    .border(1.dp, BorderHardware, RoundedCornerShape(3.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(MatteBlack)
                                            .border(1.dp, if (playbackState.isPlaying) ElectricBlue else TextMuted, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "SHUTTER",
                                        fontFamily = ShareTechMonoFont,
                                        fontSize = 7.sp,
                                        color = TextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // MiniDisc Track Listing
        item {
            Text(
                text = "MINIDISC TRACK LIST // TOC",
                fontFamily = ChakraPetchFont,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        val mdTracks = currentMiniDisc?.tracks ?: allTracks.take(8)

        itemsIndexed(mdTracks) { index, track ->
            val isCurrent = currentTrack?.id == track.id
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (isCurrent) GunmetalMedium else GunmetalDark)
                    .border(1.dp, if (isCurrent) ElectricBlue else BorderHardware, RoundedCornerShape(2.dp))
                    .clickable {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        onTrackSelect(track)
                    }
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = String.format("%02d", index + 1),
                        fontFamily = ShareTechMonoFont,
                        fontSize = 11.sp,
                        color = if (isCurrent) ElectricBlue else TextMuted,
                        modifier = Modifier.width(28.dp)
                    )
                    Column {
                        Text(
                            text = track.title,
                            fontFamily = ChakraPetchFont,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = track.artist,
                            fontFamily = ShareTechMonoFont,
                            fontSize = 9.sp,
                            color = TextSecondary
                        )
                    }
                }

                Text(
                    text = String.format("%02d:%02d", (track.durationMs / 60000), (track.durationMs % 60000) / 1000),
                    fontFamily = ShareTechMonoFont,
                    fontSize = 10.sp,
                    color = if (isCurrent) ElectricBlue else TextMuted
                )
            }
        }

        // Saved MiniDiscs Collection
        item {
            Text(
                text = "CARTRIDGE VAULT (${allMiniDiscs.size} DISCS)",
                fontFamily = ChakraPetchFont,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        itemsIndexed(allMiniDiscs) { _, disc ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(3.dp))
                    .background(GunmetalDark)
                    .border(1.dp, BorderHardware, RoundedCornerShape(3.dp))
                    .clickable { onLoadMiniDisc(disc) }
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(android.graphics.Color.parseColor(disc.colorHex)))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(text = disc.discTitle, fontFamily = ChakraPetchFont, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextPrimary)
                        Text(text = "${disc.labelText} // ATRAC ${disc.atracMode}", fontFamily = ShareTechMonoFont, fontSize = 9.sp, color = TextSecondary)
                    }
                }

                Text(text = "LOAD ►", fontFamily = ChakraPetchFont, fontSize = 10.sp, color = ElectricBlue)
            }
        }
    }

    // Record MiniDisc Dialog
    if (showCreateDialog) {
        var discTitle by remember { mutableStateOf("") }
        var atracMode by remember { mutableStateOf("SP") }
        var discColor by remember { mutableStateOf("#0055AA") }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = {
                Text(
                    text = "RECORD NEW MINIDISC",
                    fontFamily = ChakraPetchFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = ElectricBlue
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = discTitle,
                        onValueChange = { discTitle = it },
                        label = { Text("Disc Title") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "ATRAC MODE", fontFamily = ChakraPetchFont, fontSize = 11.sp, color = TextSecondary)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("SP", "LP2", "LP4").forEach { m ->
                                Box(
                                    modifier = Modifier
                                        .background(if (atracMode == m) ElectricBlue else GunmetalLight)
                                        .clickable { atracMode = m }
                                        .padding(horizontal = 6.dp, vertical = 4.dp)
                                ) {
                                    Text(text = m, fontFamily = ChakraPetchFont, fontSize = 10.sp, color = if (atracMode == m) MatteBlack else TextPrimary)
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "CARTRIDGE COLOR", fontFamily = ChakraPetchFont, fontSize = 11.sp, color = TextSecondary)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("#0055AA", "#880022", "#008844", "#333333", "#CC7700").forEach { col ->
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(Color(android.graphics.Color.parseColor(col)))
                                        .border(2.dp, if (discColor == col) TextPrimary else Color.Transparent, CircleShape)
                                        .clickable { discColor = col }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val ids = allTracks.map { it.id }
                        onSaveMiniDisc(
                            discTitle.ifBlank { "UNTITLED MD" },
                            "MD-DATA // 74 MIN",
                            discColor,
                            atracMode,
                            ids
                        )
                        showCreateDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                ) {
                    Text(text = "RECORD MD", color = MatteBlack, fontFamily = ChakraPetchFont, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text(text = "CANCEL", color = TextSecondary, fontFamily = ChakraPetchFont)
                }
            },
            containerColor = GunmetalDark
        )
    }
}
