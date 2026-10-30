package com.simats.stocksense.data.repository

import com.simats.stocksense.core.network.AuthApiService
import com.simats.stocksense.core.network.NetworkClient
import com.simats.stocksense.core.network.SessionManager
import com.simats.stocksense.core.network.TokenStore
import com.simats.stocksense.data.model.AuthUser
import com.simats.stocksense.data.model.BaseResponse
import com.simats.stocksense.data.model.LoginRequest
import com.simats.stocksense.data.model.LoginResponse
import com.simats.stocksense.data.model.SignUpRequest
import com.simats.stocksense.data.model.SignUpResponse
import com.simats.stocksense.data.model.StoreProfile
import com.simats.stocksense.data.model.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Authentication repository executing real backend operations via Retrofit,
 * managing session tokens with SessionManager/TokenStore, and translating network/HTTP errors
 * into friendly user-facing messages.
 */
class AuthRepository(
    private val apiService: AuthApiService = NetworkClient.createService(),
    private val sessionManager: SessionManager? = NetworkClient.getSessionManager(),
    private val tokenStore: TokenStore? = NetworkClient.getTokenStore()
) {

    private val _currentUser = MutableStateFlow(
        UserProfile(
            fullName = sessionManager?.getUserFullName() ?: "",
            email = sessionManager?.getUserEmail() ?: "",
            shopName = sessionManager?.getShopName() ?: ""
        )
    )
    val currentUser: StateFlow<UserProfile> = _currentUser.asStateFlow()

    /**
     * Executes login against auth/login.php.
     * On 200, clears previous session and persists Bearer token, expiration, and user details in SessionManager SharedPreferences.
     */
    suspend fun login(email: String, password: String): Result<LoginResponse> = withContext(Dispatchers.IO) {
        try {
            val normalizedEmail = email.trim().lowercase()
            val response = apiService.login(LoginRequest(email = normalizedEmail, password = password))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && (body.status == true || body.success == true)) {
                    sessionManager?.clearSession()
                    tokenStore?.clearToken()

                    val token = body.token
                    if (!token.isNullOrBlank()) {
                        sessionManager?.saveSession(token, body.expiresAt, body.user)
                        tokenStore?.saveToken(token, body.expiresAt)
                    }
                    body.user?.let { u ->
                        _currentUser.value = UserProfile(
                            fullName = u.fullName.orEmpty(),
                            email = u.email ?: normalizedEmail,
                            shopName = u.shopName.orEmpty()
                        )
                    }
                    Result.success(body)
                } else {
                    Result.failure(Exception(body?.message ?: "Login failed. Please try again."))
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
     * Executes signup against auth/signup.php.
     */
    suspend fun signUp(
        fullName: String,
        shopName: String,
        location: String = "",
        storeType: String = "",
        currency: String = "",
        email: String,
        password: String
    ): Result<SignUpResponse> = withContext(Dispatchers.IO) {
        try {
            val normalizedEmail = email.trim().lowercase()
            val request = SignUpRequest(
                fullName = fullName.trim(),
                shopName = shopName.trim(),
                location = location.trim().ifEmpty { null },
                storeType = storeType.trim().ifEmpty { null },
                currency = currency.trim().ifEmpty { null },
                email = normalizedEmail,
                password = password
            )

            val response = apiService.signup(request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && (body.status == true || body.success == true)) {
                    sessionManager?.clearSession()
                    tokenStore?.clearToken()

                    val token = body.token
                    val userObj = body.user ?: AuthUser(
                        id = body.data?.userId,
                        fullName = fullName.trim(),
                        shopName = shopName.trim(),
                        location = location.trim(),
                        storeType = storeType.trim(),
                        currency = currency.trim(),
                        email = normalizedEmail
                    )
                    if (!token.isNullOrBlank()) {
                        sessionManager?.saveSession(token, body.expiresAt, userObj)
                        tokenStore?.saveToken(token, body.expiresAt)
                    } else {
                        sessionManager?.saveSession(
                            token = "",
                            expiresAt = null,
                            user = userObj
                        )
                    }

                    _currentUser.value = UserProfile(
                        fullName = fullName.trim(),
                        email = normalizedEmail,
                        shopName = shopName.trim()
                    )
                    sessionManager?.saveStoreProfile(
                        StoreProfile(
                            name = shopName.trim(),
                            ownerName = fullName.trim(),
                            email = normalizedEmail,
                            location = location.trim().ifEmpty { "Bengaluru" },
                            storeType = storeType.trim().ifEmpty { "Neighbourhood supermarket" },
                            currency = currency.trim().ifEmpty { "INR (₹)" }
                        )
                    )
                    Result.success(body)
                } else {
                    Result.failure(Exception(body?.message ?: "Sign up failed. Please try again."))
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
     * Executes logout against auth/logout.php.
     * Deletes the local session and token from SharedPreferences regardless of whether the network call succeeds.
     */
    suspend fun logout(): Result<BaseResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.logout()
            sessionManager?.clearSession()
            tokenStore?.clearToken()
            _currentUser.value = UserProfile()
            if (response.isSuccessful) {
                Result.success(
                    response.body() ?: BaseResponse(status = true, message = "Logged out successfully.")
                )
            } else {
                Result.success(BaseResponse(status = true, message = "Logged out locally."))
            }
        } catch (_: Throwable) {
            // If the call fails, still clear the session and token locally
            sessionManager?.clearSession()
            tokenStore?.clearToken()
            _currentUser.value = UserProfile()
            Result.success(BaseResponse(status = true, message = "Logged out locally."))
        }
    }

    fun hasValidToken(): Boolean = sessionManager?.isLoggedIn() ?: (tokenStore?.hasValidToken() == true)

    fun clearToken() {
        sessionManager?.clearSession()
        tokenStore?.clearToken()
    }

    fun sendPasswordReset(email: String): Boolean {
        return email.isNotBlank() && email.contains("@")
    }

    /**
     * Maps HTTP status codes and error JSON payloads to friendly user-facing messages.
     */
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
            401 -> "Invalid email or password."
            409 -> serverMessage ?: "This email is already registered."
            422 -> serverMessage ?: "Please check the entered values."
            429 -> "Too many attempts, try later."
            in 500..599 -> "An unexpected error occurred. Please try again later."
            else -> serverMessage ?: "Request failed with code $code."
        }
    }

    /**
     * Maps network/socket exceptions to friendly user-facing messages.
     */
    private fun parseNetworkException(e: Throwable): String {
        return when (e) {
            is UnknownHostException, is ConnectException -> "No internet connection. Please check your network and try again."
            is SocketTimeoutException -> "Connection timed out. Please try again."
            else -> e.message ?: "An unexpected error occurred. Please try again."
        }
    }
}
