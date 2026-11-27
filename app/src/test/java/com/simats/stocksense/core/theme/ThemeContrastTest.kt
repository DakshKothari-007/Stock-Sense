package com.simats.stocksense.core.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.max
import kotlin.math.min

class ThemeContrastTest {

    private val originalColors = ThemeState.currentBoldColors

    @After
    fun tearDown() {
        ThemeState.currentBoldColors = originalColors
    }

    private fun contrastRatio(foreground: Color, background: Color): Double {
        val l1 = foreground.luminance().toDouble()
        val l2 = background.luminance().toDouble()
        val lighter = max(l1, l2)
        val darker = min(l1, l2)
        return (lighter + 0.05) / (darker + 0.05)
    }

    private fun assertWcagAA(label: String, foreground: Color, background: Color) {
        val ratio = contrastRatio(foreground, background)
        assertTrue(
            "WCAG AA contrast failed for $label: ratio=${"%.2f".format(ratio)} (< 4.5)",
            ratio >= 4.5
        )
    }

    @Test
    fun lightTheme_allTextBackgroundPairsMeetWcagAA() {
        ThemeState.currentBoldColors = LightBoldColors

        val neutralBackgrounds = listOf(
            "light.background" to LightBoldColors.background,
            "light.cardSurface" to LightBoldColors.cardSurface,
            "light.raisedSurface" to LightBoldColors.raisedSurface
        )
        for ((name, bg) in neutralBackgrounds) {
            assertWcagAA("inkText on $name", LightBoldColors.inkText, bg)
            assertWcagAA("mutedText on $name", LightBoldColors.mutedText, bg)
            assertWcagAA("boldContentColorFor($name)", boldContentColorFor(bg), bg)
            assertWcagAA("boldMutedContentColorFor($name)", boldMutedContentColorFor(bg), bg)
        }

        val pastelBackgrounds = listOf(
            "light.softLavender" to LightBoldColors.softLavender,
            "light.softPeach" to LightBoldColors.softPeach,
            "light.paleBlue" to LightBoldColors.paleBlue,
            "light.softLightBlue" to LightBoldColors.softLightBlue,
            "light.softPink" to LightBoldColors.softPink,
            "light.mainYellow" to LightBoldColors.mainYellow,
            "light.statusInStock" to LightBoldColors.statusInStock,
            "light.statusLowStock" to LightBoldColors.statusLowStock
        )
        for ((name, bg) in pastelBackgrounds) {
            assertWcagAA("OnPastelInk on $name", OnPastelInk, bg)
            assertWcagAA("OnPastelMuted on $name", OnPastelMuted, bg)
            assertWcagAA("boldContentColorFor($name)", boldContentColorFor(bg), bg)
            assertWcagAA("boldMutedContentColorFor($name)", boldMutedContentColorFor(bg), bg)
        }

        assertWcagAA("OnPastelInk on light.statusDangerRed", OnPastelInk, LightBoldColors.statusDangerRed)
        assertWcagAA("White on light.mainBlue", Color.White, LightBoldColors.mainBlue)
    }

    @Test
    fun darkTheme_allTextBackgroundPairsMeetWcagAA() {
        ThemeState.currentBoldColors = DarkBoldColors

        val darkBackgrounds = listOf(
            "dark.background" to DarkBoldColors.background,
            "dark.cardSurface" to DarkBoldColors.cardSurface,
            "dark.raisedSurface" to DarkBoldColors.raisedSurface,
            "dark.softPink" to DarkBoldColors.softPink
        )
        for ((name, bg) in darkBackgrounds) {
            assertWcagAA("inkText on $name", DarkBoldColors.inkText, bg)
            assertWcagAA("mutedText on $name", DarkBoldColors.mutedText, bg)
            assertWcagAA("boldContentColorFor($name)", boldContentColorFor(bg), bg)
            assertWcagAA("boldMutedContentColorFor($name)", boldMutedContentColorFor(bg), bg)
        }

        val darkThemeLightPastels = listOf(
            "dark.softLavender" to DarkBoldColors.softLavender,
            "dark.softPeach" to DarkBoldColors.softPeach,
            "dark.paleBlue" to DarkBoldColors.paleBlue,
            "dark.softLightBlue" to DarkBoldColors.softLightBlue,
            "dark.mainYellow" to DarkBoldColors.mainYellow,
            "dark.statusInStock" to DarkBoldColors.statusInStock,
            "dark.statusLowStock" to DarkBoldColors.statusLowStock
        )
        for ((name, bg) in darkThemeLightPastels) {
            assertWcagAA("OnPastelInk on $name", OnPastelInk, bg)
            assertWcagAA("OnPastelMuted on $name", OnPastelMuted, bg)
            assertWcagAA("boldContentColorFor($name)", boldContentColorFor(bg), bg)
            assertWcagAA("boldMutedContentColorFor($name)", boldMutedContentColorFor(bg), bg)
        }

        assertWcagAA("OnPastelInk on dark.statusDangerRed", OnPastelInk, DarkBoldColors.statusDangerRed)
        assertWcagAA("boldContentColorFor(dark.statusDangerRed)", boldContentColorFor(DarkBoldColors.statusDangerRed), DarkBoldColors.statusDangerRed)
        assertWcagAA("boldContentColorFor(dark.mainBlue)", boldContentColorFor(DarkBoldColors.mainBlue), DarkBoldColors.mainBlue)
    }
}
