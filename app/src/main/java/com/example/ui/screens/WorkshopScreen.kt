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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.model.HardwareMaterial
import com.example.model.HardwareSkin
import com.example.ui.theme.AcidGreen
import com.example.ui.theme.BorderHardware
import com.example.ui.theme.BurntOrange
import com.example.ui.theme.ChakraPetchFont
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.GunmetalDark
import com.example.ui.theme.GunmetalLight
import com.example.ui.theme.GunmetalMedium
import com.example.ui.theme.LcdAmber
import com.example.ui.theme.LcdCyan
import com.example.ui.theme.MatteBlack
import com.example.ui.theme.ShareTechMonoFont
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun WorkshopScreen(
    currentSkin: HardwareSkin,
    onUpdateSkin: (HardwareMaterial, String, String) -> Unit,
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
        // Workshop Title
        item {
            Column {
                Text(
                    text = "HARDWARE WORKSHOP // FABRICATION",
                    fontFamily = ChakraPetchFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(android.graphics.Color.parseColor(currentSkin.accentColorHex)),
                    letterSpacing = 1.sp
                )
                Text(
                    text = "CUSTOM CHASSIS MATERIALS, LED ACCENTS & SKINS",
                    fontFamily = ShareTechMonoFont,
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }
        }

        // Active Machine Chassis Material Selector
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
                        text = "CHASSIS MATERIAL & FINISH",
                        fontFamily = ChakraPetchFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        HardwareMaterial.values().forEach { mat ->
                            val isSel = currentSkin.material == mat
                            val baseColor = Color(android.graphics.Color.parseColor(mat.baseColorHex))
                            val borderColor = Color(android.graphics.Color.parseColor(mat.borderColorHex))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(baseColor)
                                    .border(2.dp, if (isSel) AcidGreen else borderColor, RoundedCornerShape(3.dp))
                                    .clickable {
                                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                        onUpdateSkin(mat, currentSkin.accentColorHex, currentSkin.lcdColorHex)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(if (isSel) AcidGreen else TextMuted)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = mat.label,
                                        fontFamily = ChakraPetchFont,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = TextPrimary
                                    )
                                }

                                Text(
                                    text = if (isSel) "INSTALLED" else "SELECT",
                                    fontFamily = ShareTechMonoFont,
                                    fontSize = 10.sp,
                                    color = if (isSel) AcidGreen else TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Accent LED Color
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
                        text = "ACCENT LED COLOR",
                        fontFamily = ChakraPetchFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf(
                            Triple("ACID GREEN", "#00FF66", AcidGreen),
                            Triple("BURNT ORANGE", "#FF6B1A", BurntOrange),
                            Triple("ELECTRIC BLUE", "#00E5FF", ElectricBlue),
                            Triple("LASER RED", "#FF3344", Color(0xFFFF3344))
                        ).forEach { (label, hex, col) ->
                            val isSel = currentSkin.accentColorHex == hex
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                        onUpdateSkin(currentSkin.material, hex, currentSkin.lcdColorHex)
                                    }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(col)
                                        .border(2.dp, if (isSel) TextPrimary else Color.Transparent, CircleShape)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = label.split(" ").first(), fontFamily = ChakraPetchFont, fontSize = 9.sp, color = TextSecondary)
                            }
                        }
                    }
                }
            }
        }

        // LCD Backlight Display Color
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
                        text = "LCD MATRIX BACKLIGHT",
                        fontFamily = ChakraPetchFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf(
                            Triple("CYAN MATRIX", "#38E1FF", LcdCyan),
                            Triple("AMBER LCD", "#FFB800", LcdAmber),
                            Triple("RETRO GREEN", "#54FC54", Color(0xFF54FC54))
                        ).forEach { (label, hex, col) ->
                            val isSel = currentSkin.lcdColorHex == hex
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                        onUpdateSkin(currentSkin.material, currentSkin.accentColorHex, hex)
                                    }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(30.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(col.copy(alpha = 0.25f))
                                        .border(2.dp, if (isSel) col else BorderHardware, RoundedCornerShape(2.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "1999", fontFamily = ShareTechMonoFont, fontSize = 11.sp, color = col, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = label, fontFamily = ChakraPetchFont, fontSize = 9.sp, color = TextSecondary)
                            }
                        }
                    }
                }
            }
        }
    }
}
