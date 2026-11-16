package com.simats.stocksense.core.network

import com.simats.stocksense.BuildConfig

/**
 * Single source of truth for backend API configuration.
 */
object ApiConstants {
    /**
     * Base URL provided by build configuration via BuildConfig.
     * Debug: Local, development, or tunneling URL (e.g. ngrok)
     * Release: Production HTTPS URL (strictly no debug IPs)
     *
     * Note: Must end with a trailing slash ('/') for Retrofit endpoint resolution.
     */
    val BASE_URL: String = BuildConfig.BASE_URL
}

