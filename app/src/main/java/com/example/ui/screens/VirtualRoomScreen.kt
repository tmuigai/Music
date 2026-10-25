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
import com.example.data.MixtapeEntity
import com.example.model.Track
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
fun VirtualRoomScreen(
    tracks: List<Track>,
    mixtapes: List<MixtapeEntity>,
    accentColor: Color,
    onTrackSelect: (Track) -> Unit,
    onLoadMixtape: (MixtapeEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MatteBlack),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Room Ambience Header
        item {
            Column {
                Text(
                    text = "VIRTUAL COLLECTION ROOM // 1999",
                    fontFamily = ChakraPetchFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = accentColor,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "TAP ANY PHYSICAL CASSETTE OR CD TO LOAD INTO DECK",
                    fontFamily = ShareTechMonoFont,
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }
        }

        // 1. Physical Cassette Tape Rack Shelf
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CASSETTE TAPE SHELF // CHROME & TYPE I",
                        fontFamily = ChakraPetchFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = BurntOrange
                    )
                    Text(
                        text = "${mixtapes.size + tracks.size} TAPES",
                        fontFamily = ShareTechMonoFont,
                        fontSize = 9.sp,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Wooden / Metal Cassette Rack Wall Unit
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF141718))
                        .border(2.dp, BorderHardware, RoundedCornerShape(4.dp))
                        .padding(8.dp)
                ) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Mixtapes first
                        items(mixtapes) { tape ->
                            Box(
                                modifier = Modifier
                                    .width(70.dp)
                                    .height(130.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color(android.graphics.Color.parseColor(tape.shellColorHex)))
                                    .border(1.dp, AcidGreen, RoundedCornerShape(3.dp))
                                    .clickable {
                                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                        onLoadMixtape(tape)
                                    }
                                    .padding(4.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "C-${tape.durationMinutes}", fontFamily = ShareTechMonoFont, fontSize = 8.sp, color = AcidGreen)
                                    Text(
                                        text = tape.title,
                                        fontFamily = ChakraPetchFont,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = TextPrimary,
                                        maxLines = 4,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(text = "CHROME", fontFamily = ShareTechMonoFont, fontSize = 7.sp, color = BurntOrange)
                                }
                            }
                        }

                        // Tracks as individual tape singles
                        items(tracks) { track ->
                            Box(
                                modifier = Modifier
                                    .width(65.dp)
                                    .height(130.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(GunmetalDark)
                                    .border(1.dp, BorderHardware, RoundedCornerShape(3.dp))
                                    .clickable {
                                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                        onTrackSelect(track)
                                    }
                                    .padding(4.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "C-60", fontFamily = ShareTechMonoFont, fontSize = 8.sp, color = TextMuted)
                                    Text(
                                        text = track.title,
                                        fontFamily = ChakraPetchFont,
                                        fontSize = 9.sp,
                                        color = TextPrimary,
                                        maxLines = 4,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(text = track.artist.take(8), fontFamily = ShareTechMonoFont, fontSize = 7.sp, color = TextSecondary)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. CD Jewel Case Display Tower
        item {
            Column {
                Text(
                    text = "COMPACT DISC JEWEL TOWER",
                    fontFamily = ChakraPetchFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = LcdCyan
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF101214))
                        .border(2.dp, BorderHardware, RoundedCornerShape(4.dp))
                        .padding(8.dp)
                ) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(tracks.reversed()) { track ->
                            Box(
                                modifier = Modifier
                                    .size(105.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(GunmetalDark)
                                    .border(1.5.dp, BorderHardware, RoundedCornerShape(2.dp))
                                    .clickable {
                                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                        onTrackSelect(track)
                                    }
                                    .padding(6.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = "CD", fontFamily = ShareTechMonoFont, fontSize = 8.sp, color = LcdCyan)
                                        Text(text = "DDD", fontFamily = ShareTechMonoFont, fontSize = 8.sp, color = TextMuted)
                                    }

                                    Column {
                                        Text(
                                            text = track.title,
                                            fontFamily = ChakraPetchFont,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            color = TextPrimary,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = track.album,
                                            fontFamily = ShareTechMonoFont,
                                            fontSize = 8.sp,
                                            color = TextSecondary,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Underground 12" Vinyl Crate
        item {
            Column {
                Text(
                    text = "12\" RECORD CRATE // CLUB PROMOS",
                    fontFamily = ChakraPetchFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = AcidGreen
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(GunmetalDark)
                        .border(1.dp, BorderHardware, RoundedCornerShape(4.dp))
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        tracks.take(4).forEach { track ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(GunmetalMedium)
                                    .clickable {
                                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                        onTrackSelect(track)
                                    }
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(MatteBlack)
                                            .border(1.dp, AcidGreen, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(text = track.title, fontFamily = ChakraPetchFont, fontSize = 11.sp, color = TextPrimary)
                                        Text(text = "12\" White Label Promo // 45 RPM", fontFamily = ShareTechMonoFont, fontSize = 9.sp, color = TextSecondary)
                                    }
                                }

                                Text(text = "DROP NEEDLE ►", fontFamily = ChakraPetchFont, fontSize = 10.sp, color = AcidGreen)
                            }
                        }
                    }
                }
            }
        }
    }
}
