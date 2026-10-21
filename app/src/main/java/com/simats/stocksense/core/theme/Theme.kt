package com.simats.stocksense.core.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val StockSenseColors = lightColorScheme(
    primary = Green700,
    onPrimary = Color.White,
    primaryContainer = Green50,
    onPrimaryContainer = Green700,
    secondary = Teal500,
    background = ScreenBackground,
    onBackground = InkPrimary,
    surface = CardSurface,
    onSurface = InkPrimary,
    onSurfaceVariant = InkSecondary,
    outline = BorderGray,
    outlineVariant = Green100,
)

private val StockSenseShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
)

@Composable
fun StockSenseTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = StockSenseColors,
        typography = Typography,
        shapes = StockSenseShapes,
        content = content
    )
}
