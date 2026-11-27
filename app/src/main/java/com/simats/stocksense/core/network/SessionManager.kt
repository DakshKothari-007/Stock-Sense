@file:Suppress("DEPRECATION")

package com.simats.stocksense.core.network

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.simats.stocksense.core.avatar.AvatarCatalog
import com.simats.stocksense.core.avatar.AvatarCode
import com.simats.stocksense.data.model.AuthUser
import com.simats.stocksense.data.model.StoreProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class TokenMigrationOutcome {
    NOTHING_TO_MIGRATE,
    MIGRATED,
    ALREADY_MIGRATED_CLEANED_PLAIN,
    ENCRYPTION_UNAVAILABLE,
    MIGRATION_FAILED
}

/**
 * Stores authentication token state strictly inside Keystore-backed SharedPreferences,
 * performs one-time migration from legacy plaintext SharedPreferences, and degrades safely
 * to logged-out state if encryption fails.
 */
internal class SessionTokenStorage(
    private val plainPrefs: SharedPreferences,
    securePrefsProvider: () -> SharedPreferences?
) {
    private val securePrefs: SharedPreferences? = try {
        securePrefsProvider()
    } catch (_: Throwable) {
        null
    }

    val migrationOutcome: TokenMigrationOutcome = migrateLegacyPlainToken()

    private fun migrateLegacyPlainToken(): TokenMigrationOutcome {
        val plainToken = try {
            plainPrefs.getString(SessionManager.KEY_TOKEN, null)
        } catch (_: Throwable) {
            null
        }
        val plainExpiresAt = try {
            plainPrefs.getString(SessionManager.KEY_EXPIRES_AT, null)
        } catch (_: Throwable) {
            null
        }
        val hasPlainKeys = try {
            plainPrefs.contains(SessionManager.KEY_TOKEN) ||
                plainPrefs.contains(SessionManager.KEY_EXPIRES_AT) ||
                plainPrefs.contains(SessionManager.KEY_IS_LOGGED_IN)
        } catch (_: Throwable) {
            false
        }

        val sp = securePrefs
        if (sp == null) {
            if (hasPlainKeys) {
                removePlainAuthKeys()
            }
            return TokenMigrationOutcome.ENCRYPTION_UNAVAILABLE
        }

        if (plainToken.isNullOrBlank()) {
            if (hasPlainKeys) {
                removePlainAuthKeys()
            }
            return TokenMigrationOutcome.NOTHING_TO_MIGRATE
        }

        return try {
            val existingSecureToken = sp.getString(SessionManager.KEY_TOKEN, null)
            if (!existingSecureToken.isNullOrBlank()) {
                removePlainAuthKeys()
                TokenMigrationOutcome.ALREADY_MIGRATED_CLEANED_PLAIN
            } else {
                val committed = sp.edit()
                    .putBoolean(SessionManager.KEY_IS_LOGGED_IN, true)
                    .putString(SessionManager.KEY_TOKEN, plainToken)
                    .apply {
                        if (plainExpiresAt != null) {
                            putString(SessionManager.KEY_EXPIRES_AT, plainExpiresAt)
                        } else {
                            remove(SessionManager.KEY_EXPIRES_AT)
                        }
                    }
                    .commit()

                removePlainAuthKeys()
                if (committed) TokenMigrationOutcome.MIGRATED else TokenMigrationOutcome.MIGRATION_FAILED
            }
        } catch (_: Throwable) {
            removePlainAuthKeys()
            TokenMigrationOutcome.MIGRATION_FAILED
        }
    }

    fun saveToken(token: String, expiresAt: String?): Boolean {
        val sp = securePrefs ?: run {
            removePlainAuthKeys()
            return false
        }
        val committed = try {
            sp.edit()
                .putBoolean(SessionManager.KEY_IS_LOGGED_IN, true)
                .putString(SessionManager.KEY_TOKEN, token)
                .apply {
                    if (expiresAt != null) {
                        putString(SessionManager.KEY_EXPIRES_AT, expiresAt)
                    } else {
                        remove(SessionManager.KEY_EXPIRES_AT)
                    }
                }
                .commit()
        } catch (_: Throwable) {
            false
        }
        removePlainAuthKeys()
        return committed
    }

    fun getToken(onExpired: () -> Unit): String? {
        val token = readSecureString(SessionManager.KEY_TOKEN) ?: return null
        if (token.isBlank()) return null

        if (isExpired()) {
            onExpired()
            return null
        }
        return token
    }

    fun getExpiresAt(): String? = readSecureString(SessionManager.KEY_EXPIRES_AT)

    fun isLoggedIn(onExpired: () -> Unit): Boolean {
        val isLoggedInFlag = readSecureBoolean(SessionManager.KEY_IS_LOGGED_IN)
        val token = readSecureString(SessionManager.KEY_TOKEN)
        if (!isLoggedInFlag || token.isNullOrBlank()) {
            return false
        }
        if (isExpired()) {
            onExpired()
            return false
        }
        return true
    }

    fun clear() {
        try {
            securePrefs?.edit()?.clear()?.commit()
        } catch (_: Throwable) { }
        removePlainAuthKeys()
    }

    private fun readSecureString(key: String): String? {
        val sp = securePrefs ?: return null
        return try {
            sp.getString(key, null)
        } catch (_: Throwable) {
            removePlainAuthKeys()
            null
        }
    }

    private fun readSecureBoolean(key: String): Boolean {
        val sp = securePrefs ?: return false
        return try {
            sp.getBoolean(key, false)
        } catch (_: Throwable) {
            removePlainAuthKeys()
            false
        }
    }

    private fun isExpired(): Boolean {
        val expiresAt = readSecureString(SessionManager.KEY_EXPIRES_AT) ?: return false
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val expiryDate = format.parse(expiresAt)
            expiryDate != null && expiryDate.before(Date())
        } catch (_: Exception) {
            false
        }
    }

    private fun removePlainAuthKeys() {
        try {
            plainPrefs.edit()
                .remove(SessionManager.KEY_TOKEN)
                .remove(SessionManager.KEY_EXPIRES_AT)
                .remove(SessionManager.KEY_IS_LOGGED_IN)
                .commit()
        } catch (_: Throwable) { }
    }
}

