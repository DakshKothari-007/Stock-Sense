package com.simats.stocksense.core.network

/**
 * Single source of truth for backend API configuration.
 */
object ApiConstants {
    /**
     * Common Base URL accessible from:
     * - Android Emulator
     * - Physical Android Phones (via Wi-Fi or Mobile Data)
     * - Postman & Web Browsers
     *
     * Note: Must end with a trailing slash ('/') for Retrofit endpoint resolution.
     */
    const val BASE_URL = "https://0nkjz0wl-80.inc1.devtunnels.ms/StockSense_backend/"
}
