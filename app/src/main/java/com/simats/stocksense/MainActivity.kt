package com.simats.stocksense

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.simats.stocksense.core.network.NetworkClient
import com.simats.stocksense.core.network.TokenStore
import com.simats.stocksense.core.theme.StockSenseTheme
import com.simats.stocksense.feature.analytics.ui.SalesAnalyticsScreen
import com.simats.stocksense.feature.auth.ui.ForgotPasswordScreen
import com.simats.stocksense.feature.auth.ui.GoogleSignInScreen
import com.simats.stocksense.feature.auth.ui.LoginScreen
import com.simats.stocksense.feature.auth.ui.OnboardingScreen1
import com.simats.stocksense.feature.auth.ui.OnboardingScreen2
import com.simats.stocksense.feature.auth.ui.OnboardingScreen3
import com.simats.stocksense.feature.auth.ui.SignUpScreen
import com.simats.stocksense.feature.auth.ui.SignOutScreen
import com.simats.stocksense.feature.auth.ui.SplashScreen
import com.simats.stocksense.feature.auth.viewmodel.AuthViewModel
import com.simats.stocksense.feature.dashboard.ui.MainScreen
import com.simats.stocksense.feature.dashboard.ui.NotificationsScreen
import com.simats.stocksense.feature.dashboard.ui.SettingsScreen
import com.simats.stocksense.feature.inventory.ui.ImportInventoryPreviewScreen
import com.simats.stocksense.feature.inventory.ui.ImportInventoryReadyScreen
import com.simats.stocksense.feature.inventory.ui.ImportInventoryUploadScreen
import com.simats.stocksense.feature.inventory.ui.ImportInventoryValidationScreen
import com.simats.stocksense.feature.inventory.ui.InventoryImportedScreen
import com.simats.stocksense.feature.inventory.ui.ProductAttaScreen
import com.simats.stocksense.feature.inventory.ui.ProductBiscuitsScreen
import com.simats.stocksense.feature.inventory.ui.ProductDetailScreen
import com.simats.stocksense.feature.inventory.ui.ProductParleGScreen
import com.simats.stocksense.feature.inventory.ui.ProductTeaScreen
import com.simats.stocksense.feature.reorder.ui.AcceptedOverlayScreen
import com.simats.stocksense.feature.reorder.ui.DismissedOverlayScreen
import com.simats.stocksense.feature.reorder.ui.RecommendationPairsScreen
import com.simats.stocksense.feature.reorder.ui.ReorderPlacedScreen
import com.simats.stocksense.feature.reorder.ui.ReorderRecommendationScreen
import com.simats.stocksense.feature.reorder.ui.SlowMovingRecommendationScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StockSenseTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    StockSenseAppNavigation()
                }
            }
        }
    }
}

