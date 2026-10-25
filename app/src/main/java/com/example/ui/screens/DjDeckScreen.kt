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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DjDeckState
import com.example.model.Track
import com.example.ui.components.DjCrossfader
import com.example.ui.components.VinylPlatter
import com.example.ui.theme.AcidGreen
import com.example.ui.theme.BorderHardware
import com.example.ui.theme.BurntOrange
import com.example.ui.theme.ChakraPetchFont
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.GunmetalDark
import com.example.ui.theme.GunmetalLight
import com.example.ui.theme.GunmetalMedium
import com.example.ui.theme.MatteBlack
import com.example.ui.theme.ShareTechMonoFont
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun DjDeckScreen(
    djState: DjDeckState,
    allTracks: List<Track>,
    accentColor: Color,
    onTogglePlayA: () -> Unit,
    onTogglePlayB: () -> Unit,
    onCrossfaderChange: (Float) -> Unit,
    onPitchAChange: (Float) -> Unit,
    onPitchBChange: (Float) -> Unit,
    onScratchA: (Float) -> Unit,
    onScratchB: (Float) -> Unit,
    onSetEq: (Boolean, String, Float) -> Unit,
    onSelectTrackA: (Track) -> Unit,
    onSelectTrackB: (Track) -> Unit,
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
        // DJ Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DUAL TURNTABLE // DJ MIXER 99",
                    fontFamily = ChakraPetchFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = accentColor,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "BPM SYNC / DUAL DECK",
                    fontFamily = ShareTechMonoFont,
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }
        }

        // Turntables Side by Side (Deck A & Deck B)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Deck A
                Column(modifier = Modifier.weight(1f)) {
                    VinylPlatter(
                        deckName = "DECK A",
                        track = djState.deckA.track,
                        isPlaying = djState.deckA.isPlaying,
                        platterRotationDeg = djState.deckA.platterRotationDeg,
                        pitchPercent = djState.deckA.pitchPercent,
                        accentColor = AcidGreen,
                        onScratch = onScratchA
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (djState.deckA.isPlaying) AcidGreen else GunmetalDark)
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                onTogglePlayA()
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (djState.deckA.isPlaying) "■ STOP A" else "► START A",
                            fontFamily = ChakraPetchFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (djState.deckA.isPlaying) MatteBlack else TextPrimary
                        )
                    }
                }

                // Deck B
                Column(modifier = Modifier.weight(1f)) {
                    VinylPlatter(
                        deckName = "DECK B",
                        track = djState.deckB.track,
                        isPlaying = djState.deckB.isPlaying,
                        platterRotationDeg = djState.deckB.platterRotationDeg,
                        pitchPercent = djState.deckB.pitchPercent,
                        accentColor = ElectricBlue,
                        onScratch = onScratchB
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (djState.deckB.isPlaying) ElectricBlue else GunmetalDark)
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                onTogglePlayB()
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (djState.deckB.isPlaying) "■ STOP B" else "► START B",
                            fontFamily = ChakraPetchFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (djState.deckB.isPlaying) MatteBlack else TextPrimary
                        )
                    }
                }
            }
        }

        // Crossfader
        item {
            DjCrossfader(
                crossfader = djState.crossfader,
                onCrossfaderChange = onCrossfaderChange
            )
        }

        // Pitch Faders
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
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Pitch Deck A
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "PITCH DECK A", fontFamily = ChakraPetchFont, fontSize = 10.sp, color = AcidGreen)
                        Slider(
                            value = djState.deckA.pitchPercent,
                            onValueChange = onPitchAChange,
                            valueRange = -16f..16f,
                            colors = SliderDefaults.colors(thumbColor = AcidGreen, activeTrackColor = AcidGreen)
                        )
                    }

                    // Pitch Deck B
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "PITCH DECK B", fontFamily = ChakraPetchFont, fontSize = 10.sp, color = ElectricBlue)
                        Slider(
                            value = djState.deckB.pitchPercent,
                            onValueChange = onPitchBChange,
                            valueRange = -16f..16f,
                            colors = SliderDefaults.colors(thumbColor = ElectricBlue, activeTrackColor = ElectricBlue)
                        )
                    }
                }
            }
        }

        // 3-Band Equalizer Section
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GunmetalDark)
                    .border(1.dp, BorderHardware, RoundedCornerShape(4.dp))
                    .padding(10.dp)
            ) {
                Column {
                    Text(text = "3-BAND ISOLATOR EQ", fontFamily = ChakraPetchFont, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = BurntOrange)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Deck A EQ
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "LOW A", fontFamily = ShareTechMonoFont, fontSize = 9.sp, color = TextMuted)
                            Slider(value = djState.eqLowA, onValueChange = { onSetEq(true, "low", it) }, valueRange = -12f..12f)
                            Text(text = "MID A", fontFamily = ShareTechMonoFont, fontSize = 9.sp, color = TextMuted)
                            Slider(value = djState.eqMidA, onValueChange = { onSetEq(true, "mid", it) }, valueRange = -12f..12f)
                            Text(text = "HIGH A", fontFamily = ShareTechMonoFont, fontSize = 9.sp, color = TextMuted)
                            Slider(value = djState.eqHighA, onValueChange = { onSetEq(true, "high", it) }, valueRange = -12f..12f)
                        }

                        // Deck B EQ
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "LOW B", fontFamily = ShareTechMonoFont, fontSize = 9.sp, color = TextMuted)
                            Slider(value = djState.eqLowB, onValueChange = { onSetEq(false, "low", it) }, valueRange = -12f..12f)
                            Text(text = "MID B", fontFamily = ShareTechMonoFont, fontSize = 9.sp, color = TextMuted)
                            Slider(value = djState.eqMidB, onValueChange = { onSetEq(false, "mid", it) }, valueRange = -12f..12f)
                            Text(text = "HIGH B", fontFamily = ShareTechMonoFont, fontSize = 9.sp, color = TextMuted)
                            Slider(value = djState.eqHighB, onValueChange = { onSetEq(false, "high", it) }, valueRange = -12f..12f)
                        }
                    }
                }
            }
        }

        // Crate Quick Loader for Deck A and B
        item {
            Column {
                Text(
                    text = "CRATE DIGGER // LOAD TRACKS",
                    fontFamily = ChakraPetchFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    allTracks.take(6).forEach { track ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(2.dp))
                                .background(GunmetalDark)
                                .border(1.dp, BorderHardware, RoundedCornerShape(2.dp))
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = track.title, fontFamily = ChakraPetchFont, fontSize = 11.sp, color = TextPrimary)
                                Text(text = track.artist, fontFamily = ShareTechMonoFont, fontSize = 9.sp, color = TextSecondary)
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier
                                        .background(GunmetalMedium)
                                        .border(1.dp, AcidGreen, RoundedCornerShape(2.dp))
                                        .clickable { onSelectTrackA(track) }
                                        .padding(horizontal = 6.dp, vertical = 4.dp)
                                ) {
                                    Text(text = "LOAD A", fontFamily = ChakraPetchFont, fontSize = 9.sp, color = AcidGreen)
                                }

                                Box(
                                    modifier = Modifier
                                        .background(GunmetalMedium)
                                        .border(1.dp, ElectricBlue, RoundedCornerShape(2.dp))
                                        .clickable { onSelectTrackB(track) }
                                        .padding(horizontal = 6.dp, vertical = 4.dp)
                                ) {
                                    Text(text = "LOAD B", fontFamily = ChakraPetchFont, fontSize = 9.sp, color = ElectricBlue)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
