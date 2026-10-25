package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ListeningHistoryEntity
import com.example.model.Track
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MusicDiaryScreen(
    tracks: List<Track>,
    history: List<ListeningHistoryEntity>,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val totalPlays = tracks.sumOf { it.replayCount }
    val topTracks = tracks.sortedByDescending { it.replayCount }.take(5)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MatteBlack),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Diary Title
        item {
            Column {
                Text(
                    text = "MUSIC DIARY // TELEMETRY",
                    fontFamily = ChakraPetchFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = LcdCyan,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "LOCAL LISTENING LOGS, HEAVY ROTATION & TOTAL SESSIONS",
                    fontFamily = ShareTechMonoFont,
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }
        }

        // Summary Metric Tiles
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Total Replays
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(GunmetalDark)
                        .border(1.dp, BorderHardware, RoundedCornerShape(4.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Text(text = "TOTAL REPLAYS", fontFamily = ShareTechMonoFont, fontSize = 9.sp, color = TextMuted)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$totalPlays",
                            fontFamily = ShareTechMonoFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = AcidGreen
                        )
                    }
                }

                // Library Size
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(GunmetalDark)
                        .border(1.dp, BorderHardware, RoundedCornerShape(4.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Text(text = "VAULT TRACKS", fontFamily = ShareTechMonoFont, fontSize = 9.sp, color = TextMuted)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${tracks.size}",
                            fontFamily = ShareTechMonoFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = BurntOrange
                        )
                    }
                }

                // Logged Sessions
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(GunmetalDark)
                        .border(1.dp, BorderHardware, RoundedCornerShape(4.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Text(text = "SESSION LOGS", fontFamily = ShareTechMonoFont, fontSize = 9.sp, color = TextMuted)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${history.size}",
                            fontFamily = ShareTechMonoFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = LcdCyan
                        )
                    }
                }
            }
        }

        // Top Played Rotation Chart Bars
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
                        text = "TOP ROTATION GRAPH",
                        fontFamily = ChakraPetchFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val maxCount = (topTracks.firstOrNull()?.replayCount ?: 1).coerceAtLeast(1)

                    topTracks.forEach { track ->
                        val ratio = track.replayCount.toFloat() / maxCount.toFloat()
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = track.title,
                                    fontFamily = ChakraPetchFont,
                                    fontSize = 11.sp,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "${track.replayCount} plays",
                                    fontFamily = ShareTechMonoFont,
                                    fontSize = 10.sp,
                                    color = AcidGreen
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .background(GunmetalMedium)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(ratio)
                                        .fillMaxHeight()
                                        .background(AcidGreen)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Recent Listening History Log
        item {
            Text(
                text = "RECENT SESSION LOG",
                fontFamily = ChakraPetchFont,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        items(history.take(15)) { item ->
            val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(item.playedAtMs))
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
                    Text(text = item.title, fontFamily = ChakraPetchFont, fontSize = 11.sp, color = TextPrimary)
                    Text(text = item.artist, fontFamily = ShareTechMonoFont, fontSize = 9.sp, color = TextSecondary)
                }
                Text(text = timeStr, fontFamily = ShareTechMonoFont, fontSize = 9.sp, color = TextMuted)
            }
        }
    }
}
