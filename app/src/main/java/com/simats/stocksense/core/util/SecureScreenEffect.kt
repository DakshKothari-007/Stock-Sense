package com.simats.stocksense.core.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import com.simats.stocksense.BuildConfig

private object SecureWindowTracker {
    private val refCounts = mutableMapOf<Int, Int>()

    fun acquire(activity: Activity) {
        val key = System.identityHashCode(activity)
        val next = (refCounts[key] ?: 0) + 1
        refCounts[key] = next
        if (next == 1) {
            activity.window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    fun release(activity: Activity) {
        val key = System.identityHashCode(activity)
        val current = refCounts[key] ?: 0
        if (current <= 1) {
            refCounts.remove(key)
            activity.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            refCounts[key] = current - 1
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

/**
 * Sets WindowManager.LayoutParams.FLAG_SECURE while this screen is active in RELEASE builds only.
 * Debug builds leave screenshots enabled for testing. Ref-counted across auth route transitions.
 */
@Composable
fun SecureScreenInReleaseEffect() {
    val context = LocalContext.current
    DisposableEffect(context) {
        val activity = if (!BuildConfig.DEBUG) context.findActivity() else null
        if (activity != null) {
            SecureWindowTracker.acquire(activity)
        }
        onDispose {
            if (activity != null) {
                SecureWindowTracker.release(activity)
            }
        }
    }
}