@Composable
fun StockSenseAppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val tokenStore = remember { TokenStore.getInstance(context) }
    val authViewModel: AuthViewModel = viewModel()

    DisposableEffect(Unit) {
        NetworkClient.initialize(context) {
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
        onDispose { }
    }

    val startDestination = remember {
        if (tokenStore.hasValidToken()) "main" else "splash"
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
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
                onBackClick = {
                    navController.popBackStack()
                },
                onLoginClick = {
                    navController.navigate("main") {
                        popUpTo("splash") { inclusive = true }
                        launchSingleTop = true
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
                },
                viewModel = authViewModel
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
                        launchSingleTop = true
                    }
                },
                onGoogleSignInClick = {
                    navController.navigate("google_signin")
                },
                onLoginClick = {
                    navController.navigate("login") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                viewModel = authViewModel
            )
        }
        composable("forgot_password") {
            ForgotPasswordScreen(
                onBackToLoginClick = {
                    navController.popBackStack()
                }
            )
        }
        composable("google_signin") {
            GoogleSignInScreen(
                onAccountSelected = { _ ->
                    navController.navigate("main") {
                        popUpTo("splash") { inclusive = true }
                        launchSingleTop = true
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
                    navController.navigate("notifications") {
                        launchSingleTop = true
                    }
                },
                onProductMilkClick = {
                    navController.navigate("product_detail") {
                        launchSingleTop = true
                    }
                },
                onProductBiscuitsClick = {
                    navController.navigate("product_biscuits") {
                        launchSingleTop = true
                    }
                },
                onProductAttaClick = {
                    navController.navigate("product_atta") {
                        launchSingleTop = true
                    }
                },
                onProductTeaClick = {
                    navController.navigate("product_tea") {
                        launchSingleTop = true
                    }
                },
                onProductParleGClick = {
                    navController.navigate("product_parleg") {
                        launchSingleTop = true
                    }
                },
                onSalesAnalyticsClick = {
                    navController.navigate("sales_analytics") {
                        launchSingleTop = true
                    }
                },
                onReorderRecommendationClick = {
                    navController.navigate("reorder_recommendation") {
                        launchSingleTop = true
                    }
                },
                onRecommendationPairsClick = {
                    navController.navigate("recommendation_pairs") {
                        launchSingleTop = true
                    }
                },
                onInventoryImportedClick = {
                    navController.navigate("inventory_imported") {
                        launchSingleTop = true
                    }
                },
                onSettingsClick = {
                    navController.navigate("settings") {
                        launchSingleTop = true
                    }
                },
                onSignOutClick = {
                    authViewModel.logout()
                    navController.navigate("signout") {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable("settings") {
            SettingsScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        composable("signout") {
            SignOutScreen(
                onBackToLoginClick = {
                    authViewModel.logout {
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }
        composable("notifications") {
            NotificationsScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onAmulMilkClick = {
                    navController.navigate("product_detail")
                },
                onMarieBiscuitsClick = {
                    navController.navigate("product_biscuits")
                },
                onSalesAnalyticsClick = {
                    navController.navigate("sales_analytics")
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
                    navController.navigate("slow_moving_recommendation")
                }
            )
        }
        composable("product_atta") {
            ProductAttaScreen(
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
        composable("product_tea") {
            ProductTeaScreen(
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
        composable("product_parleg") {
            ProductParleGScreen(
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
        composable("slow_moving_recommendation") {
            SlowMovingRecommendationScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onAcceptClick = {
                    navController.navigate("accepted_overlay")
                },
                onDismissClick = {
                    navController.navigate("dismissed_overlay")
                }
            )
        }
        composable("reorder_recommendation") {
            ReorderRecommendationScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onAcceptClick = {
                    navController.navigate("accepted_overlay")
                },
                onDismissClick = {
                    navController.navigate("dismissed_overlay")
                }
            )
        }
        composable("recommendation_pairs") {
            RecommendationPairsScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onAcceptClick = {
                    navController.navigate("accepted_overlay")
                },
                onDismissClick = {
                    navController.navigate("dismissed_overlay")
                }
            )
        }
        composable("accepted_overlay") {
            AcceptedOverlayScreen(
                onDoneClick = {
                    navController.popBackStack("main", inclusive = false)
                }
            )
        }
        composable("dismissed_overlay") {
            DismissedOverlayScreen(
                onDoneClick = {
                    navController.popBackStack("main", inclusive = false)
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
            ImportInventoryUploadScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onPreviewImportClick = {
                    navController.navigate("inventory_preview")
                }
            )
        }
        composable("inventory_preview") {
            ImportInventoryPreviewScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onContinueToValidationClick = {
                    navController.navigate("inventory_validation")
                }
            )
        }
        composable("inventory_validation") {
            ImportInventoryValidationScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onApplyFixClick = {
                    navController.navigate("inventory_ready")
                }
            )
        }
        composable("inventory_ready") {
            ImportInventoryReadyScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onImportProductsClick = {
                    navController.navigate("inventory_success")
                }
            )
        }
        composable("inventory_success") {
            InventoryImportedScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onDoneClick = {
                    navController.popBackStack("main", inclusive = false)
                }
            )
        }
    }
}
