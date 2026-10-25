package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

// Bundled Google Fonts from font-util
val ShareTechMonoFont = FontFamily(
    Font(R.font.share_tech_mono, FontWeight.Normal)
)

val ChakraPetchFont = FontFamily(
    Font(R.font.chakra_petch, FontWeight.Normal),
    Font(R.font.chakra_petch, FontWeight.Bold)
)

val ReplayTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = ChakraPetchFont,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        letterSpacing = 1.sp,
        color = TextPrimary
    ),
    displayMedium = TextStyle(
        fontFamily = ChakraPetchFont,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.5.sp,
        color = TextPrimary
    ),
    titleLarge = TextStyle(
        fontFamily = ChakraPetchFont,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.5.sp,
        color = TextPrimary
    ),
    titleMedium = TextStyle(
        fontFamily = ChakraPetchFont,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.25.sp,
        color = TextPrimary
    ),
    bodyLarge = TextStyle(
        fontFamily = ShareTechMonoFont,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
        color = TextPrimary
    ),
    bodyMedium = TextStyle(
        fontFamily = ShareTechMonoFont,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp,
        color = TextSecondary
    ),
    labelLarge = TextStyle(
        fontFamily = ChakraPetchFont,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        lineHeight = 16.sp,
        letterSpacing = 1.sp,
        color = TextPrimary
    ),
    labelSmall = TextStyle(
        fontFamily = ShareTechMonoFont,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp,
        lineHeight = 12.sp,
        letterSpacing = 0.5.sp,
        color = TextMuted
    )
)
