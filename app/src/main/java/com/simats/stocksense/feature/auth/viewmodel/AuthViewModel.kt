package com.simats.stocksense.feature.auth.viewmodel

import androidx.lifecycle.ViewModel
import com.simats.stocksense.data.model.UserProfile
import com.simats.stocksense.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class LoginUiState(
    val email: String = "ananya@greenbasket.in",
    val password: String = "password123",
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

data class SignUpUiState(
    val fullName: String = "Ananya Rao",
    val shopName: String = "Green Basket",
    val email: String = "ananya@greenbasket.in",
    val password: String = "password123",
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

data class ForgotPasswordUiState(
    val email: String = "ananya@greenbasket.in",
    val isSubmitted: Boolean = false,
    val errorMessage: String? = null
)

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _loginState = MutableStateFlow(LoginUiState())
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()

    private val _signUpState = MutableStateFlow(SignUpUiState())
    val signUpState: StateFlow<SignUpUiState> = _signUpState.asStateFlow()

    private val _forgotPasswordState = MutableStateFlow(ForgotPasswordUiState())
    val forgotPasswordState: StateFlow<ForgotPasswordUiState> = _forgotPasswordState.asStateFlow()

    fun updateLoginEmail(email: String) {
        _loginState.update { it.copy(email = email, errorMessage = null) }
    }

    fun updateLoginPassword(password: String) {
        _loginState.update { it.copy(password = password, errorMessage = null) }
    }

    fun login(onSuccess: () -> Unit) {
        val result = authRepository.login(_loginState.value.email, _loginState.value.password)
        if (result.isSuccess) {
            _loginState.update { it.copy(isSuccess = true, errorMessage = null) }
            onSuccess()
        } else {
            _loginState.update { it.copy(errorMessage = result.errorMessage) }
        }
    }

    fun updateSignUpFullName(name: String) {
        _signUpState.update { it.copy(fullName = name, errorMessage = null) }
    }

    fun updateSignUpShopName(shop: String) {
        _signUpState.update { it.copy(shopName = shop, errorMessage = null) }
    }

    fun updateSignUpEmail(email: String) {
        _signUpState.update { it.copy(email = email, errorMessage = null) }
    }

    fun updateSignUpPassword(password: String) {
        _signUpState.update { it.copy(password = password, errorMessage = null) }
    }

    fun signUp(onSuccess: () -> Unit) {
        val s = _signUpState.value
        val result = authRepository.signUp(s.fullName, s.shopName, s.email, s.password)
        if (result.isSuccess) {
            _signUpState.update { it.copy(isSuccess = true, errorMessage = null) }
            onSuccess()
        } else {
            _signUpState.update { it.copy(errorMessage = result.errorMessage) }
        }
    }

    fun updateForgotPasswordEmail(email: String) {
        _forgotPasswordState.update { it.copy(email = email, errorMessage = null) }
    }

    fun sendPasswordReset() {
        val success = authRepository.sendPasswordReset(_forgotPasswordState.value.email)
        if (success) {
            _forgotPasswordState.update { it.copy(isSubmitted = true, errorMessage = null) }
        } else {
            _forgotPasswordState.update { it.copy(errorMessage = "Please enter a valid email address") }
        }
    }
}
