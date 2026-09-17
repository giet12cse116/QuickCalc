package com.pp.Quickcalc.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF6366F1),
    onPrimary = Color.White,
    secondary = Color(0xFF4ADE80),
    background = Color(0xFFFFF9C4),
    surface = Color.White,
    onBackground = Color(0xFF2D2A4A),
    onSurface = Color(0xFF2D2A4A),
    error = Color(0xFFEF4444)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF818CF8),
    onPrimary = Color(0xFF1E1B2E),
    secondary = Color(0xFF34D399),
    background = Color(0xFF14121F),
    surface = Color(0xFF1F1B33),
    onBackground = Color(0xFFEDE9FE),
    onSurface = Color(0xFFEDE9FE),
    error = Color(0xFFF87171)
)

@Composable
fun NumflowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}
