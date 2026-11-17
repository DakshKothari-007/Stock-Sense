package com.simats.stocksense.core.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * StockSense "Bold Outline" Design System Tokens
 *
 * Light Palette:
 * - Background Cream: #FFF4DC
 * - Card / Surface: #FFFFFF
 * - Ink (Borders & Text): #0B1230
 * - Hard Shadow: #0B1230
 * - Muted Text: #4A5275 (>= 4.5:1 contrast)
 * - Main Blue: #2B49D8 (Text on Main Blue is white)
 * - Main Yellow: #FFC93C (Text on Yellow is #0B1230)
 * - Soft Light Blue: #9DB8FF
 * - Pale Blue: #DCE6FF
 * - Soft Peach: #FFD6A5
 * - Soft Pink: #FFB8D0
 * - Soft Lavender: #C9B8FF
 * - Status In Stock: #C9B8FF (Lavender)
 * - Status Low Stock: #FF9F5A (Orange)
 * - Status Danger Red: #E5484D (Red)
 *
 * Dark Palette:
 * - Background Navy: #0B1230
 * - Card / Surface: #172050
 * - Ink (Borders & Text): #FFF4DC (Cream)
 * - Hard Shadow: #050A1E (Darker than background)
 * - Muted Text: #B8C0E0 (>= 4.5:1 contrast)
 * - Main Blue: #6C8CFF (Lighter blue for dark background)
 * - Main Yellow: #FFC93C (Text on Yellow remains #0B1230)
 * - Soft Light Blue: #2B3F8F
 * - Pale Blue: #2B3F8F
 * - Soft Peach: #7A4A1E
 * - Soft Pink: #7A2E4C
 * - Soft Lavender: #4B3A8C
 * - Status In Stock: #C9B8FF
 * - Status Low Stock: #FF9F5A
 * - Status Danger Red: #E5484D
 *
 * Strictly NO GREEN anywhere in the application.
 */

data class BoldColors(
    val isDark: Boolean,
    val background: Color,
    val cardSurface: Color,
    val ink: Color,
    val inkText: Color,
    val border: Color,
    val shadowColor: Color,
    val mainBlue: Color,
    val mainYellow: Color,
    val onYellowText: Color,
    val mutedText: Color,
    val softLightBlue: Color,
    val paleBlue: Color,
    val softPeach: Color,
    val softPink: Color,
    val softLavender: Color,
    val statusInStock: Color,
    val statusLowStock: Color,
    val statusDangerRed: Color
)

val LightBoldColors = BoldColors(
    isDark = false,
    background = Color(0xFFFFF4DC),
    cardSurface = Color(0xFFFFFFFF),
    ink = Color(0xFF0B1230),
    inkText = Color(0xFF0B1230),
    border = Color(0xFF0B1230),
    shadowColor = Color(0xFF0B1230),
    mainBlue = Color(0xFF2B49D8),
    mainYellow = Color(0xFFFFC93C),
    onYellowText = Color(0xFF0B1230),
    mutedText = Color(0xFF4A5275),
    softLightBlue = Color(0xFF9DB8FF),
    paleBlue = Color(0xFFDCE6FF),
    softPeach = Color(0xFFFFD6A5),
    softPink = Color(0xFFFFB8D0),
    softLavender = Color(0xFFC9B8FF),
    statusInStock = Color(0xFFC9B8FF),
    statusLowStock = Color(0xFFFF9F5A),
    statusDangerRed = Color(0xFFE5484D)
)

val DarkBoldColors = BoldColors(
    isDark = true,
    background = Color(0xFF0B1230),
    cardSurface = Color(0xFF172050),
    ink = Color(0xFFFFF4DC),
    inkText = Color(0xFFFFF4DC),
    border = Color(0xFFFFF4DC),
    shadowColor = Color(0xFF050A1E),
    mainBlue = Color(0xFF6C8CFF),
    mainYellow = Color(0xFFFFC93C),
    onYellowText = Color(0xFF0B1230),
    mutedText = Color(0xFFB8C0E0),
    softLightBlue = Color(0xFF2B3F8F),
    paleBlue = Color(0xFF2B3F8F),
    softPeach = Color(0xFF7A4A1E),
    softPink = Color(0xFF7A2E4C),
    softLavender = Color(0xFF4B3A8C),
    statusInStock = Color(0xFFC9B8FF),
    statusLowStock = Color(0xFFFF9F5A),
    statusDangerRed = Color(0xFFE5484D)
)

