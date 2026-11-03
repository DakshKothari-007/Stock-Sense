package com.simats.stocksense.core.network

import com.simats.stocksense.data.model.ChangePasswordSettingsRequest
import com.simats.stocksense.data.model.SettingsResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Retrofit API service for Module 9: Store & System Settings (settings/).
 */
interface SettingsApiService {

    @GET("settings/get.php")
    suspend fun getSettings(): Response<SettingsResponse>

    @POST("settings/update.php")
    suspend fun updateSettings(
        @Body request: Map<String, @JvmSuppressWildcards Any>
    ): Response<SettingsResponse>

    @POST("settings/reset_defaults.php")
    suspend fun resetDefaults(
        @Body emptyBody: Map<String, String> = emptyMap()
    ): Response<SettingsResponse>

    @POST("settings/change_password.php")
    suspend fun changePassword(
        @Body request: ChangePasswordSettingsRequest
    ): Response<SettingsResponse>
}

