package com.example.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode as AnimRepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.HardwareNavCategory
import com.example.model.HardwareNavScreen
import com.example.model.StickerItem
import com.example.model.TapeSide
import com.example.model.audioFormat
import com.example.ui.theme.AcidGreen
import com.example.ui.theme.AcidGreenDim
import com.example.ui.theme.BevelLight
import com.example.ui.theme.BevelShadow
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

// Top Machine Header with LED and segmented mode indicators
@Composable
fun HardwareHeader(
    currentScreen: HardwareNavScreen,
    hudMessage: String?,
    tapeCounter: Int,
    accentColor: Color = AcidGreen,
    onEjectClick: () -> Unit = {}
) {
    val view = LocalView.current
    val infiniteTransition = rememberInfiniteTransition(label = "power_led")
    val ledAlpha by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = AnimRepeatMode.Reverse
        ),
        label = "led_pulse"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(GunmetalDark)
            .border(width = 1.dp, color = BorderHardware)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Machine branding
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Power LED
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = ledAlpha))
                        .border(1.dp, BevelLight, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "REPLAY 99",
                    fontFamily = ChakraPetchFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TextPrimary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "// MK-II",
                    fontFamily = ShareTechMonoFont,
                    fontSize = 10.sp,
                    color = accentColor
                )
            }

            // LCD Counter & Mode status
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(MatteBlack)
                        .border(1.dp, BorderHardware, RoundedCornerShape(2.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = String.format("%04d", tapeCounter),
                        fontFamily = ShareTechMonoFont,
                        fontSize = 13.sp,
                        color = accentColor,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Mode Code Badge
                Box(
                    modifier = Modifier
                        .background(GunmetalLight)
                        .border(1.dp, BevelLight, RoundedCornerShape(2.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = currentScreen.code,
                        fontFamily = ChakraPetchFont,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }
        }

        // HUD Message Bar (if active)
        if (hudMessage != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MatteBlack)
                    .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = ">> $hudMessage",
                    fontFamily = ShareTechMonoFont,
                    fontSize = 11.sp,
                    color = accentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// Navigation Selector Rack: 5 Primary Hubs + Mechanical Sub-Selector Rack
@Composable
fun HardwareNavRack(
    currentScreen: HardwareNavScreen,
    accentColor: Color = AcidGreen,
    onNavigateCategory: (HardwareNavCategory) -> Unit,
    onNavigateScreen: (HardwareNavScreen) -> Unit,
    selectedLibraryTab: String = "SONGS",
    onSelectLibraryTab: (String) -> Unit = {},
    selectedSoundTab: String = "EQ",
    onSelectSoundTab: (String) -> Unit = {}
) {
    val currentCategory = currentScreen.category
    val subScrollState = rememberScrollState()
    val view = LocalView.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(GunmetalDark)
            .border(width = 1.dp, color = BorderHardware)
    ) {
        // ROW 1: The 5 Primary Master Hardware Push Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MatteBlack)
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            HardwareNavCategory.values().forEach { category ->
                val isCategorySelected = category == currentCategory
                val bgColor = if (isCategorySelected) GunmetalLight else GunmetalDark
                val textColor = if (isCategorySelected) accentColor else TextSecondary
                val borderC = if (isCategorySelected) accentColor else BorderHardware

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(bgColor)
                        .border(1.5.dp, borderC, RoundedCornerShape(3.dp))
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            onNavigateCategory(category)
                        }
                        .testTag("nav_btn_${category.name.lowercase()}")
                        .padding(horizontal = 2.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // LED and Category Title
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (isCategorySelected) accentColor else Color(0xFF2A2D30))
                                    .border(0.5.dp, if (isCategorySelected) BevelLight else Color(0xFF1A1A1A), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = category.title,
                                fontFamily = ChakraPetchFont,
                                fontSize = 10.sp,
                                fontWeight = if (isCategorySelected) FontWeight.Bold else FontWeight.Medium,
                                color = textColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = category.code,
                            fontFamily = ShareTechMonoFont,
                            fontSize = 8.sp,
                            color = if (isCategorySelected) accentColor.copy(alpha = 0.8f) else TextMuted
                        )
                    }
                }
            }
        }

        // ROW 2: Tactile Sub-Selector Strip for the active category
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF101214))
                .border(width = 0.5.dp, color = BorderHardware)
                .horizontalScroll(subScrollState)
                .padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Label prefix for sub-rack
            Text(
                text = "${currentCategory.title} //",
                fontFamily = ShareTechMonoFont,
                fontSize = 9.sp,
                color = TextMuted,
                modifier = Modifier.padding(end = 2.dp)
            )

            when (currentCategory) {
                HardwareNavCategory.HOME -> {
                    SubRackButton(
                        label = "CONTROL DECK",
                        code = "01",
                        isSelected = currentScreen == HardwareNavScreen.CONTROL_DECK,
                        accentColor = accentColor,
                        onClick = { onNavigateScreen(HardwareNavScreen.CONTROL_DECK) }
                    )
                    SubRackButton(
                        label = "CASSETTE DECK",
                        code = "02",
                        isSelected = false,
                        accentColor = accentColor,
                        onClick = { onNavigateScreen(HardwareNavScreen.CASSETTE) }
                    )
                    SubRackButton(
                        label = "MASTER AMP",
                        code = "AMP",
                        isSelected = false,
                        accentColor = accentColor,
                        onClick = {
                            onSelectSoundTab("VOLUME")
                            onNavigateScreen(HardwareNavScreen.SOUND_LAB)
                        }
                    )
                    SubRackButton(
                        label = "CRATE VAULT",
                        code = "06",
                        isSelected = false,
                        accentColor = accentColor,
                        onClick = { onNavigateScreen(HardwareNavScreen.MIXTAPE_LIBRARY) }
                    )
                }

                HardwareNavCategory.MACHINES -> {
                    // Physical Machines: Cassette, CD, DJ, MiniDisc, Boombox, Vinyl, Four-track, Rotary Phone, Party Jukebox
                    val machineList = listOf(
                        HardwareNavScreen.CASSETTE to "CASSETTE",
                        HardwareNavScreen.DISCMAN to "CD (DISCMAN)",
                        HardwareNavScreen.DJ_DECK to "DJ MIXER",
                        HardwareNavScreen.MINIDISC to "MINIDISC",
                        HardwareNavScreen.BOOMBOX to "BOOMBOX",
                        HardwareNavScreen.VINYL to "VINYL",
                        HardwareNavScreen.FOUR_TRACK to "FOUR-TRACK",
                        HardwareNavScreen.ROTARY_PHONE to "ROTARY PHONE",
                        HardwareNavScreen.PARTY_JUKEBOX to "JUKEBOX"
                    )
                    machineList.forEach { (screen, label) ->
                        SubRackButton(
                            label = label,
                            code = screen.code,
                            isSelected = currentScreen == screen,
                            accentColor = accentColor,
                            onClick = { onNavigateScreen(screen) }
                        )
                    }
                }

                HardwareNavCategory.LIBRARY -> {
                    // Library Sub-Destinations: Songs, Albums, Artists, Playlists, Mixtapes
                    val libTabs = listOf("SONGS", "ALBUMS", "ARTISTS", "PLAYLISTS", "MIXTAPES")
                    libTabs.forEach { tab ->
                        val isSel = currentScreen == HardwareNavScreen.MIXTAPE_LIBRARY && selectedLibraryTab == tab
                        SubRackButton(
                            label = tab,
                            code = tab.take(3),
                            isSelected = isSel,
                            accentColor = accentColor,
                            onClick = {
                                onSelectLibraryTab(tab)
                                if (currentScreen != HardwareNavScreen.MIXTAPE_LIBRARY) {
                                    onNavigateScreen(HardwareNavScreen.MIXTAPE_LIBRARY)
                                }
                            }
                        )
                    }
                }

                HardwareNavCategory.SOUND -> {
                    // Sound Sub-Destinations: Lyrics, EQ, Volume, Visualisers, Recording, Discovery
                    SubRackButton(
                        label = "LYRICS",
                        code = "LRC",
                        isSelected = currentScreen == HardwareNavScreen.LYRICS_ROOM,
                        accentColor = accentColor,
                        onClick = { onNavigateScreen(HardwareNavScreen.LYRICS_ROOM) }
                    )
                    SubRackButton(
                        label = "EQ",
                        code = "EQ",
                        isSelected = currentScreen == HardwareNavScreen.SOUND_LAB && selectedSoundTab == "EQ",
                        accentColor = accentColor,
                        onClick = {
                            onSelectSoundTab("EQ")
                            onNavigateScreen(HardwareNavScreen.SOUND_LAB)
                        }
                    )
                    SubRackButton(
                        label = "VOLUME (300%)",
                        code = "VOL",
                        isSelected = currentScreen == HardwareNavScreen.SOUND_LAB && selectedSoundTab == "VOLUME",
                        accentColor = accentColor,
                        onClick = {
                            onSelectSoundTab("VOLUME")
                            onNavigateScreen(HardwareNavScreen.SOUND_LAB)
                        }
                    )
                    SubRackButton(
                        label = "VISUALISERS",
                        code = "VIS",
                        isSelected = currentScreen == HardwareNavScreen.SOUND_LAB && selectedSoundTab == "VISUALISERS",
                        accentColor = accentColor,
                        onClick = {
                            onSelectSoundTab("VISUALISERS")
                            onNavigateScreen(HardwareNavScreen.SOUND_LAB)
                        }
                    )
                    SubRackButton(
                        label = "RECORDING",
                        code = "REC",
                        isSelected = currentScreen == HardwareNavScreen.RECORDING_BOOTH,
                        accentColor = accentColor,
                        onClick = { onNavigateScreen(HardwareNavScreen.RECORDING_BOOTH) }
                    )
                    SubRackButton(
                        label = "DISCOVERY",
                        code = "DSC",
                        isSelected = currentScreen == HardwareNavScreen.DISCOVERY_MACHINE,
                        accentColor = accentColor,
                        onClick = { onNavigateScreen(HardwareNavScreen.DISCOVERY_MACHINE) }
                    )
                }

                HardwareNavCategory.WORKSHOP -> {
                    // Workshop Sub-Destinations: Customisation, Collection Room, Archive, Settings, Build-A-Machine
                    SubRackButton(
                        label = "BUILD-A-MACHINE",
                        code = "21",
                        isSelected = currentScreen == HardwareNavScreen.BUILD_A_MACHINE,
                        accentColor = accentColor,
                        onClick = { onNavigateScreen(HardwareNavScreen.BUILD_A_MACHINE) }
                    )
                    SubRackButton(
                        label = "CUSTOMISE",
                        code = "08",
                        isSelected = currentScreen == HardwareNavScreen.WORKSHOP,
                        accentColor = accentColor,
                        onClick = { onNavigateScreen(HardwareNavScreen.WORKSHOP) }
                    )
                    SubRackButton(
                        label = "COLLECTION ROOM",
                        code = "09",
                        isSelected = currentScreen == HardwareNavScreen.VIRTUAL_ROOM,
                        accentColor = accentColor,
                        onClick = { onNavigateScreen(HardwareNavScreen.VIRTUAL_ROOM) }
                    )
                    SubRackButton(
                        label = "ARCHIVE / DIARY",
                        code = "14",
                        isSelected = currentScreen == HardwareNavScreen.MUSIC_DIARY,
                        accentColor = accentColor,
                        onClick = { onNavigateScreen(HardwareNavScreen.MUSIC_DIARY) }
                    )
                    SubRackButton(
                        label = "OBSESSION",
                        code = "13",
                        isSelected = currentScreen == HardwareNavScreen.OBSESSION_MODE,
                        accentColor = accentColor,
                        onClick = { onNavigateScreen(HardwareNavScreen.OBSESSION_MODE) }
                    )
                    SubRackButton(
                        label = "SETTINGS (FM)",
                        code = "11",
                        isSelected = currentScreen == HardwareNavScreen.RADIO_MODE,
                        accentColor = accentColor,
                        onClick = { onNavigateScreen(HardwareNavScreen.RADIO_MODE) }
                    )
                }
            }
        }
    }
}

