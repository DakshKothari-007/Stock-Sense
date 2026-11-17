package com.simats.stocksense.core.theme

import androidx.compose.ui.graphics.Color

/**
 * StockSense "Bold Outline" Design System Tokens
 * Palette specifications:
 * - Background Cream: #FFF4DC
 * - Ink (Text & Borders): #0B1230
 * - Muted text: #4A5275
 * - Main Blue: #2B49D8 (Text on Main Blue must be white)
 * - Light Blue: #9DB8FF
 * - Pale Blue: #DCE6FF
 * - Main Yellow: #FFC93C
 * - Soft Boxes (Pastels): Light Blue #9DB8FF, Pale Blue #DCE6FF, Peach #FFD6A5, Pink #FFB8D0, Lavender #C9B8FF
 * - Status & Semantic: Low Stock Orange #FF9F5A, Danger Red #E5484D, In Stock Lavender #C9B8FF, White Surface #FFFFFF
 * - Strictly NO GREEN anywhere in the application.
 */

// Core Brand Tokens
val CreamBackground = Color(0xFFFFF4DC) // Canvas & App Background
val InkDark = Color(0xFF0B1230)         // Headings, body text, 2.5dp borders, hard shadows
val MainYellow = Color(0xFFFFC93C)      // Primary buttons, selected navigation pill, badges
val MainBlue = Color(0xFF2B49D8)        // Action links, AI Dukaan Mitra ring, user chat bubbles
val CardWhite = Color(0xFFFFFFFF)       // Surface cards, secondary buttons

// Soft Box Tints (Pastels)
val SoftLightBlue = Color(0xFF9DB8FF)   // Category boxes, metric card backgrounds
val PaleBlue = Color(0xFFDCE6FF)        // Very soft blue highlights, tint backgrounds
val SoftPeach = Color(0xFFFFD6A5)       // Highlights, alert backgrounds
val SoftPink = Color(0xFFFFB8D0)        // Secondary alerts, tags
val SoftLavender = Color(0xFFC9B8FF)    // Healthy stock, neutral highlights

// Semantic Status
val StatusInStock = Color(0xFFC9B8FF)   // In-stock chip fill (lavender)
val StatusLowStock = Color(0xFFFF9F5A)  // Low-stock warning (orange)
val StatusDangerRed = Color(0xFFE5484D) // Out-of-stock / destructive action (danger red)
val OutlineInk = Color(0xFF0B1230)      // Structural outlines and drop shadows
val MutedInk = Color(0xFF4A5275)        // Secondary text with >= 4.5:1 contrast on cream/white

// Aliases for legacy references to avoid broken builds and remove all green references
val Green700 = MainBlue
val Teal500 = MainBlue
val Green50 = SoftLightBlue
val Green100 = OutlineInk
val ScreenBackground = CreamBackground
val CardSurface = CardWhite
val BorderGray = OutlineInk
val InkPrimary = InkDark
val InkSecondary = MutedInk
val Amber700 = StatusLowStock
val Amber800 = Color(0xFFD97706)
val Amber900 = Color(0xFF8A3B00)
val Amber50 = SoftPeach
val Amber100 = SoftPeach
val Neutral100 = SoftLavender

// Dark colors re-mapped to Bold Outline tokens (dark mode disabled)
val DarkPrimary = MainYellow
val DarkOnPrimary = InkDark
val DarkPrimaryContainer = SoftPeach
val DarkOnPrimaryContainer = InkDark
val DarkSecondary = MainBlue
val DarkBackground = CreamBackground
val DarkOnBackground = InkDark
val DarkSurface = CardWhite
val DarkOnSurface = InkDark
val DarkOnSurfaceVariant = MutedInk
val DarkOutline = OutlineInk
val DarkOutlineVariant = OutlineInk
val DarkAmber50 = SoftPeach
val DarkAmber700 = StatusLowStock
