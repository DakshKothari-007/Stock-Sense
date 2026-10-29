package com.simats.stocksense.core.network

import com.simats.stocksense.data.model.BaseResponse
import com.simats.stocksense.data.model.ChangePasswordRequest
import com.simats.stocksense.data.model.LoginRequest
import com.simats.stocksense.data.model.LoginResponse
import com.simats.stocksense.data.model.ProfileResponse
import com.simats.stocksense.data.model.SignUpRequest
import com.simats.stocksense.data.model.SignUpResponse
import com.simats.stocksense.data.model.UpdateProfileRequest
import com.simats.stocksense.data.model.UpdateShopRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Retrofit service interface for StockSense Auth and Profile endpoints.
 * Defined strictly in accordance with API_DOCUMENT.md.
 */
interface AuthApiService {

    @POST("auth/login.php")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @POST("auth/signup.php")
    suspend fun signup(
        @Body request: SignUpRequest
    ): Response<SignUpResponse>

    @POST("auth/logout.php")
    suspend fun logout(): Response<BaseResponse>

    @GET("auth/profile.php")
    suspend fun getProfile(
        @Query("id") userId: Int? = null,
        @Query("email") email: String? = null
    ): Response<ProfileResponse>

    @POST("auth/update_profile.php")
    suspend fun updateProfile(
        @Body request: UpdateProfileRequest
    ): Response<ProfileResponse>

    @POST("auth/update_shop.php")
    suspend fun updateShop(
        @Body request: UpdateShopRequest
    ): Response<ProfileResponse>

    @POST("auth/change_password.php")
    suspend fun changePassword(
        @Body request: ChangePasswordRequest
    ): Response<BaseResponse>
}
