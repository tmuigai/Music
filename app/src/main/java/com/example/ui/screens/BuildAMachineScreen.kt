package com.example.ui.screens

import android.view.HapticFeedbackConstants
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ReplayViewModel
import com.example.ui.components.HardwarePushSwitch
import com.example.ui.components.HardwareSlider
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

@Composable
fun BuildAMachineScreen(
    viewModel: ReplayViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateBack()
    }

    val slots by viewModel.customMachineSlots.collectAsState()
    val playbackState by viewModel.audioEngine.playbackState.collectAsState()
    val djState by viewModel.audioEngine.djState.collectAsState()
    val eqState by viewModel.audioEngine.effectsManager.eqState.collectAsState()
    val masterVol by viewModel.audioEngine.effectsManager.volumeState.collectAsState()
    val view = LocalView.current

    var activeBayIndex by remember { mutableIntStateOf(0) }

    val bay1Modules = remember {
        listOf(
            "CASSETTE DECK" to "TAPE",
            "PORTABLE CD" to "CD",
            "TURNTABLE VINYL" to "VINYL",
            "MINIDISC DECK" to "MD"
        )
    }

    val bay2Modules = remember {
        listOf(
            "10-BAND EQ" to "EQ",
            "PARAMETRIC FILTER" to "PARAM",
            "TAPE SATURATION" to "SAT",
            "300% PREAMP" to "AMP"
        )
    }

    val bay3Modules = remember {
        listOf(
            "CROSSFADER" to "DJ",
            "MPC SAMPLER" to "PADS",
            "4-TRK FADER" to "4TRK",
            "STEREO VU METERS" to "VU"
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MatteBlack)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Machine Workshop Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GunmetalDark)
                    .border(1.dp, BorderHardware)
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(BurntOrange)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "MODULAR HARDWARE WORKSHOP",
                            fontFamily = ChakraPetchFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = TextPrimary
                        )
                    }
                    Text(
                        text = "BAY 1999 // CUSTOM RACK",
                        fontFamily = ShareTechMonoFont,
                        fontSize = 10.sp,
                        color = AcidGreen
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Assemble, mount, and customize your own hybrid music machine. Mix vintage cassette transports, graphic equalizers, and DJ crossfaders into a single unified console.",
                    fontFamily = ShareTechMonoFont,
                    fontSize = 10.sp,
                    color = TextSecondary
                )
            }
        }

        // 3-Bay Hardware Chassis Rack
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GunmetalDark)
                    .border(1.5.dp, BorderHardware, RoundedCornerShape(4.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "HARDWARE EXPANSION CHASSIS",
                    fontFamily = ChakraPetchFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = BurntOrange
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    (0..2).forEach { bayIdx ->
                        val slot = slots.find { it.slotIndex == bayIdx }
                        val isBaySelected = activeBayIndex == bayIdx
                        val bayTitle = when (bayIdx) {
                            0 -> "BAY 1: SOURCE"
                            1 -> "BAY 2: PROCESSOR"
                            else -> "BAY 3: OUTPUT"
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (isBaySelected) Color(0xFF1E281E) else MatteBlack)
                                .border(1.5.dp, if (isBaySelected) AcidGreen else BorderHardware, RoundedCornerShape(3.dp))
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    activeBayIndex = bayIdx
                                }
                                .padding(8.dp)
                                .testTag("custom_bay_$bayIdx")
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = bayTitle,
                                    fontFamily = ShareTechMonoFont,
                                    fontSize = 8.sp,
                                    color = if (isBaySelected) AcidGreen else TextMuted
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = slot?.moduleName ?: "EMPTY",
                                    fontFamily = ChakraPetchFont,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = TextPrimary,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .background(GunmetalMedium)
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = slot?.icon ?: "NONE",
                                        fontFamily = ShareTechMonoFont,
                                        fontSize = 8.sp,
                                        color = BurntOrange
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Active Bay Module Selector Drawer
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GunmetalDark)
                    .border(1.dp, BorderHardware)
                    .padding(12.dp)
            ) {
                Text(
                    text = "AVAILABLE MODULES FOR BAY ${activeBayIndex + 1}",
                    fontFamily = ChakraPetchFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = LcdCyan
                )

                Spacer(modifier = Modifier.height(8.dp))

                val availableModules = when (activeBayIndex) {
                    0 -> bay1Modules
                    1 -> bay2Modules
                    else -> bay3Modules
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    availableModules.forEach { (name, icon) ->
                        val currentSlot = slots.find { it.slotIndex == activeBayIndex }
                        val isMounted = currentSlot?.moduleName == name

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (isMounted) ElectricBlue else GunmetalLight)
                                .border(1.dp, if (isMounted) LcdCyan else BorderHardware)
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    viewModel.updateMachineSlot(activeBayIndex, name, icon)
                                }
                                .padding(vertical = 10.dp, horizontal = 4.dp)
                                .testTag("mount_module_${icon.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = icon,
                                    fontFamily = ShareTechMonoFont,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isMounted) MatteBlack else BurntOrange
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = name,
                                    fontFamily = ChakraPetchFont,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isMounted) MatteBlack else TextPrimary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        // Live Interactive Control Deck for the Custom Machine
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GunmetalDark)
                    .border(1.5.dp, BorderHardware, RoundedCornerShape(4.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "LIVE MACHINE CONTROL SURFACE",
                    fontFamily = ChakraPetchFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = AcidGreen
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Transport Controls (Play / Stop / Next)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HardwarePushSwitch(
                        label = if (playbackState.isPlaying) "PAUSE" else "PLAY",
                        code = "TRANSPORT",
                        isActive = playbackState.isPlaying,
                        activeColor = AcidGreen,
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            viewModel.togglePlayPause()
                        },
                        modifier = Modifier.weight(1f).testTag("custom_machine_play")
                    )

                    HardwarePushSwitch(
                        label = "STOP",
                        code = "STOP",
                        isActive = false,
                        activeColor = DangerRed,
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            viewModel.stopPlayback()
                        },
                        modifier = Modifier.weight(1f).testTag("custom_machine_stop")
                    )

                    HardwarePushSwitch(
                        label = "NEXT TRACK",
                        code = "SKIP",
                        isActive = false,
                        activeColor = BurntOrange,
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            viewModel.nextTrack()
                        },
                        modifier = Modifier.weight(1f).testTag("custom_machine_next")
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Live DJ Crossfader or Amp Slider
                Text(
                    text = "EQUIPMENT FADER / GAIN LEVEL",
                    fontFamily = ShareTechMonoFont,
                    fontSize = 9.sp,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                HardwareSlider(
                    value = (djState.crossfader + 1.0f) / 2.0f,
                    onValueChange = { norm ->
                        val cf = (norm * 2.0f) - 1.0f
                        viewModel.audioEngine.setDjCrossfader(cf)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    activeColor = AcidGreen
                )

                Spacer(modifier = Modifier.height(12.dp))

                // MPC Sampler Sound FX Triggers
                Text(
                    text = "INTEGRATED MPC SAMPLER TRIGGERS",
                    fontFamily = ShareTechMonoFont,
                    fontSize = 9.sp,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("AIRHORN", "VINYL_BRAKE", "TR808_SNARE", "TAPE_REWIND").forEach { snd ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(GunmetalLight)
                                .border(1.dp, BurntOrange, RoundedCornerShape(3.dp))
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    viewModel.triggerSamplerSound(snd)
                                }
                                .padding(4.dp)
                                .testTag("sampler_btn_${snd.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = snd.replace("_", " "),
                                fontFamily = ChakraPetchFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                color = TextPrimary,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
