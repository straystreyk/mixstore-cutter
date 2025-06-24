package com.pokrikinc.mixpokrikcutter.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF0061A4),
    onPrimary = Color.White,
    secondary = Color(0xFF5D9CEC),
    background = Color(0xFFF4F4F4),
    surface = Color.White,
    surfaceContainer = Color(0xFFF0F7FF),
    surfaceContainerHigh = Color(0xFFE1EDFA),
    surfaceContainerHighest = Color(0xFFD2E3F5),
    surfaceContainerLow = Color(0xFFF8FBFF),
    surfaceContainerLowest = Color(0xFFFFFFFF)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF90CAF9),
    onPrimary = Color.Black,
    secondary = Color(0xFF5D9CEC),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    surfaceContainer = Color(0xFF1A2A3A),
    surfaceContainerHigh = Color(0xFF223346),
    surfaceContainerHighest = Color(0xFF2A3D52),
    surfaceContainerLow = Color(0xFF14202E),
    surfaceContainerLowest = Color(0xFF0F1721)
)

@Composable
fun PokrikTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors,
        typography = Typography(),
        shapes = Shapes(),
        content = content
    )
}
