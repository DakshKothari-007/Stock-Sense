package com.simats.stocksense

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.simats.stocksense.core.theme.StockSenseTheme
import com.simats.stocksense.feature.analytics.ui.SalesAnalyticsScreen
import com.simats.stocksense.feature.auth.ui.ForgotPasswordScreen
import com.simats.stocksense.feature.auth.ui.GoogleSignInScreen
import com.simats.stocksense.feature.auth.ui.LoginScreen
import com.simats.stocksense.feature.auth.ui.OnboardingScreen1
import com.simats.stocksense.feature.auth.ui.OnboardingScreen2
import com.simats.stocksense.feature.auth.ui.OnboardingScreen3
import com.simats.stocksense.feature.auth.ui.SignUpScreen
import com.simats.stocksense.feature.auth.ui.SplashScreen
import com.simats.stocksense.feature.dashboard.ui.MainScreen
import com.simats.stocksense.feature.dashboard.ui.NotificationsScreen
import com.simats.stocksense.feature.inventory.ui.InventoryImportedScreen
import com.simats.stocksense.feature.inventory.ui.ProductBiscuitsScreen
import com.simats.stocksense.feature.inventory.ui.ProductDetailScreen
import com.simats.stocksense.feature.reorder.ui.ReorderPlacedScreen
import com.simats.stocksense.feature.reorder.ui.ReorderRecommendationScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StockSenseTheme {
                StockSenseAppNavigation()
            }
        }
    }
}

@Composable
fun StockSenseAppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(
                onGetStartedClick = {
                    navController.navigate("onboarding1")
                }
            )
        }
        composable("onboarding1") {
            OnboardingScreen1(
                onNextClick = {
                    navController.navigate("onboarding2")
                },
                onSkipClick = {
                    navController.navigate("login") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }
        composable("onboarding2") {
            OnboardingScreen2(
                onNextClick = {
                    navController.navigate("onboarding3")
                },
                onSkipClick = {
                    navController.navigate("login") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }
        composable("onboarding3") {
            OnboardingScreen3(
                onGetStartedClick = {
                    navController.navigate("login") {
                        popUpTo("splash") { inclusive = true }
                    }
                },
                onSkipClick = {
                    navController.navigate("login") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }
        composable("login") {
            LoginScreen(
                onLoginClick = {
                    navController.navigate("main") {
                        popUpTo("splash") { inclusive = true }
                    }
                },
                onForgotPasswordClick = {
                    navController.navigate("forgot_password")
                },
                onGoogleSignInClick = {
                    navController.navigate("google_signin")
                },
                onSignUpClick = {
                    navController.navigate("signup")
                }
            )
        }
        composable("signup") {
            SignUpScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onCreateAccountClick = {
                    navController.navigate("main") {
                        popUpTo("splash") { inclusive = true }
                    }
                },
                onGoogleSignInClick = {
                    navController.navigate("google_signin")
                },
                onLoginClick = {
                    navController.navigate("login") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable("forgot_password") {
            ForgotPasswordScreen(
                onBackToLoginClick = {
                    navController.popBackStack()
                },
                onSendResetLinkClick = { _ ->
                    navController.popBackStack()
                }
            )
        }
        composable("google_signin") {
            GoogleSignInScreen(
                onAccountSelected = { _ ->
                    navController.navigate("main") {
                        popUpTo("splash") { inclusive = true }
                    }
                },
                onCancelClick = {
                    navController.popBackStack()
                }
            )
        }
        composable("main") {
            MainScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onNotificationsClick = {
                    navController.navigate("notifications")
                },
                onProductMilkClick = {
                    navController.navigate("product_detail")
                },
                onProductBiscuitsClick = {
                    navController.navigate("product_biscuits")
                },
                onSalesAnalyticsClick = {
                    navController.navigate("sales_analytics")
                },
                onReorderRecommendationClick = {
                    navController.navigate("reorder_recommendation")
                },
                onInventoryImportedClick = {
                    navController.navigate("inventory_imported")
                }
            )
        }
        composable("notifications") {
            NotificationsScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        composable("product_detail") {
            ProductDetailScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onReorderClick = {
                    navController.navigate("reorder_placed")
                },
                onSalesAnalyticsClick = {
                    navController.navigate("sales_analytics")
                }
            )
        }
        composable("product_biscuits") {
            ProductBiscuitsScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onSalesAnalyticsClick = {
                    navController.navigate("sales_analytics")
                },
                onActionClick = {
                    navController.navigate("reorder_recommendation")
                }
            )
        }
        composable("reorder_recommendation") {
            ReorderRecommendationScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onAcceptClick = {
                    navController.navigate("reorder_placed") {
                        popUpTo("main") { inclusive = false }
                    }
                },
                onDismissClick = {
                    navController.popBackStack()
                }
            )
        }
        composable("reorder_placed") {
            ReorderPlacedScreen(
                onDoneClick = {
                    navController.popBackStack()
                }
            )
        }
        composable("sales_analytics") {
            SalesAnalyticsScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        composable("inventory_imported") {
            InventoryImportedScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
