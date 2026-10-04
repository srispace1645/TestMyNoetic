package com.testmynoetic.prep.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF2F6F73),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFCDE8E6),
    onPrimaryContainer = Color(0xFF0B3437),
    secondary = Color(0xFFB8662B),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFDCC2),
    onSecondaryContainer = Color(0xFF3B1D05),
    tertiary = Color(0xFF6B5FA8),
    background = Color(0xFFF7F2EA),
    onBackground = Color(0xFF1E1B16),
    surface = Color(0xFFFFFBF5),
    onSurface = Color(0xFF1E1B16),
    surfaceVariant = Color(0xFFEDE6DA),
    onSurfaceVariant = Color(0xFF4D463C),
    outline = Color(0xFF7F766A),
    outlineVariant = Color(0xFFD3CABD),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8FD0CC),
    onPrimary = Color(0xFF00363A),
    primaryContainer = Color(0xFF1E5256),
    onPrimaryContainer = Color(0xFFCDE8E6),
    secondary = Color(0xFFF2B48A),
    onSecondary = Color(0xFF4A2507),
    secondaryContainer = Color(0xFF6B3A12),
    onSecondaryContainer = Color(0xFFFFDCC2),
    tertiary = Color(0xFFC9BFFF),
    background = Color(0xFF141A1A),
    onBackground = Color(0xFFE6E1D9),
    surface = Color(0xFF1B2222),
    onSurface = Color(0xFFE6E1D9),
    surfaceVariant = Color(0xFF2A3333),
    onSurfaceVariant = Color(0xFFC9C2B6),
    outline = Color(0xFF948E84),
    outlineVariant = Color(0xFF444C4C),
)

@Composable
fun PrepTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors, content = content)
}

/** Green for right answers, readable in both light and dark mode. */
val rightColor: Color
    @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) Color(0xFF81C784) else Color(0xFF2E7D32)

/** Red for wrong answers, readable in both light and dark mode. */
val wrongColor: Color
    @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) Color(0xFFEF9A9A) else Color(0xFFC62828)
