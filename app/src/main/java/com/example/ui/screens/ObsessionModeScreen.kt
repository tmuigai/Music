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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ObsessionProfileEntity
import com.example.model.PlaybackState
import com.example.model.Track
import com.example.ui.theme.AcidGreen
import com.example.ui.theme.BorderHardware
import com.example.ui.theme.BurntOrange
import com.example.ui.theme.ChakraPetchFont
import com.example.ui.theme.GunmetalDark
import com.example.ui.theme.GunmetalLight
import com.example.ui.theme.GunmetalMedium
import com.example.ui.theme.MatteBlack
import com.example.ui.theme.ShareTechMonoFont
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ObsessionModeScreen(
    currentTrack: Track?,
    obsessions: List<ObsessionProfileEntity>,
    playbackState: PlaybackState,
    accentColor: Color,
    onCreateObsession: (Long, String, String, Int) -> Unit,
    onTrackSelect: (Track) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    var showCreateDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MatteBlack),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Obsession Mode Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "OBSESSION MODE // SHRINE",
                        fontFamily = ChakraPetchFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = BurntOrange,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "DEDICATED REPEAT SANCTUARY FOR YOUR OBSESSIONS",
                        fontFamily = ShareTechMonoFont,
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }

                if (currentTrack != null) {
                    Box(
                        modifier = Modifier
                            .background(BurntOrange)
                            .border(1.dp, MatteBlack, RoundedCornerShape(2.dp))
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                showCreateDialog = true
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = "+ CONSECRATE", fontFamily = ChakraPetchFont, fontWeight = FontWeight.Bold, fontSize = 9.sp, color = MatteBlack)
                    }
                }
            }
        }

        // Active Obsession Shrine Card
        item {
            val target = 99
            val count = currentTrack?.replayCount ?: 1
            val progress = (count.toFloat() / target).coerceIn(0f, 1f)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GunmetalDark)
                    .border(2.dp, BurntOrange, RoundedCornerShape(6.dp))
                    .padding(14.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = currentTrack?.title ?: "NO OBSESSION SELECTED",
                        fontFamily = ChakraPetchFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = currentTrack?.artist ?: "SELECT A TRACK FROM THE CRATE",
                        fontFamily = ShareTechMonoFont,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Replay Target Counter
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "OBSESSION REPLAY COUNTER", fontFamily = ShareTechMonoFont, fontSize = 9.sp, color = TextMuted)
                        Text(
                            text = "$count / $target PLAYS",
                            fontFamily = ShareTechMonoFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = BurntOrange
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Progress bar toward 99 replays
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .background(GunmetalMedium)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress)
                                .fillMaxSize()
                                .background(BurntOrange)
                        )
                    }
                }
            }
        }

        // Existing Obsession Shrines List
        item {
            Text(
                text = "SAVED OBSESSION PROFILES (${obsessions.size})",
                fontFamily = ChakraPetchFont,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        items(obsessions) { ob ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(3.dp))
                    .background(GunmetalDark)
                    .border(1.dp, BorderHardware, RoundedCornerShape(3.dp))
                    .padding(10.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = ob.shrineTitle, fontFamily = ChakraPetchFont, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextPrimary)
                        Text(text = "${ob.currentReplays} / ${ob.replayTarget} PLAYS", fontFamily = ShareTechMonoFont, fontSize = 10.sp, color = BurntOrange)
                    }
                    if (ob.personalNotes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = ob.personalNotes, fontFamily = ShareTechMonoFont, fontSize = 10.sp, color = TextMuted)
                    }
                }
            }
        }
    }

    if (showCreateDialog && currentTrack != null) {
        var shrineTitle by remember { mutableStateOf("${currentTrack.title} SHRINE") }
        var notes by remember { mutableStateOf("Looping this track indefinitely. Authentic 1999 sound.") }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = {
                Text(
                    text = "CONSECRATE OBSESSION",
                    fontFamily = ChakraPetchFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = BurntOrange
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = shrineTitle,
                        onValueChange = { shrineTitle = it },
                        label = { Text("Shrine Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Personal Notes") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onCreateObsession(currentTrack.id, shrineTitle, notes, 99)
                        showCreateDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BurntOrange)
                ) {
                    Text(text = "CONSECRATE", color = MatteBlack, fontFamily = ChakraPetchFont, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text(text = "CANCEL", color = TextSecondary, fontFamily = ChakraPetchFont)
                }
            },
            containerColor = GunmetalDark
        )
    }
}
