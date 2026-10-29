package com.simats.stocksense.core.network

import android.content.Context

/**
 * Manages persistent storage of the authenticated session Bearer token
 * by delegating to SessionManager for unified session lifecycle.
 */
class TokenStore(context: Context) {

    private val sessionManager = SessionManager.getInstance(context)

    fun saveToken(token: String, expiresAt: String? = null) {
        sessionManager.saveSession(token, expiresAt, null)
    }

    fun getToken(): String? = sessionManager.getToken()

    fun getExpiresAt(): String? = sessionManager.getExpiresAt()

    fun hasValidToken(): Boolean = sessionManager.hasValidToken()

    fun clearToken() {
        sessionManager.clearSession()
    }

    companion object {
        @Volatile
        private var instance: TokenStore? = null

        fun getInstance(context: Context): TokenStore {
            return instance ?: synchronized(this) {
                instance ?: TokenStore(context.applicationContext).also { instance = it }
            }
        }
    }
}