@Composable
fun SubRackButton(
    label: String,
    code: String,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    val view = LocalView.current
    val bgColor = if (isSelected) GunmetalLight else GunmetalDark
    val textColor = if (isSelected) accentColor else TextSecondary
    val borderC = if (isSelected) accentColor else BorderHardware

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(2.dp))
            .background(bgColor)
            .border(1.dp, borderC, RoundedCornerShape(2.dp))
            .clickable {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onClick()
            }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = label,
                fontFamily = ChakraPetchFont,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = textColor,
                letterSpacing = 0.5.sp
            )
        }
    }
}

// Mechanical Transport Control Buttons (PLAY, PAUSE, STOP, REW, FF, PREV, NEXT)
@Composable
fun HardwareTransportBar(
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onStop: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onRewindHold: () -> Unit,
    onFastForwardHold: () -> Unit,
    accentColor: Color = AcidGreen
) {
    val view = LocalView.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(GunmetalDark)
            .border(1.dp, BorderHardware)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // PREV
        MechanicalButton(
            label = "PREV",
            onClick = {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onPrevious()
            }
        ) {
            Icon(Icons.Default.SkipPrevious, contentDescription = "Previous", tint = TextPrimary, modifier = Modifier.size(18.dp))
        }

        // REWIND
        MechanicalButton(
            label = "REW",
            onClick = {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onRewindHold()
            }
        ) {
            Icon(Icons.Default.FastRewind, contentDescription = "Rewind", tint = TextPrimary, modifier = Modifier.size(18.dp))
        }

        // STOP
        MechanicalButton(
            label = "STOP",
            onClick = {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onStop()
            }
        ) {
            Icon(Icons.Default.Stop, contentDescription = "Stop", tint = DangerRed, modifier = Modifier.size(18.dp))
        }

        // PLAY / PAUSE (Main large button)
        MechanicalButton(
            label = if (isPlaying) "PAUSE" else "PLAY",
            isPrimary = true,
            activeColor = accentColor,
            onClick = {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onPlayPause()
            }
        ) {
            Icon(
                if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = if (isPlaying) MatteBlack else accentColor,
                modifier = Modifier.size(24.dp)
            )
        }

        // FAST FORWARD
        MechanicalButton(
            label = "FFWD",
            onClick = {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onFastForwardHold()
            }
        ) {
            Icon(Icons.Default.FastForward, contentDescription = "Fast Forward", tint = TextPrimary, modifier = Modifier.size(18.dp))
        }

        // NEXT
        MechanicalButton(
            label = "NEXT",
            onClick = {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onNext()
            }
        ) {
            Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = TextPrimary, modifier = Modifier.size(18.dp))
        }
    }
}

