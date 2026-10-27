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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FourTrackState
import com.example.model.PlaybackState
import com.example.model.PortaChannelState
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
fun FourTrackRecorderScreen(
    playbackState: PlaybackState,
    fourTrackState: FourTrackState,
    accentColor: Color,
    onSetSpeed: (Boolean) -> Unit,
    onSetPitch: (Float) -> Unit,
    onSetMasterFader: (Float) -> Unit,
    onSetChannelFader: (Int, Float) -> Unit,
    onToggleRecArm: (Int) -> Unit,
    onToggleDbx: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MatteBlack),
        contentPadding = PaddingValues(10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Portastudio Machine Header & Quad Analog VU Meters
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
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "PORTASTUDIO 424 // 4-TRACK CASSETTE",
                            fontFamily = ChakraPetchFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = accentColor,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "DISCRETE ANALOG MULTITRACK RECORDER",
                            fontFamily = ShareTechMonoFont,
                            fontSize = 9.sp,
                            color = TextMuted
                        )
                    }

                    // DBX Noise Reduction Status Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (fourTrackState.isDbxNoiseReductionOn) BurntOrange else MatteBlack)
                            .border(1.dp, BurntOrange, RoundedCornerShape(2.dp))
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                onToggleDbx()
                            }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (fourTrackState.isDbxNoiseReductionOn) "DBX TYPE II ON" else "DBX OFF",
                            fontFamily = ShareTechMonoFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            color = if (fourTrackState.isDbxNoiseReductionOn) MatteBlack else BurntOrange
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 4-Channel Analog Needle VU Meters Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    fourTrackState.channels.forEach { ch ->
                        AnalogNeedleVuMeter(
                            channelNumber = ch.channelNumber,
                            level = ch.vuLevel,
                            isArmed = ch.isArmedForRec,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 4 Discrete Channel Strips (Faders, Pan, Trim & Rec Arm)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GunmetalDark)
                    .border(1.dp, BorderHardware, RoundedCornerShape(6.dp))
                    .padding(8.dp)
            ) {
                Text(
                    text = "CHANNEL MIXER SECTION // GAIN & FADERS",
                    fontFamily = ChakraPetchFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 4 Channel Strips side by side (optimized for 360dp Nokia G21 width)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    fourTrackState.channels.forEach { ch ->
                        PortaChannelStrip(
                            channel = ch,
                            onFaderChange = { onSetChannelFader(ch.channelNumber, it) },
                            onToggleRecArm = { onToggleRecArm(ch.channelNumber) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Master Section: Tape Speed (4.75 vs 9.5 cm/s), Pitch Control, Master Fader
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GunmetalDark)
                    .border(1.dp, BorderHardware, RoundedCornerShape(6.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = "MASTER TRANSPORT & TAPE SPEED",
                    fontFamily = ChakraPetchFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tape Speed Switch (4.75 vs 9.5 cm/s)
                    Column {
                        Text(text = "TAPE SPEED SELECTOR", fontFamily = ShareTechMonoFont, fontSize = 9.sp, color = TextMuted)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Standard 4.75 cm/s
                            val isStandard = !fourTrackState.isHighSpeed
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(if (isStandard) GunmetalLight else GunmetalMedium)
                                    .border(1.dp, if (isStandard) AcidGreen else BorderHardware, RoundedCornerShape(3.dp))
                                    .clickable {
                                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                        onSetSpeed(false)
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "4.75 CM/S",
                                    fontFamily = ChakraPetchFont,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = if (isStandard) AcidGreen else TextSecondary
                                )
                            }

                            // High Speed 9.5 cm/s (Double speed for studio fidelity)
                            val isHigh = fourTrackState.isHighSpeed
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(if (isHigh) GunmetalLight else GunmetalMedium)
                                    .border(1.dp, if (isHigh) BurntOrange else BorderHardware, RoundedCornerShape(3.dp))
                                    .clickable {
                                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                        onSetSpeed(true)
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "9.5 CM/S [HI-FI]",
                                    fontFamily = ChakraPetchFont,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = if (isHigh) BurntOrange else TextSecondary
                                )
                            }
                        }
                    }

                    // Pitch Vari-Speed Control
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "VARI-SPEED", fontFamily = ShareTechMonoFont, fontSize = 9.sp, color = TextMuted)
                        Text(
                            text = String.format("%+.1f%%", fourTrackState.pitchKnob),
                            fontFamily = ShareTechMonoFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (kotlin.math.abs(fourTrackState.pitchKnob) < 0.2f) AcidGreen else BurntOrange
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Pitch slider
                Slider(
                    value = fourTrackState.pitchKnob,
                    onValueChange = { onSetPitch(it) },
                    valueRange = -12.0f..12.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = LightAluminium,
                        activeTrackColor = accentColor,
                        inactiveTrackColor = GunmetalMedium
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Master Stereo Fader
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MASTER STEREO FADER",
                        fontFamily = ChakraPetchFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "${(fourTrackState.masterFader * 100).toInt()}%",
                        fontFamily = ShareTechMonoFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = accentColor
                    )
                }

                Slider(
                    value = fourTrackState.masterFader,
                    onValueChange = { onSetMasterFader(it) },
                    valueRange = 0.0f..1.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = AcidGreen,
                        activeTrackColor = AcidGreen,
                        inactiveTrackColor = GunmetalMedium
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// Analog Warm Illuminated Needle VU Meter
@Composable
fun AnalogNeedleVuMeter(
    channelNumber: Int,
    level: Float,
    isArmed: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color(0xFF14120E))
            .border(1.dp, BorderHardware, RoundedCornerShape(3.dp))
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "CH $channelNumber",
            fontFamily = ShareTechMonoFont,
            fontSize = 9.sp,
            color = if (isArmed) DangerRed else TextMuted,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Amber backlit meter face
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(Color(0xFF262014))
                .border(0.5.dp, BorderHardware)
        ) {
            val cw = size.width
            val ch = size.height
            val pivot = Offset(cw * 0.5f, ch * 1.15f)
            val needleLen = ch * 0.95f

            // Scale arc marks: -20 to +3 dB
            val minAngle = 135f * (PI.toFloat() / 180f)
            val maxAngle = 45f * (PI.toFloat() / 180f)

            // Needle angle based on audio level
            val normLevel = level.coerceIn(0f, 1f)
            val currentAngle = minAngle + (maxAngle - minAngle) * normLevel

            val needleEnd = Offset(
                pivot.x + cos(currentAngle) * needleLen,
                pivot.y - sin(currentAngle) * needleLen
            )

            // Red overload zone arc at top-right
            drawLine(
                color = DangerRed,
                start = Offset(cw * 0.70f, ch * 0.20f),
                end = Offset(cw * 0.92f, ch * 0.35f),
                strokeWidth = 3f
            )

            // Black scale line
            drawLine(
                color = Color(0xFF6B5833),
                start = Offset(cw * 0.12f, ch * 0.35f),
                end = Offset(cw * 0.70f, ch * 0.20f),
                strokeWidth = 1.5f
            )

            // Moving Black Needle
            drawLine(
                color = MatteBlack,
                start = pivot,
                end = needleEnd,
                strokeWidth = 2f
            )
            // Pivot hub
            drawCircle(color = MatteBlack, radius = 5f, center = pivot)
        }
    }
}

