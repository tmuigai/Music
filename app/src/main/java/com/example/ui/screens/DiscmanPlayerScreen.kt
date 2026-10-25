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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.model.RepeatMode
import com.example.model.Track
import com.example.ui.components.DiscmanGraphic
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
fun DiscmanPlayerScreen(
    playbackState: PlaybackState,
    showJewelCase: Boolean,
    accentColor: Color,
    onToggleJewelCase: (Boolean) -> Unit,
    onTrackSelect: (Track) -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val currentTrack = playbackState.currentTrack

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MatteBlack),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Portable Discman Clamshell Hardware
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PORTABLE CD SYSTEM // D-990",
                        fontFamily = ChakraPetchFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = accentColor,
                        letterSpacing = 1.sp
                    )

                    Box(
                        modifier = Modifier
                            .background(GunmetalDark)
                            .border(1.dp, BorderHardware, RoundedCornerShape(2.dp))
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                onToggleJewelCase(true)
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "JEWEL CASE // BOOKLET",
                            fontFamily = ChakraPetchFont,
                            fontSize = 10.sp,
                            color = LcdCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                DiscmanGraphic(
                    title = currentTrack?.title ?: "COMPACT DISC 99",
                    artist = currentTrack?.artist ?: "UNKNOWN ARTIST",
                    isPlaying = playbackState.isPlaying,
                    rotationAngle = playbackState.cdRotationAngle,
                    antiSkipPercent = playbackState.antiSkipBufferPercent,
                    onOpenJewelCase = { onToggleJewelCase(true) }
                )
            }
        }

        // Discman Control Strip (ESP, Mega Bass, Repeat, Shuffle)
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
                    // Repeat Mode
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (playbackState.repeatMode != RepeatMode.OFF) GunmetalLight else MatteBlack)
                            .border(1.dp, if (playbackState.repeatMode != RepeatMode.OFF) accentColor else BorderHardware, RoundedCornerShape(3.dp))
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                onCycleRepeat()
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "REPEAT: ${playbackState.repeatMode.name}",
                            fontFamily = ChakraPetchFont,
                            fontSize = 11.sp,
                            color = if (playbackState.repeatMode != RepeatMode.OFF) accentColor else TextMuted
                        )
                    }

                    // Shuffle
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (playbackState.isShuffle) GunmetalLight else MatteBlack)
                            .border(1.dp, if (playbackState.isShuffle) BurntOrange else BorderHardware, RoundedCornerShape(3.dp))
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                onToggleShuffle()
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (playbackState.isShuffle) "SHUFFLE: ON" else "SHUFFLE: OFF",
                            fontFamily = ChakraPetchFont,
                            fontSize = 11.sp,
                            color = if (playbackState.isShuffle) BurntOrange else TextMuted
                        )
                    }

                    // 1-BIT DAC Indicator
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "1-BIT D/A CONVERTER", fontFamily = ShareTechMonoFont, fontSize = 8.sp, color = TextMuted)
                        Text(text = "8x OVERSAMPLING", fontFamily = ShareTechMonoFont, fontSize = 8.sp, color = accentColor)
                    }
                }
            }
        }

        // CD Track Index List
        item {
            Column {
                Text(
                    text = "DISC INDEX // DIRECT ACCESS",
                    fontFamily = ChakraPetchFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
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
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = String.format("%02d", idx + 1),
                                    fontFamily = ShareTechMonoFont,
                                    fontSize = 12.sp,
                                    color = if (isPlaying) accentColor else TextMuted,
                                    modifier = Modifier.width(30.dp)
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
                                        fontSize = 10.sp,
                                        color = TextSecondary
                                    )
                                }
                            }

                            Text(
                                text = String.format("%02d:%02d", (track.durationMs / 60000), (track.durationMs % 60000) / 1000),
                                fontFamily = ShareTechMonoFont,
                                fontSize = 11.sp,
                                color = if (isPlaying) accentColor else TextMuted
                            )
                        }
                    }
                }
            }
        }
    }

    // Jewel Case / Digital Booklet Dialog
    if (showJewelCase) {
        AlertDialog(
            onDismissRequest = { onToggleJewelCase(false) },
            title = {
                Text(
                    text = "JEWEL CASE // DIGITAL BOOKLET",
                    fontFamily = ChakraPetchFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = LcdCyan
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = currentTrack?.album ?: "1999 UNDERGROUND COMPILATION",
                        fontFamily = ChakraPetchFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "PRODUCED & MASTERED IN 16-BIT 44.1kHz STEREO.\n\n" +
                                "Liner Notes: Recorded in underground warehouse sessions on analog tape, " +
                                "digitized to Red Book Audio CD specifications. Featuring optical lens tracking, " +
                                "45-second electronic shock protection buffer, and dynamic mega-bass circuit.",
                        fontFamily = ShareTechMonoFont,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "CATALOG: REPLAY-CD-9901 // ALL RIGHTS RESERVED.",
                        fontFamily = ShareTechMonoFont,
                        fontSize = 9.sp,
                        color = TextMuted
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { onToggleJewelCase(false) },
                    colors = ButtonDefaults.buttonColors(containerColor = LcdCyan)
                ) {
                    Text(text = "CLOSE CASE", color = MatteBlack, fontFamily = ChakraPetchFont, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = GunmetalDark
        )
    }
}
