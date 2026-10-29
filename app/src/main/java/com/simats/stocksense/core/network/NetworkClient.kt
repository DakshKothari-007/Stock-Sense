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
    private var onUnauthorizedCallback: (() -> Unit)? = null

    /**
     * Initializes NetworkClient with application context and optional 401 navigation listener.
     */
    fun initialize(context: Context, onUnauthorized: (() -> Unit)? = null) {
        tokenStore = TokenStore.getInstance(context)
        onUnauthorizedCallback = onUnauthorized
    }

    fun getTokenStore(): TokenStore? = tokenStore

    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val token = tokenStore?.getToken()

        val requestBuilder = originalRequest.newBuilder()
            .header("Accept", "application/json")

        // Attach Authorization: Bearer <token> when a valid token exists
        if (!token.isNullOrBlank()) {
            requestBuilder.header("Authorization", "Bearer $token")
        }

        val response = chain.proceed(requestBuilder.build())

        // If any API returns 401 Unauthorized, automatically clear token and redirect to Login
        if (response.code == 401) {
            tokenStore?.clearToken()
            onUnauthorizedCallback?.invoke()
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

    inline fun <reified T> createService(): T = retrofit.create(T::class.java)
}
