package com.simats.stocksense.core.theme

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}

object ThemeState {
    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    var currentBoldColors: BoldColors by mutableStateOf(LightBoldColors)
        internal set

    val isDark: Boolean
        get() = currentBoldColors.isDark

    private var appContext: Context? = null
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun initialize(context: Context) {
        appContext = context.applicationContext
        coroutineScope.launch {
            try {
                val flow = ThemePreferences.getThemeMode(context.applicationContext)
                _themeMode.value = flow.first()
                flow.collect { savedMode ->
                    _themeMode.value = savedMode
                }
            } catch (_: Throwable) {
                // Safe fallback to default SYSTEM
            }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        appContext?.let { ctx ->
            coroutineScope.launch {
                try {
                    ThemePreferences.saveThemeMode(ctx, mode)
                } catch (_: Throwable) {
                    // Safe fallback
                }
            }
        }
    }
}

/**
 * Bold Outline Light Color Scheme
 */
private val BoldOutlineLightColorScheme = lightColorScheme(
    primary = Color(0xFFFFC93C),
    onPrimary = Color(0xFF0B1230),
    primaryContainer = Color(0xFFFFD6A5),
    onPrimaryContainer = Color(0xFF0B1230),
    secondary = Color(0xFF2B49D8),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF9DB8FF),
    onSecondaryContainer = Color(0xFF0B1230),
    tertiary = Color(0xFFC9B8FF),
    onTertiary = Color(0xFF0B1230),
    background = Color(0xFFFFF4DC),
    onBackground = Color(0xFF0B1230),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0B1230),
    surfaceVariant = Color(0xFF9DB8FF),
    onSurfaceVariant = Color(0xFF4A5275),
    outline = Color(0xFF0B1230),
    outlineVariant = Color(0xFF0B1230),
    error = Color(0xFFE5484D),
    onError = Color.White,
    errorContainer = Color(0xFFFFB8D0),
    onErrorContainer = Color(0xFF0B1230)
)

/**
 * Bold Outline Dark Color Scheme
 */
private val BoldOutlineDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFC93C),
    onPrimary = Color(0xFF0B1230),
    primaryContainer = Color(0xFF7A4A1E),
    onPrimaryContainer = Color(0xFFFFF4DC),
    secondary = Color(0xFF6C8CFF),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF2B3F8F),
    onSecondaryContainer = Color(0xFFFFF4DC),
    tertiary = Color(0xFF4B3A8C),
    onTertiary = Color(0xFFFFF4DC),
    background = Color(0xFF0B1230),
    onBackground = Color(0xFFFFF4DC),
    surface = Color(0xFF172050),
    onSurface = Color(0xFFFFF4DC),
    surfaceVariant = Color(0xFF2B3F8F),
    onSurfaceVariant = Color(0xFFB8C0E0),
    outline = Color(0xFFFFF4DC),
    outlineVariant = Color(0xFFFFF4DC),
    error = Color(0xFFE5484D),
    onError = Color.White,
    errorContainer = Color(0xFF5A2D6E),
    onErrorContainer = Color(0xFFFFF4DC)
)

@Composable
fun StockSenseTheme(
    themeMode: ThemeMode = ThemeState.themeMode.collectAsState().value,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> systemDark
    }

    val boldColors = if (isDark) DarkBoldColors else LightBoldColors
    ThemeState.currentBoldColors = boldColors
    val colorScheme = if (isDark) BoldOutlineDarkColorScheme else BoldOutlineLightColorScheme

    // Adjust system status bar and navigation bar contrast
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !isDark
                insetsController.isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    CompositionLocalProvider(
        LocalBoldColors provides boldColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = StockSenseShapes,
            content = content
        )
    }
}
