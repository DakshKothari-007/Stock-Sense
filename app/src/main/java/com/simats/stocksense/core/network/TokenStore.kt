package com.simats.stocksense.core.network

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Manages persistent storage of the authenticated 64-character session Bearer token
 * and its expiration timestamp using Android SharedPreferences.
 */
class TokenStore(context: Context) {

    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    /**
     * Saves the Bearer token and optional expiration date (e.g. "2026-11-05 14:30:00").
     */
    fun saveToken(token: String, expiresAt: String? = null) {
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_EXPIRES_AT, expiresAt)
            .apply()
    }

    /**
     * Returns the saved token, or null if missing or expired.
     */
    fun getToken(): String? {
        val token = prefs.getString(KEY_TOKEN, null)
        if (token.isNullOrBlank()) return null

        if (isExpired()) {
            clearToken()
            return null
        }
        return token
    }

    /**
     * Returns saved expiry string.
     */
    fun getExpiresAt(): String? = prefs.getString(KEY_EXPIRES_AT, null)

    /**
     * Returns true if a non-expired token exists in local storage.
     */
    fun hasValidToken(): Boolean {
        val token = prefs.getString(KEY_TOKEN, null)
        if (token.isNullOrBlank()) return false

        if (isExpired()) {
            clearToken()
            return false
        }
        return true
    }

    /**
     * Deletes token and expiry from SharedPreferences.
     */
    fun clearToken() {
        prefs.edit()
            .remove(KEY_TOKEN)
            .remove(KEY_EXPIRES_AT)
            .apply()
    }

    private fun isExpired(): Boolean {
        val expiresAt = prefs.getString(KEY_EXPIRES_AT, null) ?: return false
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val expiryDate = format.parse(expiresAt)
            expiryDate != null && expiryDate.before(Date())
        } catch (_: Exception) {
            false
        }
    }

    companion object {
        private const val PREFS_NAME = "stocksense_auth_prefs"
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_EXPIRES_AT = "auth_token_expires_at"

        @Volatile
        private var instance: TokenStore? = null

        fun getInstance(context: Context): TokenStore {
            return instance ?: synchronized(this) {
                instance ?: TokenStore(context.applicationContext).also { instance = it }
            }
        }
    }
}
