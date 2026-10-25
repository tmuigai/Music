package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.view.HapticFeedbackConstants
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.RecordingEntity
import com.example.model.Track
import com.example.ui.components.VuSegmentBar
import com.example.ui.theme.AcidGreen
import com.example.ui.theme.BorderHardware
import com.example.ui.theme.BurntOrange
import com.example.ui.theme.ChakraPetchFont
import com.example.ui.theme.DangerRed
import com.example.ui.theme.GunmetalDark
import com.example.ui.theme.GunmetalLight
import com.example.ui.theme.GunmetalMedium
import com.example.ui.theme.MatteBlack
import com.example.ui.theme.ShareTechMonoFont
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun RecordingBoothScreen(
    isRecording: Boolean,
    recordingDurationMs: Long,
    recordingAmplitude: Float,
    recordings: List<RecordingEntity>,
    accentColor: Color,
    onStartRecording: (String) -> Unit,
    onStopRecording: (String) -> Unit,
    onDeleteRecording: (Long) -> Unit,
    onPlayTrack: (Track) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    var sessionTitle by remember { mutableStateOf("MIXTAPE_INTRO_99") }

    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasMicPermission = isGranted
        if (isGranted) {
            onStartRecording(sessionTitle)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MatteBlack),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Recording Console Title
        item {
            Column {
                Text(
                    text = "RECORDING BOOTH // MIC RECORDER",
                    fontFamily = ChakraPetchFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = accentColor,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "RECORD SPOKEN MIXTAPE INTROS & SOUND EFFECTS",
                    fontFamily = ShareTechMonoFont,
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }
        }

        // Live Mic VU Meter & Tape Deck Transport
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GunmetalDark)
                    .border(2.dp, if (isRecording) DangerRed else BorderHardware, RoundedCornerShape(6.dp))
                    .padding(14.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Record LED & Timer
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (isRecording) DangerRed else TextMuted)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isRecording) "REC ARMED // STANDBY" else "STANDBY // READY",
                                fontFamily = ChakraPetchFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (isRecording) DangerRed else TextSecondary
                            )
                        }

                        val sec = (recordingDurationMs / 1000) % 60
                        val min = (recordingDurationMs / 60000)
                        val ms = (recordingDurationMs % 1000) / 10
                        Text(
                            text = String.format("%02d:%02d.%02d", min, sec, ms),
                            fontFamily = ShareTechMonoFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = if (isRecording) DangerRed else TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Live Mic VU Input Level
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(text = "LIVE MICROPHONE INPUT LEVEL", fontFamily = ShareTechMonoFont, fontSize = 9.sp, color = TextMuted)
                        Spacer(modifier = Modifier.height(4.dp))
                        VuSegmentBar(
                            level = if (isRecording) recordingAmplitude * 1.5f else 0.05f,
                            accentColor = AcidGreen,
                            segmentCount = 28
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = sessionTitle,
                        onValueChange = { sessionTitle = it },
                        label = { Text("Session Name") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isRecording
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Big Physical REC / STOP Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isRecording) DangerRed else GunmetalMedium)
                            .border(2.dp, if (isRecording) DangerRed else BorderHardware, RoundedCornerShape(4.dp))
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                if (isRecording) {
                                    onStopRecording(sessionTitle)
                                } else {
                                    if (hasMicPermission) {
                                        onStartRecording(sessionTitle)
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = if (isRecording) "Stop" else "Record",
                                tint = if (isRecording) MatteBlack else DangerRed,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isRecording) "STOP & SAVE RECORDING" else "ARM & START RECORDING",
                                fontFamily = ChakraPetchFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isRecording) MatteBlack else TextPrimary
                            )
                        }
                    }
                }
            }
        }

        // Saved Recordings Library
        item {
            Text(
                text = "RECORDED MASTERS // LIBRARY (${recordings.size})",
                fontFamily = ChakraPetchFont,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        items(recordings) { rec ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(3.dp))
                    .background(GunmetalDark)
                    .border(1.dp, BorderHardware, RoundedCornerShape(3.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = rec.title,
                        fontFamily = ChakraPetchFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = String.format("LENGTH: %02d:%02d", (rec.durationMs / 60000), (rec.durationMs % 60000) / 1000),
                        fontFamily = ShareTechMonoFont,
                        fontSize = 9.sp,
                        color = TextMuted
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Playback
                    Box(
                        modifier = Modifier
                            .background(AcidGreen)
                            .clickable {
                                onPlayTrack(
                                    Track(
                                        id = rec.id + 700000L,
                                        title = rec.title,
                                        artist = "Recording Session",
                                        album = "Local Masters",
                                        durationMs = rec.durationMs,
                                        contentUri = rec.filePath
                                    )
                                )
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = "PLAY", fontFamily = ChakraPetchFont, fontSize = 9.sp, color = MatteBlack, fontWeight = FontWeight.Bold)
                    }

                    // Delete
                    Box(
                        modifier = Modifier
                            .background(GunmetalMedium)
                            .clickable { onDeleteRecording(rec.id) }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = DangerRed, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}
