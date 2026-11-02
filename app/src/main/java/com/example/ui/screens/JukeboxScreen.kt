package com.example.ui.screens

import android.view.HapticFeedbackConstants
import androidx.activity.compose.BackHandler
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Track
import com.example.ui.ReplayViewModel
import com.example.ui.components.HardwarePushSwitch
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
import com.example.ui.theme.LightAluminium
import com.example.ui.theme.MatteBlack
import com.example.ui.theme.ShareTechMonoFont
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun JukeboxScreen(
    viewModel: ReplayViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateBack()
    }

    val credits by viewModel.jukeboxCredits.collectAsState()
    val selectionCode by viewModel.jukeboxSelectionCode.collectAsState()
    val isLocked by viewModel.isPartyLockoutActive.collectAsState()
    val allTracks by viewModel.allTracks.collectAsState()
    val playbackState by viewModel.audioEngine.playbackState.collectAsState()
    val view = LocalView.current

    val infiniteTransition = rememberInfiniteTransition(label = "jukebox_neon")
    val neonGlow by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = AnimRepeatMode.Reverse
        ),
        label = "neon_pulse"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MatteBlack)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Neon Jukebox Arch Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GunmetalDark)
                    .border(2.dp, ElectricBlue.copy(alpha = neonGlow), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
                    .padding(14.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PARTY JUKEBOX // 100-SELECT",
                            fontFamily = ChakraPetchFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = ElectricBlue
                        )
                        Box(
                            modifier = Modifier
                                .background(if (isLocked) DangerRed else AcidGreen)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isLocked) "GUEST LOCKOUT" else "OPEN FOR PARTY",
                                fontFamily = ShareTechMonoFont,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MatteBlack
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Center LCD Selection & Credits Readout
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF081018))
                            .border(1.5.dp, ElectricBlue.copy(alpha = 0.5f))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "SELECT CODE",
                                fontFamily = ShareTechMonoFont,
                                fontSize = 9.sp,
                                color = TextMuted
                            )
                            Text(
                                text = if (selectionCode.isEmpty()) "--" else selectionCode,
                                fontFamily = ShareTechMonoFont,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = LcdCyan,
                                letterSpacing = 4.sp
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "CREDITS",
                                fontFamily = ShareTechMonoFont,
                                fontSize = 9.sp,
                                color = TextMuted
                            )
                            Text(
                                text = String.format("%02d", credits),
                                fontFamily = ShareTechMonoFont,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (credits > 0) AcidGreen else DangerRed
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Coin deposit slot button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HardwarePushSwitch(
                            label = "INSERT COIN // 25¢",
                            code = "+3 CREDITS",
                            isActive = true,
                            activeColor = BurntOrange,
                            onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                viewModel.insertJukeboxCoin()
                            },
                            modifier = Modifier.weight(1f).testTag("jukebox_coin_btn")
                        )

                        HardwarePushSwitch(
                            label = "HOST LOCKOUT",
                            code = if (isLocked) "LOCKED" else "UNLOCKED",
                            isActive = isLocked,
                            activeColor = DangerRed,
                            onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                viewModel.togglePartyLockout()
                            },
                            modifier = Modifier.weight(1f).testTag("jukebox_lockout_btn")
                        )
                    }
                }
            }
        }

        // Mechanical Push-Button Selector Matrix (Letters A-H and Numbers 1-8)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GunmetalDark)
                    .border(1.dp, BorderHardware)
                    .padding(12.dp)
            ) {
                Text(
                    text = "KEYPAD MATRIX // LETTERS (A-H) & NUMBERS (1-8)",
                    fontFamily = ChakraPetchFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = BurntOrange
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Letter Row: A to H
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("A", "B", "C", "D", "E", "F", "G", "H").forEach { letter ->
                        val isLetterSelected = selectionCode.startsWith(letter)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (isLetterSelected) ElectricBlue else GunmetalLight)
                                .border(1.5.dp, if (isLetterSelected) LcdCyan else BorderHardware)
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    viewModel.pressJukeboxKey(letter)
                                }
                                .testTag("jukebox_key_$letter"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = letter,
                                fontFamily = ChakraPetchFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = if (isLetterSelected) MatteBlack else TextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Number Row: 1 to 8
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    (1..8).forEach { number ->
                        val numStr = number.toString()
                        val isNumSelected = selectionCode.endsWith(numStr) && selectionCode.length >= 2
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (isNumSelected) AcidGreen else GunmetalLight)
                                .border(1.5.dp, if (isNumSelected) BevelLight else BorderHardware)
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    viewModel.pressJukeboxKey(numStr)
                                }
                                .testTag("jukebox_key_$numStr"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = numStr,
                                fontFamily = ChakraPetchFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = if (isNumSelected) MatteBlack else TextPrimary
                            )
                        }
                    }
                }
            }
        }

        // Title Strips Rack
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TITLE STRIP RACK // REPERTOIRE",
                    fontFamily = ChakraPetchFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = TextPrimary
                )
                Text(
                    text = "${allTracks.size} AVAILABLE",
                    fontFamily = ShareTechMonoFont,
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }
        }

        val letters = listOf("A", "B", "C", "D", "E", "F", "G", "H")
        val stripItems = allTracks.take(64).mapIndexed { idx, track ->
            val rowLetter = letters[(idx / 8) % letters.size]
            val colDigit = (idx % 8) + 1
            val code = "$rowLetter$colDigit"
            code to track
        }

        if (stripItems.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(GunmetalDark)
                        .border(1.dp, BorderHardware)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "NO STRIPS LOADED // SCAN LOCAL AUDIO IN CRATE",
                        fontFamily = ShareTechMonoFont,
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }
        } else {
            itemsIndexed(stripItems) { _, (code, track) ->
                val isCurrentlyPlaying = playbackState.currentTrack?.id == track.id && playbackState.isPlaying

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (isCurrentlyPlaying) Color(0xFF1E281E) else GunmetalDark)
                        .border(
                            1.dp,
                            if (isCurrentlyPlaying) AcidGreen else BorderHardware
                        )
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            viewModel.pressJukeboxKey(code[0].toString())
                            viewModel.pressJukeboxKey(code[1].toString())
                        }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Code badge
                    Box(
                        modifier = Modifier
                            .background(if (isCurrentlyPlaying) AcidGreen else GunmetalMedium)
                            .border(1.dp, if (isCurrentlyPlaying) BevelLight else Color(0xFF333333))
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = code,
                            fontFamily = ShareTechMonoFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (isCurrentlyPlaying) MatteBlack else LcdCyan
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Title & Artist
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = track.title,
                            fontFamily = ChakraPetchFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (isCurrentlyPlaying) AcidGreen else TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${track.artist} - ${track.album}",
                            fontFamily = ShareTechMonoFont,
                            fontSize = 9.sp,
                            color = TextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Playing indicator
                    if (isCurrentlyPlaying) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(AcidGreen)
                        )
                    }
                }
            }
        }
    }
}
