package com.example.ui.screens

import android.view.HapticFeedbackConstants
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ReplayViewModel
import com.example.ui.components.HardwarePushSwitch
import com.example.ui.theme.AcidGreen
import com.example.ui.theme.BevelLight
import com.example.ui.theme.BorderHardware
import com.example.ui.theme.BurntOrange
import com.example.ui.theme.ChakraPetchFont
import com.example.ui.theme.DangerRed
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.GunmetalDark
import com.example.ui.theme.GunmetalLight
import com.example.ui.theme.GunmetalMedium
import com.example.ui.theme.LcdCyan
import com.example.ui.theme.LightAluminium
import com.example.ui.theme.MatteBlack
import com.example.ui.theme.ShareTechMonoFont
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun TelephoneScreen(
    viewModel: ReplayViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateBack()
    }

    val telephoneNumber by viewModel.telephoneNumber.collectAsState()
    val playbackState by viewModel.audioEngine.playbackState.collectAsState()
    val view = LocalView.current

    val hotlines = remember {
        listOf(
            Triple("99", "1999 RAVE LINE", "Hardcore breakbeats & rave classics"),
            Triple("411", "DEEP CUTS OPERATOR", "Extended club mixes > 4 minutes"),
            Triple("808", "TR-808 BEAT VAULT", "Boom-bap & Roland 808 percussion"),
            Triple("254", "NAIROBI HOTLINE", "Benga & Kenyan underground audio")
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MatteBlack)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Telephone Station Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GunmetalDark)
                    .border(1.dp, BorderHardware)
                    .padding(12.dp)
            ) {
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
                                .background(if (telephoneNumber.isNotEmpty()) BurntOrange else AcidGreen)
                                .border(1.dp, BevelLight, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "UNDERGROUND ROTARY HOTLINE",
                            fontFamily = ChakraPetchFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = TextPrimary
                        )
                    }
                    Text(
                        text = "BELL 1989 // PULSE",
                        fontFamily = ShareTechMonoFont,
                        fontSize = 10.sp,
                        color = BurntOrange
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // LCD Phone Display
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0A1210))
                        .border(1.5.dp, Color(0xFF1B3D2B))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "DIALED NUMBER",
                                fontFamily = ShareTechMonoFont,
                                fontSize = 9.sp,
                                color = TextMuted
                            )
                            Text(
                                text = if (playbackState.isPlaying) "LINE BUSY // AUDIO ACTIVE" else "OFF-HOOK READY",
                                fontFamily = ShareTechMonoFont,
                                fontSize = 9.sp,
                                color = if (playbackState.isPlaying) AcidGreen else BurntOrange
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (telephoneNumber.isEmpty()) "DIAL NUMBER..." else telephoneNumber,
                            fontFamily = ShareTechMonoFont,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (telephoneNumber.isEmpty()) Color(0xFF1B3D2B) else LcdCyan,
                            letterSpacing = 4.sp
                        )
                    }
                }
            }
        }

        // Rotary Dial Unit
        item {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GunmetalDark)
                    .border(1.5.dp, BorderHardware, RoundedCornerShape(4.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                val dialSize = 250.dp
                var dialAngle by remember { mutableFloatStateOf(0f) }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(dialSize)
                            .clip(CircleShape)
                            .background(MatteBlack)
                            .border(3.dp, GunmetalMedium, CircleShape)
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        dialAngle = (dialAngle + dragAmount.x * 0.4f).coerceIn(0f, 180f)
                                    },
                                    onDragEnd = {
                                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                        dialAngle = 0f
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val radius = size.minDimension / 2f

                            // Outer phone bezel
                            drawCircle(
                                color = Color(0xFF1C1F22),
                                radius = radius * 0.95f,
                                center = center
                            )

                            // Mechanical finger stop at bottom right
                            drawCircle(
                                color = Color(0xFF888888),
                                radius = 8.dp.toPx(),
                                center = Offset(center.x + radius * 0.65f, center.y + radius * 0.65f)
                            )
                        }

                        // Rotary Digits (1 through 9, then 0)
                        val digits = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 0)
                        digits.forEachIndexed { index, digit ->
                            val angleRad = (index * 26.0 - 130.0) * (PI / 180.0)
                            val r = 85.dp
                            val xOffset = (r.value * cos(angleRad)).dp
                            val yOffset = (r.value * sin(angleRad)).dp

                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .padding(start = 0.dp)
                                    .offset(x = xOffset, y = yOffset)
                                    .clip(CircleShape)
                                    .background(GunmetalLight)
                                    .border(1.dp, BorderHardware, CircleShape)
                                    .clickable {
                                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                        viewModel.dialTelephoneDigit(digit)
                                    }
                                    .testTag("rotary_digit_$digit"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = digit.toString(),
                                    fontFamily = ChakraPetchFont,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                        }

                        // Central Hub Label
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                                .background(GunmetalDark)
                                .border(2.dp, BurntOrange, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "REPLAY",
                                    fontFamily = ChakraPetchFont,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BurntOrange
                                )
                                Text(
                                    text = "PULSE",
                                    fontFamily = ShareTechMonoFont,
                                    fontSize = 8.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action Controls: Call Hotline & Clear
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        HardwarePushSwitch(
                            label = "CONNECT LINE",
                            code = "CALL",
                            isActive = telephoneNumber.isNotEmpty(),
                            activeColor = AcidGreen,
                            onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                viewModel.callTelephoneHotline()
                            },
                            modifier = Modifier.weight(1f).testTag("telephone_call_btn")
                        )

                        HardwarePushSwitch(
                            label = "CLEAR DIGITS",
                            code = "RESET",
                            isActive = false,
                            activeColor = DangerRed,
                            onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                viewModel.clearTelephoneNumber()
                            },
                            modifier = Modifier.weight(1f).testTag("telephone_clear_btn")
                        )
                    }
                }
            }
        }

        // Speed Dial Hotline Directory
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GunmetalDark)
                    .border(1.dp, BorderHardware)
                    .padding(12.dp)
            ) {
                Text(
                    text = "HOTLINE SPEED DIAL DIRECTORY",
                    fontFamily = ChakraPetchFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = BurntOrange
                )
                Spacer(modifier = Modifier.height(8.dp))

                hotlines.forEach { (code, title, desc) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(MatteBlack)
                            .border(1.dp, Color(0xFF22262B))
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                viewModel.clearTelephoneNumber()
                                code.forEach { char ->
                                    val d = char.digitToIntOrNull()
                                    if (d != null) viewModel.dialTelephoneDigit(d)
                                }
                                viewModel.callTelephoneHotline()
                            }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = title,
                                fontFamily = ChakraPetchFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = desc,
                                fontFamily = ShareTechMonoFont,
                                fontSize = 9.sp,
                                color = TextMuted
                            )
                        }

                        Box(
                            modifier = Modifier
                                .background(GunmetalMedium)
                                .border(1.dp, BurntOrange, RoundedCornerShape(2.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "[$code]",
                                fontFamily = ShareTechMonoFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = BurntOrange
                            )
                        }
                    }
                }
            }
        }
    }
}
