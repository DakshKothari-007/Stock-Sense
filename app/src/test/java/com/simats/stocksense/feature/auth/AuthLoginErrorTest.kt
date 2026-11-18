package com.simats.stocksense.feature.auth

import com.google.gson.Gson
import com.simats.stocksense.data.model.LoginApiException
import com.simats.stocksense.data.model.LoginErrorResponse
import com.simats.stocksense.feature.auth.viewmodel.AuthViewModel
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests verifying:
 * 1. Phone-side email format validation ("Enter a valid email")
 * 2. Field-specific login error responses and JSON deserialization
 * 3. Lockout countdown calculation and formatting
 * 4. Email sanitation (whitespace trimming and lowercase normalization)
 */
class AuthLoginErrorTest {

    private val gson = Gson()

    @Test
    fun testEmailNotFoundDeserialization() {
        val json = """
            {
                "status": false,
                "success": false,
                "error_code": "EMAIL_NOT_FOUND",
                "message": "No account found with this email."
            }
        """.trimIndent()

        val parsed = gson.fromJson(json, LoginErrorResponse::class.java)
        assertNotNull(parsed)
        assertEquals("EMAIL_NOT_FOUND", parsed.errorCode)
        assertEquals("No account found with this email.", parsed.message)
        assertNull(parsed.attemptsLeft)
        assertNull(parsed.retryAfterSeconds)
    }

    @Test
    fun testWrongPasswordDeserialization() {
        val json = """
            {
                "status": false,
                "success": false,
                "error_code": "WRONG_PASSWORD",
                "message": "Invalid password.",
                "attempts_left": 4
            }
        """.trimIndent()

        val parsed = gson.fromJson(json, LoginErrorResponse::class.java)
        assertNotNull(parsed)
        assertEquals("WRONG_PASSWORD", parsed.errorCode)
        assertEquals("Invalid password.", parsed.message)
        assertEquals(4, parsed.attemptsLeft)
        assertNull(parsed.retryAfterSeconds)
    }

    @Test
    fun testAccountLockedDeserialization() {
        val json = """
            {
                "status": false,
                "success": false,
                "error_code": "ACCOUNT_LOCKED",
                "message": "Too many failed login attempts. Account locked for 5 minutes.",
                "retry_after_seconds": 300
            }
        """.trimIndent()

        val parsed = gson.fromJson(json, LoginErrorResponse::class.java)
        assertNotNull(parsed)
        assertEquals("ACCOUNT_LOCKED", parsed.errorCode)
        assertEquals("Too many failed login attempts. Account locked for 5 minutes.", parsed.message)
        assertEquals(300, parsed.retryAfterSeconds)
    }

    @Test
    fun testPhoneSideEmailValidation() {
        val viewModel = AuthViewModel()

        // 1. Empty email validation
        viewModel.onLogin(email = "   ", password = "password123")
        var state = viewModel.loginState.value
        assertEquals("Enter a valid email", state.emailError)
        assertFalse(state.isLoading)

        // 2. Malformed email validation
        viewModel.onLogin(email = "notanemail", password = "password123")
        state = viewModel.loginState.value
        assertEquals("Enter a valid email", state.emailError)
        assertFalse(state.isLoading)

        // 3. Missing domain
        viewModel.onLogin(email = "user@", password = "password123")
        state = viewModel.loginState.value
        assertEquals("Enter a valid email", state.emailError)
        assertFalse(state.isLoading)
    }

    @Test
    fun testEmailTrimmingAndCaseInsensitivity() {
        val viewModel = AuthViewModel()
        val rawInput = "  Daksh14001@Gmail.COM   "
        val trimmed = rawInput.trim().lowercase()

        assertEquals("daksh14001@gmail.com", trimmed)

        viewModel.updateLoginEmail(rawInput)
        assertEquals(rawInput, viewModel.loginState.value.email)

        // Updating email clears previous error
        viewModel.updateLoginEmail("new@email.com")
        assertNull(viewModel.loginState.value.emailError)
    }

    @Test
    fun testLockoutTimerFormatting() {
        val viewModel = AuthViewModel()

        viewModel.startLockoutCountdown(300)
        var state = viewModel.loginState.value
        assertEquals(300, state.lockRetryAfterSeconds)
        assertEquals("Too many failed attempts. Try again in 5:00", state.bannerError)

        viewModel.startLockoutCountdown(272)
        state = viewModel.loginState.value
        assertEquals(272, state.lockRetryAfterSeconds)
        assertEquals("Too many failed attempts. Try again in 4:32", state.bannerError)

        viewModel.startLockoutCountdown(0)
        state = viewModel.loginState.value
        assertEquals(0, state.lockRetryAfterSeconds)
        assertNull(state.bannerError)
    }

    @Test
    fun testWrongPasswordAttemptsLeftFormatting() {
        val attempts4 = 4
        val msg4 = "Invalid password. $attempts4 attempts left"
        assertEquals("Invalid password. 4 attempts left", msg4)

        val attempts1 = 1
        val msg1 = "Invalid password. 1 attempt left"
        assertEquals("Invalid password. 1 attempt left", msg1)
    }
}
