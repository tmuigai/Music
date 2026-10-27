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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MasterVolumeState
import com.example.model.PlaybackState
import com.example.model.Track
import com.example.ui.components.StereoVuMeters
import com.example.ui.theme.AcidGreen
import com.example.ui.theme.BorderHardware
import com.example.ui.theme.BurntOrange
import com.example.ui.theme.ChakraPetchFont
import com.example.ui.theme.DangerRed
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
fun BoomboxScreen(
    playbackState: PlaybackState,
    volumeState: MasterVolumeState,
    vuMeterLeft: Float,
    vuMeterRight: Float,
    accentColor: Color,
    onSetVolume: (Int) -> Unit,
    onTogglePlayPause: () -> Unit,
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
        // Boombox Title
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "GHETTOBLASTER 99 // DUAL TAPE",
                        fontFamily = ChakraPetchFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = BurntOrange,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "HIGH-OUTPUT STEREO SOUNDSYSTEM // BASS REFLEX",
                        fontFamily = ShareTechMonoFont,
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
                Text(
                    text = "DUAL CONE",
                    fontFamily = ChakraPetchFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = AcidGreen
                )
            }
        }

        // Heavy Boombox Chassis with Animated Bouncing Speaker Cones
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF16191D))
                    .border(3.dp, BorderHardware, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Column {
                    // Top Chrome Carrying Handle & Antenna
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(18.dp)
                            .background(GunmetalMedium)
                            .border(1.dp, BorderHardware, RoundedCornerShape(2.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "=== HIGH PRECISION DUBBING SYSTEM ===", fontFamily = ShareTechMonoFont, fontSize = 8.sp, color = TextMuted)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Dual Animated Speaker Cones
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Speaker Cone
                        SpeakerCone(
                            label = "LEFT WOOFER",
                            amplitude = vuMeterLeft,
                            accentColor = BurntOrange
                        )

                        // Center Deck Wells (Well 1 & Well 2)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f).padding(horizontal = 6.dp)
                        ) {
                            Text(text = "WELL 1 // PLAYBACK", fontFamily = ShareTechMonoFont, fontSize = 8.sp, color = TextMuted)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(36.dp)
                                    .background(MatteBlack)
                                    .border(1.dp, if (playbackState.isPlaying) AcidGreen else BorderHardware, RoundedCornerShape(2.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (playbackState.isPlaying) "TAPE 1 SPINNING" else "TAPE 1 STOP",
                                    fontFamily = ChakraPetchFont,
                                    fontSize = 9.sp,
                                    color = if (playbackState.isPlaying) AcidGreen else TextMuted
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(text = "WELL 2 // DUBBING", fontFamily = ShareTechMonoFont, fontSize = 8.sp, color = TextMuted)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(36.dp)
                                    .background(MatteBlack)
                                    .border(1.dp, BorderHardware, RoundedCornerShape(2.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "HIGH-SPEED DUB",
                                    fontFamily = ChakraPetchFont,
                                    fontSize = 9.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        // Right Speaker Cone
                        SpeakerCone(
                            label = "RIGHT WOOFER",
                            amplitude = vuMeterRight,
                            accentColor = BurntOrange
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Track & Status LCD
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0F1B1F))
                            .border(1.dp, LcdCyan.copy(alpha = 0.5f), RoundedCornerShape(3.dp))
                            .padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentTrack?.title ?: "NO CASSETTE INSERTED",
                                    fontFamily = ChakraPetchFont,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = currentTrack?.artist ?: "READY FOR TAPE INSERTION",
                                    fontFamily = ShareTechMonoFont,
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }
                            Text(
                                text = "BASS REFLEX ON",
                                fontFamily = ShareTechMonoFont,
                                fontSize = 9.sp,
                                color = AcidGreen
                            )
                        }
                    }
                }
            }
        }

        // Stereo VU Meter Section
        item {
            StereoVuMeters(
                leftLevel = vuMeterLeft,
                rightLevel = vuMeterRight,
                accentColor = BurntOrange
            )
        }

        // Large Physical Master Volume Rotary Fader
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GunmetalDark)
                    .border(1.dp, BorderHardware, RoundedCornerShape(4.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "MASTER GHETTOBLASTER VOLUME",
                            fontFamily = ChakraPetchFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "${volumeState.amplificationPercent}%",
                            fontFamily = ShareTechMonoFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (volumeState.isClipping) DangerRed else BurntOrange
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Slider(
                        value = volumeState.amplificationPercent.toFloat(),
                        onValueChange = { onSetVolume(it.toInt()) },
                        valueRange = 0f..300f,
                        colors = SliderDefaults.colors(
                            thumbColor = BurntOrange,
                            activeTrackColor = BurntOrange
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "0% MUTE", fontFamily = ShareTechMonoFont, fontSize = 9.sp, color = TextMuted)
                        Text(text = "100% NOMINAL", fontFamily = ShareTechMonoFont, fontSize = 9.sp, color = AcidGreen)
                        Text(text = "200% BOOST", fontFamily = ShareTechMonoFont, fontSize = 9.sp, color = BurntOrange)
                        Text(text = "300% MAX", fontFamily = ShareTechMonoFont, fontSize = 9.sp, color = DangerRed)
                    }
                }
            }
        }
    }
}

@Composable
fun SpeakerCone(
    label: String,
    amplitude: Float,
    accentColor: Color
) {
    // Cone excursion physically responds to amplitude: excursion scale 1.0f to 1.15f
    val excursion = 1.0f + (amplitude.coerceIn(0f, 1f) * 0.15f)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(115.dp)
                .clip(CircleShape)
                .background(Color(0xFF0D0F12))
                .border(3.dp, GunmetalMedium, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(105.dp)) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val baseR = (size.width / 2f) * excursion

                // Outer rubber surround rim
                drawCircle(color = Color(0xFF22262C), radius = (size.width / 2f), center = center)
                drawCircle(color = Color(0xFF16181C), radius = (size.width / 2f) * 0.90f, center = center)

                // Paper cone with ribbed concentric rings
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(Color(0xFF1F242A), Color(0xFF0F1114)),
                        center,
                        baseR
                    ),
                    radius = baseR * 0.85f,
                    center = center
                )
                drawCircle(color = Color(0x33FFFFFF), radius = baseR * 0.65f, center = center, style = Stroke(1f))
                drawCircle(color = Color(0x33FFFFFF), radius = baseR * 0.45f, center = center, style = Stroke(1f))

                // Center dust cap (pumps forward with bass beat!)
                val dustCapR = baseR * 0.32f
                drawCircle(color = accentColor, radius = dustCapR, center = center)
                drawCircle(color = MatteBlack, radius = dustCapR * 0.25f, center = center)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, fontFamily = ShareTechMonoFont, fontSize = 8.sp, color = TextMuted)
    }
}
