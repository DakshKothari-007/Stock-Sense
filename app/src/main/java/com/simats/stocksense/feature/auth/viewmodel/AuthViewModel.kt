package com.simats.stocksense.feature.auth.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.stocksense.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Unified UI state representing async operations for loading, errors, and success.
 */
data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

data class SignUpUiState(
    val fullName: String = "",
    val shopName: String = "",
    val location: String = "",
    val storeType: String = "Neighbourhood supermarket",
    val currency: String = "INR (₹)",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

data class ForgotPasswordUiState(
    val email: String = "",
    val isSubmitted: Boolean = false,
    val errorMessage: String? = null
)

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _authUiState = MutableStateFlow(AuthUiState())
    val authUiState: StateFlow<AuthUiState> = _authUiState.asStateFlow()

    private val _loginState = MutableStateFlow(LoginUiState())
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()

    private val _signUpState = MutableStateFlow(SignUpUiState())
    val signUpState: StateFlow<SignUpUiState> = _signUpState.asStateFlow()

    private val _forgotPasswordState = MutableStateFlow(ForgotPasswordUiState())
    val forgotPasswordState: StateFlow<ForgotPasswordUiState> = _forgotPasswordState.asStateFlow()

    // -------------------------------------------------------------
    // Input State Modifiers
    // -------------------------------------------------------------

    fun updateLoginEmail(email: String) {
        _loginState.update { it.copy(email = email, errorMessage = null) }
        _authUiState.update { it.copy(errorMessage = null) }
    }

    fun updateLoginPassword(password: String) {
        _loginState.update { it.copy(password = password, errorMessage = null) }
        _authUiState.update { it.copy(errorMessage = null) }
    }

    fun updateSignUpFullName(name: String) {
        _signUpState.update { it.copy(fullName = name, errorMessage = null) }
        _authUiState.update { it.copy(errorMessage = null) }
    }

    fun updateSignUpShopName(shop: String) {
        _signUpState.update { it.copy(shopName = shop, errorMessage = null) }
        _authUiState.update { it.copy(errorMessage = null) }
    }

    fun updateSignUpLocation(location: String) {
        _signUpState.update { it.copy(location = location, errorMessage = null) }
        _authUiState.update { it.copy(errorMessage = null) }
    }

    fun updateSignUpStoreType(storeType: String) {
        _signUpState.update { it.copy(storeType = storeType, errorMessage = null) }
        _authUiState.update { it.copy(errorMessage = null) }
    }

    fun updateSignUpCurrency(currency: String) {
        _signUpState.update { it.copy(currency = currency, errorMessage = null) }
        _authUiState.update { it.copy(errorMessage = null) }
    }

    fun updateSignUpEmail(email: String) {
        _signUpState.update { it.copy(email = email, errorMessage = null) }
        _authUiState.update { it.copy(errorMessage = null) }
    }

    fun updateSignUpPassword(password: String) {
        _signUpState.update { it.copy(password = password, errorMessage = null) }
        _authUiState.update { it.copy(errorMessage = null) }
    }

    fun updateSignUpConfirmPassword(confirm: String) {
        _signUpState.update { it.copy(confirmPassword = confirm, errorMessage = null) }
        _authUiState.update { it.copy(errorMessage = null) }
    }

    fun updateForgotPasswordEmail(email: String) {
        _forgotPasswordState.update { it.copy(email = email, errorMessage = null) }
    }

    // -------------------------------------------------------------
    // Login Operations
    // -------------------------------------------------------------

    /**
     * Executes login with input validation, viewModelScope coroutine, and real backend authentication.
     */
    fun onLogin(email: String, password: String, onSuccess: () -> Unit = {}) {
        val trimmedEmail = email.trim().lowercase()

        // Validation
        if (trimmedEmail.isBlank() || password.isBlank()) {
            val error = "Please enter both email and password."
            _loginState.update { it.copy(errorMessage = error, isLoading = false) }
            _authUiState.update { it.copy(errorMessage = error, isLoading = false) }
            return
        }

        if (!isValidEmail(trimmedEmail)) {
            val error = "Please enter a valid email address."
            _loginState.update { it.copy(errorMessage = error, isLoading = false) }
            _authUiState.update { it.copy(errorMessage = error, isLoading = false) }
            return
        }

        _loginState.update { it.copy(isLoading = true, errorMessage = null) }
        _authUiState.update { it.copy(isLoading = true, errorMessage = null, isSuccess = false) }

        viewModelScope.launch {
            val result = authRepository.login(trimmedEmail, password)
            result.fold(
                onSuccess = {
                    _loginState.update { it.copy(isLoading = false, isSuccess = true, errorMessage = null) }
                    _authUiState.update { it.copy(isLoading = false, isSuccess = true, errorMessage = null) }
                    onSuccess()
                },
                onFailure = { err ->
                    val errorMsg = err.message ?: "Login failed. Please check your credentials."
                    _loginState.update { it.copy(isLoading = false, errorMessage = errorMsg, isSuccess = false) }
                    _authUiState.update { it.copy(isLoading = false, errorMessage = errorMsg, isSuccess = false) }
                }
            )
        }
    }

    /**
     * Bridge method for existing LoginScreen.kt callers.
     */
    fun login(onSuccess: () -> Unit) {
        val s = _loginState.value
        onLogin(s.email, s.password, onSuccess)
    }

    // -------------------------------------------------------------
    // Signup Operations
    // -------------------------------------------------------------

    /**
     * Executes signup with input validation, viewModelScope coroutine, and real backend registration.
     */
    fun onSignup(
        fullName: String,
        shopName: String,
        location: String = "",
        storeType: String = "",
        currency: String = "",
        email: String,
        password: String,
        confirmPassword: String? = null,
        onSuccess: () -> Unit = {}
    ) {
        val trimmedName = fullName.trim()
        val trimmedShop = shopName.trim()
        val trimmedLocation = location.trim()
        val trimmedStoreType = storeType.trim()
        val trimmedCurrency = currency.trim()
        val trimmedEmail = email.trim().lowercase()

        // Non-empty fields check
        if (trimmedName.isBlank() || trimmedShop.isBlank() || trimmedLocation.isBlank() || trimmedStoreType.isBlank() || trimmedEmail.isBlank() || password.isBlank()) {
            val error = "Please fill in all required fields."
            _signUpState.update { it.copy(errorMessage = error, isLoading = false) }
            _authUiState.update { it.copy(errorMessage = error, isLoading = false) }
            return
        }

        // Valid email check
        if (!isValidEmail(trimmedEmail)) {
            val error = "Please enter a valid email address."
            _signUpState.update { it.copy(errorMessage = error, isLoading = false) }
            _authUiState.update { it.copy(errorMessage = error, isLoading = false) }
            return
        }

        // Passwords match check (if confirmPassword provided)
        if (confirmPassword != null && confirmPassword.isNotEmpty() && password != confirmPassword) {
            val error = "Passwords do not match."
            _signUpState.update { it.copy(errorMessage = error, isLoading = false) }
            _authUiState.update { it.copy(errorMessage = error, isLoading = false) }
            return
        }

        // Minimum password length check (min 6 chars per API_DOCUMENT.md)
        if (password.length < 6) {
            val error = "Password must be at least 6 characters."
            _signUpState.update { it.copy(errorMessage = error, isLoading = false) }
            _authUiState.update { it.copy(errorMessage = error, isLoading = false) }
            return
        }

        _signUpState.update { it.copy(isLoading = true, errorMessage = null) }
        _authUiState.update { it.copy(isLoading = true, errorMessage = null, isSuccess = false) }

        viewModelScope.launch {
            val result = authRepository.signUp(
                fullName = trimmedName,
                shopName = trimmedShop,
                location = trimmedLocation,
                storeType = trimmedStoreType,
                currency = trimmedCurrency,
                email = trimmedEmail,
                password = password
            )
            result.fold(
                onSuccess = {
                    _signUpState.update { it.copy(isLoading = false, isSuccess = true, errorMessage = null) }
                    _authUiState.update { it.copy(isLoading = false, isSuccess = true, errorMessage = null) }
                    onSuccess()
                },
                onFailure = { err ->
                    val errorMsg = err.message ?: "Registration failed. Please try again."
                    _signUpState.update { it.copy(isLoading = false, errorMessage = errorMsg, isSuccess = false) }
                    _authUiState.update { it.copy(isLoading = false, errorMessage = errorMsg, isSuccess = false) }
                }
            )
        }
    }

    /**
     * Bridge method for existing SignUpScreen.kt callers.
     */
    fun signUp(onSuccess: () -> Unit) {
        val s = _signUpState.value
        val confirm = if (s.confirmPassword.isNotBlank()) s.confirmPassword else null
        onSignup(
            fullName = s.fullName,
            shopName = s.shopName,
            location = s.location,
            storeType = s.storeType,
            currency = s.currency,
            email = s.email,
            password = s.password,
            confirmPassword = confirm,
            onSuccess = onSuccess
        )
    }

    // -------------------------------------------------------------
    // Logout Operations
    // -------------------------------------------------------------

    fun logout(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            authRepository.logout()
            _authUiState.update { AuthUiState() }
            _loginState.update { LoginUiState(email = "", password = "") }
            onComplete()
        }
    }

    fun sendPasswordReset() {
        val success = authRepository.sendPasswordReset(_forgotPasswordState.value.email)
        if (success) {
            _forgotPasswordState.update { it.copy(isSubmitted = true, errorMessage = null) }
        } else {
            _forgotPasswordState.update { it.copy(errorMessage = "Please enter a valid email address.") }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