// 1990s physical beveled push button
@Composable
fun MechanicalButton(
    label: String,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false,
    activeColor: Color = AcidGreen,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val bgBrush = if (isPressed) {
        Brush.verticalGradient(listOf(GunmetalDark, MatteBlack))
    } else if (isPrimary) {
        Brush.verticalGradient(listOf(GunmetalLight, GunmetalDark))
    } else {
        Brush.verticalGradient(listOf(GunmetalMedium, GunmetalDark))
    }

    val shadowElevation = if (isPressed) 1.dp else 4.dp
    val topBorderColor = if (isPressed) BevelShadow else BevelLight
    val bottomBorderColor = if (isPressed) BevelLight else BevelShadow

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(if (isPrimary) 52.dp else 44.dp)
                .shadow(shadowElevation, RoundedCornerShape(4.dp))
                .clip(RoundedCornerShape(4.dp))
                .background(bgBrush)
                .border(1.dp, topBorderColor, RoundedCornerShape(4.dp))
                .border(2.dp, bottomBorderColor, RoundedCornerShape(4.dp))
                .clickable(interactionSource = interactionSource, indication = null) { onClick() }
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontFamily = ChakraPetchFont,
            fontSize = 9.sp,
            color = if (isPressed) activeColor else TextSecondary,
            fontWeight = FontWeight.Bold
        )
    }
}

