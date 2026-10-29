package com.simats.stocksense.data.model

import com.google.gson.annotations.SerializedName

/**
 * Request payload for POST auth/login.php
 */
data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

/**
 * Request payload for POST auth/signup.php
 */
data class SignUpRequest(
    @SerializedName("full_name") val fullName: String,
    @SerializedName("shop_name") val shopName: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

/**
 * Response envelope for POST auth/login.php
 */
data class LoginResponse(
    @SerializedName("status") val status: Boolean? = null,
    @SerializedName("success") val success: Boolean? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("token") val token: String? = null,
    @SerializedName("expires_at") val expiresAt: String? = null,
    @SerializedName("user") val user: AuthUser? = null
)

/**
 * Authenticated user object returned in login and signup responses
 */
data class AuthUser(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("full_name") val fullName: String? = null,
    @SerializedName("shop_name") val shopName: String? = null,
    @SerializedName("location") val location: String? = null,
    @SerializedName("store_type") val storeType: String? = null,
    @SerializedName("currency") val currency: String? = null,
    @SerializedName("email") val email: String? = null
)

/**
 * Response envelope for POST auth/signup.php
 */
data class SignUpResponse(
    @SerializedName("status") val status: Boolean? = null,
    @SerializedName("success") val success: Boolean? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: SignUpData? = null,
    @SerializedName("user") val user: AuthUser? = null
)

/**
 * Data payload returned in signup response
 */
data class SignUpData(
    @SerializedName("user_id") val userId: Int? = null,
    @SerializedName("full_name") val fullName: String? = null,
    @SerializedName("shop_name") val shopName: String? = null,
    @SerializedName("email") val email: String? = null
)

/**
 * Standard generic response envelope (e.g. for logout or errors)
 */
data class BaseResponse(
    @SerializedName("status") val status: Boolean? = null,
    @SerializedName("success") val success: Boolean? = null,
    @SerializedName("message") val message: String? = null
)

/**
 * Legacy mock models retained for compatibility
 */
data class UserProfile(
    val fullName: String = "Ananya Rao",
    val email: String = "ananya@greenbasket.in",
    val shopName: String = "Green Basket"
)

data class LoginResult(
    val isSuccess: Boolean,
    val errorMessage: String? = null
)
