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
    const val BASE_URL = "https://gossip-renderer-void.ngrok-free.dev/StockSense_backend/"
}