// Vintage Dual Analog / Segment VU Meter
@Composable
fun StereoVuMeters(
    leftLevel: Float,
    rightLevel: Float,
    modifier: Modifier = Modifier,
    accentColor: Color = AcidGreen
) {
    Column(
        modifier = modifier
            .background(MatteBlack)
            .border(1.dp, BorderHardware, RoundedCornerShape(2.dp))
            .padding(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "L [dB]", fontFamily = ShareTechMonoFont, fontSize = 9.sp, color = TextMuted)
            Text(text = "-20  -10  -5   0  +3", fontFamily = ShareTechMonoFont, fontSize = 9.sp, color = TextMuted)
            Text(text = "R [dB]", fontFamily = ShareTechMonoFont, fontSize = 9.sp, color = TextMuted)
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Left Channel Bar
        VuSegmentBar(level = leftLevel, accentColor = accentColor)
        Spacer(modifier = Modifier.height(3.dp))
        // Right Channel Bar
        VuSegmentBar(level = rightLevel, accentColor = accentColor)
    }
}

@Composable
fun VuSegmentBar(
    level: Float,
    accentColor: Color = AcidGreen,
    segmentCount: Int = 20
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(7.dp)
            .background(GunmetalDark),
        horizontalArrangement = Arrangement.spacedBy(1.5.dp)
    ) {
        val litCount = (level.coerceIn(0f, 1f) * segmentCount).toInt()
        for (i in 0 until segmentCount) {
            val isLit = i <= litCount
            val segColor = when {
                i >= segmentCount - 3 -> DangerRed
                i >= segmentCount - 7 -> BurntOrange
                else -> accentColor
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(if (isLit) segColor else segColor.copy(alpha = 0.15f))
            )
        }
    }
}

