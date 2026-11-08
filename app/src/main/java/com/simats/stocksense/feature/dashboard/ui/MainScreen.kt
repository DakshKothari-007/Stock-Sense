package com.simats.stocksense.feature.dashboard.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.stocksense.core.components.StockSenseBottomNavigation
import com.simats.stocksense.core.theme.StockSenseTheme
import com.simats.stocksense.feature.ai.ui.DukaanMitraFloatingButton
import com.simats.stocksense.feature.dashboard.viewmodel.DashboardViewModel
import com.simats.stocksense.feature.dashboard.viewmodel.MainViewModel
import com.simats.stocksense.feature.inventory.ui.InventoryScreen
import com.simats.stocksense.feature.inventory.viewmodel.InventoryViewModel
import com.simats.stocksense.feature.reorder.viewmodel.InsightsViewModel

@Composable
fun MainScreen(
    onBackClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onProductDetailClick: (com.simats.stocksense.data.model.InventoryProduct) -> Unit = {},
    onProductMilkClick: () -> Unit = {},
    onProductBiscuitsClick: () -> Unit = {},
    onProductAttaClick: () -> Unit = {},
    onProductTeaClick: () -> Unit = {},
    onProductParleGClick: () -> Unit = {},
    onSalesAnalyticsClick: () -> Unit = {},
    onReorderRecommendationClick: () -> Unit = {},
    onRecommendationPairsClick: () -> Unit = {},
    onSlowMovingRecommendationClick: () -> Unit = onProductBiscuitsClick,
    onInventoryImportedClick: () -> Unit = {},
    onAddProductClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onSignOutClick: () -> Unit = {},
    onDeleteAccountClick: () -> Unit = {},
    onAiChatClick: () -> Unit = {},
    insightsViewModel: InsightsViewModel = viewModel(),
    dashboardViewModel: DashboardViewModel = viewModel(),
    inventoryViewModel: InventoryViewModel = viewModel(),
    viewModel: MainViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val saveableStateHolder = rememberSaveableStateHolder()

    LaunchedEffect(Unit) {
        viewModel.refreshProfile()
    }

    // System back button on a non-Home tab returns to Home with slide animation to the left
    BackHandler(enabled = uiState.selectedTab != 0) {
        viewModel.selectTab(0)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            bottomBar = {
                StockSenseBottomNavigation(
                    selectedTab = uiState.selectedTab,
                    onTabSelected = { viewModel.selectTab(it) }
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = uiState.selectedTab,
                    transitionSpec = {
                        val direction = if (targetState > initialState) 1 else -1
                        (slideInHorizontally(
                            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                            initialOffsetX = { fullWidth -> direction * fullWidth }
                        ) + fadeIn(
                            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                        )).togetherWith(
                            slideOutHorizontally(
                                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                                targetOffsetX = { fullWidth -> -direction * fullWidth }
                            ) + fadeOut(
                                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                            )
                        )
                    },
                    label = "MainTabTransition",
                    modifier = Modifier.fillMaxSize()
                ) { tabIndex ->
                    saveableStateHolder.SaveableStateProvider(key = tabIndex) {
                        when (tabIndex) {
                            0 -> DashboardHomeScreen(
                                viewModel = dashboardViewModel,
                                onNotificationsClick = onNotificationsClick,
                                onLowStockClick = onProductMilkClick,
                                onSlowMovingClick = onProductBiscuitsClick,
                                onAnalyticsClick = onSalesAnalyticsClick,
                                onInsightsClick = { viewModel.selectTab(2) },
                                onReorderMilkClick = onReorderRecommendationClick,
                                onFreeUpStockClick = onProductBiscuitsClick,
                                onImportClick = onInventoryImportedClick
                            )
                            1 -> InventoryScreen(
                                viewModel = inventoryViewModel,
                                onImportDataClick = onInventoryImportedClick,
                                onAddProductClick = onAddProductClick,
                                onProductClick = onProductDetailClick,
                                onMilkProductClick = onProductMilkClick,
                                onBiscuitsProductClick = onProductBiscuitsClick,
                                onAttaProductClick = onProductAttaClick,
                                onTeaProductClick = onProductTeaClick,
                                onParleGProductClick = onProductParleGClick
                            )
                            2 -> InsightsContent(
                                storeName = uiState.storeProfile.name.takeIf { it.isNotBlank() },
                                onReorderMilkClick = onReorderRecommendationClick,
                                onMoveBiscuitsClick = onSlowMovingRecommendationClick,
                                onTeaPairClick = onRecommendationPairsClick,
                                viewModel = insightsViewModel
                            )
                            3 -> ProfileContent(
                                storeProfile = uiState.storeProfile,
                                isProfileLoading = uiState.isProfileLoading,
                                profileErrorMessage = uiState.profileErrorMessage,
                                onRetryClick = { viewModel.refreshProfile() },
                                onUpdateShop = { name, loc, type, cb ->
                                    viewModel.updateShop(name, loc, type, cb)
                                },
                                onShopNameChange = { viewModel.updateShopName(it) },
                                onUserNameChange = { viewModel.updateUserName(it) },
                                onEmailChange = { viewModel.updateEmail(it) },
                                onUpdateProfile = { name, email, cb ->
                                    viewModel.updateUserProfile(name, email, cb)
                                },
                                onChangePassword = { cur, new, conf, cb ->
                                    viewModel.changePassword(cur, new, conf, cb)
                                },
                                onSettingsClick = onSettingsClick,
                                onSignOutClick = onSignOutClick,
                                onDeleteAccountClick = onDeleteAccountClick
                            )
                        }
                    }
                }

                // Dukaan Mitra Floating Action Button (Home, Inventory, Insights only)
                if (uiState.selectedTab in 0..2) {
                    DukaanMitraFloatingButton(
                        onClick = onAiChatClick,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 16.dp, bottom = 16.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    StockSenseTheme {
        MainScreen()
    }
}
