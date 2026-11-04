package com.simats.stocksense.core.fcm

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.simats.stocksense.BuildConfig
import com.simats.stocksense.MainActivity
import com.simats.stocksense.R

/**
 * Firebase Cloud Messaging Service handling device token refreshes
 * and system alert notifications for StockSense.
 */
class StockSenseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        FcmTokenManager.logTokenIfDebug(token)
        // If logged in, sends token to register_device.php. Otherwise stores it for after login.
        FcmTokenManager.registerDeviceTokenAsync(applicationContext, token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "FCM Message received from: ${remoteMessage.from}")
            Log.d(TAG, "FCM Message data: ${remoteMessage.data}")
        }

        // Title and body from notification payload or data payload fallback
        val title = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: "StockSense"

        val body = remoteMessage.notification?.body
            ?: remoteMessage.data["body"]
            ?: ""

        val type = remoteMessage.data["type"] ?: ""
        val productId = remoteMessage.data["product_id"] ?: ""

        showSystemNotification(title, body, type, productId)
    }

    private fun showSystemNotification(
        title: String,
        body: String,
        type: String,
        productId: String
    ) {
        val intent = Intent(this, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_PUSH_TYPE, type)
            putExtra("type", type)
            putExtra(EXTRA_PRODUCT_ID, productId)
            putExtra("product_id", productId)
        }

        val notificationId = (System.currentTimeMillis() % 100000).toInt()
        val pendingIntent = PendingIntent.getActivity(
            this,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
    }

    companion object {
        private const val TAG = "StockSenseFCM"
        const val CHANNEL_ID = "stocksense_alerts"
        const val CHANNEL_NAME = "Stock alerts"
        const val EXTRA_PUSH_TYPE = "extra_push_type"
        const val EXTRA_PRODUCT_ID = "extra_product_id"
    }
}

