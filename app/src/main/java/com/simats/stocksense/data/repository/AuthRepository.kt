package com.simats.stocksense.data.repository

import com.simats.stocksense.core.network.AuthApiService
import com.simats.stocksense.core.network.NetworkClient
import com.simats.stocksense.core.network.TokenStore
import com.simats.stocksense.data.model.AuthUser
import com.simats.stocksense.data.model.BaseResponse
import com.simats.stocksense.data.model.LoginRequest
import com.simats.stocksense.data.model.LoginResponse
import com.simats.stocksense.data.model.SignUpRequest
import com.simats.stocksense.data.model.SignUpResponse
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
 * managing session tokens with TokenStore, and translating network/HTTP errors
 * into friendly user-facing messages.
 */
class AuthRepository(
    private val apiService: AuthApiService = NetworkClient.createService(),
    private val tokenStore: TokenStore? = NetworkClient.getTokenStore()
) {

    private val _currentUser = MutableStateFlow(
        UserProfile(
            fullName = "Ananya Rao",
            email = "ananya@greenbasket.in",
            shopName = "Green Basket"
        )
    )
    val currentUser: StateFlow<UserProfile> = _currentUser.asStateFlow()

    /**
     * Executes login against auth/login.php.
     * On 200, persists Bearer token and expiry to TokenStore.
     */
    suspend fun login(email: String, password: String): Result<LoginResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.login(LoginRequest(email = email.trim(), password = password))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && (body.status == true || body.success == true)) {
                    val token = body.token
                    if (!token.isNullOrBlank()) {
                        tokenStore?.saveToken(token, body.expiresAt)
                    }
                    body.user?.let { u ->
                        _currentUser.value = UserProfile(
                            fullName = u.fullName ?: "Store Owner",
                            email = u.email ?: email,
                            shopName = u.shopName ?: "Store"
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
        email: String,
        password: String
    ): Result<SignUpResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.signup(
                SignUpRequest(
                    fullName = fullName.trim(),
                    shopName = shopName.trim(),
                    email = email.trim(),
                    password = password
                )
            )
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && (body.status == true || body.success == true)) {
                    _currentUser.value = UserProfile(
                        fullName = fullName.trim(),
                        email = email.trim(),
                        shopName = shopName.trim()
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
     * Deletes the local token regardless of whether the network call succeeds.
     */
    suspend fun logout(): Result<BaseResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.logout()
            tokenStore?.clearToken()
            if (response.isSuccessful) {
                Result.success(
                    response.body() ?: BaseResponse(status = true, message = "Logged out successfully.")
                )
            } else {
                Result.success(BaseResponse(status = true, message = "Logged out locally."))
            }
        } catch (_: Throwable) {
            // If the call fails, still delete the token locally
            tokenStore?.clearToken()
            Result.success(BaseResponse(status = true, message = "Logged out locally."))
        }
    }

    fun hasValidToken(): Boolean = tokenStore?.hasValidToken() == true

    fun clearToken() {
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
