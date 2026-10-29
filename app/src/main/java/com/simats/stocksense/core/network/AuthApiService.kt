package com.simats.stocksense.core.network

import com.simats.stocksense.data.model.BaseResponse
import com.simats.stocksense.data.model.LoginRequest
import com.simats.stocksense.data.model.LoginResponse
import com.simats.stocksense.data.model.SignUpRequest
import com.simats.stocksense.data.model.SignUpResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Retrofit service interface for StockSense Auth endpoints.
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
}
