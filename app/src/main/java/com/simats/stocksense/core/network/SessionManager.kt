package com.simats.stocksense.core.network

import android.content.Context
import android.content.SharedPreferences
import com.simats.stocksense.data.model.AuthUser
import com.simats.stocksense.data.model.StoreProfile
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Central session manager persisting authentication tokens, login state,
 * and store owner profile details using Android SharedPreferences.
 */
class SessionManager private constructor(context: Context) {

    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    /**
     * Saves complete session upon successful login or registration.
     */
    fun saveSession(token: String, expiresAt: String? = null, user: AuthUser? = null) {
        val editor = prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_TOKEN, token)
            .putString(KEY_EXPIRES_AT, expiresAt)

        if (user != null) {
            user.id?.let { editor.putInt(KEY_USER_ID, it) }
            user.fullName?.let { editor.putString(KEY_FULL_NAME, it) }
            user.shopName?.let { editor.putString(KEY_SHOP_NAME, it) }
            user.email?.let { editor.putString(KEY_EMAIL, it) }
            user.location?.let { editor.putString(KEY_LOCATION, it) }
            user.storeType?.let { editor.putString(KEY_STORE_TYPE, it) }
            user.currency?.let { editor.putString(KEY_CURRENCY, it) }
            user.role?.let { editor.putString(KEY_ROLE, it) }
            user.plan?.let { editor.putString(KEY_PLAN, it) }
            user.storeId?.let { editor.putString(KEY_STORE_ID, it) }
            (user.dataSnapshot ?: user.updatedAt)?.let { editor.putString(KEY_LAST_UPDATED, it) }
        }
        editor.apply()
    }

    /**
     * Updates stored profile information from backend response.
     */
    fun saveStoreProfile(profile: StoreProfile, userId: Int? = null) {
        val editor = prefs.edit()
            .putString(KEY_SHOP_NAME, profile.name)
            .putString(KEY_FULL_NAME, profile.ownerName)
            .putString(KEY_EMAIL, profile.email)
            .putString(KEY_LOCATION, profile.location)
            .putString(KEY_STORE_TYPE, profile.storeType)
            .putString(KEY_CURRENCY, profile.currency)
            .putString(KEY_LAST_UPDATED, profile.lastUpdated)
            .putString(KEY_ROLE, profile.role)
            .putString(KEY_PLAN, profile.plan)
            .putString(KEY_STORE_ID, profile.storeId)

        if (userId != null && userId > 0) {
            editor.putInt(KEY_USER_ID, userId)
        }
        editor.apply()
    }

    /**
     * Updates user owner full name and email in SharedPreferences.
     */
    fun updateProfileUser(fullName: String, email: String) {
        prefs.edit()
            .putString(KEY_FULL_NAME, fullName)
            .putString(KEY_EMAIL, email)
            .apply()
    }

    /**
     * Updates shop name in SharedPreferences.
     */
    fun updateShopName(shopName: String) {
        prefs.edit()
            .putString(KEY_SHOP_NAME, shopName)
            .apply()
    }

    /**
     * Reads StoreProfile from SharedPreferences. Returns empty strings if not yet cached.
     */
    fun getStoreProfile(): StoreProfile {
        val shopName = prefs.getString(KEY_SHOP_NAME, "") ?: ""
        val ownerName = prefs.getString(KEY_FULL_NAME, "") ?: ""
        val email = prefs.getString(KEY_EMAIL, "") ?: ""
        val location = prefs.getString(KEY_LOCATION, "") ?: ""
        val storeType = prefs.getString(KEY_STORE_TYPE, "") ?: ""
        val currency = prefs.getString(KEY_CURRENCY, "") ?: ""
        val lastUpdated = prefs.getString(KEY_LAST_UPDATED, "") ?: ""
        val role = prefs.getString(KEY_ROLE, "") ?: ""
        val plan = prefs.getString(KEY_PLAN, "") ?: ""
        val userId = prefs.getInt(KEY_USER_ID, 0)
        val storeId = prefs.getString(KEY_STORE_ID, "")?.takeIf { it.isNotBlank() } ?: (if (userId > 0) "GB-BLR-00$userId" else "")

        return StoreProfile(
            name = shopName,
            location = location,
            lastUpdated = lastUpdated,
            email = email,
            ownerName = ownerName,
            storeType = storeType,
            storeId = storeId,
            currency = currency,
            role = role,
            plan = plan
        )
    }

    fun getUserId(): Int = prefs.getInt(KEY_USER_ID, 0)

    fun getUserEmail(): String? = prefs.getString(KEY_EMAIL, null)

    fun getUserFullName(): String? = prefs.getString(KEY_FULL_NAME, null)

    fun getShopName(): String? = prefs.getString(KEY_SHOP_NAME, null)

    /**
     * Returns Bearer token if session is valid and not expired; otherwise clears and returns null.
     */
    fun getToken(): String? {
        val token = prefs.getString(KEY_TOKEN, null)
        if (token.isNullOrBlank()) return null

        if (isExpired()) {
            clearSession()
            return null
        }
        return token
    }

    fun getExpiresAt(): String? = prefs.getString(KEY_EXPIRES_AT, null)

    /**
     * Returns true if user has logged in and has an active, non-expired token.
     */
    fun isLoggedIn(): Boolean {
        val isLoggedInFlag = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        val token = prefs.getString(KEY_TOKEN, null)
        if (!isLoggedInFlag || token.isNullOrBlank()) {
            return false
        }
        if (isExpired()) {
            clearSession()
            return false
        }
        return true
    }

    fun hasValidToken(): Boolean = isLoggedIn()

    /**
     * Wipes session data completely from SharedPreferences upon logout or session invalidation.
     */
    fun clearSession() {
        prefs.edit().clear().apply()
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
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_EXPIRES_AT = "auth_token_expires_at"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_FULL_NAME = "full_name"
        private const val KEY_SHOP_NAME = "shop_name"
        private const val KEY_EMAIL = "email"
        private const val KEY_LOCATION = "location"
        private const val KEY_STORE_TYPE = "store_type"
        private const val KEY_CURRENCY = "currency"
        private const val KEY_LAST_UPDATED = "last_updated"
        private const val KEY_ROLE = "role"
        private const val KEY_PLAN = "plan"
        private const val KEY_STORE_ID = "store_id"

        @Volatile
        private var instance: SessionManager? = null

        fun getInstance(context: Context): SessionManager {
            return instance ?: synchronized(this) {
                instance ?: SessionManager(context.applicationContext).also { instance = it }
            }
        }
    }
}

