package com.simats.stocksense.core.fcm

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import com.simats.stocksense.BuildConfig
import com.simats.stocksense.core.network.SessionManager
import com.simats.stocksense.data.repository.NotificationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Manages Firebase Cloud Messaging (FCM) token registration, lifecycle,
 * and persistence adhering to StockSense specifications.
 */
object FcmTokenManager {

    private const val PREFS_NAME = "stocksense_fcm_prefs"
    private const val KEY_CURRENT_TOKEN = "current_fcm_token"
    private const val KEY_PENDING_TOKEN = "pending_fcm_token"
    private const val KEY_PERMISSION_PROMPTED = "has_prompted_notification_permission"
    private const val TAG = "StockSenseFCM"

    private fun getPrefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getCurrentToken(context: Context): String? =
        getPrefs(context).getString(KEY_CURRENT_TOKEN, null)

    fun saveCurrentToken(context: Context, token: String) {
        getPrefs(context).edit().putString(KEY_CURRENT_TOKEN, token).apply()
    }

    fun getPendingToken(context: Context): String? =
        getPrefs(context).getString(KEY_PENDING_TOKEN, null)

    fun savePendingToken(context: Context, token: String) {
        getPrefs(context).edit().putString(KEY_PENDING_TOKEN, token).apply()
    }

    fun clearStoredTokens(context: Context) {
        getPrefs(context).edit()
            .remove(KEY_CURRENT_TOKEN)
            .remove(KEY_PENDING_TOKEN)
            .apply()
    }

    fun hasPromptedPermission(context: Context): Boolean =
        getPrefs(context).getBoolean(KEY_PERMISSION_PROMPTED, false)

    fun setPromptedPermission(context: Context, prompted: Boolean = true) {
        getPrefs(context).edit().putBoolean(KEY_PERMISSION_PROMPTED, prompted).apply()
    }

    fun logTokenIfDebug(token: String) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "FCM Device Token: $token")
        }
    }

    /**
     * Awaits completion of a Google Play Services Task in a coroutine.
     */
    suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { cont ->
        addOnCompleteListener { task ->
            if (task.isSuccessful) {
                cont.resume(task.result)
            } else {
                cont.resumeWithException(task.exception ?: Exception("Task failed"))
            }
        }
    }

    /**
     * Safely fetches current token from FirebaseMessaging.
     */
    suspend fun fetchCurrentToken(): String? {
        return try {
            FirebaseMessaging.getInstance().token.awaitTask()
        } catch (e: Throwable) {
            if (BuildConfig.DEBUG) {
                Log.w(TAG, "Failed to fetch FCM token from FirebaseMessaging", e)
            }
            null
        }
    }

    /**
     * Registers device token with the backend (POST /notifications/register_device.php).
     * If user is not logged in, stores as pending token to be registered upon login.
     * Best-effort: never blocks or crashes.
     */
    suspend fun registerDeviceToken(context: Context, explicitToken: String? = null): Boolean {
        return try {
            val token = explicitToken?.ifBlank { null }
                ?: getPendingToken(context)?.ifBlank { null }
                ?: fetchCurrentToken()

            if (token.isNullOrBlank()) {
                return false
            }

            saveCurrentToken(context, token)
            logTokenIfDebug(token)

            val sessionManager = SessionManager.getInstance(context)
            if (!sessionManager.isLoggedIn()) {
                savePendingToken(context, token)
                return false
            }

            val repo = NotificationRepository()
            val result = repo.registerDevice(token)
            if (result.isSuccess) {
                savePendingToken(context, "")
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Device token registered with StockSense backend successfully.")
                }
                true
            } else {
                savePendingToken(context, token)
                if (BuildConfig.DEBUG) {
                    Log.w(TAG, "Backend device registration returned failure: ${result.exceptionOrNull()?.message}")
                }
                false
            }
        } catch (e: Throwable) {
            if (BuildConfig.DEBUG) {
                Log.w(TAG, "registerDeviceToken exception", e)
            }
            false
        }
    }

    /**
     * Non-blocking asynchronous registration helper.
     */
    fun registerDeviceTokenAsync(context: Context, explicitToken: String? = null) {
        CoroutineScope(Dispatchers.IO).launch {
            registerDeviceToken(context, explicitToken)
        }
    }

    /**
     * Logout flow:
     * 1. Calls unregister_device.php on backend
     * 2. Deletes local token with FirebaseMessaging.getInstance().deleteToken()
     * 3. Clears local preferences
     * Logout must still succeed if any call fails.
     */
    suspend fun unregisterAndClearToken(context: Context) {
        val token = getCurrentToken(context) ?: fetchCurrentToken()
        if (!token.isNullOrBlank()) {
            try {
                val repo = NotificationRepository()
                repo.unregisterDevice(token)
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Device token unregistered from backend.")
                }
            } catch (e: Throwable) {
                if (BuildConfig.DEBUG) {
                    Log.w(TAG, "Failed to unregister device from backend on logout", e)
                }
            }
        }

        try {
            FirebaseMessaging.getInstance().deleteToken().awaitTask()
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "FirebaseMessaging local token deleted.")
            }
        } catch (e: Throwable) {
            if (BuildConfig.DEBUG) {
                Log.w(TAG, "Failed to delete local FCM token on logout", e)
            }
        }

        clearStoredTokens(context)
    }

    /**
     * Account deletion flow:
     * Backend automatically purges tokens from database; app only clears local token.
     */
    suspend fun clearLocalTokenOnly(context: Context) {
        try {
            FirebaseMessaging.getInstance().deleteToken().awaitTask()
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "FirebaseMessaging local token deleted after account deletion.")
            }
        } catch (e: Throwable) {
            if (BuildConfig.DEBUG) {
                Log.w(TAG, "Failed to delete local FCM token on account deletion", e)
            }
        }
        clearStoredTokens(context)
    }
}

