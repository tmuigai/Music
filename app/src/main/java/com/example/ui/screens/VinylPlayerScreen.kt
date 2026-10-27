package com.example.ui.screens

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode as AnimRepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PlaybackState
import com.example.model.Track
import com.example.model.VinylState
import com.example.ui.theme.AcidGreen
import com.example.ui.theme.BevelLight
import com.example.ui.theme.BevelShadow
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
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun VinylPlayerScreen(
    playbackState: PlaybackState,
    vinylState: VinylState,
    allTracks: List<Track>,
    accentColor: Color,
    onSetRpm: (Int) -> Unit,
    onSetPitch: (Float) -> Unit,
    onCueTonearm: (Boolean) -> Unit,
    onScratchVinyl: (Float) -> Unit,
    onTrackSelect: (Track) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val currentTrack = playbackState.currentTrack
    val progress = if (playbackState.durationMs > 0) {
        (playbackState.positionMs.toFloat() / playbackState.durationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MatteBlack),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Direct-Drive Turntable Chassis
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp))
                    .background(GunmetalDark)
                    .border(2.dp, BorderHardware, RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                // Header: Model and Quartz Lock indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SL-99 DIRECT DRIVE TURNTABLE",
                            fontFamily = ChakraPetchFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = accentColor,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "QUARTZ SYNTHESIZER // HIGH TORQUE",
                            fontFamily = ShareTechMonoFont,
                            fontSize = 9.sp,
                            color = TextMuted
                        )
                    }

                    // Quartz Lock 0% indicator LED
                    val isLocked = kotlin.math.abs(vinylState.pitchPercent) < 0.2f
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(MatteBlack)
                            .border(1.dp, if (isLocked) AcidGreen else BorderHardware, RoundedCornerShape(2.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isLocked) AcidGreen else GunmetalMedium)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isLocked) "QUARTZ LOCK" else "PITCH VAR",
                            fontFamily = ShareTechMonoFont,
                            fontSize = 9.sp,
                            color = if (isLocked) AcidGreen else BurntOrange
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Vinyl Record Platter and Moving Tonearm
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MatteBlack)
                        .border(1.dp, BorderHardware, RoundedCornerShape(6.dp))
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                val dragDelta = dragAmount.x + dragAmount.y
                                onScratchVinyl(dragDelta * 0.4f)
                            }
                        }
                ) {
                    val cw = maxWidth
                    val ch = maxHeight

                    // Canvas: Spinning Platter, Grooves, Center Label, and Tonearm
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val widthPx = size.width
                        val heightPx = size.height
                        val platterCenter = Offset(widthPx * 0.44f, heightPx * 0.50f)
                        val platterRadius = heightPx * 0.44f

                        // 1. Brushed Aluminum Platter Rim & Strobe Dots
                        drawCircle(color = LightAluminium, radius = platterRadius, center = platterCenter)
                        drawCircle(color = GunmetalDark, radius = platterRadius - 4f, center = platterCenter)

                        // Strobe Dots on rim
                        val strobeCount = if (vinylState.rpm == 45) 36 else 48
                        for (i in 0 until strobeCount) {
                            val angleRad = (vinylState.platterRotationDeg + (i * 360f / strobeCount)) * (PI.toFloat() / 180f)
                            val dotOffset = Offset(
                                platterCenter.x + cos(angleRad) * (platterRadius - 2f),
                                platterCenter.y + sin(angleRad) * (platterRadius - 2f)
                            )
                            drawCircle(color = if (i % 2 == 0) Color.White else GunmetalLight, radius = 2f, center = dotOffset)
                        }

                        // 2. Heavy Black Vinyl Disc (12" LP)
                        val vinylRadius = platterRadius - 6f
                        drawCircle(color = Color(0xFF111315), radius = vinylRadius, center = platterCenter)

                        // 3. Concentric Micro-Grooves & Runout Spiral
                        val grooveColors = listOf(Color(0xFF1E2124), Color(0xFF15181A), Color(0xFF23272B))
                        for (g in 1..12) {
                            val r = vinylRadius * (0.42f + g * 0.045f)
                            drawCircle(
                                color = grooveColors[g % 3],
                                radius = r,
                                center = platterCenter,
                                style = Stroke(width = 1.2f)
                            )
                        }

                        // 4. Center Paper Label (1990s Underground Record Label)
                        val labelRadius = vinylRadius * 0.38f
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(BurntOrange, Color(0xFF8B3200)),
                                center = platterCenter,
                                radius = labelRadius
                            ),
                            radius = labelRadius,
                            center = platterCenter
                        )
                        drawCircle(
                            color = MatteBlack,
                            radius = labelRadius,
                            center = platterCenter,
                            style = Stroke(width = 2f)
                        )

                        // Center Spindle Hole & 45 RPM Adapter Puck
                        drawCircle(color = LightAluminium, radius = 16f, center = platterCenter)
                        drawCircle(color = MatteBlack, radius = 6f, center = platterCenter)

                        // Center Label Text markings (rotating radial spikes)
                        for (i in 0 until 4) {
                            val rad = (vinylState.platterRotationDeg + i * 90f) * (PI.toFloat() / 180f)
                            drawLine(
                                color = MatteBlack.copy(alpha = 0.5f),
                                start = platterCenter,
                                end = Offset(platterCenter.x + cos(rad) * (labelRadius * 0.85f), platterCenter.y + sin(rad) * (labelRadius * 0.85f)),
                                strokeWidth = 1.5f
                            )
                        }

                        // 5. Direct-Drive Strobe Light Tower (Red LED in top-left)
                        val strobeTower = Offset(widthPx * 0.06f, heightPx * 0.12f)
                        drawCircle(color = GunmetalLight, radius = 12f, center = strobeTower)
                        drawCircle(color = DangerRed, radius = 6f, center = strobeTower)

                        // 6. S-Shaped Mechanical Tonearm & Cartridge
                        val pivotOffset = Offset(widthPx * 0.90f, heightPx * 0.22f)
                        // Tonearm base gimbal
                        drawCircle(color = LightAluminium, radius = 16f, center = pivotOffset)
                        drawCircle(color = GunmetalDark, radius = 10f, center = pivotOffset)
                        // Counterweight
                        drawRoundRect(
                            color = GunmetalLight,
                            topLeft = Offset(pivotOffset.x - 8f, pivotOffset.y - 28f),
                            size = androidx.compose.ui.geometry.Size(16f, 18f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
                        )

                        // Needle tip position on record: Moves from outer lead-in (0%) to inner lead-out (100%)
                        val needleDist = (platterRadius * 0.92f) - (progress * (platterRadius * 0.50f))
                        val needleAngleRad = 15f * (PI.toFloat() / 180f)
                        val needlePos = Offset(
                            platterCenter.x + cos(needleAngleRad) * needleDist,
                            platterCenter.y + sin(needleAngleRad) * needleDist
                        )

                        // Tonearm tube (S-curve drawn as lines)
                        val midKink = Offset(pivotOffset.x - 40f, pivotOffset.y + 70f)
                        drawLine(
                            color = LightAluminium,
                            start = pivotOffset,
                            end = midKink,
                            strokeWidth = 4f
                        )
                        drawLine(
                            color = LightAluminium,
                            start = midKink,
                            end = needlePos,
                            strokeWidth = 4f
                        )

                        // Headshell and Stylus Needle Cartridge
                        val isCuedDown = vinylState.isTonearmCued
                        val cartColor = if (isCuedDown) AcidGreen else BurntOrange
                        drawRoundRect(
                            color = cartColor,
                            topLeft = Offset(needlePos.x - 12f, needlePos.y - 6f),
                            size = androidx.compose.ui.geometry.Size(24f, 12f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
                        )
                        // Tiny needle point
                        drawCircle(color = Color.White, radius = 2.5f, center = needlePos)
                    }

                    // Overlay HUD on vinyl display
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                            .background(MatteBlack.copy(alpha = 0.85f))
                            .border(1.dp, BorderHardware, RoundedCornerShape(2.dp))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Column {
                            Text(
                                text = currentTrack?.title ?: "RP-9917 VINYL MASTER",
                                fontFamily = ChakraPetchFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${vinylState.rpm} RPM // ${if (vinylState.isTonearmCued) "NEEDLE ENGAGED" else "ARM CUED UP"}",
                                fontFamily = ShareTechMonoFont,
                                fontSize = 9.sp,
                                color = if (vinylState.isTonearmCued) AcidGreen else BurntOrange
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Physical Controls: 33/45 RPM, Tonearm Cue Lever, Pitch Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // RPM Speed Buttons (33 and 45)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // 33 RPM
                        val is33 = vinylState.rpm == 33
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (is33) GunmetalLight else GunmetalMedium)
                                .border(1.dp, if (is33) AcidGreen else BorderHardware, RoundedCornerShape(3.dp))
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    onSetRpm(33)
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "33 RPM",
                                fontFamily = ChakraPetchFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (is33) AcidGreen else TextSecondary
                            )
                        }

                        // 45 RPM
                        val is45 = vinylState.rpm == 45
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (is45) GunmetalLight else GunmetalMedium)
                                .border(1.dp, if (is45) BurntOrange else BorderHardware, RoundedCornerShape(3.dp))
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    onSetRpm(45)
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "45 RPM",
                                fontFamily = ChakraPetchFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (is45) BurntOrange else TextSecondary
                            )
                        }
                    }

                    // Tonearm Cueing Switch (UP vs DOWN)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (vinylState.isTonearmCued) AcidGreen else DangerRed)
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                onCueTonearm(!vinylState.isTonearmCued)
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (vinylState.isTonearmCued) "CUE: DOWN [PLAY]" else "CUE: UP [LIFT]",
                            fontFamily = ChakraPetchFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = MatteBlack
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Direct-Drive Pitch Adjustment Slider (+/- 8%)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MatteBlack)
                        .border(1.dp, BorderHardware, RoundedCornerShape(4.dp))
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PITCH CONTROL [DIRECT DRIVE]",
                            fontFamily = ChakraPetchFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = String.format("%+.1f%%", vinylState.pitchPercent),
                            fontFamily = ShareTechMonoFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (kotlin.math.abs(vinylState.pitchPercent) < 0.2f) AcidGreen else BurntOrange
                        )
                    }

                    Slider(
                        value = vinylState.pitchPercent,
                        onValueChange = { onSetPitch(it) },
                        valueRange = -8.0f..8.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = LightAluminium,
                            activeTrackColor = accentColor,
                            inactiveTrackColor = GunmetalMedium
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "-8%", fontFamily = ShareTechMonoFont, fontSize = 8.sp, color = TextMuted)
                        Text(
                            text = "0% [RESET]",
                            fontFamily = ShareTechMonoFont,
                            fontSize = 8.sp,
                            color = AcidGreen,
                            modifier = Modifier.clickable { onSetPitch(0.0f) }
                        )
                        Text(text = "+8%", fontFamily = ShareTechMonoFont, fontSize = 8.sp, color = TextMuted)
                    }
                }
            }
        }

        // Vinyl Crate / Record Selection
        item {
            Column {
                Text(
                    text = "CRATE DIGGING // 12\" VINYL COLLECTION",
                    fontFamily = ChakraPetchFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    allTracks.take(8).forEachIndexed { idx, track ->
                        val isPlaying = currentTrack?.id == track.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (isPlaying) GunmetalLight else GunmetalDark)
                                .border(1.dp, if (isPlaying) accentColor else BorderHardware, RoundedCornerShape(3.dp))
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    onTrackSelect(track)
                                }
                                .padding(horizontal = 10.dp, vertical = 7.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = String.format("%02d", idx + 1),
                                    fontFamily = ShareTechMonoFont,
                                    fontSize = 11.sp,
                                    color = if (isPlaying) accentColor else TextMuted,
                                    modifier = Modifier.width(26.dp)
                                )
                                Column {
                                    Text(
                                        text = track.title,
                                        fontFamily = ChakraPetchFont,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${track.artist} // 12\" PRESSING",
                                        fontFamily = ShareTechMonoFont,
                                        fontSize = 10.sp,
                                        color = TextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            if (isPlaying) {
                                Box(
                                    modifier = Modifier
                                        .background(MatteBlack)
                                        .border(1.dp, accentColor, RoundedCornerShape(2.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "SPINNING",
                                        fontFamily = ShareTechMonoFont,
                                        fontSize = 9.sp,
                                        color = accentColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
