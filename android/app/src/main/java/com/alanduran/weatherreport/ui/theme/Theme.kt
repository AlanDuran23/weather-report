package com.alanduran.weatherreport.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val WeatherColorScheme = darkColorScheme(
    primary = VioletAccent,
    onPrimary = Color.White,
    primaryContainer = NightPlum,
    onPrimaryContainer = TextPrimary,
    secondary = MagentaAccent,
    onSecondary = Color.White,
    tertiary = CyanAccent,
    background = NightIndigo,
    onBackground = TextPrimary,
    surface = NightViolet,
    onSurface = TextPrimary,
    surfaceVariant = NightPlum,
    onSurfaceVariant = TextSecondary,
    outline = GlassBorder
)

@Composable
fun WeatherReportTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = WeatherColorScheme,
        typography = WeatherTypography,
        content = content
    )
}
