package com.simats.stocksense

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.simats.stocksense.core.fcm.FcmTokenManager
import com.simats.stocksense.core.fcm.StockSenseMessagingService
import com.simats.stocksense.core.network.NetworkClient

/**
 * Custom Application class for StockSense.
 * Ensures NetworkClient and SessionManager are initialized at the earliest possible lifecycle event,
 * creates the system notification channel for StockSense alerts, and checks device token registration.
 */
class StockSenseApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NetworkClient.initialize(this)
        createNotificationChannel()
        FcmTokenManager.registerDeviceTokenAsync(this)
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

