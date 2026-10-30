package com.simats.stocksense

import android.app.Application
import com.simats.stocksense.core.network.NetworkClient

/**
 * Custom Application class for StockSense.
 * Ensures NetworkClient and SessionManager are initialized at the earliest possible lifecycle event.
 */
class StockSenseApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NetworkClient.initialize(this)
    }
}

