package com.simats.stocksense.data.repository

import com.simats.stocksense.data.model.LoginResult
import com.simats.stocksense.data.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthRepository {

    private val _currentUser = MutableStateFlow(
        UserProfile(
            fullName = "Ananya Rao",
            email = "ananya@greenbasket.in",
            shopName = "Green Basket"
        )
    )
    val currentUser: StateFlow<UserProfile> = _currentUser.asStateFlow()

    fun login(email: String, password: String): LoginResult {
        return if (email.isNotBlank() && password.isNotBlank()) {
            LoginResult(isSuccess = true)
        } else {
            LoginResult(isSuccess = false, errorMessage = "Please enter valid credentials")
        }
    }

    fun signUp(fullName: String, shopName: String, email: String, password: String): LoginResult {
        return if (fullName.isNotBlank() && shopName.isNotBlank() && email.isNotBlank() && password.isNotBlank()) {
            _currentUser.value = UserProfile(fullName = fullName, email = email, shopName = shopName)
            LoginResult(isSuccess = true)
        } else {
            LoginResult(isSuccess = false, errorMessage = "Please fill in all fields")
        }
    }

    fun sendPasswordReset(email: String): Boolean {
        return email.isNotBlank() && email.contains("@")
    }
}
