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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import com.example.model.PlaybackState
import com.example.model.StickerItem
import com.example.model.TapeSide
import com.example.model.Track
import com.example.ui.components.InteractiveCassetteDeck
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
fun CassettePlayerScreen(
    playbackState: PlaybackState,
    currentSide: TapeSide,
    shellColorHex: String,
    reelColorHex: String,
    stickers: List<StickerItem>,
    accentColor: Color,
    onFlipSide: () -> Unit,
    onSeekDrag: (Float) -> Unit,
    onTrackSelect: (Track) -> Unit,
    onSetShellColor: (String) -> Unit,
    onSetReelColor: (String) -> Unit,
    onAddSticker: (String) -> Unit,
    onEjectClick: () -> Unit = {},
    onInsertClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val currentTrack = playbackState.currentTrack
    var showCustomizerDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MatteBlack),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Detailed Interactive Cassette Deck
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "VINTAGE CASSETTE DECK // TC-99X",
                        fontFamily = ChakraPetchFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = accentColor,
                        letterSpacing = 1.sp
                    )

                    // Cassette Shell Customizer Toggle
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(GunmetalDark)
                            .border(1.dp, BorderHardware, RoundedCornerShape(3.dp))
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                showCustomizerDialog = true
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ColorLens, contentDescription = "Customize", tint = BurntOrange, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "CUSTOM SHELL", fontFamily = ChakraPetchFont, fontSize = 10.sp, color = TextPrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                InteractiveCassetteDeck(
                    title = currentTrack?.title ?: "REPLAY 99 MIXTAPE",
                    artist = currentTrack?.artist ?: "UNDERGROUND SYSTEM",
                    currentSide = currentSide,
                    isPlaying = playbackState.isPlaying,
                    progress = playbackState.tapeReelProgress,
                    tapeCounter = playbackState.tapeCounter,
                    isInserted = playbackState.isCassetteInserted,
                    shellColorHex = shellColorHex,
                    reelColorHex = reelColorHex,
                    isTransparent = true,
                    stickers = stickers,
                    onSeekDrag = onSeekDrag,
                    onFlipSide = onFlipSide,
                    onEjectClick = onEjectClick,
                    onInsertClick = onInsertClick
                )
            }
        }

        // Mechanical Tape Counter & Transport Status LCD
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GunmetalDark)
                    .border(1.dp, BorderHardware, RoundedCornerShape(4.dp))
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "TAPE HEAD POSITION", fontFamily = ShareTechMonoFont, fontSize = 9.sp, color = TextMuted)
                        Text(
                            text = String.format("INDEX: %04d", playbackState.tapeCounter),
                            fontFamily = ShareTechMonoFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = accentColor
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "TAPE SPEED", fontFamily = ShareTechMonoFont, fontSize = 9.sp, color = TextMuted)
                        Text(
                            text = String.format("%.2fx SPEED", playbackState.playbackSpeed),
                            fontFamily = ShareTechMonoFont,
                            fontSize = 12.sp,
                            color = BurntOrange
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "ACTIVE SIDE", fontFamily = ShareTechMonoFont, fontSize = 9.sp, color = TextMuted)
                        Text(
                            text = currentSide.name,
                            fontFamily = ChakraPetchFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = LcdCyan
                        )
                    }
                }
            }
        }

        // Side Track Listing (Cassette J-Card Insert)
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "J-CARD // ${currentSide.name} TRACKLIST",
                        fontFamily = ChakraPetchFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Box(
                        modifier = Modifier
                            .background(GunmetalDark)
                            .border(1.dp, accentColor, RoundedCornerShape(2.dp))
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                onFlipSide()
                            }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "FLIP TO ${if (currentSide == TapeSide.SIDE_A) "SIDE B" else "SIDE A"}",
                            fontFamily = ChakraPetchFont,
                            fontSize = 10.sp,
                            color = accentColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    playbackState.queue.forEachIndexed { idx, track ->
                        val isPlaying = currentTrack?.id == track.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (isPlaying) GunmetalLight else GunmetalDark)
                                .border(1.dp, if (isPlaying) accentColor else BorderHardware, RoundedCornerShape(2.dp))
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    onTrackSelect(track)
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${if (currentSide == TapeSide.SIDE_A) "A" else "B"}${idx + 1}",
                                    fontFamily = ShareTechMonoFont,
                                    fontSize = 11.sp,
                                    color = if (isPlaying) accentColor else TextMuted,
                                    modifier = Modifier.width(28.dp)
                                )
                                Column {
                                    Text(
                                        text = track.title,
                                        fontFamily = ChakraPetchFont,
                                        fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp,
                                        color = TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
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
                                color = if (isPlaying) accentColor else TextMuted
                            )
                        }
                    }
                }
            }
        }
    }

    // Cassette Customization Modal
    if (showCustomizerDialog) {
        AlertDialog(
            onDismissRequest = { showCustomizerDialog = false },
            title = {
                Text(
                    text = "CUSTOMIZE CASSETTE HARDWARE",
                    fontFamily = ChakraPetchFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = accentColor
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "SHELL COLOR", fontFamily = ChakraPetchFont, fontSize = 12.sp, color = TextSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("#16191C", "#2D3035", "#801B1B", "#173B25", "#3A2A1A").forEach { hex ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(hex)))
                                    .border(2.dp, if (shellColorHex == hex) accentColor else Color.Transparent, CircleShape)
                                    .clickable { onSetShellColor(hex) }
                            )
                        }
                    }

                    Text(text = "REEL SPOOL COLOR", fontFamily = ChakraPetchFont, fontSize = 12.sp, color = TextSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("#00FF66", "#FF6B1A", "#00E5FF", "#FFFF33", "#FFFFFF").forEach { hex ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(hex)))
                                    .border(2.dp, if (reelColorHex == hex) accentColor else Color.Transparent, CircleShape)
                                    .clickable { onSetReelColor(hex) }
                            )
                        }
                    }

                    Text(text = "APPLY STICKER", fontFamily = ChakraPetchFont, fontSize = 12.sp, color = TextSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("90s RAVE", "ACID BASS", "D&B JUNGLE", "CHROME II").forEach { stk ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(GunmetalDark)
                                    .border(1.dp, BorderHardware, RoundedCornerShape(3.dp))
                                    .clickable { onAddSticker(stk) }
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Text(text = "+ $stk", fontFamily = ChakraPetchFont, fontSize = 9.sp, color = AcidGreen)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showCustomizerDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    Text(text = "APPLY", color = MatteBlack, fontFamily = ChakraPetchFont, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = GunmetalDark,
            tonalElevation = 8.dp
        )
    }
}
