package com.simats.stocksense

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.SvgDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import com.simats.stocksense.core.fcm.FcmTokenManager
import com.simats.stocksense.core.fcm.StockSenseMessagingService
import com.simats.stocksense.core.network.NetworkClient
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * Custom Application class for StockSense.
 * Initializes NetworkClient, Coil ImageLoader (with SVG support + disk cache),
 * notification channel, and device token registration.
 */
class StockSenseApp : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        NetworkClient.initialize(this)
        createNotificationChannel()
        FcmTokenManager.registerDeviceTokenAsync(this)
    }

    override fun newImageLoader(): ImageLoader {
        // Dedicated clean HTTP client so third-party/CDN avatar requests never carry StockSense auth headers.
        val cleanHttpClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()

        return ImageLoader.Builder(this)
            .okHttpClient(cleanHttpClient)
            .components {
                add(SvgDecoder.Factory())
            }
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.15)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("dicebear_avatar_cache"))
                    .maxSizeBytes(20L * 1024L * 1024L)
                    .build()
            }
            .respectCacheHeaders(false)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                StockSenseMessagingService.CHANNEL_ID,
                StockSenseMessagingService.CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "StockSense inventory alerts and operational notifications."
                enableLights(true)
                enableVibration(true)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