/**
 * Central session manager persisting store profile details and Keystore-backed encrypted
 * authentication tokens via EncryptedSharedPreferences.
 */
class SessionManager private constructor(
    private val prefs: SharedPreferences,
    securePrefsProvider: () -> SharedPreferences?
) {

    private constructor(context: Context) : this(
        prefs = context.applicationContext.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        ),
        securePrefsProvider = { createDefaultSecurePrefs(context.applicationContext) }
    )

    internal constructor(
        plainPrefs: SharedPreferences,
        securePrefs: SharedPreferences
    ) : this(
        prefs = plainPrefs,
        securePrefsProvider = { securePrefs }
    )

    private val tokenStorage = SessionTokenStorage(
        plainPrefs = prefs,
        securePrefsProvider = securePrefsProvider
    )

    private val _avatarState = MutableStateFlow<AvatarCode?>(readAvatarFromPrefs())
    val avatarState: StateFlow<AvatarCode?> = _avatarState.asStateFlow()

    private fun readAvatarFromPrefs(): AvatarCode? =
        AvatarCatalog.parseAvatarJson(prefs.getString(KEY_AVATAR_JSON, null))

    /**
     * Saves complete session upon successful login or registration.
     * Authentication token and session validity stay strictly in EncryptedSharedPreferences.
     */
    fun saveSession(token: String, expiresAt: String? = null, user: AuthUser? = null) {
        tokenStorage.saveToken(token, expiresAt)

        val editor = prefs.edit()
            .remove(KEY_IS_LOGGED_IN)
            .remove(KEY_TOKEN)
            .remove(KEY_EXPIRES_AT)

        var parsedAvatar: AvatarCode? = null
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
            user.gstin?.let { editor.putString(KEY_GSTIN, it) }
            parsedAvatar = AvatarCatalog.parseAvatarCode(user.avatar)
            val avatarJson = AvatarCatalog.toJson(parsedAvatar)
            if (avatarJson != null) {
                editor.putString(KEY_AVATAR_JSON, avatarJson)
            } else {
                editor.remove(KEY_AVATAR_JSON)
            }
            (user.dataSnapshot ?: user.updatedAt)?.let { editor.putString(KEY_LAST_UPDATED, it) }
        }
        editor.commit()
        if (user != null) {
            _avatarState.value = parsedAvatar
        }
    }

    /**
     * Updates stored profile information from backend response.
     */
    fun saveStoreProfile(profile: StoreProfile, userId: Int? = null) {
        val cleanAvatar = AvatarCatalog.parseAvatarCode(profile.avatar)
        val avatarJson = AvatarCatalog.toJson(cleanAvatar)
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
            .putString(KEY_GSTIN, profile.gstin)

        if (avatarJson != null) {
            editor.putString(KEY_AVATAR_JSON, avatarJson)
        } else {
            editor.remove(KEY_AVATAR_JSON)
        }

        if (userId != null && userId > 0) {
            editor.putInt(KEY_USER_ID, userId)
        }
        editor.commit()
        _avatarState.value = cleanAvatar
    }

    /**
     * Persists or removes the user's avatar code and notifies active collectors immediately.
     */
    fun saveAvatar(avatar: AvatarCode?) {
        val cleanAvatar = AvatarCatalog.parseAvatarCode(avatar)
        val avatarJson = AvatarCatalog.toJson(cleanAvatar)
        val editor = prefs.edit()
        if (avatarJson != null) {
            editor.putString(KEY_AVATAR_JSON, avatarJson)
        } else {
            editor.remove(KEY_AVATAR_JSON)
        }
        editor.commit()
        _avatarState.value = cleanAvatar
    }

    fun getAvatar(): AvatarCode? = readAvatarFromPrefs()

    fun updateProfileUser(fullName: String, email: String) {
        prefs.edit()
            .putString(KEY_FULL_NAME, fullName)
            .putString(KEY_EMAIL, email)
            .commit()
    }

    fun updateShopName(shopName: String) {
        prefs.edit()
            .putString(KEY_SHOP_NAME, shopName)
            .commit()
    }

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
        val gstin = prefs.getString(KEY_GSTIN, null)
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
            plan = plan,
            gstin = gstin,
            avatar = getAvatar()
        )
    }

    fun getGstin(): String? = prefs.getString(KEY_GSTIN, null)

    fun getUserId(): Int = prefs.getInt(KEY_USER_ID, 0)

    fun getUserEmail(): String? = prefs.getString(KEY_EMAIL, null)

    fun getUserFullName(): String? = prefs.getString(KEY_FULL_NAME, null)

    fun getShopName(): String? = prefs.getString(KEY_SHOP_NAME, null)

    fun getToken(): String? = tokenStorage.getToken(onExpired = { clearSession() })

    fun getExpiresAt(): String? = tokenStorage.getExpiresAt()

    fun isLoggedIn(): Boolean = tokenStorage.isLoggedIn(onExpired = { clearSession() })

    fun hasValidToken(): Boolean = isLoggedIn()

    fun clearSession() {
        tokenStorage.clear()
        try {
            prefs.edit().clear().commit()
        } catch (_: Throwable) { }
        _avatarState.value = null
        try {
            com.simats.stocksense.feature.ai.AiChatStore.clear()
        } catch (_: Throwable) { }
    }

    companion object {
        internal const val PREFS_NAME = "stocksense_auth_prefs"
        internal const val SECURE_PREFS_NAME = "stocksense_secure_auth_prefs"
        internal const val KEY_IS_LOGGED_IN = "is_logged_in"
        internal const val KEY_TOKEN = "auth_token"
        internal const val KEY_EXPIRES_AT = "auth_token_expires_at"
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
        private const val KEY_GSTIN = "gstin"
        private const val KEY_AVATAR_JSON = "avatar_json"

        @Volatile
        private var instance: SessionManager? = null

        fun getInstance(context: Context): SessionManager {
            return instance ?: synchronized(this) {
                instance ?: SessionManager(context.applicationContext).also { instance = it }
            }
        }

        private fun createDefaultSecurePrefs(appContext: Context): SharedPreferences? {
            return try {
                buildEncryptedPrefs(appContext)
            } catch (_: Throwable) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        appContext.deleteSharedPreferences(SECURE_PREFS_NAME)
                    } else {
                        appContext.getSharedPreferences(SECURE_PREFS_NAME, Context.MODE_PRIVATE)
                            .edit().clear().commit()
                    }
                    buildEncryptedPrefs(appContext)
                } catch (_: Throwable) {
                    null
                }
            }
        }

        private fun buildEncryptedPrefs(appContext: Context): SharedPreferences {
            val masterKey = MasterKey.Builder(appContext)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            return EncryptedSharedPreferences.create(
                appContext,
                SECURE_PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
    }
}
