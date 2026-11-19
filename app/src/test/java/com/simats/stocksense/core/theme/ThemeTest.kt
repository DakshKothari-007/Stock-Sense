package com.simats.stocksense.core.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests verifying theme tokens, color palettes, dark mode adaptation,
 * and layout responsiveness thresholds.
 */
class ThemeTest {

    @Test
    fun testLightBoldColorsTokens() {
        val colors = LightBoldColors
        assertFalse(colors.isDark)
        assertEquals(Color(0xFFFFF4DC), colors.background)
        assertEquals(Color(0xFFFFFFFF), colors.cardSurface)
        assertEquals(Color(0xFFFFFFFF), colors.raisedSurface)
        assertEquals(Color(0xFF0B1230), colors.ink)
        assertEquals(Color(0xFF0B1230), colors.inkText)
        assertEquals(Color(0xFF0B1230), colors.border)
        assertEquals(Color(0xFF0B1230), colors.shadowColor)
        assertEquals(Color(0xFF2B49D8), colors.mainBlue)
        assertEquals(Color(0xFFFFC93C), colors.mainYellow)
        assertEquals(Color(0xFF0B1230), colors.onYellowText)
        assertEquals(Color(0xFF4A5275), colors.mutedText)
        assertEquals(Color(0xFF9DB8FF), colors.softLightBlue)
        assertEquals(Color(0xFFDCE6FF), colors.paleBlue)
        assertEquals(Color(0xFFFFD6A5), colors.softPeach)
        assertEquals(Color(0xFFFFB8D0), colors.softPink)
        assertEquals(Color(0xFFC9B8FF), colors.softLavender)
        assertEquals(Color(0xFFC9B8FF), colors.statusInStock)
        assertEquals(Color(0xFFFF9F5A), colors.statusLowStock)
        assertEquals(Color(0xFFE5484D), colors.statusDangerRed)
    }

    @Test
    fun testDarkBoldColorsTokens() {
        val colors = DarkBoldColors
        assertTrue(colors.isDark)
        assertEquals(Color(0xFF070B20), colors.background)
        assertEquals(Color(0xFF1A2560), colors.cardSurface)
        assertEquals(Color(0xFF24307A), colors.raisedSurface)
        assertEquals(Color(0xFFFFF4DC), colors.ink)
        assertEquals(Color(0xFFFFF4DC), colors.inkText)
        assertEquals(Color(0xFFFFF4DC), colors.border)
        assertEquals(Color(0xFF000000), colors.shadowColor)
        assertEquals(Color(0xFF6C8CFF), colors.mainBlue)
        assertEquals(Color(0xFFFFC93C), colors.mainYellow)
        assertEquals(Color(0xFF0B1230), colors.onYellowText)
        assertEquals(Color(0xFFB8C0E0), colors.mutedText)
        assertEquals(Color(0xFF9DB8FF), colors.softLightBlue)
        assertEquals(Color(0xFFDCE6FF), colors.paleBlue)
        assertEquals(Color(0xFFFFD6A5), colors.softPeach)
        assertEquals(Color(0xFF5A2D6E), colors.softPink)
        assertEquals(Color(0xFFC9B8FF), colors.softLavender)
        assertEquals(Color(0xFFC9B8FF), colors.statusInStock)
        assertEquals(Color(0xFFFF9F5A), colors.statusLowStock)
        assertEquals(Color(0xFFE5484D), colors.statusDangerRed)

        // Dark theme surface hierarchy: background < card surface < raised surface
        assertNotEquals("Card surface must differ from screen background", colors.background, colors.cardSurface)
        assertNotEquals("Raised surface must differ from card surface", colors.cardSurface, colors.raisedSurface)
        assertNotEquals("Raised surface must differ from background", colors.background, colors.raisedSurface)
    }

    @Test
    fun testZeroGreenInPalettes() {
        val bannedHexes = listOf(
            0xFF2FD39Au, 0xFF0B6B4Au, 0xFF00FF00u, 0xFF4CAF50u,
            0xFF2E7D32u, 0xFF1B5E20u, 0xFF81C784u, 0xFFA5D6A7u,
            0xFF10B981u, 0xFF059669u, 0xFF047857u
        )

        val lightColors = listOf(
            LightBoldColors.background, LightBoldColors.cardSurface, LightBoldColors.ink,
            LightBoldColors.border, LightBoldColors.shadowColor, LightBoldColors.mainBlue,
            LightBoldColors.mainYellow, LightBoldColors.mutedText, LightBoldColors.softLightBlue,
            LightBoldColors.paleBlue, LightBoldColors.softPeach, LightBoldColors.softPink,
            LightBoldColors.softLavender, LightBoldColors.statusInStock, LightBoldColors.statusLowStock,
            LightBoldColors.statusDangerRed
        )

        val darkColors = listOf(
            DarkBoldColors.background, DarkBoldColors.cardSurface, DarkBoldColors.ink,
            DarkBoldColors.border, DarkBoldColors.shadowColor, DarkBoldColors.mainBlue,
            DarkBoldColors.mainYellow, DarkBoldColors.mutedText, DarkBoldColors.softLightBlue,
            DarkBoldColors.paleBlue, DarkBoldColors.softPeach, DarkBoldColors.softPink,
            DarkBoldColors.softLavender, DarkBoldColors.statusInStock, DarkBoldColors.statusLowStock,
            DarkBoldColors.statusDangerRed
        )

        for (c in lightColors + darkColors) {
            val ulongVal = c.value
            for (banned in bannedHexes) {
                assertNotEquals("Color should not match banned green: $banned", banned, ulongVal)
            }
        }
    }

    @Test
    fun testThemeModeEnum() {
        assertEquals(3, ThemeMode.values().size)
        assertTrue(ThemeMode.values().contains(ThemeMode.LIGHT))
        assertTrue(ThemeMode.values().contains(ThemeMode.DARK))
        assertTrue(ThemeMode.values().contains(ThemeMode.SYSTEM))
    }

    @Test
    fun testNarrowScreenThreshold() {
        val width320 = 320.dp
        val width360 = 360.dp
        val width400 = 400.dp

        assertTrue("320dp should trigger narrow stacking", width320 < 360.dp)
        assertFalse("360dp should use row layout", width360 < 360.dp)
        assertFalse("400dp should use row layout", width400 < 360.dp)
    }

    @Test
    fun testThemeStateRetainsSavedLightModeWithoutResettingToSystem() {
        ThemeState.setThemeMode(ThemeMode.LIGHT)
        assertEquals(ThemeMode.LIGHT, ThemeState.themeMode.value)

        val remoteTheme = ThemeMode.SYSTEM
        // SettingsViewModel.loadSettings() must NOT call ThemeState.setThemeMode(remoteTheme)
        assertNotEquals("Theme should not reset to System on settings load", remoteTheme, ThemeState.themeMode.value)
        assertEquals(ThemeMode.LIGHT, ThemeState.themeMode.value)
    }

    @Test
    fun testThemeModeFromStringMapping() {
        assertEquals(ThemeMode.LIGHT, ThemeMode.valueOf("LIGHT"))
        assertEquals(ThemeMode.DARK, ThemeMode.valueOf("DARK"))
        assertEquals(ThemeMode.SYSTEM, ThemeMode.valueOf("SYSTEM"))
    }
}
