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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun DiscoveryMachineScreen(
    tracks: List<Track>,
    accentColor: Color,
    onTriggerRoulette: () -> Unit,
    onTrackSelect: (Track) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    var selectedGenre by remember { mutableStateOf("ALL") }

    val genres = listOf("ALL", "Acid Techno", "Drum & Bass", "90s Hip Hop", "Eurodance", "Lo-Fi Dub")

    val filtered = remember(selectedGenre, tracks) {
        if (selectedGenre == "ALL") tracks
        else tracks.filter { it.genre.equals(selectedGenre, ignoreCase = true) }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MatteBlack),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Discovery Title
        item {
            Column {
                Text(
                    text = "DISCOVERY MACHINE // CRATE ROULETTE",
                    fontFamily = ChakraPetchFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = accentColor,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "REDISCOVER FORGOTTEN GEMS & UNDERGROUND CUTS",
                    fontFamily = ShareTechMonoFont,
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }
        }

        // Mechanical Roulette Trigger Pad
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GunmetalDark)
                    .border(2.dp, BurntOrange, RoundedCornerShape(6.dp))
                    .padding(14.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "TAPE ROULETTE // RANDOM SELECT",
                        fontFamily = ChakraPetchFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = BurntOrange
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Spin the mechanism to load an unexpected underground track",
                        fontFamily = ShareTechMonoFont,
                        fontSize = 10.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(BurntOrange)
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                onTriggerRoulette()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "► SPIN ROULETTE",
                            fontFamily = ChakraPetchFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MatteBlack
                        )
                    }
                }
            }
        }

        // Genre / Era Filter Bank
        item {
            Column {
                Text(
                    text = "GENRE CRATES",
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
                    genres.take(3).forEach { g ->
                        val isSel = g == selectedGenre
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (isSel) GunmetalLight else GunmetalDark)
                                .border(1.dp, if (isSel) accentColor else BorderHardware, RoundedCornerShape(3.dp))
                                .clickable { selectedGenre = g }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = g, fontFamily = ChakraPetchFont, fontSize = 9.sp, color = if (isSel) accentColor else TextSecondary)
                        }
                    }
                }
            }
        }

        // Filtered Tracks
        items(filtered) { track ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(3.dp))
                    .background(GunmetalDark)
                    .border(1.dp, BorderHardware, RoundedCornerShape(3.dp))
                    .clickable { onTrackSelect(track) }
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = track.title, fontFamily = ChakraPetchFont, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextPrimary)
                    Text(text = "${track.artist} // ${track.genre}", fontFamily = ShareTechMonoFont, fontSize = 9.sp, color = TextSecondary)
                }
                Text(text = "C-60", fontFamily = ShareTechMonoFont, fontSize = 10.sp, color = BurntOrange)
            }
        }
    }
}
