package com.simats.stocksense.feature.debug

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.simats.stocksense.BuildConfig

/**
 * Debug-only activity for the Product Icon Gallery.
 * Excluded from release builds automatically by Gradle sourceSets.
 * Strictly unreachable and finishes immediately if launched in non-debug builds.
 */
class DebugIconGalleryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!BuildConfig.DEBUG) {
            finish()
            return
        }
        enableEdgeToEdge()
        setContent {
            ProductIconGalleryScreen(
                onBackClick = { finish() }
            )
        }
    }
}
