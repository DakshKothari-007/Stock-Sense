package com.simats.stocksense.core.network

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton networking client configured with Retrofit, Gson, 15-second timeouts,
 * automatic Bearer token injection, and 401 session invalidation.
 */
object NetworkClient {

    private var tokenStore: TokenStore? = null
    private var sessionManager: SessionManager? = null
    private var onUnauthorizedCallback: (() -> Unit)? = null

    /**
     * Initializes NetworkClient with application context and optional 401 navigation listener.
     */
    fun initialize(context: Context, onUnauthorized: (() -> Unit)? = null) {
        tokenStore = TokenStore.getInstance(context)
        sessionManager = SessionManager.getInstance(context)
        onUnauthorizedCallback = onUnauthorized
    }

    fun getTokenStore(): TokenStore? = tokenStore

    fun getSessionManager(): SessionManager? = sessionManager

    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val token = sessionManager?.getToken() ?: tokenStore?.getToken()

        val requestBuilder = originalRequest.newBuilder()
            .header("Accept", "application/json")

        // Attach Authorization: Bearer <token> when a valid token exists
        if (!token.isNullOrBlank()) {
            requestBuilder.header("Authorization", "Bearer $token")
        }

        val response = chain.proceed(requestBuilder.build())

        // If any API returns 401 Unauthorized on an authenticated session, clear session and redirect to Login on Main thread
        if (response.code == 401) {
            val hadSession = sessionManager?.isLoggedIn() == true || !token.isNullOrBlank()
            sessionManager?.clearSession()
            tokenStore?.clearToken()
            if (hadSession) {
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    onUnauthorizedCallback?.invoke()
                }
            }
        }

        response
    }

    // Never log request/response bodies to avoid leaking passwords or sensitive tokens
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.HEADERS
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    // 60-second timeouts specifically for bulk spreadsheet import endpoints
    private val importOkHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    val gson: Gson by lazy {
        GsonBuilder()
            .setLenient()
            .create()
    }

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(ApiConstants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val importRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(ApiConstants.BASE_URL)
            .client(importOkHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    inline fun <reified T> createService(): T {
        return if (T::class.java.name.contains("ImportApiService")) {
            importRetrofit.create(T::class.java)
        } else {
            retrofit.create(T::class.java)
        }
    }
}
