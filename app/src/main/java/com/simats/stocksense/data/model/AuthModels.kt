package com.simats.stocksense.data.model

data class UserProfile(
    val fullName: String = "Ananya Rao",
    val email: String = "ananya@greenbasket.in",
    val shopName: String = "Green Basket"
)

data class LoginResult(
    val isSuccess: Boolean,
    val errorMessage: String? = null
)
