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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EqualizerState
import com.example.ui.components.StereoVuMeters
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
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SoundLaboratoryScreen(
    eqState: EqualizerState,
    visualizerBands: FloatArray,
    vuMeterLeft: Float,
    vuMeterRight: Float,
    accentColor: Color,
    onSetBand: (Int, Int) -> Unit,
    onSetBassBoost: (Int) -> Unit,
    onSetVirtualizer: (Int) -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    onApplyPreset: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    var visualizerMode by remember { mutableStateOf("SPECTRUM") } // "SPECTRUM", "OSCILLOSCOPE", "CIRCULAR", "NEEDLES"

    val bandLabels = listOf("60 Hz", "230 Hz", "910 Hz", "3 kHz", "14 kHz")
    val presets = listOf("90s Rave Club", "Tape Cassette Warmth", "Discman Crystal", "Underground Bass", "Flat")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MatteBlack),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Lab Header & Switch
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SOUND LABORATORY // DSP-99",
                        fontFamily = ChakraPetchFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = accentColor,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "5-BAND EQUALIZER & HARDWARE FX",
                        fontFamily = ShareTechMonoFont,
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (eqState.isEnabled) "ACTIVE" else "BYPASS",
                        fontFamily = ChakraPetchFont,
                        fontSize = 10.sp,
                        color = if (eqState.isEnabled) AcidGreen else TextMuted
                    )
                    Switch(
                        checked = eqState.isEnabled,
                        onCheckedChange = { onToggleEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AcidGreen,
                            checkedTrackColor = GunmetalLight
                        )
                    )
                }
            }
        }

        // Real-Time Visualizer Window
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(Color(0xFF090C0E))
                    .border(2.dp, BorderHardware, RoundedCornerShape(4.dp))
                    .padding(8.dp)
            ) {
                // Visualizer Canvas depending on mode
                when (visualizerMode) {
                    "SPECTRUM" -> {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            visualizerBands.forEachIndexed { idx, value ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(value.coerceIn(0.05f, 1.0f))
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(
                                                    BurntOrange,
                                                    accentColor,
                                                    accentColor.copy(alpha = 0.4f)
                                                )
                                            )
                                        )
                                )
                            }
                        }
                    }
                    "OSCILLOSCOPE" -> {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val midY = h / 2f
                            val path = Path()
                            path.moveTo(0f, midY)

                            for (i in visualizerBands.indices) {
                                val x = (i.toFloat() / (visualizerBands.size - 1)) * w
                                val yOffset = (visualizerBands[i] - 0.5f) * h * 0.8f
                                path.lineTo(x, midY + yOffset)
                            }

                            drawPath(
                                path = path,
                                color = accentColor,
                                style = Stroke(width = 3f)
                            )
                        }
                    }
                    "CIRCULAR" -> {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val baseR = size.height * 0.28f
                            val n = visualizerBands.size

                            for (i in 0 until n) {
                                val angle = (i.toFloat() / n) * 2f * Math.PI.toFloat()
                                val barLen = visualizerBands[i] * size.height * 0.25f
                                val startX = center.x + cos(angle) * baseR
                                val startY = center.y + sin(angle) * baseR
                                val endX = center.x + cos(angle) * (baseR + barLen)
                                val endY = center.y + sin(angle) * (baseR + barLen)

                                drawLine(
                                    color = if (i % 2 == 0) accentColor else ElectricBlue,
                                    start = Offset(startX, startY),
                                    end = Offset(endX, endY),
                                    strokeWidth = 4f
                                )
                            }
                            drawCircle(color = GunmetalDark, radius = baseR, center = center)
                        }
                    }
                    else -> {
                        // NEEDLES / ANALOG
                        StereoVuMeters(
                            leftLevel = vuMeterLeft,
                            rightLevel = vuMeterRight,
                            accentColor = accentColor,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // Mode switch pills in top corner
                Row(
                    modifier = Modifier.align(Alignment.TopEnd),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("SPECTRUM", "OSCILLOSCOPE", "CIRCULAR", "NEEDLES").forEach { mode ->
                        val isSel = mode == visualizerMode
                        Box(
                            modifier = Modifier
                                .background(if (isSel) accentColor else GunmetalDark)
                                .clickable { visualizerMode = mode }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = mode.take(4),
                                fontFamily = ShareTechMonoFont,
                                fontSize = 8.sp,
                                color = if (isSel) MatteBlack else TextSecondary
                            )
                        }
                    }
                }
            }
        }

        // Preset Selector
        item {
            Column {
                Text(
                    text = "HARDWARE CURVE PRESETS",
                    fontFamily = ChakraPetchFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presets.forEach { preset ->
                        val isCurrent = eqState.presetName == preset
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (isCurrent) GunmetalLight else GunmetalDark)
                                .border(1.dp, if (isCurrent) accentColor else BorderHardware, RoundedCornerShape(3.dp))
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    onApplyPreset(preset)
                                }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = preset.split(" ").first(),
                                fontFamily = ChakraPetchFont,
                                fontSize = 10.sp,
                                color = if (isCurrent) accentColor else TextSecondary
                            )
                        }
                    }
                }
            }
        }

        // 5-Band Graphic Equalizer Faders
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GunmetalDark)
                    .border(1.dp, BorderHardware, RoundedCornerShape(4.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "GRAPHIC EQUALIZER [-12dB to +12dB]",
                        fontFamily = ChakraPetchFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = BurntOrange
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        bandLabels.forEachIndexed { index, label ->
                            val currentGain = eqState.bands[index] ?: 0
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = String.format("%+d", currentGain),
                                    fontFamily = ShareTechMonoFont,
                                    fontSize = 10.sp,
                                    color = if (currentGain > 0) AcidGreen else if (currentGain < 0) BurntOrange else TextMuted
                                )

                                Slider(
                                    value = currentGain.toFloat(),
                                    onValueChange = { onSetBand(index, it.toInt()) },
                                    valueRange = -12f..12f,
                                    steps = 24,
                                    colors = SliderDefaults.colors(
                                        thumbColor = accentColor,
                                        activeTrackColor = accentColor
                                    ),
                                    modifier = Modifier.height(140.dp)
                                )

                                Text(
                                    text = label,
                                    fontFamily = ShareTechMonoFont,
                                    fontSize = 9.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Bass Boost & Virtualizer Dial Controls
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GunmetalDark)
                    .border(1.dp, BorderHardware, RoundedCornerShape(4.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Bass Boost
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "MEGA BASS", fontFamily = ChakraPetchFont, fontSize = 11.sp, color = AcidGreen)
                            Text(text = "${eqState.bassBoost / 10}%", fontFamily = ShareTechMonoFont, fontSize = 10.sp, color = TextMuted)
                        }
                        Slider(
                            value = eqState.bassBoost.toFloat(),
                            onValueChange = { onSetBassBoost(it.toInt()) },
                            valueRange = 0f..1000f,
                            colors = SliderDefaults.colors(thumbColor = AcidGreen, activeTrackColor = AcidGreen)
                        )
                    }

                    // 3D Virtualizer
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "3D SURROUND", fontFamily = ChakraPetchFont, fontSize = 11.sp, color = LcdCyan)
                            Text(text = "${eqState.virtualizer / 10}%", fontFamily = ShareTechMonoFont, fontSize = 10.sp, color = TextMuted)
                        }
                        Slider(
                            value = eqState.virtualizer.toFloat(),
                            onValueChange = { onSetVirtualizer(it.toInt()) },
                            valueRange = 0f..1000f,
                            colors = SliderDefaults.colors(thumbColor = LcdCyan, activeTrackColor = LcdCyan)
                        )
                    }
                }
            }
        }
    }
}
