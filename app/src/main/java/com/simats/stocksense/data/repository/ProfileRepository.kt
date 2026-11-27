package com.simats.stocksense.data.repository

import android.util.Log
import com.simats.stocksense.core.avatar.AvatarCatalog
import com.simats.stocksense.core.avatar.AvatarCode
import com.simats.stocksense.core.network.AuthApiService
import com.simats.stocksense.core.network.NetworkClient
import com.simats.stocksense.core.network.SessionManager
import com.simats.stocksense.core.network.TokenStore
import com.simats.stocksense.data.model.BaseResponse
import com.simats.stocksense.data.model.ChangePasswordRequest
import com.simats.stocksense.data.model.StoreProfile
import com.simats.stocksense.data.model.UpdateProfileRequest
import com.simats.stocksense.data.model.UpdateShopRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Repository responsible for profile operations: fetching store profile from MySQL backend,
 * updating shop and owner details, changing password, and persisting data in SessionManager.
 */
class ProfileRepository(
    private val apiService: AuthApiService = NetworkClient.createService(),
    customSessionManager: SessionManager? = null,
    customTokenStore: TokenStore? = null
) {
    private val _customSessionManager = customSessionManager
    private val _customTokenStore = customTokenStore

    private val sessionManager: SessionManager?
        get() = _customSessionManager ?: NetworkClient.getSessionManager()

    private val tokenStore: TokenStore?
        get() = _customTokenStore ?: NetworkClient.getTokenStore()

    /**
     * Fetches current profile from DB via GET auth/profile.php.
     * Caches result in SessionManager SharedPreferences.
     * Returns failure on network or auth errors (never demo data).
     */
    suspend fun fetchProfile(): Result<StoreProfile> = withContext(Dispatchers.IO) {
        val userId = sessionManager?.getUserId()?.takeIf { it > 0 }
        val email = sessionManager?.getUserEmail()
        val hasToken = !sessionManager?.getToken().isNullOrBlank()

        if (com.simats.stocksense.BuildConfig.DEBUG) {
            Log.d("StockSenseProfile", "Profile request: GET auth/profile.php (hasToken=$hasToken)")
        }

        if (sessionManager?.isLoggedIn() != true && !hasToken && userId == null && email.isNullOrBlank()) {
            return@withContext Result.failure(Exception("User is not logged in."))
        }

        try {
            val response = apiService.getProfile(userId = userId, email = email)
            Log.d("StockSenseProfile", "Profile response status: ${response.code()} isSuccessful=${response.isSuccessful}")

            if (response.isSuccessful) {
                val body = response.body()
                val user = body?.getEffectiveUser()
                if (user != null) {
                    val updatedProfile = StoreProfile(
                        name = user.shopName.orEmpty(),
                        location = user.location.orEmpty(),
                        lastUpdated = user.dataSnapshot ?: user.updatedAt ?: user.createdAt.orEmpty(),
                        email = user.email.orEmpty(),
                        ownerName = user.fullName.orEmpty(),
                        storeType = user.storeType.orEmpty(),
                        storeId = user.storeId?.takeIf { it.isNotBlank() } ?: (if ((user.id ?: 0) > 0) "GB-BLR-00${user.id}" else ""),
                        currency = user.currency.orEmpty(),
                        role = user.role.orEmpty(),
                        plan = user.plan.orEmpty(),
                        gstin = user.gstin,
                        avatar = AvatarCatalog.parseAvatarCode(user.avatar ?: body.avatar)
                    )
                    sessionManager?.saveStoreProfile(updatedProfile, user.id)
                    Result.success(updatedProfile)
                } else {
                    Result.failure(Exception(body?.message ?: "Profile data is empty."))
                }
            } else {
                val errorMsg = parseHttpError(response.code(), response.errorBody()?.string())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Throwable) {
            Result.failure(Exception(parseNetworkException(e)))
        }
    }

    /**
     * Updates shop details (name, location, store_type) in DB via POST auth/update_shop.php
     * and persists in SessionManager SharedPreferences.
     */
    suspend fun updateShop(
        shopName: String,
        location: String,
        storeType: String,
        gstin: String? = null
    ): Result<StoreProfile> = withContext(Dispatchers.IO) {
        val trimmedShop = shopName.trim()
        val trimmedLocation = location.trim()
        val trimmedStoreType = storeType.trim()
        val trimmedGstin = gstin?.trim()?.uppercase()?.ifBlank { null }

        if (trimmedShop.isEmpty()) {
            return@withContext Result.failure(Exception("Shop name cannot be empty."))
        }

        try {
            val token = sessionManager?.getToken()
            val userId = sessionManager?.getUserId()?.takeIf { it > 0 }
            val email = sessionManager?.getUserEmail() ?: sessionManager?.getStoreProfile()?.email
            val response = apiService.updateShop(
                UpdateShopRequest(
                    shopName = trimmedShop,
                    location = trimmedLocation,
                    storeType = trimmedStoreType,
                    currency = null,
                    gstin = trimmedGstin,
                    userId = userId,
                    email = email,
                    token = token
                )
            )

            if (response.isSuccessful) {
                val body = response.body()
                val user = body?.getEffectiveUser()
                if (user != null) {
                    val updatedProfile = StoreProfile(
                        name = user.shopName.orEmpty(),
                        location = user.location.orEmpty(),
                        lastUpdated = user.dataSnapshot ?: user.updatedAt ?: user.createdAt.orEmpty(),
                        email = user.email.orEmpty(),
                        ownerName = user.fullName.orEmpty(),
                        storeType = user.storeType.orEmpty(),
                        storeId = user.storeId?.takeIf { it.isNotBlank() } ?: (if ((user.id ?: 0) > 0) "GB-BLR-00${user.id}" else ""),
                        currency = user.currency.orEmpty(),
                        role = user.role.orEmpty(),
                        plan = user.plan.orEmpty(),
                        gstin = user.gstin ?: trimmedGstin,
                        avatar = AvatarCatalog.parseAvatarCode(user.avatar ?: body.avatar)
                    )
                    sessionManager?.saveStoreProfile(updatedProfile, user.id ?: userId)
                    Result.success(updatedProfile)
                } else {
                    val current = sessionManager?.getStoreProfile() ?: StoreProfile()
                    val updated = current.copy(
                        name = trimmedShop,
                        location = trimmedLocation,
                        storeType = trimmedStoreType,
                        gstin = trimmedGstin
                    )
                    sessionManager?.saveStoreProfile(updated, userId)
                    Result.success(updated)
                }
            } else {
                val err = parseHttpError(response.code(), response.errorBody()?.string())
                Result.failure(Exception(err))
            }
        } catch (e: Throwable) {
            Result.failure(Exception(parseNetworkException(e)))
        }
    }

    /**
     * Updates shop name in DB via POST auth/update_shop.php and persists in SharedPreferences.
     */
    suspend fun updateShopName(newName: String): Result<StoreProfile> = withContext(Dispatchers.IO) {
        val current = sessionManager?.getStoreProfile() ?: StoreProfile()
        updateShop(newName, current.location, current.storeType)
    }

    /**
     * Updates owner user full name and email in DB via POST auth/update_profile.php.
     */
    suspend fun updateUserProfile(fullName: String, email: String): Result<StoreProfile> = withContext(Dispatchers.IO) {
        val trimmedName = fullName.trim()
        val trimmedEmail = email.trim()

        if (trimmedName.isEmpty()) {
            return@withContext Result.failure(Exception("Full name cannot be empty."))
        }
        if (trimmedEmail.isEmpty() || !trimmedEmail.contains("@")) {
            return@withContext Result.failure(Exception("A valid email address is required."))
        }

        try {
            val token = sessionManager?.getToken()
            val userId = sessionManager?.getUserId()?.takeIf { it > 0 }
            val currentEmail = sessionManager?.getUserEmail() ?: sessionManager?.getStoreProfile()?.email
            val response = apiService.updateProfile(
                UpdateProfileRequest(
                    fullName = trimmedName,
                    email = trimmedEmail,
                    newEmail = trimmedEmail,
                    currentEmail = currentEmail,
                    userId = userId,
                    token = token
                )
            )

            if (response.isSuccessful) {
                val body = response.body()
                val user = body?.getEffectiveUser()
                if (user != null) {
                    val updatedProfile = StoreProfile(
                        name = user.shopName.orEmpty(),
                        location = user.location.orEmpty(),
                        lastUpdated = user.dataSnapshot ?: user.updatedAt ?: user.createdAt.orEmpty(),
                        email = user.email.orEmpty(),
                        ownerName = user.fullName.orEmpty(),
                        storeType = user.storeType.orEmpty(),
                        storeId = user.storeId?.takeIf { it.isNotBlank() } ?: (if ((user.id ?: 0) > 0) "GB-BLR-00${user.id}" else ""),
                        currency = user.currency.orEmpty(),
                        role = user.role.orEmpty(),
                        plan = user.plan.orEmpty(),
                        gstin = user.gstin,
                        avatar = AvatarCatalog.parseAvatarCode(user.avatar ?: body.avatar)
                    )
                    sessionManager?.saveStoreProfile(updatedProfile, user.id ?: userId)
                    sessionManager?.updateProfileUser(trimmedName, trimmedEmail)
                    Result.success(updatedProfile)
                } else {
                    sessionManager?.updateProfileUser(trimmedName, trimmedEmail)
                    val current = sessionManager?.getStoreProfile() ?: StoreProfile()
                    val updated = current.copy(ownerName = trimmedName, email = trimmedEmail)
                    sessionManager?.saveStoreProfile(updated, userId)
                    Result.success(updated)
                }
            } else {
                val err = parseHttpError(response.code(), response.errorBody()?.string())
                Result.failure(Exception(err))
            }
        } catch (e: Throwable) {
            Result.failure(Exception(parseNetworkException(e)))
        }
    }

    /**
     * Changes password via POST auth/change_password.php.
     */
    suspend fun changePassword(
        currentPass: String,
        newPass: String,
        confirmPass: String
    ): Result<String> = withContext(Dispatchers.IO) {
        if (currentPass.isEmpty()) {
            return@withContext Result.failure(Exception("Current password is required."))
        }
        if (newPass.length < 8) {
            return@withContext Result.failure(Exception("New password must be at least 8 characters long."))
        }
        if (newPass != confirmPass) {
            return@withContext Result.failure(Exception("Passwords do not match."))
        }
        if (newPass == currentPass) {
            return@withContext Result.failure(Exception("New password must be different from current password."))
        }

        try {
            val response = apiService.changePassword(
                ChangePasswordRequest(
                    currentPassword = currentPass,
                    newPassword = newPass,
                    confirmPassword = confirmPass
                )
            )

            if (response.isSuccessful) {
                val msg = response.body()?.message ?: "Password changed successfully."
                Result.success(msg)
            } else {
                val err = parseHttpError(response.code(), response.errorBody()?.string())
                Result.failure(Exception(err))
            }
        } catch (e: Throwable) {
            Result.failure(Exception(parseNetworkException(e)))
        }
    }

    /**
     * Saves user's DiceBear avatar code via POST auth/update_avatar.php and updates SessionManager.
     */
    suspend fun saveAvatar(avatar: AvatarCode): Result<AvatarCode?> = withContext(Dispatchers.IO) {
        val sanitized = AvatarCatalog.parseAvatarCode(avatar)
            ?: return@withContext Result.failure(Exception("Invalid avatar selection."))
        val serializedJson = AvatarCatalog.toJson(sanitized)
        if (serializedJson == null) {
            return@withContext Result.failure(Exception("Avatar configuration is too large or invalid."))
        }

        try {
            val response = apiService.updateAvatar(sanitized)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && (body.status == true || body.success == true)) {
                    val savedAvatar = AvatarCatalog.parseAvatarCode(
                        body.avatar ?: body.getEffectiveUser()?.avatar
                    ) ?: sanitized
                    sessionManager?.saveAvatar(savedAvatar)
                    Result.success(savedAvatar)
                } else {
                    Result.failure(Exception(body?.message ?: "Failed to update avatar."))
                }
            } else {
                val err = parseHttpError(response.code(), response.errorBody()?.string())
                Result.failure(Exception(err))
            }
        } catch (e: Throwable) {
            Result.failure(Exception(parseNetworkException(e)))
        }
    }

    /**
     * Removes user's avatar via POST auth/remove_avatar.php, returning UI to letter badge.
     */
    suspend fun removeAvatar(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.removeAvatar()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && (body.status == true || body.success == true)) {
                    sessionManager?.saveAvatar(null)
                    Result.success(Unit)
                } else {
                    Result.failure(Exception(body?.message ?: "Failed to remove avatar."))
                }
            } else {
                val err = parseHttpError(response.code(), response.errorBody()?.string())
                Result.failure(Exception(err))
            }
        } catch (e: Throwable) {
            Result.failure(Exception(parseNetworkException(e)))
        }
    }

    /**
     * Reads the current auth token, then immediately clears local session, token store,
     * cached AI chat, and stored FCM tokens.
     */
    fun clearLocalSession(): String? {
        val savedToken = sessionManager?.getToken()?.takeIf { it.isNotBlank() }
            ?: tokenStore?.getToken()?.takeIf { it.isNotBlank() }
        sessionManager?.clearSession()
        tokenStore?.clearToken()
        try {
            com.simats.stocksense.feature.ai.AiChatStore.clear()
        } catch (_: Throwable) {}
        try {
            NetworkClient.getAppContext()?.let { ctx ->
                com.simats.stocksense.core.fcm.FcmTokenManager.clearStoredTokens(ctx)
            }
        } catch (_: Throwable) {}
        return savedToken
    }

    /**
     * Best-effort background token revoke & FCM unregister. Ignores all errors and timeouts.
     */
    suspend fun revokeTokenInBackground(savedToken: String?) = withContext(Dispatchers.IO) {
        withTimeoutOrNull(10_000L) {
            try {
                NetworkClient.getAppContext()?.let { ctx ->
                    com.simats.stocksense.core.fcm.FcmTokenManager.unregisterAndClearToken(ctx, savedToken)
                }
            } catch (_: Throwable) {}
            if (!savedToken.isNullOrBlank()) {
                try {
                    apiService.logout("Bearer $savedToken")
                } catch (_: Throwable) {}
            }
        }
    }

    /**
     * Logs out user, clearing local session immediately and revoking server token best-effort.
     */
    suspend fun logout(): Result<BaseResponse> {
        val savedToken = clearLocalSession()
        revokeTokenInBackground(savedToken)
        return Result.success(BaseResponse(status = true, message = "Logged out successfully."))
    }

    private fun parseHttpError(code: Int, errorBody: String?): String {
        val serverMessage = try {
            if (!errorBody.isNullOrBlank()) {
                val envelope = NetworkClient.gson.fromJson(errorBody, BaseResponse::class.java)
                envelope?.message
            } else null
        } catch (_: Exception) {
            null
        }

        return when (code) {
            400 -> serverMessage ?: "Invalid request details."
            401 -> "Unauthorized or current password incorrect."
            409 -> serverMessage ?: "This email address is already in use."
            422 -> serverMessage ?: "Validation failed. Please verify your inputs."
            429 -> "Too many failed attempts. Please try again later."
            in 500..599 -> "Server error. Please try again later."
            else -> serverMessage ?: "Request failed with code $code."
        }
    }

    private fun parseNetworkException(e: Throwable): String {
        return when (e) {
            is UnknownHostException, is ConnectException -> "No internet connection. Please check your network and try again."
            is SocketTimeoutException -> "Connection timed out. Please try again."
            else -> e.message ?: "An unexpected error occurred. Please try again."
        }
    }
}