val LocalBoldColors = staticCompositionLocalOf { LightBoldColors }

// Dynamic theme token properties that adapt automatically based on the current theme
val CreamBackground: Color
    get() = ThemeState.currentBoldColors.background

val InkDark: Color
    get() = ThemeState.currentBoldColors.ink

val MainYellow: Color
    get() = ThemeState.currentBoldColors.mainYellow

val MainBlue: Color
    get() = ThemeState.currentBoldColors.mainBlue

val CardWhite: Color
    get() = ThemeState.currentBoldColors.cardSurface

val SoftLightBlue: Color
    get() = ThemeState.currentBoldColors.softLightBlue

val PaleBlue: Color
    get() = ThemeState.currentBoldColors.paleBlue

val SoftPeach: Color
    get() = ThemeState.currentBoldColors.softPeach

val SoftPink: Color
    get() = ThemeState.currentBoldColors.softPink

val SoftLavender: Color
    get() = ThemeState.currentBoldColors.softLavender

val StatusInStock: Color
    get() = ThemeState.currentBoldColors.statusInStock

val StatusLowStock: Color
    get() = ThemeState.currentBoldColors.statusLowStock

val SoftLowStock: Color
    get() = ThemeState.currentBoldColors.statusLowStock

val StatusDangerRed: Color
    get() = ThemeState.currentBoldColors.statusDangerRed

val OutlineInk: Color
    get() = ThemeState.currentBoldColors.border

val ShadowColor: Color
    get() = ThemeState.currentBoldColors.shadowColor

val MutedInk: Color
    get() = ThemeState.currentBoldColors.mutedText

// Aliases for legacy references
val Green700: Color
    get() = ThemeState.currentBoldColors.mainBlue

val Teal500: Color
    get() = ThemeState.currentBoldColors.mainBlue

val Green50: Color
    get() = ThemeState.currentBoldColors.softLightBlue

val Green100: Color
    get() = ThemeState.currentBoldColors.border

val ScreenBackground: Color
    get() = ThemeState.currentBoldColors.background

val CardSurface: Color
    get() = ThemeState.currentBoldColors.cardSurface

val BorderGray: Color
    get() = ThemeState.currentBoldColors.border

val InkPrimary: Color
    get() = ThemeState.currentBoldColors.ink

val InkSecondary: Color
    get() = ThemeState.currentBoldColors.mutedText

val Amber700: Color
    get() = ThemeState.currentBoldColors.statusLowStock

val Amber800 = Color(0xFFD97706)
val Amber900 = Color(0xFF8A3B00)

val Amber50: Color
    get() = ThemeState.currentBoldColors.softPeach

val Amber100: Color
    get() = ThemeState.currentBoldColors.softPeach

val Neutral100: Color
    get() = ThemeState.currentBoldColors.softLavender

// Dark colors mapped to Bold Outline tokens
val DarkPrimary: Color
    get() = ThemeState.currentBoldColors.mainYellow

val DarkOnPrimary: Color
    get() = ThemeState.currentBoldColors.onYellowText

val DarkPrimaryContainer: Color
    get() = ThemeState.currentBoldColors.softPeach

val DarkOnPrimaryContainer: Color
    get() = ThemeState.currentBoldColors.ink

val DarkSecondary: Color
    get() = ThemeState.currentBoldColors.mainBlue

val DarkBackground: Color
    get() = ThemeState.currentBoldColors.background

val DarkOnBackground: Color
    get() = ThemeState.currentBoldColors.inkText

val DarkSurface: Color
    get() = ThemeState.currentBoldColors.cardSurface

val DarkOnSurface: Color
    get() = ThemeState.currentBoldColors.inkText

val DarkOnSurfaceVariant: Color
    get() = ThemeState.currentBoldColors.mutedText

val DarkOutline: Color
    get() = ThemeState.currentBoldColors.border

val DarkOutlineVariant: Color
    get() = ThemeState.currentBoldColors.border

val DarkAmber50: Color
    get() = ThemeState.currentBoldColors.softPeach

val DarkAmber700: Color
    get() = ThemeState.currentBoldColors.statusLowStock
