package com.simats.stocksense

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.simats.stocksense.ui.SplashScreen
import com.simats.stocksense.ui.theme.StockSenseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StockSenseTheme {
                SplashScreen(
                    onGetStartedClick = {
                        // Handle Get Started action here (e.g. navigate to login / main inventory screen)
                    }
                )
            }
        }
    }
}
