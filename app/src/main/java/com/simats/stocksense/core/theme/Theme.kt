package com.simats.stocksense.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}

object ThemeState {
    private val _themeMode = MutableStateFlow(ThemeMode.LIGHT)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        // Enforce light theme only per design system specification
        _themeMode.value = ThemeMode.LIGHT
    }
}

/**
 * Bold Outline Light Color Scheme (Strictly light, no green, high contrast)
 */
private val BoldOutlineColorScheme = lightColorScheme(
    primary = MainYellow,
    onPrimary = InkDark,
    primaryContainer = SoftPeach,
    onPrimaryContainer = InkDark,
    secondary = MainBlue,
    onSecondary = Color.White,
    secondaryContainer = SoftLightBlue,
    onSecondaryContainer = InkDark,
    tertiary = SoftLavender,
    onTertiary = InkDark,
    background = CreamBackground,
    onBackground = InkDark,
    surface = CardWhite,
    onSurface = InkDark,
    surfaceVariant = SoftLightBlue,
    onSurfaceVariant = MutedInk,
    outline = OutlineInk,
    outlineVariant = OutlineInk,
    error = StatusDangerRed,
    onError = Color.White,
    errorContainer = SoftPink,
    onErrorContainer = InkDark
)

@Composable
fun StockSenseTheme(
    themeMode: ThemeMode = ThemeMode.LIGHT,
    content: @Composable () -> Unit
) {
    // Light theme only (disable dark theme / dynamic color)
    MaterialTheme(
        colorScheme = BoldOutlineColorScheme,
        typography = Typography,
        shapes = StockSenseShapes,
        content = content
    )
}
