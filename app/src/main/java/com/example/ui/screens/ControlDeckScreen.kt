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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.HardwareNavScreen
import com.example.model.PlaybackState
import com.example.model.TapeSide
import com.example.model.Track
import com.example.ui.components.InteractiveCassetteDeck
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

@Composable
fun ControlDeckScreen(
    playbackState: PlaybackState,
    tracks: List<Track>,
    favoriteTracks: List<Track>,
    mostPlayedTracks: List<Track>,
    vuMeterLeft: Float,
    vuMeterRight: Float,
    accentColor: Color,
    onTrackSelect: (Track) -> Unit,
    onNavigate: (HardwareNavScreen) -> Unit,
    onFlipCassetteSide: () -> Unit,
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
                    title = currentTrack?.title ?: "REPLAY 99 MASTER TAPE",
                    artist = currentTrack?.artist ?: "UNDERGROUND SYSTEM",
                    currentSide = TapeSide.SIDE_A,
                    isPlaying = playbackState.isPlaying,
                    progress = playbackState.tapeReelProgress,
                    tapeCounter = playbackState.tapeCounter,
                    onFlipSide = onFlipCassetteSide
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

        // Quick Hardware Jump Rack (Virtual Room, Recording Booth, FM Radio, Obsession, Diary)
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
                    // Collection Room
                    BayQuickButton(
                        title = "COLLECTION ROOM",
                        code = "BAY 09",
                        color = BurntOrange,
                        modifier = Modifier.weight(1f)
                    ) {
                        onNavigate(HardwareNavScreen.VIRTUAL_ROOM)
                    }

                    // Recording Booth
                    BayQuickButton(
                        title = "RECORD BOOTH",
                        code = "BAY 10",
                        color = AcidGreen,
                        modifier = Modifier.weight(1f)
                    ) {
                        onNavigate(HardwareNavScreen.RECORDING_BOOTH)
                    }

                    // FM Tuner
                    BayQuickButton(
                        title = "FM TUNER",
                        code = "BAY 11",
                        color = ElectricBlue,
                        modifier = Modifier.weight(1f)
                    ) {
                        onNavigate(HardwareNavScreen.RADIO_MODE)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Obsession Mode
                    BayQuickButton(
                        title = "OBSESSION SHRINE",
                        code = "BAY 13",
                        color = BurntOrange,
                        modifier = Modifier.weight(1f)
                    ) {
                        onNavigate(HardwareNavScreen.OBSESSION_MODE)
                    }

                    // Music Diary & Stats
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

        // Most Played & Underground Favorites
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "HEAVY ROTATION // MOST PLAYED",
                        fontFamily = ChakraPetchFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "${mostPlayedTracks.size} CUTS",
                        fontFamily = ShareTechMonoFont,
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(mostPlayedTracks.ifEmpty { tracks.take(5) }) { track ->
                        CrateTrackCard(
                            track = track,
                            isPlaying = currentTrack?.id == track.id && playbackState.isPlaying,
                            accentColor = accentColor,
                            onClick = { onTrackSelect(track) }
                        )
                    }
                }
            }
        }

        // Active Queue & Library Crate
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MASTER LIBRARY // VAULT",
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

@Composable
fun BayQuickButton(
    title: String,
    code: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val view = LocalView.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(GunmetalDark)
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            .clickable {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onClick()
            }
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Column {
            Text(text = code, fontFamily = ShareTechMonoFont, fontSize = 9.sp, color = color)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                fontFamily = ChakraPetchFont,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun CrateTrackCard(
    track: Track,
    isPlaying: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    val view = LocalView.current
    Box(
        modifier = Modifier
            .width(135.dp)
            .height(105.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(GunmetalDark)
            .border(1.dp, if (isPlaying) accentColor else BorderHardware, RoundedCornerShape(4.dp))
            .clickable {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onClick()
            }
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isPlaying) "PLAYING" else "C-60",
                    fontFamily = ShareTechMonoFont,
                    fontSize = 9.sp,
                    color = if (isPlaying) accentColor else TextMuted
                )
                Text(
                    text = "${track.replayCount}x",
                    fontFamily = ShareTechMonoFont,
                    fontSize = 9.sp,
                    color = BurntOrange
                )
            }

            Column {
                Text(
                    text = track.title,
                    fontFamily = ChakraPetchFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = track.artist,
                    fontFamily = ShareTechMonoFont,
                    fontSize = 10.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun LibraryTrackRow(
    index: Int,
    track: Track,
    isCurrent: Boolean,
    isPlaying: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(3.dp))
            .background(if (isCurrent) GunmetalMedium else GunmetalDark)
            .border(1.dp, if (isCurrent) accentColor else BorderHardware, RoundedCornerShape(3.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = String.format("%02d", index),
                fontFamily = ShareTechMonoFont,
                fontSize = 11.sp,
                color = if (isCurrent) accentColor else TextMuted,
                modifier = Modifier.width(26.dp)
            )

            Column {
                Text(
                    text = track.title,
                    fontFamily = ChakraPetchFont,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 13.sp,
                    color = if (isCurrent) TextPrimary else TextPrimary.copy(alpha = 0.9f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${track.artist} // ${track.genre}",
                    fontFamily = ShareTechMonoFont,
                    fontSize = 10.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Duration or Play indicator
        Text(
            text = if (isPlaying) "► PLAY" else String.format("%02d:%02d", (track.durationMs / 60000), (track.durationMs % 60000) / 1000),
            fontFamily = ShareTechMonoFont,
            fontSize = 11.sp,
            color = if (isCurrent) accentColor else TextMuted
        )
    }
}
