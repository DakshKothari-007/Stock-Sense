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
    @SerializedName("location") val location: String? = null,
    @SerializedName("store_type") val storeType: String? = null,
    @SerializedName("currency") val currency: String? = null,
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
 * Authenticated user object returned in login, signup, and profile responses
 */
data class AuthUser(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("store_id") val storeId: String? = null,
    @SerializedName("full_name") val fullName: String? = null,
    @SerializedName("shop_name") val shopName: String? = null,
    @SerializedName("location") val location: String? = null,
    @SerializedName("store_type") val storeType: String? = null,
    @SerializedName("currency") val currency: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("role") val role: String? = null,
    @SerializedName("plan") val plan: String? = null,
    @SerializedName("data_snapshot") val dataSnapshot: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null
)

/**
 * Response envelope for POST auth/signup.php
 */
data class SignUpResponse(
    @SerializedName("status") val status: Boolean? = null,
    @SerializedName("success") val success: Boolean? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("token") val token: String? = null,
    @SerializedName("expires_at") val expiresAt: String? = null,
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
 * Response envelope for GET auth/profile.php and POST auth/update_profile.php
 */
data class ProfileResponse(
    @SerializedName("status") val status: Boolean? = null,
    @SerializedName("success") val success: Boolean? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("profile") val profile: AuthUser? = null,
    @SerializedName("user") val user: AuthUser? = null,
    @SerializedName("data") val data: AuthUser? = null,
    @SerializedName("shop") val shop: AuthUser? = null
) {
    fun getEffectiveUser(): AuthUser? = profile ?: user ?: data ?: shop
}

/**
 * Request payload for POST auth/update_profile.php
 */
data class UpdateProfileRequest(
    @SerializedName("full_name") val fullName: String? = null,
    @SerializedName("new_email") val newEmail: String? = null,
    @SerializedName("current_email") val currentEmail: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("user_id") val userId: Int? = null,
    @SerializedName("token") val token: String? = null
)

/**
 * Request payload for POST auth/update_shop.php
 */
data class UpdateShopRequest(
    @SerializedName("shop_name") val shopName: String,
    @SerializedName("location") val location: String? = null,
    @SerializedName("store_type") val storeType: String? = null,
    @SerializedName("currency") val currency: String? = null,
    @SerializedName("user_id") val userId: Int? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("token") val token: String? = null
)

/**
 * Request payload for POST auth/change_password.php
 */
data class ChangePasswordRequest(
    @SerializedName("current_password") val currentPassword: String,
    @SerializedName("new_password") val newPassword: String,
    @SerializedName("confirm_password") val confirmPassword: String? = null
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
    val fullName: String = "",
    val email: String = "",
    val shopName: String = ""
)

data class LoginResult(
    val isSuccess: Boolean,
    val errorMessage: String? = null
)
