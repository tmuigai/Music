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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LyricsLine
import com.example.model.PlaybackState
import com.example.ui.theme.AcidGreen
import com.example.ui.theme.BorderHardware
import com.example.ui.theme.BurntOrange
import com.example.ui.theme.ChakraPetchFont
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
fun LyricsRoomScreen(
    playbackState: PlaybackState,
    lyricsLines: List<LyricsLine>,
    lyricsOffsetMs: Long,
    accentColor: Color,
    onAdjustOffset: (Long) -> Unit,
    onSaveLyrics: (Long, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val currentTrack = playbackState.currentTrack
    val currentPos = playbackState.positionMs + lyricsOffsetMs
    val listState = rememberLazyListState()
    var isEditingLyrics by remember { mutableStateOf(false) }
    var editLyricsText by remember { mutableStateOf("") }

    // Find active line
    val activeIndex = lyricsLines.indexOfLast { it.timestampMs in 0..currentPos }

    // Auto-scroll to active lyric line
    LaunchedEffect(activeIndex) {
        if (activeIndex >= 0 && activeIndex < lyricsLines.size) {
            listState.animateScrollToItem(activeIndex)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MatteBlack)
            .padding(12.dp)
    ) {
        // Control Bar (Timing Offset, Manual Edit)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(GunmetalDark)
                .border(1.dp, BorderHardware, RoundedCornerShape(4.dp))
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "KARAOKE SYNC // LRC ENGINE",
                    fontFamily = ChakraPetchFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = accentColor
                )
                Text(
                    text = String.format("TIMING OFFSET: %+d ms", lyricsOffsetMs),
                    fontFamily = ShareTechMonoFont,
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Offset buttons
                Box(
                    modifier = Modifier
                        .background(GunmetalMedium)
                        .border(1.dp, BorderHardware, RoundedCornerShape(2.dp))
                        .clickable { onAdjustOffset(-100) }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Text(text = "-100ms", fontFamily = ShareTechMonoFont, fontSize = 9.sp, color = TextPrimary)
                }
                Box(
                    modifier = Modifier
                        .background(GunmetalMedium)
                        .border(1.dp, BorderHardware, RoundedCornerShape(2.dp))
                        .clickable { onAdjustOffset(100) }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Text(text = "+100ms", fontFamily = ShareTechMonoFont, fontSize = 9.sp, color = TextPrimary)
                }

                // Edit Button
                Box(
                    modifier = Modifier
                        .background(GunmetalMedium)
                        .border(1.dp, BurntOrange, RoundedCornerShape(2.dp))
                        .clickable {
                            editLyricsText = currentTrack?.lyrics ?: ""
                            isEditingLyrics = true
                        }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Text(text = "EDIT LRC", fontFamily = ChakraPetchFont, fontSize = 10.sp, color = BurntOrange)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Big Retro LCD Lyrics Display Screen
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFF0C1418))
                .border(2.dp, BorderHardware, RoundedCornerShape(6.dp))
                .padding(12.dp)
        ) {
            if (lyricsLines.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "NO TIMED LYRICS DETECTED",
                            fontFamily = ChakraPetchFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap 'EDIT LRC' to paste or synchronize lyrics",
                            fontFamily = ShareTechMonoFont,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 40.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    itemsIndexed(lyricsLines) { index, line ->
                        val isActive = index == activeIndex
                        val isPassed = index < activeIndex

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Line Text or Word-by-word Karaoke
                            if (line.wordTimestamps != null && isActive) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    line.wordTimestamps.forEach { word ->
                                        val isWordActive = currentPos >= word.startMs
                                        Text(
                                            text = "${word.word} ",
                                            fontFamily = ChakraPetchFont,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp,
                                            color = if (isWordActive) AcidGreen else LcdCyan.copy(alpha = 0.4f),
                                            lineHeight = 28.sp
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    text = line.text,
                                    fontFamily = ChakraPetchFont,
                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = if (isActive) 19.sp else 14.sp,
                                    color = when {
                                        isActive -> AcidGreen
                                        isPassed -> TextMuted
                                        else -> LcdCyan.copy(alpha = 0.5f)
                                    },
                                    textAlign = TextAlign.Center,
                                    lineHeight = 24.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Lyrics Editor Dialog
    if (isEditingLyrics) {
        AlertDialog(
            onDismissRequest = { isEditingLyrics = false },
            title = {
                Text(
                    text = "EDIT LRC TIMESTAMPED LYRICS",
                    fontFamily = ChakraPetchFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = BurntOrange
                )
            },
            text = {
                Column {
                    Text(
                        text = "Format: [mm:ss.xx]Line lyrics or <mm:ss.xx>word",
                        fontFamily = ShareTechMonoFont,
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = editLyricsText,
                        onValueChange = { editLyricsText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = ShareTechMonoFont,
                            fontSize = 11.sp,
                            color = TextPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (currentTrack != null) {
                            onSaveLyrics(currentTrack.id, editLyricsText)
                        }
                        isEditingLyrics = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BurntOrange)
                ) {
                    Text(text = "SAVE LYRICS", color = MatteBlack, fontFamily = ChakraPetchFont, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { isEditingLyrics = false }) {
                    Text(text = "CANCEL", color = TextSecondary, fontFamily = ChakraPetchFont)
                }
            },
            containerColor = GunmetalDark
        )
    }
}
