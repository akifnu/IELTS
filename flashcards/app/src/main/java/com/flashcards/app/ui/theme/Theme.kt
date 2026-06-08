package com.flashcards.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = ShinePrimary,
    onPrimary = Color.White,
    primaryContainer = ShinePrimaryLight,
    onPrimaryContainer = ShinePrimaryDark,
    secondary = ShineAccent,
    onSecondary = Color.White,
    background = ShineBackground,
    onBackground = Color(0xFF111827),
    surface = ShineSurface,
    onSurface = Color(0xFF111827),
    surfaceVariant = Color(0xFFF8F9FD),
    onSurfaceVariant = ShineOnSurfaceVariant,
    error = Color(0xFFEF4444),
)

private val DarkColors = darkColorScheme(
    primary = ShinePrimary,
    onPrimary = Color.White,
    primaryContainer = ShinePrimaryDark,
    onPrimaryContainer = ShinePrimaryLight,
    secondary = ShineAccent,
    onSecondary = Color.White,
    background = Color(0xFF0F1117),
    onBackground = Color(0xFFF4F6FB),
    surface = Color(0xFF1A1D27),
    onSurface = Color(0xFFF4F6FB),
    surfaceVariant = Color(0xFF252836),
    onSurfaceVariant = Color(0xFF9CA3AF),
    error = Color(0xFFEF4444),
)

@Composable
fun FlashcardsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
