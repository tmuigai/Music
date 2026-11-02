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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import com.example.model.MasterVolumeState
import com.example.model.OutputProfile
import com.example.model.PlaybackState
import com.example.model.TapeDegradationState
import com.example.ui.components.StereoVuMeters
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
    tapeDegradation: TapeDegradationState,
    visualizerBands: FloatArray,
    vuMeterLeft: Float,
    vuMeterRight: Float,
    accentColor: Color,
    onSetBand: (Int, Int) -> Unit,
    onSetBassBoost: (Int) -> Unit,
    onSetVirtualizer: (Int) -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    onApplyPreset: (String) -> Unit,
    onToggleTapeHiss: () -> Unit,
    onToggleWowFlutter: () -> Unit,
    onSetSaturationWarmth: (Float) -> Unit,
    volumeState: MasterVolumeState? = null,
    onSetVolume: ((Int) -> Unit)? = null,
    onSetPreamp: ((Float) -> Unit)? = null,
    onSetBalance: ((Float) -> Unit)? = null,
    onToggleMute: (() -> Unit)? = null,
    onSetOutputProfile: ((OutputProfile) -> Unit)? = null,
    onToggleExperimentalCeiling: (() -> Unit)? = null,
    playbackState: PlaybackState? = null,
    onSetCrossfade: ((Int) -> Unit)? = null,
    onSetAbPointA: (() -> Unit)? = null,
    onSetAbPointB: (() -> Unit)? = null,
    onClearAb: (() -> Unit)? = null,
    onStartSleepTimer: ((Int) -> Unit)? = null,
    onCancelSleepTimer: (() -> Unit)? = null,
    selectedSection: String = "EQ",
    onSectionSelect: (String) -> Unit = {},
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
                        text = "5-BAND EQUALIZER & MASTER AMPLIFIER",
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

        // Section Selector Tabs: EQ, VOLUME, VISUALISERS, TAPE FX, ENGINE
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("EQ", "VOLUME", "VISUALISERS", "TAPE FX", "ENGINE").forEach { sec ->
                    val isSel = sec == selectedSection
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (isSel) GunmetalLight else GunmetalDark)
                            .border(1.dp, if (isSel) accentColor else BorderHardware, RoundedCornerShape(3.dp))
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                onSectionSelect(sec)
                            }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = sec,
                            fontFamily = ChakraPetchFont,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 9.sp,
                            color = if (isSel) accentColor else TextSecondary,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // Volume Amplification Section (When VOLUME is selected)
        if (selectedSection == "VOLUME" && volumeState != null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(GunmetalDark)
                        .border(1.dp, BorderHardware, RoundedCornerShape(4.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "MASTER AMPLIFICATION // 300% VLC BOOST",
                                fontFamily = ChakraPetchFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = TextPrimary
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (volumeState.isMuted) "MUTED" else "${volumeState.amplificationPercent}%",
                                    fontFamily = ShareTechMonoFont,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = if (volumeState.isMuted) DangerRed else if (volumeState.isClipping) BurntOrange else AcidGreen
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                if (onToggleMute != null) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(if (volumeState.isMuted) DangerRed else GunmetalMedium)
                                            .clickable { onToggleMute() }
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (volumeState.isMuted) "UNMUTE" else "MUTE",
                                            fontFamily = ChakraPetchFont,
                                            fontSize = 9.sp,
                                            color = if (volumeState.isMuted) MatteBlack else TextPrimary
                                        )
                                    }
                                }
                            }
                        }

                        if (volumeState.isClipping) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "⚠ DIGITAL CLIPPING DETECTED — REDUCE PREAMP GAIN",
                                fontFamily = ShareTechMonoFont,
                                fontSize = 9.sp,
                                color = DangerRed
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Amplification Slider (up to 300% or 500%)
                        val maxAmp = if (volumeState.allowExperimentalCeiling) 500f else 300f
                        if (onSetVolume != null) {
                            Slider(
                                value = volumeState.amplificationPercent.toFloat(),
                                onValueChange = { onSetVolume(it.toInt()) },
                                valueRange = 0f..maxAmp,
                                colors = SliderDefaults.colors(
                                    thumbColor = if (volumeState.amplificationPercent > 100) BurntOrange else AcidGreen,
                                    activeTrackColor = if (volumeState.amplificationPercent > 100) BurntOrange else AcidGreen
                                )
                            )
                        }

                        // Preamp Gain Slider
                        if (onSetPreamp != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "DIGITAL PREAMP GAIN", fontFamily = ChakraPetchFont, fontSize = 10.sp, color = TextSecondary)
                                Text(text = String.format("%+.1f dB", volumeState.preampGainDb), fontFamily = ShareTechMonoFont, fontSize = 10.sp, color = BurntOrange)
                            }
                            Slider(
                                value = volumeState.preampGainDb,
                                onValueChange = { onSetPreamp(it) },
                                valueRange = -12f..18f,
                                colors = SliderDefaults.colors(thumbColor = BurntOrange, activeTrackColor = BurntOrange)
                            )
                        }

                        // Stereo Balance Slider
                        if (onSetBalance != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "STEREO BALANCE (PAN)", fontFamily = ChakraPetchFont, fontSize = 10.sp, color = TextSecondary)
                                val panLabel = when {
                                    volumeState.balancePan < -0.05f -> "L ${(-volumeState.balancePan * 100).toInt()}%"
                                    volumeState.balancePan > 0.05f -> "R ${(volumeState.balancePan * 100).toInt()}%"
                                    else -> "CENTER"
                                }
                                Text(text = panLabel, fontFamily = ShareTechMonoFont, fontSize = 10.sp, color = LcdCyan)
                            }
                            Slider(
                                value = volumeState.balancePan,
                                onValueChange = { onSetBalance(it) },
                                valueRange = -1.0f..1.0f,
                                colors = SliderDefaults.colors(thumbColor = LcdCyan, activeTrackColor = LcdCyan)
                            )
                        }

                        // Output Device Profiles & 500% Ceiling
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (onSetOutputProfile != null) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    OutputProfile.values().forEach { profile ->
                                        val isSel = profile == volumeState.outputProfile
                                        Box(
                                            modifier = Modifier
                                                .background(if (isSel) GunmetalLight else MatteBlack)
                                                .border(1.dp, if (isSel) accentColor else BorderHardware, RoundedCornerShape(2.dp))
                                                .clickable { onSetOutputProfile(profile) }
                                                .padding(horizontal = 6.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = profile.name.take(4),
                                                fontFamily = ChakraPetchFont,
                                                fontSize = 9.sp,
                                                color = if (isSel) accentColor else TextSecondary
                                            )
                                        }
                                    }
                                }
                            }

                            if (onToggleExperimentalCeiling != null) {
                                Box(
                                    modifier = Modifier
                                        .background(if (volumeState.allowExperimentalCeiling) BurntOrange else MatteBlack)
                                        .border(1.dp, BorderHardware, RoundedCornerShape(2.dp))
                                        .clickable { onToggleExperimentalCeiling() }
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = if (volumeState.allowExperimentalCeiling) "500% UNLOCKED" else "300% CEIL",
                                        fontFamily = ChakraPetchFont,
                                        fontSize = 9.sp,
                                        color = if (volumeState.allowExperimentalCeiling) MatteBlack else TextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Music Engine Operations (When ENGINE is selected)
        if (selectedSection == "ENGINE") {
            // Audio Diagnostics
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(GunmetalDark)
                        .border(1.dp, BorderHardware, RoundedCornerShape(4.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "DSP AUDIO DIAGNOSTICS & TELEMETRY",
                                fontFamily = ChakraPetchFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = AcidGreen
                            )
                            Text(
                                text = "REAL-TIME PIPELINE",
                                fontFamily = ShareTechMonoFont,
                                fontSize = 9.sp,
                                color = TextMuted
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        val diag = playbackState?.audioDiagnostics
                        val metrics = listOf(
                            "AUDIO CODEC" to (diag?.codec ?: "PCM / 16-Bit FLAC/MP3"),
                            "SAMPLE RATE" to "${diag?.sampleRateKhz ?: 44.1f} kHz",
                            "BIT DEPTH" to "${diag?.bitDepth ?: 16}-Bit Master",
                            "CHANNELS" to (diag?.channels ?: "Stereo 2.0 (L/R)"),
                            "OUTPUT ROUTE" to (diag?.activeOutput ?: "High Definition Audio Route"),
                            "GAIN MEMORY" to "${String.format("%+.1f", diag?.perSongGainOffsetDb ?: 0.0f)} dB"
                        )

                        metrics.forEach { (label, value) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = label,
                                    fontFamily = ShareTechMonoFont,
                                    fontSize = 9.sp,
                                    color = TextMuted
                                )
                                Text(
                                    text = value,
                                    fontFamily = ShareTechMonoFont,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
            }

            // AB Repeat Loop Control
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(GunmetalDark)
                        .border(1.dp, BorderHardware, RoundedCornerShape(4.dp))
                        .padding(12.dp)
                ) {
                    val ab = playbackState?.abRepeat
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "A-B REPEAT CONTINUOUS LOOP",
                                fontFamily = ChakraPetchFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = BurntOrange
                            )
                            Text(
                                text = if (ab?.isEnabled == true) "LOOP ENGAGED" else "DISENGAGED",
                                fontFamily = ShareTechMonoFont,
                                fontSize = 9.sp,
                                color = if (ab?.isEnabled == true) AcidGreen else TextMuted
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(if (ab?.pointA != null) GunmetalLight else MatteBlack)
                                    .border(1.dp, if (ab?.pointA != null) AcidGreen else BorderHardware, RoundedCornerShape(3.dp))
                                    .clickable {
                                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                        onSetAbPointA?.invoke()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = "MARK [A]", fontFamily = ChakraPetchFont, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = TextPrimary)
                                    val aTime = if (ab?.pointA != null) "${ab.pointA / 1000}s" else "--"
                                    Text(text = aTime, fontFamily = ShareTechMonoFont, fontSize = 8.sp, color = AcidGreen)
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(if (ab?.pointB != null) GunmetalLight else MatteBlack)
                                    .border(1.dp, if (ab?.pointB != null) AcidGreen else BorderHardware, RoundedCornerShape(3.dp))
                                    .clickable {
                                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                        onSetAbPointB?.invoke()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = "MARK [B]", fontFamily = ChakraPetchFont, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = TextPrimary)
                                    val bTime = if (ab?.pointB != null) "${ab.pointB / 1000}s" else "--"
                                    Text(text = bTime, fontFamily = ShareTechMonoFont, fontSize = 8.sp, color = AcidGreen)
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(GunmetalDark)
                                    .border(1.dp, DangerRed, RoundedCornerShape(3.dp))
                                    .clickable {
                                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                        onClearAb?.invoke()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "CLEAR LOOP", fontFamily = ChakraPetchFont, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = DangerRed)
                            }
                        }
                    }
                }
            }

            // Sleep Timer with 30s Gentle Fadeout
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(GunmetalDark)
                        .border(1.dp, BorderHardware, RoundedCornerShape(4.dp))
                        .padding(12.dp)
                ) {
                    val timer = playbackState?.sleepTimer
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SLEEP TIMER // 30s FADEOUT",
                                fontFamily = ChakraPetchFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = LcdCyan
                            )
                            val timerStatus = when {
                                timer?.fadeoutActive == true -> "FADING OUT NOW"
                                timer?.isRunning == true -> "${timer.remainingSeconds / 60}m ${timer.remainingSeconds % 60}s"
                                else -> "OFF"
                            }
                            Text(
                                text = timerStatus,
                                fontFamily = ShareTechMonoFont,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (timer?.isRunning == true) BurntOrange else TextMuted
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(15, 30, 45, 60).forEach { mins ->
                                val isTimerForMin = timer?.isRunning == true && timer.totalMinutes == mins
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(if (isTimerForMin) LcdCyan else MatteBlack)
                                    .border(1.dp, if (isTimerForMin) LcdCyan else BorderHardware, RoundedCornerShape(2.dp))
                                    .clickable {
                                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                        onStartSleepTimer?.invoke(mins)
                                    },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${mins}M",
                                        fontFamily = ChakraPetchFont,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (isTimerForMin) MatteBlack else TextPrimary
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(GunmetalDark)
                                    .border(1.dp, DangerRed, RoundedCornerShape(2.dp))
                                    .clickable {
                                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                        onCancelSleepTimer?.invoke()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "OFF",
                                    fontFamily = ChakraPetchFont,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = DangerRed
                                )
                            }
                        }
                    }
                }
            }

            // Gapless & Crossfade Duration Selector
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(GunmetalDark)
                        .border(1.dp, BorderHardware, RoundedCornerShape(4.dp))
                        .padding(12.dp)
                ) {
                    val currentCrossfade = playbackState?.crossfadeDurationSec ?: 0
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "GAPLESS PLAYBACK & CROSSFADE",
                                fontFamily = ChakraPetchFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = AcidGreen
                            )
                            Text(
                                text = if (currentCrossfade == 0) "GAPLESS DIRECT" else "${currentCrossfade}s BLEND",
                                fontFamily = ShareTechMonoFont,
                                fontSize = 9.sp,
                                color = BurntOrange
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val crossfadeOptions = listOf(0 to "0s (GAPLESS)", 2 to "2s", 4 to "4s", 8 to "8s", 12 to "12s")
                            crossfadeOptions.forEach { (sec, label) ->
                                val isSel = currentCrossfade == sec
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(if (isSel) AcidGreen else MatteBlack)
                                        .border(1.dp, if (isSel) BevelLight else BorderHardware, RoundedCornerShape(2.dp))
                                        .clickable {
                                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                            onSetCrossfade?.invoke(sec)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontFamily = ChakraPetchFont,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp,
                                        color = if (isSel) MatteBlack else TextPrimary,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Audio Patch Bay / Hardware Signal Flow
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GunmetalDark)
                    .border(1.dp, BorderHardware, RoundedCornerShape(4.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AUDIO PATCH BAY // SIGNAL ROUTING",
                            fontFamily = ChakraPetchFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "DIRECT 44.1kHz",
                            fontFamily = ShareTechMonoFont,
                            fontSize = 9.sp,
                            color = AcidGreen
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SignalNode(label = "INPUT", active = true, color = AcidGreen)
                        SignalFlowArrow()
                        SignalNode(label = "PREAMP", active = true, color = BurntOrange)
                        SignalFlowArrow()
                        SignalNode(label = "5-BAND EQ", active = eqState.isEnabled, color = if (eqState.isEnabled) accentColor else TextMuted)
                        SignalFlowArrow()
                        SignalNode(label = "DEGRADE", active = tapeDegradation.tapeHissEnabled || tapeDegradation.wowFlutterEnabled, color = BurntOrange)
                        SignalFlowArrow()
                        SignalNode(label = "OUT D/A", active = true, color = LcdCyan)
                    }
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

        // Tape Degradation Simulator (Hiss, Wow & Flutter, Magnetic Saturation)
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
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "TAPE DEGRADATION SIMULATOR",
                                fontFamily = ChakraPetchFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = BurntOrange
                            )
                            Text(
                                text = "ANALOG FLUTTER, MAGNETIC HISS & WARMTH",
                                fontFamily = ShareTechMonoFont,
                                fontSize = 9.sp,
                                color = TextMuted
                            )
                        }

                        Box(
                            modifier = Modifier
                                .background(if (tapeDegradation.tapeHissEnabled || tapeDegradation.wowFlutterEnabled) BurntOrange else MatteBlack)
                                .border(1.dp, BorderHardware, RoundedCornerShape(2.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (tapeDegradation.tapeHissEnabled || tapeDegradation.wowFlutterEnabled) "DEGRADE ACTIVE" else "CLEAN TAPE",
                                fontFamily = ChakraPetchFont,
                                fontSize = 9.sp,
                                color = if (tapeDegradation.tapeHissEnabled || tapeDegradation.wowFlutterEnabled) MatteBlack else TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Tape Hiss Toggle
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (tapeDegradation.tapeHissEnabled) GunmetalLight else MatteBlack)
                                .border(1.dp, if (tapeDegradation.tapeHissEnabled) BurntOrange else BorderHardware, RoundedCornerShape(3.dp))
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    onToggleTapeHiss()
                                }
                                .padding(horizontal = 8.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = "TAPE HISS", fontFamily = ChakraPetchFont, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = TextPrimary)
                                    Text(text = "TYPE-I FERRIC", fontFamily = ShareTechMonoFont, fontSize = 8.sp, color = TextMuted)
                                }
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (tapeDegradation.tapeHissEnabled) BurntOrange else GunmetalMedium)
                                )
                            }
                        }

                        // Wow & Flutter Toggle
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (tapeDegradation.wowFlutterEnabled) GunmetalLight else MatteBlack)
                                .border(1.dp, if (tapeDegradation.wowFlutterEnabled) AcidGreen else BorderHardware, RoundedCornerShape(3.dp))
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    onToggleWowFlutter()
                                }
                                .padding(horizontal = 8.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = "WOW & FLUTTER", fontFamily = ChakraPetchFont, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = TextPrimary)
                                    Text(text = "REEL WOBBLE", fontFamily = ShareTechMonoFont, fontSize = 8.sp, color = TextMuted)
                                }
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (tapeDegradation.wowFlutterEnabled) AcidGreen else GunmetalMedium)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Magnetic Saturation Warmth Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "TAPE SATURATION WARMTH", fontFamily = ChakraPetchFont, fontSize = 10.sp, color = TextSecondary)
                            Text(text = "${(tapeDegradation.saturationWarmth * 100).toInt()}%", fontFamily = ShareTechMonoFont, fontSize = 10.sp, color = BurntOrange)
                        }
                        Slider(
                            value = tapeDegradation.saturationWarmth,
                            onValueChange = { onSetSaturationWarmth(it) },
                            valueRange = 0.0f..1.0f,
                            colors = SliderDefaults.colors(thumbColor = BurntOrange, activeTrackColor = BurntOrange)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SignalNode(label: String, active: Boolean, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(if (active) color else GunmetalMedium)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontFamily = ShareTechMonoFont,
            fontSize = 8.sp,
            color = if (active) color else TextMuted
        )
    }
}

@Composable
private fun SignalFlowArrow() {
    Text(
        text = "→",
        fontFamily = ShareTechMonoFont,
        fontSize = 10.sp,
        color = TextMuted
    )
}

