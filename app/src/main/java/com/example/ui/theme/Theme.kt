package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val AetherCinemaColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = ObsidianVoid,
    primaryContainer = ObsidianElevated,
    onPrimaryContainer = ElectricCyan,
    secondary = CrimsonPulse,
    onSecondary = Color.White,
    secondaryContainer = CrimsonPulseDim,
    onSecondaryContainer = Color.White,
    tertiary = AuroraViolet,
    onTertiary = Color.White,
    background = ObsidianVoid,
    onBackground = CinemaWhite,
    surface = ObsidianSurface,
    onSurface = CinemaWhite,
    surfaceVariant = ObsidianCard,
    onSurfaceVariant = CinemaSilver,
    surfaceContainerLowest = ObsidianVoid,
    surfaceContainerLow = ObsidianSurface,
    surfaceContainer = ObsidianCard,
    surfaceContainerHigh = ObsidianElevated,
    outline = ObsidianBorder,
    error = CrimsonPulse,
    onError = Color.White
)

private val AetherShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = AetherCinemaColorScheme,
        typography = Typography,
        shapes = AetherShapes,
        content = content
    )
}
