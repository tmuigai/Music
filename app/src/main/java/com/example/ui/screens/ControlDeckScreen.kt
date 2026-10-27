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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TrackBookmarkEntity
import com.example.model.HardwareNavScreen
import com.example.model.MasterVolumeState
import com.example.model.OutputProfile
import com.example.model.PlaybackState
import com.example.model.TapeSide
import com.example.model.Track
import com.example.ui.components.BayQuickButton
import com.example.ui.components.InteractiveCassetteDeck
import com.example.ui.components.LibraryTrackRow
import com.example.ui.components.StereoVuMeters
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

@Composable
fun ControlDeckScreen(
    playbackState: PlaybackState,
    tracks: List<Track>,
    favoriteTracks: List<Track>,
    mostPlayedTracks: List<Track>,
    bookmarks: List<TrackBookmarkEntity>,
    volumeState: MasterVolumeState,
    vuMeterLeft: Float,
    vuMeterRight: Float,
    accentColor: Color,
    onTrackSelect: (Track) -> Unit,
    onNavigate: (HardwareNavScreen) -> Unit,
    onFlipCassetteSide: () -> Unit,
    onSetVolume: (Int) -> Unit,
    onSetPreamp: (Float) -> Unit,
    onSetBalance: (Float) -> Unit,
    onToggleMute: () -> Unit,
    onSetOutputProfile: (OutputProfile) -> Unit,
    onAddBookmark: (String) -> Unit,
    onJumpToBookmark: (Long) -> Unit,
    onRescanLibrary: () -> Unit,
    onTriggerFilePicker: () -> Unit,
    onEjectCassette: () -> Unit = {},
    onInsertCassette: () -> Unit = {},
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
        // Master Cassette Deck / Active Player Rack
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DECK A // REEL TRANSPORT",
                        fontFamily = ChakraPetchFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = accentColor,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = if (playbackState.isPlaying) "RUNNING [4.76 CM/S]" else "STOPPED",
                        fontFamily = ShareTechMonoFont,
                        fontSize = 10.sp,
                        color = if (playbackState.isPlaying) AcidGreen else TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                InteractiveCassetteDeck(
                    title = currentTrack?.title ?: "REPLAY 99 CONTROL DECK",
                    artist = currentTrack?.artist ?: "SELECT TRACK BELOW",
                    currentSide = TapeSide.SIDE_A,
                    isPlaying = playbackState.isPlaying,
                    progress = playbackState.tapeReelProgress,
                    tapeCounter = playbackState.tapeCounter,
                    isInserted = playbackState.isCassetteInserted,
                    onFlipSide = onFlipCassetteSide,
                    onEjectClick = onEjectCassette,
                    onInsertClick = onInsertCassette
                )
            }
        }

        // Stereo VU Meter Section
        item {
            StereoVuMeters(
                leftLevel = vuMeterLeft,
                rightLevel = vuMeterRight,
                accentColor = accentColor
            )
        }

        // Complete Volume System (VLC-Style Amplification up to 300% & Preamp)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GunmetalDark)
                    .border(1.dp, if (volumeState.isClipping) DangerRed else BorderHardware, RoundedCornerShape(4.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (volumeState.isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                                contentDescription = "Volume",
                                tint = if (volumeState.isMuted) DangerRed else accentColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "MASTER AMPLIFICATION",
                                fontFamily = ChakraPetchFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = TextPrimary
                            )
                        }

                        // Amplification Readout & Mute Toggle
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (volumeState.isMuted) "MUTED" else "${volumeState.amplificationPercent}%",
                                fontFamily = ShareTechMonoFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = if (volumeState.isMuted) DangerRed else if (volumeState.isClipping) BurntOrange else AcidGreen
                            )
                            Spacer(modifier = Modifier.width(8.dp))
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

                    // Amplification Slider
                    val maxSlider = if (volumeState.allowExperimentalCeiling) 500f else 300f
                    Slider(
                        value = volumeState.amplificationPercent.toFloat(),
                        onValueChange = { onSetVolume(it.toInt()) },
                        valueRange = 0f..maxSlider,
                        colors = SliderDefaults.colors(
                            thumbColor = if (volumeState.isClipping) DangerRed else accentColor,
                            activeTrackColor = accentColor
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "0%", fontFamily = ShareTechMonoFont, fontSize = 8.sp, color = TextMuted)
                        Text(text = "100% NOMINAL", fontFamily = ShareTechMonoFont, fontSize = 8.sp, color = AcidGreen)
                        Text(text = "200% BOOST", fontFamily = ShareTechMonoFont, fontSize = 8.sp, color = BurntOrange)
                        Text(text = "300% MAX", fontFamily = ShareTechMonoFont, fontSize = 8.sp, color = DangerRed)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Preamp Gain & Stereo Balance
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Preamp
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "PREAMP GAIN", fontFamily = ShareTechMonoFont, fontSize = 9.sp, color = TextMuted)
                                Text(text = String.format("%+.1fdB", volumeState.preampGainDb), fontFamily = ShareTechMonoFont, fontSize = 9.sp, color = BurntOrange)
                            }
                            Slider(
                                value = volumeState.preampGainDb,
                                onValueChange = { onSetPreamp(it) },
                                valueRange = -12f..18f,
                                colors = SliderDefaults.colors(thumbColor = BurntOrange, activeTrackColor = BurntOrange)
                            )
                        }

                        // Pan Balance
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "L/R BALANCE", fontFamily = ShareTechMonoFont, fontSize = 9.sp, color = TextMuted)
                                Text(text = String.format("%+.1f", volumeState.balancePan), fontFamily = ShareTechMonoFont, fontSize = 9.sp, color = LcdCyan)
                            }
                            Slider(
                                value = volumeState.balancePan,
                                onValueChange = { onSetBalance(it) },
                                valueRange = -1.0f..1.0f,
                                colors = SliderDefaults.colors(thumbColor = LcdCyan, activeTrackColor = LcdCyan)
                            )
                        }
                    }

                    // Output Profile Switcher
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutputProfile.values().forEach { profile ->
                            val isSel = volumeState.outputProfile == profile
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(if (isSel) GunmetalLight else MatteBlack)
                                    .border(1.dp, if (isSel) accentColor else BorderHardware, RoundedCornerShape(2.dp))
                                    .clickable { onSetOutputProfile(profile) }
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = profile.label.split(" ").first(),
                                    fontFamily = ChakraPetchFont,
                                    fontSize = 9.sp,
                                    color = if (isSel) accentColor else TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Audio Time Machine (Exact Timestamp Bookmarks)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GunmetalDark)
                    .border(1.dp, BorderHardware, RoundedCornerShape(4.dp))
                    .padding(10.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AUDIO TIME MACHINE // BOOKMARKS",
                            fontFamily = ChakraPetchFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = LcdCyan
                        )

                        if (currentTrack != null) {
                            Box(
                                modifier = Modifier
                                    .background(LcdCyan)
                                    .clickable {
                                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                        val sec = (playbackState.positionMs / 1000)
                                        onAddBookmark("MOMENT @ ${sec}s")
                                    }
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(text = "+ PIN MOMENT", fontFamily = ChakraPetchFont, fontWeight = FontWeight.Bold, fontSize = 9.sp, color = MatteBlack)
                            }
                        }
                    }

                    if (bookmarks.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(bookmarks) { b ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(GunmetalMedium)
                                        .border(1.dp, LcdCyan.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
                                        .clickable { onJumpToBookmark(b.timestampMs) }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${b.label} (${b.timestampMs / 1000}s)",
                                        fontFamily = ShareTechMonoFont,
                                        fontSize = 9.sp,
                                        color = LcdCyan
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Quick Hardware Expansion Bays (including MINIDISC & BOOMBOX!)
        item {
            Column {
                Text(
                    text = "HARDWARE EXPANSION BAYS",
                    fontFamily = ChakraPetchFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BayQuickButton(
                        title = "MINIDISC MD-99",
                        code = "BAY 15",
                        color = ElectricBlue,
                        modifier = Modifier.weight(1f)
                    ) {
                        onNavigate(HardwareNavScreen.MINIDISC)
                    }

                    BayQuickButton(
                        title = "GHETTOBLASTER",
                        code = "BAY 16",
                        color = BurntOrange,
                        modifier = Modifier.weight(1f)
                    ) {
                        onNavigate(HardwareNavScreen.BOOMBOX)
                    }

                    BayQuickButton(
                        title = "COLLECTION ROOM",
                        code = "BAY 09",
                        color = AcidGreen,
                        modifier = Modifier.weight(1f)
                    ) {
                        onNavigate(HardwareNavScreen.VIRTUAL_ROOM)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BayQuickButton(
                        title = "RECORD BOOTH",
                        code = "BAY 10",
                        color = DangerRed,
                        modifier = Modifier.weight(1f)
                    ) {
                        onNavigate(HardwareNavScreen.RECORDING_BOOTH)
                    }

                    BayQuickButton(
                        title = "FM TUNER",
                        code = "BAY 11",
                        color = ElectricBlue,
                        modifier = Modifier.weight(1f)
                    ) {
                        onNavigate(HardwareNavScreen.RADIO_MODE)
                    }

                    BayQuickButton(
                        title = "LISTENING DIARY",
                        code = "BAY 14",
                        color = LcdCyan,
                        modifier = Modifier.weight(1f)
                    ) {
                        onNavigate(HardwareNavScreen.MUSIC_DIARY)
                    }
                }
            }
        }

        // Master Library Crate & Real Music Actions
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "REAL MUSIC LIBRARY // LOCAL VAULT",
                        fontFamily = ChakraPetchFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "${tracks.size} TRACKS",
                        fontFamily = ShareTechMonoFont,
                        fontSize = 10.sp,
                        color = accentColor
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (tracks.isEmpty()) {
                    // Clean real empty state
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(GunmetalDark)
                            .border(1.dp, BorderHardware, RoundedCornerShape(6.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "NO LOCAL AUDIO FILES FOUND",
                                fontFamily = ChakraPetchFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "REPLAY 99 only plays real music from device storage.",
                                fontFamily = ShareTechMonoFont,
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = onRescanLibrary,
                                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Scan", tint = MatteBlack, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "SCAN STORAGE", fontFamily = ChakraPetchFont, color = MatteBlack, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = onTriggerFilePicker,
                                    colors = ButtonDefaults.buttonColors(containerColor = BurntOrange)
                                ) {
                                    Icon(Icons.Default.FileOpen, contentDescription = "Import", tint = MatteBlack, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "IMPORT FILES", fontFamily = ChakraPetchFont, color = MatteBlack, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        tracks.forEachIndexed { index, track ->
                            val isCurrent = currentTrack?.id == track.id
                            LibraryTrackRow(
                                index = index + 1,
                                track = track,
                                isCurrent = isCurrent,
                                isPlaying = isCurrent && playbackState.isPlaying,
                                accentColor = accentColor,
                                onClick = {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    onTrackSelect(track)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
