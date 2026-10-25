package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ReplayColorScheme = darkColorScheme(
    primary = AcidGreen,
    onPrimary = MatteBlack,
    primaryContainer = GunmetalLight,
    onPrimaryContainer = AcidGreen,
    secondary = BurntOrange,
    onSecondary = MatteBlack,
    secondaryContainer = GunmetalMedium,
    onSecondaryContainer = BurntOrange,
    tertiary = ElectricBlue,
    onTertiary = MatteBlack,
    background = MatteBlack,
    onBackground = TextPrimary,
    surface = SurfaceChassis,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceCard,
    onSurfaceVariant = TextSecondary,
    outline = BorderHardware
)

@Composable
fun ReplayTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ReplayColorScheme,
        typography = ReplayTypography,
        content = content
    )
}
