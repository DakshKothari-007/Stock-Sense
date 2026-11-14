package com.simats.stocksense.feature.inventory.icon

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.stocksense.core.theme.DarkSurface

/**
 * Universal product avatar composable used throughout StockSense:
 * Inventory, Product Detail, Insights, Reorder, Sales, Purchase Orders, and Dukaan Mitra cards.
 *
 * Visual specifications:
 * - Rounded square with 16 dp radius (or scaled for smaller sizes)
 * - Soft tinted background per product department
 * - High contrast & readable in both Light and Dark themes
 * - Accessible content description: "{name} icon"
 * - Fallback order:
 *     1. Saved iconKey (from server or user selection)
 *     2. Intelligent matcher (confidence >= threshold)
 *     3. Department icon from category
 *     4. Generic package icon
 *     5. Letter avatar fallback
 */
@Composable
fun ProductAvatar(
    name: String?,
    category: String? = null,
    iconKey: String? = null,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier,
    useLetterFallbackOnly: Boolean = false
) {
    val cleanName = name?.trim().orEmpty()
    val desc = if (cleanName.isNotBlank()) "$cleanName icon" else "Product icon"

    // 1. Resolve Department for background tinting
    val department = remember(cleanName, category, iconKey) {
        ProductIconMatcher.resolveDepartment(cleanName, category, iconKey)
    }

    // 2. Resolve Drawable according to fallback order
    val drawableRes = remember(cleanName, category, iconKey, useLetterFallbackOnly) {
        if (useLetterFallbackOnly) {
            0
        } else {
            val res = ProductIconMatcher.resolveDrawableRes(cleanName, category, iconKey)
            if (res == 0 && !iconKey.isNullOrBlank()) {
                try {
                    android.util.Log.w("ProductAvatar", "Icon key '$iconKey' resolved to no drawable for product '$cleanName'")
                } catch (_: Throwable) {
                    // Safe in JVM unit tests
                }
            }
            res
        }
    }

    // Determine theme background
    val isDark = isSystemInDarkTheme() || MaterialTheme.colorScheme.surface == DarkSurface
    val bgColor = if (isDark) department.darkBg else department.lightBg
    val cornerRadius = if (size < 40.dp) 12.dp else 16.dp

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .background(bgColor)
            .semantics { contentDescription = desc },
        contentAlignment = Alignment.Center
    ) {
        if (drawableRes != 0) {
            val iconInnerSize = size * 0.65f
            Image(
                painter = painterResource(id = drawableRes),
                contentDescription = null,
                modifier = Modifier.size(iconInnerSize),
                contentScale = ContentScale.Fit
            )
        } else {
            // Letter avatar fallback
            val initial = cleanName.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
            val fontSize = (size.value * 0.42f).sp
            Text(
                text = initial,
                color = department.badgeTextColor,
                fontWeight = FontWeight.Bold,
                fontSize = fontSize
            )
        }
    }
}

