package com.simats.stocksense

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.simats.stocksense.ui.ForgotPasswordScreen
import com.simats.stocksense.ui.GoogleSignInScreen
import com.simats.stocksense.ui.LoginScreen
import com.simats.stocksense.ui.MainScreen
import com.simats.stocksense.ui.OnboardingScreen1
import com.simats.stocksense.ui.OnboardingScreen2
import com.simats.stocksense.ui.OnboardingScreen3
import com.simats.stocksense.ui.ReorderRecommendationScreen
import com.simats.stocksense.ui.SalesAnalyticsScreen
import com.simats.stocksense.ui.SignUpScreen
import com.simats.stocksense.ui.SplashScreen
import com.simats.stocksense.ui.theme.StockSenseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StockSenseTheme {
                var currentScreen by remember { mutableStateOf("splash") }

                when (currentScreen) {
                    "splash" -> SplashScreen(
                        onGetStartedClick = {
                            currentScreen = "onboarding1"
                        }
                    )
                    "onboarding1" -> OnboardingScreen1(
                        onNextClick = {
                            currentScreen = "onboarding2"
                        },
                        onSkipClick = {
                            currentScreen = "login"
                        }
                    )
                    "onboarding2" -> OnboardingScreen2(
                        onNextClick = {
                            currentScreen = "onboarding3"
                        },
                        onSkipClick = {
                            currentScreen = "login"
                        }
                    )
                    "onboarding3" -> OnboardingScreen3(
                        onGetStartedClick = {
                            currentScreen = "login"
                        },
                        onSkipClick = {
                            currentScreen = "login"
                        }
                    )
                    "login" -> LoginScreen(
                        onLoginClick = {
                            currentScreen = "main"
                        },
                        onForgotPasswordClick = {
                            currentScreen = "forgot_password"
                        },
                        onGoogleSignInClick = {
                            currentScreen = "google_signin"
                        },
                        onSignUpClick = {
                            currentScreen = "signup"
                        }
                    )
                    "signup" -> SignUpScreen(
                        onBackClick = {
                            currentScreen = "login"
                        },
                        onCreateAccountClick = {
                            currentScreen = "main"
                        },
                        onGoogleSignInClick = {
                            currentScreen = "google_signin"
                        },
                        onLoginClick = {
                            currentScreen = "login"
                        }
                    )
                    "forgot_password" -> ForgotPasswordScreen(
                        onBackToLoginClick = {
                            currentScreen = "login"
                        },
                        onSendResetLinkClick = { _ ->
                            // Password reset link sent action
                        }
                    )
                    "google_signin" -> GoogleSignInScreen(
                        onAccountSelected = { _ ->
                            currentScreen = "main"
                        },
                        onCancelClick = {
                            currentScreen = "login"
                        }
                    )
                    "main" -> MainScreen(
                        onBackClick = {
                            currentScreen = "splash"
                        },
                        onReorderRecommendationClick = {
                            currentScreen = "reorder_recommendation"
                        },
                        onSalesAnalyticsClick = {
                            currentScreen = "sales_analytics"
                        }
                    )
                    "reorder_recommendation" -> ReorderRecommendationScreen(
                        onBackClick = {
                            currentScreen = "main"
                        },
                        onAcceptClick = {
                            currentScreen = "main"
                        },
                        onDismissClick = {
                            currentScreen = "main"
                        }
                    )
                    "sales_analytics" -> SalesAnalyticsScreen(
                        onBackClick = {
                            currentScreen = "main"
                        }
                    )
                }
            }
        }
    }
}
