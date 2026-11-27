package com.simats.stocksense.feature.auth

import android.content.SharedPreferences
import com.simats.stocksense.core.avatar.AvatarCode
import com.simats.stocksense.core.network.AuthApiService
import com.simats.stocksense.core.network.SessionManager
import com.simats.stocksense.data.model.*
import com.simats.stocksense.data.repository.AuthRepository
import com.simats.stocksense.data.repository.ProfileRepository
import com.simats.stocksense.feature.auth.viewmodel.AuthViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Response
import java.io.IOException

class LogoutInstantTest {

    private class InMemorySharedPreferences : SharedPreferences {
        private val data = LinkedHashMap<String, Any?>()

        override fun getAll(): MutableMap<String, *> = LinkedHashMap(data)
        override fun getString(key: String?, defValue: String?): String? = (data[key] as? String) ?: defValue
        override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? = defValues
        override fun getInt(key: String?, defValue: Int): Int = (data[key] as? Int) ?: defValue
        override fun getLong(key: String?, defValue: Long): Long = (data[key] as? Long) ?: defValue
        override fun getFloat(key: String?, defValue: Float): Float = (data[key] as? Float) ?: defValue
        override fun getBoolean(key: String?, defValue: Boolean): Boolean = (data[key] as? Boolean) ?: defValue
        override fun contains(key: String?): Boolean = data.containsKey(key)
        override fun edit(): SharedPreferences.Editor = Editor()
        override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}
        override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}

        inner class Editor : SharedPreferences.Editor {
            private val pending = LinkedHashMap<String, Any?>()
            private var clearAll = false

            override fun putString(key: String?, value: String?): SharedPreferences.Editor = apply {
                if (key != null) pending[key] = value
            }

            override fun putStringSet(key: String?, values: MutableSet<String>?): SharedPreferences.Editor = apply {
                if (key != null) pending[key] = values
            }

            override fun putInt(key: String?, value: Int): SharedPreferences.Editor = apply {
                if (key != null) pending[key] = value
            }

            override fun putLong(key: String?, value: Long): SharedPreferences.Editor = apply {
                if (key != null) pending[key] = value
            }

            override fun putFloat(key: String?, value: Float): SharedPreferences.Editor = apply {
                if (key != null) pending[key] = value
            }

            override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor = apply {
                if (key != null) pending[key] = value
            }

            override fun remove(key: String?): SharedPreferences.Editor = apply {
                if (key != null) pending[key] = null
            }

            override fun clear(): SharedPreferences.Editor = apply {
                clearAll = true
            }

            override fun commit(): Boolean {
                apply()
                return true
            }

            override fun apply() {
                if (clearAll) data.clear()
                for ((k, v) in pending) {
                    if (v == null) data.remove(k) else data[k] = v
                }
            }
        }
    }

    private class FakeAuthApiService(
        private val onLogout: suspend (String?) -> Response<BaseResponse>
    ) : AuthApiService {
        var lastLogoutAuthHeader: String? = null
        val logoutCalled = CompletableDeferred<Unit>()

        override suspend fun logout(authHeader: String?): Response<BaseResponse> {
            lastLogoutAuthHeader = authHeader
            logoutCalled.complete(Unit)
            return onLogout(authHeader)
        }

        override suspend fun login(request: LoginRequest): Response<LoginResponse> = error("unused")
        override suspend fun signup(request: SignUpRequest): Response<SignUpResponse> = error("unused")
        override suspend fun getProfile(userId: Int?, email: String?): Response<ProfileResponse> = error("unused")
        override suspend fun updateProfile(request: UpdateProfileRequest): Response<ProfileResponse> = error("unused")
        override suspend fun updateShop(request: UpdateShopRequest): Response<ProfileResponse> = error("unused")
        override suspend fun updateAvatar(request: AvatarCode): Response<ProfileResponse> = error("unused")
        override suspend fun removeAvatar(): Response<ProfileResponse> = error("unused")
        override suspend fun changePassword(request: ChangePasswordRequest): Response<BaseResponse> = error("unused")
        override suspend fun deleteAccount(request: DeleteAccountRequest): Response<BaseResponse> = error("unused")
    }

    private fun createSessionWithToken(token: String): SessionManager {
        val plain = InMemorySharedPreferences()
        val secure = InMemorySharedPreferences()
        val sm = SessionManager(plain, secure)
        sm.saveSession(token = token)
        assertTrue(sm.isLoggedIn())
        assertEquals(token, sm.getToken())
        return sm
    }

    @Test
    fun authViewModelLogout_clearsSessionAndCallsOnCompleteWithoutWaitingForSlowNetwork() = runBlocking {
        val sessionManager = createSessionWithToken("test-token-123")
        val gate = CompletableDeferred<Unit>()
        val networkFinished = CompletableDeferred<Unit>()
        val fakeApi = FakeAuthApiService {
            gate.await()
            networkFinished.complete(Unit)
            Response.success(BaseResponse(status = true, message = "ok"))
        }
        val bgScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        try {
            val authRepo = AuthRepository(apiService = fakeApi, customSessionManager = sessionManager)
            val viewModel = AuthViewModel(authRepository = authRepo, backgroundScope = bgScope)

            var onCompleteCalled = false
            viewModel.logout {
                // Session must already be cleared before navigation callback runs
                assertFalse(sessionManager.isLoggedIn())
                assertNull(sessionManager.getToken())
                onCompleteCalled = true
            }

            // Must complete synchronously without waiting for the hanging network call
            assertTrue("onComplete should be called immediately", onCompleteCalled)
            assertFalse("Network revoke should still be waiting on gate", networkFinished.isCompleted)

            // Let background revoke proceed and verify it received the saved Bearer token
            gate.complete(Unit)
            withTimeout(2_000L) {
                fakeApi.logoutCalled.await()
                networkFinished.await()
            }
            assertEquals("Bearer test-token-123", fakeApi.lastLogoutAuthHeader)
        } finally {
            bgScope.cancel()
        }
    }

    @Test
    fun authViewModelLogout_clearsSessionEvenWhenServerRevokeFails() = runBlocking {
        val sessionManager = createSessionWithToken("test-token-err")
        val fakeApi = FakeAuthApiService {
            throw IOException("No internet")
        }
        val bgScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        try {
            val authRepo = AuthRepository(apiService = fakeApi, customSessionManager = sessionManager)
            val viewModel = AuthViewModel(authRepository = authRepo, backgroundScope = bgScope)

            var onCompleteCalled = false
            viewModel.logout {
                onCompleteCalled = true
            }

            assertTrue(onCompleteCalled)
            assertFalse(sessionManager.isLoggedIn())
            assertNull(sessionManager.getToken())

            withTimeout(2_000L) {
                fakeApi.logoutCalled.await()
            }
            assertEquals("Bearer test-token-err", fakeApi.lastLogoutAuthHeader)
        } finally {
            bgScope.cancel()
        }
    }

    @Test
    fun profileRepositoryClearAndBackgroundRevoke_preservesTokenForRevokeAfterClearingSession() = runBlocking {
        val sessionManager = createSessionWithToken("profile-token-456")
        val fakeApi = FakeAuthApiService {
            throw IOException("Offline")
        }
        val repo = ProfileRepository(apiService = fakeApi, customSessionManager = sessionManager)

        val savedToken = repo.clearLocalSession()
        assertEquals("profile-token-456", savedToken)
        assertFalse(sessionManager.isLoggedIn())
        assertNull(sessionManager.getToken())

        // Should not throw even when network fails
        repo.revokeTokenInBackground(savedToken)
        assertEquals("Bearer profile-token-456", fakeApi.lastLogoutAuthHeader)
    }
}