// Interactive Cassette Player Tape Window with Spools, Tape Thickness Physics & Stickers
@Composable
fun InteractiveCassetteDeck(
    title: String,
    artist: String,
    currentSide: TapeSide,
    isPlaying: Boolean,
    progress: Float, // 0.0f to 1.0f
    tapeCounter: Int,
    isInserted: Boolean = true,
    shellColorHex: String = "#16191C",
    reelColorHex: String = "#00FF66",
    isTransparent: Boolean = true,
    stickers: List<StickerItem> = emptyList(),
    onSeekDrag: (Float) -> Unit = {},
    onFlipSide: () -> Unit = {},
    onEjectClick: () -> Unit = {},
    onInsertClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val shellColor = remember(shellColorHex) { Color(android.graphics.Color.parseColor(shellColorHex)) }
    val reelColor = remember(reelColorHex) { Color(android.graphics.Color.parseColor(reelColorHex)) }

    // Reel physics: Side A moves tape from Left to Right; Side B moves tape from Right to Left!
    val effProgress = if (currentSide == TapeSide.SIDE_A) progress else (1.0f - progress)
    val leftReelRadiusFactor = 1.0f - (effProgress * 0.70f)
    val rightReelRadiusFactor = 0.30f + (effProgress * 0.70f)

    var reelRotationAngle by remember { mutableFloatStateOf(0f) }
    if (isPlaying && isInserted) {
        val transition = rememberInfiniteTransition(label = "tape_spin")
        val spinAngle by transition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(2400, easing = LinearEasing),
                repeatMode = AnimRepeatMode.Restart
            ),
            label = "spin"
        )
        reelRotationAngle = spinAngle
    }

    if (!isInserted) {
        // Ejected Cassette Well / Open Door Compartment
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(230.dp)
                .shadow(6.dp, RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
                .background(GunmetalDark)
                .border(2.dp, BorderHardware, RoundedCornerShape(8.dp))
                .clickable {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    onInsertClick()
                }
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Mechanical Open Well Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CASSETTE COMPARTMENT // DOOR OPEN",
                        fontFamily = ChakraPetchFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = BurntOrange
                    )
                    Box(
                        modifier = Modifier
                            .background(MatteBlack)
                            .border(1.dp, BurntOrange, RoundedCornerShape(2.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "TRAY EJECTED",
                            fontFamily = ShareTechMonoFont,
                            fontSize = 9.sp,
                            color = BurntOrange
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tape Head Block & Dual Drive Spindles Schematic
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(MatteBlack)
                        .border(1.dp, BorderHardware, RoundedCornerShape(4.dp))
                ) {
                    val cw = size.width
                    val ch = size.height
                    val leftSpindle = Offset(cw * 0.28f, ch * 0.45f)
                    val rightSpindle = Offset(cw * 0.72f, ch * 0.45f)

                    // Spindle gears
                    drawCircle(color = GunmetalLight, radius = 22f, center = leftSpindle)
                    drawCircle(color = GunmetalLight, radius = 22f, center = rightSpindle)
                    drawCircle(color = BurntOrange, radius = 8f, center = leftSpindle)
                    drawCircle(color = BurntOrange, radius = 8f, center = rightSpindle)

                    // Center tape head block
                    val headCenter = Offset(cw * 0.5f, ch * 0.78f)
                    drawRoundRect(
                        color = GunmetalLight,
                        topLeft = Offset(headCenter.x - 40f, headCenter.y - 14f),
                        size = Size(80f, 26f),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                    // Mu-metal audio read head
                    drawRect(
                        color = LightAluminium,
                        topLeft = Offset(headCenter.x - 14f, headCenter.y - 12f),
                        size = Size(28f, 18f)
                    )
                    // Capstan pinch rollers
                    drawCircle(color = MatteBlack, radius = 10f, center = Offset(cw * 0.20f, ch * 0.78f))
                    drawCircle(color = MatteBlack, radius = 10f, center = Offset(cw * 0.80f, ch * 0.78f))
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Insert Cassette Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(AcidGreen)
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            onInsertClick()
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "INSERT CASSETTE TAPE [CLICK DOOR]",
                        fontFamily = ChakraPetchFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MatteBlack
                    )
                }
            }
        }
    } else {
        BoxWithConstraints(
            modifier = modifier
                .fillMaxWidth()
                .height(230.dp)
                .shadow(6.dp, RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
                .background(shellColor)
                .border(2.dp, BorderHardware, RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            val w = maxWidth
            val h = maxHeight

            Column(modifier = Modifier.fillMaxSize()) {
                // Upper Cassette Label / Spine
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .background(GunmetalMedium)
                        .border(1.dp, BorderHardware, RoundedCornerShape(3.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = title.ifBlank { "TAPE-99 MASTER" },
                                fontFamily = ChakraPetchFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = artist.ifBlank { "UNDERGROUND SOUNDSYSTEM" },
                                fontFamily = ShareTechMonoFont,
                                fontSize = 10.sp,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Side Indicator, Flip Button & Eject Button
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(MatteBlack)
                                    .border(1.dp, AcidGreen, RoundedCornerShape(3.dp))
                                    .clickable {
                                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                        onFlipSide()
                                    }
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (currentSide == TapeSide.SIDE_A) "SIDE A [FLIP]" else "SIDE B [FLIP]",
                                    fontFamily = ChakraPetchFont,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    color = AcidGreen
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .background(GunmetalDark)
                                    .border(1.dp, DangerRed, RoundedCornerShape(3.dp))
                                    .clickable {
                                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                        onEjectClick()
                                    }
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "EJECT",
                                    fontFamily = ChakraPetchFont,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    color = DangerRed
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

            // Transparent Center Tape Window with Spools
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(if (isTransparent) MatteBlack.copy(alpha = 0.85f) else GunmetalDark)
                    .border(2.dp, GunmetalMedium, RoundedCornerShape(4.dp))
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val deltaNorm = dragAmount.x / size.width.toFloat()
                            onSeekDrag(deltaNorm)
                        }
                    }
            ) {
                // Canvas drawing dual tape spools, tape bridge, and tape packs
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cw = size.width
                    val ch = size.height
                    val leftCenter = Offset(cw * 0.28f, ch * 0.50f)
                    val rightCenter = Offset(cw * 0.72f, ch * 0.50f)
                    val maxTapeRadius = ch * 0.38f
                    val hubRadius = ch * 0.16f

                    // 1. Magnetic tape pack (dark brown / black tape coil)
                    // Left spool tape
                    drawCircle(
                        color = Color(0xFF1E1712),
                        radius = hubRadius + (maxTapeRadius - hubRadius) * leftReelRadiusFactor,
                        center = leftCenter
                    )
                    // Right spool tape
                    drawCircle(
                        color = Color(0xFF1E1712),
                        radius = hubRadius + (maxTapeRadius - hubRadius) * rightReelRadiusFactor,
                        center = rightCenter
                    )

                    // 2. Hub spools (toothed plastic reels)
                    drawCircle(color = reelColor, radius = hubRadius, center = leftCenter)
                    drawCircle(color = reelColor, radius = hubRadius, center = rightCenter)

                    // Inner teeth holes
                    drawCircle(color = MatteBlack, radius = hubRadius * 0.5f, center = leftCenter)
                    drawCircle(color = MatteBlack, radius = hubRadius * 0.5f, center = rightCenter)

                    // 3. Spool Spokes (3-spoke rotation indicator)
                    val spokeRad = hubRadius * 0.85f
                    for (angle in listOf(0f, 120f, 240f)) {
                        val radL = (reelRotationAngle + angle) * (PI.toFloat() / 180f)
                        val radR = (reelRotationAngle * 1.2f + angle) * (PI.toFloat() / 180f)

                        drawLine(
                            color = MatteBlack,
                            start = leftCenter,
                            end = Offset(leftCenter.x + cos(radL) * spokeRad, leftCenter.y + sin(radL) * spokeRad),
                            strokeWidth = 3f
                        )
                        drawLine(
                            color = MatteBlack,
                            start = rightCenter,
                            end = Offset(rightCenter.x + cos(radR) * spokeRad, rightCenter.y + sin(radR) * spokeRad),
                            strokeWidth = 3f
                        )
                    }

                    // 4. Center ruler tape window markings (0, 50, 100)
                    val rulerY = ch * 0.5f
                    drawLine(color = Color(0x66FFFFFF), start = Offset(cw * 0.45f, rulerY - 10f), end = Offset(cw * 0.45f, rulerY + 10f), strokeWidth = 2f)
                    drawLine(color = AcidGreen, start = Offset(cw * 0.50f, rulerY - 14f), end = Offset(cw * 0.50f, rulerY + 14f), strokeWidth = 2f)
                    drawLine(color = Color(0x66FFFFFF), start = Offset(cw * 0.55f, rulerY - 10f), end = Offset(cw * 0.55f, rulerY + 10f), strokeWidth = 2f)
                }

                // Render photocopied stickers overlay
                stickers.forEach { sticker ->
                    Box(
                        modifier = Modifier
                            .offset(
                                x = (w * sticker.xPercent) - 30.dp,
                                y = (h * sticker.yPercent * 0.45f)
                            )
                            .rotate(sticker.rotationDeg)
                            .background(BurntOrange)
                            .border(1.dp, MatteBlack, RoundedCornerShape(1.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = sticker.name,
                            fontFamily = ChakraPetchFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            color = MatteBlack
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Bottom Pressure Pad & Screw details
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cassette Screw
                CassetteScrew()
                Text(
                    text = "TYPE II HIGH BIAS // C-60",
                    fontFamily = ShareTechMonoFont,
                    fontSize = 9.sp,
                    color = TextMuted
                )
                // Cassette Screw
                CassetteScrew()
            }
        }
    }
}
}

@Composable
fun CassetteScrew() {
    Box(
        modifier = Modifier
            .size(7.dp)
            .clip(CircleShape)
            .background(LightAluminium)
            .border(0.5.dp, BevelShadow, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(5.dp)
                .height(1.dp)
                .background(MatteBlack)
        )
    }
}

// Discman Player Graphic with Rotating CD, Anti-Skip ESP 45s buffer, and LCD
@Composable
fun DiscmanGraphic(
    title: String,
    artist: String,
    isPlaying: Boolean,
    rotationAngle: Float,
    antiSkipPercent: Float = 0.95f,
    onOpenJewelCase: () -> Unit = {}
) {
    val view = LocalView.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MatteBlack)
            .border(1.dp, BorderHardware, RoundedCornerShape(6.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // LCD Segment Panel
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F2228))
                .border(2.dp, BorderHardware, RoundedCornerShape(4.dp))
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "COMPACT DISC DIGITAL AUDIO",
                        fontFamily = ShareTechMonoFont,
                        fontSize = 9.sp,
                        color = LcdCyan.copy(alpha = 0.7f)
                    )
                    Text(
                        text = title.ifBlank { "NO DISC LOADED" },
                        fontFamily = ChakraPetchFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = LcdCyan,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "ESP [45s BUFFER]",
                        fontFamily = ShareTechMonoFont,
                        fontSize = 9.sp,
                        color = AcidGreen
                    )
                    Text(
                        text = if (isPlaying) "PLAY >>" else "PAUSE ||",
                        fontFamily = ShareTechMonoFont,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPlaying) AcidGreen else BurntOrange
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Clamshell Circular Disc Platter
        Box(
            modifier = Modifier
                .size(190.dp)
                .shadow(8.dp, CircleShape)
                .clip(CircleShape)
                .background(GunmetalMedium)
                .border(3.dp, GunmetalLight, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Rotating Compact Disc with holographic reflections
            Canvas(
                modifier = Modifier
                    .size(176.dp)
                    .rotate(rotationAngle)
            ) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val discR = size.width / 2f
                val holeR = discR * 0.22f

                // Outer silver disc
                drawCircle(
                    brush = Brush.sweepGradient(
                        listOf(
                            Color(0xFFD6DBDE),
                            Color(0xFFA6B0B8),
                            Color(0xFF86A3B8), // iridescent cyan/blue sheen
                            Color(0xFFD6DBDE),
                            Color(0xFFB896B2), // iridescent purple sheen
                            Color(0xFFD6DBDE)
                        ),
                        center
                    ),
                    radius = discR,
                    center = center
                )

                // Laser track grooves
                drawCircle(color = Color(0x22000000), radius = discR * 0.85f, center = center, style = Stroke(1f))
                drawCircle(color = Color(0x22000000), radius = discR * 0.70f, center = center, style = Stroke(1f))
                drawCircle(color = Color(0x22000000), radius = discR * 0.55f, center = center, style = Stroke(1f))

                // Plastic inner ring
                drawCircle(color = Color(0x99FFFFFF), radius = holeR * 1.5f, center = center)

                // Center spindle hole
                drawCircle(color = MatteBlack, radius = holeR, center = center)
            }

            // Jewel Case / Liner Notes Quick Button in center spindle
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MatteBlack)
                    .border(1.dp, AcidGreen, CircleShape)
                    .clickable {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        onOpenJewelCase()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "INFO",
                    fontFamily = ChakraPetchFont,
                    fontSize = 10.sp,
                    color = AcidGreen,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ESP Buffer Level
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "ELECTRONIC SHOCK PROTECTION", fontFamily = ShareTechMonoFont, fontSize = 9.sp, color = TextMuted)
            Text(text = "100% OK", fontFamily = ShareTechMonoFont, fontSize = 9.sp, color = AcidGreen)
        }
        Spacer(modifier = Modifier.height(3.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(GunmetalMedium)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(antiSkipPercent)
                    .fillMaxHeight()
                    .background(AcidGreen)
            )
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
            .clip(RoundedCornerShape(3.dp))
            .background(GunmetalDark)
            .border(1.dp, color.copy(alpha = 0.6f), RoundedCornerShape(3.dp))
            .clickable {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onClick()
            }
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Column {
            Text(
                text = code,
                fontFamily = ShareTechMonoFont,
                fontSize = 8.sp,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                fontFamily = ChakraPetchFont,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun LibraryTrackRow(
    index: Int,
    track: com.example.model.Track,
    isCurrent: Boolean,
    isPlaying: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelectable: Boolean = false,
    isSelected: Boolean = false,
    onToggleSelect: () -> Unit = {}
) {
    val view = LocalView.current
    val min = (track.durationMs / 1000) / 60
    val sec = (track.durationMs / 1000) % 60

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(2.dp))
            .background(if (isSelected) GunmetalLight else if (isCurrent) GunmetalLight else GunmetalDark)
            .border(
                1.dp,
                if (isSelected) BurntOrange else if (isCurrent) accentColor else BorderHardware,
                RoundedCornerShape(2.dp)
            )
            .clickable {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                if (isSelectable) onToggleSelect() else onClick()
            }
            .padding(horizontal = 10.dp, vertical = 7.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSelectable) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (isSelected) BurntOrange else MatteBlack)
                            .border(1.dp, if (isSelected) BurntOrange else BorderHardware, RoundedCornerShape(2.dp))
                            .clickable { onToggleSelect() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Text("✓", color = MatteBlack, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = String.format("%02d", index),
                    fontFamily = ShareTechMonoFont,
                    fontSize = 11.sp,
                    color = if (isCurrent) accentColor else TextMuted
                )
                Spacer(modifier = Modifier.width(8.dp))
                if (isPlaying) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(AcidGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Column {
                    Text(
                        text = track.title,
                        fontFamily = ChakraPetchFont,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 12.sp,
                        color = if (isCurrent) accentColor else TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${track.artist} // ${track.album}",
                        fontFamily = ShareTechMonoFont,
                        fontSize = 10.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(2.dp))
                        .background(MatteBlack)
                        .border(
                            0.5.dp,
                            if (track.audioFormat == "FLAC" || track.audioFormat == "WAV") BurntOrange else BorderHardware,
                            RoundedCornerShape(2.dp)
                        )
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = track.audioFormat,
                        fontFamily = ShareTechMonoFont,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (track.audioFormat == "FLAC" || track.audioFormat == "WAV") BurntOrange else TextSecondary
                    )
                }
                Text(
                    text = String.format("%02d:%02d", min, sec),
                    fontFamily = ShareTechMonoFont,
                    fontSize = 10.sp,
                    color = if (isCurrent) accentColor else TextMuted
                )
            }
        }
    }
}

@Composable
fun HardwarePushSwitch(
    label: String,
    code: String,
    isActive: Boolean,
    activeColor: Color = AcidGreen,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(if (isActive) GunmetalLight else GunmetalDark)
            .border(1.5.dp, if (isActive) activeColor else BorderHardware, RoundedCornerShape(3.dp))
            .clickable {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onClick()
            }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (isActive) activeColor else Color(0xFF22262B))
                        .border(0.5.dp, if (isActive) BevelLight else Color(0xFF111111), CircleShape)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label,
                    fontFamily = ChakraPetchFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = if (isActive) TextPrimary else TextSecondary,
                    maxLines = 1
                )
            }
            Text(
                text = code,
                fontFamily = ShareTechMonoFont,
                fontSize = 8.sp,
                color = if (isActive) activeColor else TextMuted
            )
        }
    }
}

@Composable
fun HardwareSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = AcidGreen,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f
) {
    androidx.compose.material3.Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange,
        colors = androidx.compose.material3.SliderDefaults.colors(
            thumbColor = activeColor,
            activeTrackColor = activeColor,
            inactiveTrackColor = GunmetalLight
        ),
        modifier = modifier
    )
}