// Single Portastudio Vertical Channel Strip
@Composable
fun PortaChannelStrip(
    channel: PortaChannelState,
    onFaderChange: (Float) -> Unit,
    onToggleRecArm: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current

    Column(
        modifier = modifier
            .background(MatteBlack)
            .border(1.dp, if (channel.isArmedForRec) DangerRed else BorderHardware, RoundedCornerShape(3.dp))
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Channel ID Badge
        Text(
            text = "TRK ${channel.channelNumber}",
            fontFamily = ChakraPetchFont,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            color = if (channel.isArmedForRec) DangerRed else TextPrimary
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Rec Arm Button (Safe vs Ready red LED)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(2.dp))
                .background(if (channel.isArmedForRec) DangerRed else GunmetalMedium)
                .clickable {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    onToggleRecArm()
                }
                .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            Text(
                text = if (channel.isArmedForRec) "ARMED" else "SAFE",
                fontFamily = ShareTechMonoFont,
                fontSize = 8.sp,
                color = if (channel.isArmedForRec) Color.White else TextMuted
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Rotary Trim / Gain knob simulation
        RotaryDialLabel(label = "TRIM", value = "12:00")
        Spacer(modifier = Modifier.height(4.dp))
        RotaryDialLabel(label = "PAN", value = if (channel.pan == 0f) "C" else if (channel.pan < 0) "L" else "R")

        Spacer(modifier = Modifier.height(8.dp))

        // Vertical Fader Slot representation
        Text(
            text = String.format("%.1f", channel.faderLevel * 10),
            fontFamily = ShareTechMonoFont,
            fontSize = 9.sp,
            color = AcidGreen
        )

        // Fader Slider
        Slider(
            value = channel.faderLevel,
            onValueChange = onFaderChange,
            valueRange = 0.0f..1.0f,
            colors = SliderDefaults.colors(
                thumbColor = LightAluminium,
                activeTrackColor = AcidGreen,
                inactiveTrackColor = GunmetalMedium
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun RotaryDialLabel(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(GunmetalMedium)
                .border(1.dp, LightAluminium, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(8.dp)
                    .background(Color.White)
            )
        }
        Text(text = label, fontFamily = ShareTechMonoFont, fontSize = 7.sp, color = TextMuted)
    }
}
