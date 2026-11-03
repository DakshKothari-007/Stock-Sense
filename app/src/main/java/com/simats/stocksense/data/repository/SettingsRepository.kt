package com.simats.stocksense.data.repository

import com.google.gson.Gson
import com.simats.stocksense.core.network.NetworkClient
import com.simats.stocksense.core.network.SettingsApiService
import com.simats.stocksense.data.model.ChangePasswordSettingsRequest
import com.simats.stocksense.data.model.SettingsData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository handling settings operations strictly adhering to API_DOCUMENT.md Module 9.
 */
class SettingsRepository(
    private val apiService: SettingsApiService = NetworkClient.createService(),
    private val gson: Gson = NetworkClient.gson
) {

    suspend fun fetchSettings(): Result<SettingsData> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getSettings()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.status == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message?.ifBlank { "Failed to retrieve settings." } ?: "Failed to retrieve settings."))
                }
            } else {
                handleHttpError(response.code(), response.errorBody()?.string())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateSettings(updates: Map<String, Any>): Result<SettingsData> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.updateSettings(updates)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.status == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message?.ifBlank { "Failed to update settings." } ?: "Failed to update settings."))
                }
            } else {
                handleHttpError(response.code(), response.errorBody()?.string())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resetDefaults(): Result<SettingsData> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.resetDefaults()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.status == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message?.ifBlank { "Failed to reset settings." } ?: "Failed to reset settings."))
                }
            } else {
                handleHttpError(response.code(), response.errorBody()?.string())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun changePassword(request: ChangePasswordSettingsRequest): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.changePassword(request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.status == true) {
                    Result.success(body.message ?: "Password changed successfully. All other active sessions have been logged out.")
                } else {
                    Result.failure(Exception(body?.message ?: "Failed to change password."))
                }
            } else {
                handleHttpError(response.code(), response.errorBody()?.string())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun <T> handleHttpError(code: Int, errorBody: String?): Result<T> {
        if (code == 401) {
            NetworkClient.getSessionManager()?.clearSession()
            return Result.failure(Exception("Session expired. Please log in again."))
        }
        val msg = extractErrorMessage(errorBody, code)
        return Result.failure(Exception(msg))
    }

    private fun extractErrorMessage(errorBody: String?, fallbackCode: Int): String {
        if (errorBody.isNullOrBlank()) return "Server returned error code $fallbackCode"
        return try {
            val map = gson.fromJson(errorBody, Map::class.java)
            map["message"]?.toString() ?: errorBody
        } catch (e: Exception) {
            errorBody
        }
    }
}

