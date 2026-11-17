package com.simats.stocksense.core.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Bold Outline shape tokens:
 * - 16dp radius standard for cards and dialogs
 * - 14dp radius for buttons and input fields
 * - 8-10dp radius for chips and badges
 */
val StockSenseShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(20.dp)
)
