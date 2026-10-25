package com.example.ui.screens

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.RadioPresetEntity
import com.example.ui.theme.AcidGreen
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
import kotlin.math.abs

@Composable
fun RadioModeScreen(
    currentFreqMhz: Float,
    isTuned: Boolean,
    presets: List<RadioPresetEntity>,
    accentColor: Color,
    onTuneFreq: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MatteBlack),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Tuner Title
        item {
            Column {
                Text(
                    text = "ANALOG FM TUNER // MODEL ST-99",
                    fontFamily = ChakraPetchFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = accentColor,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "88.0 - 108.0 MHz FM STEREO BAND",
                    fontFamily = ShareTechMonoFont,
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }
        }

        // Mechanical Dial Display with Sliding Needle
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F1215))
                    .border(2.dp, BorderHardware, RoundedCornerShape(6.dp))
                    .padding(12.dp)
            ) {
                Column {
                    // Digital Frequency & Stereo Lock Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "FREQUENCY", fontFamily = ShareTechMonoFont, fontSize = 9.sp, color = TextMuted)
                            Text(
                                text = String.format("%.1f MHz", currentFreqMhz),
                                fontFamily = ShareTechMonoFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                color = if (isTuned) AcidGreen else BurntOrange
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isTuned) AcidGreen else DangerRed)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isTuned) "STEREO LOCKED" else "TUNING STATIC",
                                fontFamily = ChakraPetchFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (isTuned) AcidGreen else DangerRed
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Analog Glass Scale with Frequency Markers and Orange Needle
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(70.dp)
                            .background(Color(0xFF14181C))
                            .border(1.dp, BorderHardware, RoundedCornerShape(2.dp))
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height

                            // Draw frequency tick marks from 88 to 108
                            val minF = 88.0f
                            val maxF = 108.0f
                            for (f in 88..108) {
                                val x = ((f - minF) / (maxF - minF)) * w
                                val isMajor = f % 2 == 0
                                drawLine(
                                    color = if (isMajor) TextPrimary else TextMuted,
                                    start = Offset(x, 0f),
                                    end = Offset(x, if (isMajor) h * 0.4f else h * 0.2f),
                                    strokeWidth = if (isMajor) 2f else 1f
                                )
                            }

                            // Needle Line (Glowing Burnt Orange)
                            val needleX = ((currentFreqMhz - minF) / (maxF - minF)) * w
                            drawLine(
                                color = BurntOrange,
                                start = Offset(needleX, 0f),
                                end = Offset(needleX, h),
                                strokeWidth = 3f
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Manual Tuning Slider
                    Slider(
                        value = currentFreqMhz,
                        onValueChange = {
                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                            onTuneFreq(it)
                        },
                        valueRange = 88.0f..108.0f,
                        steps = 200,
                        colors = SliderDefaults.colors(
                            thumbColor = BurntOrange,
                            activeTrackColor = AcidGreen
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Preset Station Push-Buttons
        item {
            Text(
                text = "SAVED PRESETS // TUNER BANK",
                fontFamily = ChakraPetchFont,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        items(presets) { preset ->
            val isCurrent = abs(preset.freqMhz - currentFreqMhz) < 0.2f
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (isCurrent) GunmetalMedium else GunmetalDark)
                    .border(1.dp, if (isCurrent) AcidGreen else BorderHardware, RoundedCornerShape(3.dp))
                    .clickable {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        onTuneFreq(preset.freqMhz)
                    }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = preset.name,
                        fontFamily = ChakraPetchFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "${preset.genre} // ${preset.freqMhz} MHz",
                        fontFamily = ShareTechMonoFont,
                        fontSize = 10.sp,
                        color = if (isCurrent) AcidGreen else TextSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .background(if (isCurrent) AcidGreen else GunmetalLight)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isCurrent) "LOCKED" else "TUNE",
                        fontFamily = ChakraPetchFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = if (isCurrent) MatteBlack else TextPrimary
                    )
                }
            }
        }
    }
}
