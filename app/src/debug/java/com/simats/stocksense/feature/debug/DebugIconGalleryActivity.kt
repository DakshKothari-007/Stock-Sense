package com.simats.stocksense.feature.debug

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

/**
 * Debug-only activity for the Product Icon Gallery.
 * Excluded from release builds automatically by Gradle sourceSets.
 */
class DebugIconGalleryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProductIconGalleryScreen(
                onBackClick = { finish() }
            )
        }
    }
}
