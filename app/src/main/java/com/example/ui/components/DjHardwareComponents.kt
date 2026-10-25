package com.example.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Track
import com.example.ui.theme.AcidGreen
import com.example.ui.theme.BorderHardware
import com.example.ui.theme.BurntOrange
import com.example.ui.theme.ChakraPetchFont
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.GunmetalDark
import com.example.ui.theme.GunmetalLight
import com.example.ui.theme.GunmetalMedium
import com.example.ui.theme.MatteBlack
import com.example.ui.theme.ShareTechMonoFont
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.atan2

// Interactive Scratching Vinyl Platter
@Composable
fun VinylPlatter(
    deckName: String,
    track: Track?,
    isPlaying: Boolean,
    platterRotationDeg: Float,
    pitchPercent: Float,
    accentColor: Color = AcidGreen,
    onScratch: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    var lastAngle by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = modifier
            .background(GunmetalDark)
            .border(1.dp, BorderHardware, RoundedCornerShape(6.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Deck Title & Pitch Readout
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$deckName // SL-1200",
                fontFamily = ChakraPetchFont,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = accentColor
            )
            Text(
                text = String.format("%+.1f%% PITCH", pitchPercent),
                fontFamily = ShareTechMonoFont,
                fontSize = 10.sp,
                color = if (pitchPercent == 0f) TextMuted else BurntOrange
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Vinyl Disc Canvas with Drag gesture for audio scratch
        Box(
            modifier = Modifier
                .size(150.dp)
                .clip(CircleShape)
                .background(MatteBlack)
                .border(2.dp, GunmetalLight, CircleShape)
                .pointerInput(deckName) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val cx = size.width / 2f
                            val cy = size.height / 2f
                            lastAngle = Math.toDegrees(atan2((offset.y - cy).toDouble(), (offset.x - cx).toDouble())).toFloat()
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            val cx = size.width / 2f
                            val cy = size.height / 2f
                            val curAngle = Math.toDegrees(atan2((change.position.y - cy).toDouble(), (change.position.x - cx).toDouble())).toFloat()
                            var delta = curAngle - lastAngle
                            if (delta > 180f) delta -= 360f
                            if (delta < -180f) delta += 360f

                            if (Math.abs(delta) > 1f) {
                                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                onScratch(delta)
                                lastAngle = curAngle
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .rotate(platterRotationDeg)
            ) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val r = size.width / 2f

                // Deep grooved vinyl with radial light reflections
                drawCircle(
                    brush = Brush.sweepGradient(
                        listOf(
                            Color(0xFF0F1113),
                            Color(0xFF282D33),
                            Color(0xFF0F1113),
                            Color(0xFF282D33),
                            Color(0xFF0F1113)
                        ),
                        center
                    ),
                    radius = r,
                    center = center
                )

                // Concentric vinyl grooves
                for (groove in listOf(0.92f, 0.84f, 0.76f, 0.68f, 0.60f, 0.52f)) {
                    drawCircle(color = Color(0x33FFFFFF), radius = r * groove, center = center, style = Stroke(0.8f))
                }

                // Center 7" Record Label
                val labelR = r * 0.38f
                drawCircle(color = accentColor, radius = labelR, center = center)
                drawCircle(color = MatteBlack, radius = labelR * 0.25f, center = center)

                // Strobe dot markings around the rim (33 / 45 RPM)
                val strobeR = r * 0.96f
                for (i in 0 until 24) {
                    val angleRad = (i * 15f) * (Math.PI / 180.0)
                    val sx = center.x + (Math.cos(angleRad) * strobeR).toFloat()
                    val sy = center.y + (Math.sin(angleRad) * strobeR).toFloat()
                    drawCircle(color = Color(0x88FFFFFF), radius = 1.5f, center = Offset(sx, sy))
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = track?.title ?: "EMPTY DECK",
            fontFamily = ChakraPetchFont,
            fontSize = 11.sp,
            color = TextPrimary,
            maxLines = 1
        )
    }
}

// DJ Crossfader with Central Detent & Markers
@Composable
fun DjCrossfader(
    crossfader: Float, // -1.0f to +1.0f
    onCrossfaderChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(GunmetalDark)
            .border(1.dp, BorderHardware, RoundedCornerShape(4.dp))
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "◄ DECK A", fontFamily = ChakraPetchFont, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = AcidGreen)
            Text(text = "CROSSFADER [OPTICAL]", fontFamily = ShareTechMonoFont, fontSize = 9.sp, color = TextMuted)
            Text(text = "DECK B ►", fontFamily = ChakraPetchFont, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = ElectricBlue)
        }

        Slider(
            value = crossfader,
            onValueChange = onCrossfaderChange,
            valueRange = -1.0f..1.0f,
            colors = SliderDefaults.colors(
                thumbColor = BurntOrange,
                activeTrackColor = AcidGreen,
                inactiveTrackColor = ElectricBlue
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
