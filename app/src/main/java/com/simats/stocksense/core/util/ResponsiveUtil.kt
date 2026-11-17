package com.simats.stocksense.core.util

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Standard Android Window Size Classes (Compact, Medium, Expanded).
 */
enum class WindowSizeClass {
    COMPACT,
    MEDIUM,
    EXPANDED
}

/**
 * Encapsulates responsive window metrics, screen sizes, orientations, and responsive dimensions.
 */
data class WindowSizeInfo(
    val widthClass: WindowSizeClass,
    val heightClass: WindowSizeClass,
    val screenWidthDp: Dp,
    val screenHeightDp: Dp,
    val isLandscape: Boolean,
    val isCompactWidth: Boolean,
    val isMediumWidth: Boolean,
    val isExpandedWidth: Boolean
) {
    /**
     * Maximum content width for readable forms and centered lists on wide screens.
     */
    val maxContentWidth: Dp
        get() = when (widthClass) {
            WindowSizeClass.COMPACT -> Dp.Unspecified
            WindowSizeClass.MEDIUM -> 600.dp
            WindowSizeClass.EXPANDED -> 720.dp
        }

    /**
     * Responsive horizontal page padding based on screen size.
     */
    val horizontalPadding: Dp
        get() = when {
            screenWidthDp < 360.dp -> 14.dp
            isCompactWidth -> 20.dp
            isMediumWidth -> 24.dp
            else -> 32.dp
        }

    /**
     * Recommended column count for stat cards / grids.
     */
    val statCardColumnCount: Int
        get() = when {
            screenWidthDp < 340.dp -> 1
            isExpandedWidth || (isLandscape && screenWidthDp >= 600.dp) -> 4
            isMediumWidth || isLandscape -> 3
            else -> 2
        }
}

/**
 * Collects responsive screen metrics from LocalConfiguration.
 */
@Composable
fun rememberWindowSizeInfo(): WindowSizeInfo {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp

    val widthClass = when {
        configuration.screenWidthDp < 600 -> WindowSizeClass.COMPACT
        configuration.screenWidthDp < 840 -> WindowSizeClass.MEDIUM
        else -> WindowSizeClass.EXPANDED
    }

    val heightClass = when {
        configuration.screenHeightDp < 480 -> WindowSizeClass.COMPACT
        configuration.screenHeightDp < 900 -> WindowSizeClass.MEDIUM
        else -> WindowSizeClass.EXPANDED
    }

    return WindowSizeInfo(
        widthClass = widthClass,
        heightClass = heightClass,
        screenWidthDp = screenWidth,
        screenHeightDp = screenHeight,
        isLandscape = isLandscape,
        isCompactWidth = widthClass == WindowSizeClass.COMPACT,
        isMediumWidth = widthClass == WindowSizeClass.MEDIUM,
        isExpandedWidth = widthClass == WindowSizeClass.EXPANDED
    )
}

/**
 * Centers content horizontally with a maximum responsive width constraint (default 600 dp)
 * on wide/tablet/landscape screens, while expanding naturally on compact phone screens.
 */
@Composable
fun ResponsiveContainer(
    modifier: Modifier = Modifier,
    maxWidth: Dp = 600.dp,
    contentAlignment: Alignment = Alignment.TopCenter,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = contentAlignment
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = maxWidth),
            content = content
        )
    }
}
