package com.simats.stocksense.feature.dashboard.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.stocksense.core.components.StockSenseBottomNavigation
import com.simats.stocksense.core.theme.StockSenseTheme
import com.simats.stocksense.feature.dashboard.viewmodel.MainViewModel
import com.simats.stocksense.feature.inventory.ui.InventoryContent

@Composable
fun MainScreen(
    onBackClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onProductMilkClick: () -> Unit = {},
    onProductBiscuitsClick: () -> Unit = {},
    onSalesAnalyticsClick: () -> Unit = {},
    onReorderRecommendationClick: () -> Unit = {},
    onRecommendationPairsClick: () -> Unit = {},
    onInventoryImportedClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onSignOutClick: () -> Unit = {},
    viewModel: MainViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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
                when (uiState.selectedTab) {
                    0 -> DashboardHomeContent(
                        salesSummary = uiState.salesSummary,
                        storeProfile = uiState.storeProfile,
                        onNotificationsClick = onNotificationsClick,
                        onLowStockClick = onProductMilkClick,
                        onSlowMovingClick = onProductBiscuitsClick,
                        onAnalyticsClick = onSalesAnalyticsClick,
                        onInsightsClick = { viewModel.selectTab(2) },
                        onReorderMilkClick = onReorderRecommendationClick,
                        onFreeUpStockClick = onProductBiscuitsClick
                    )
                    1 -> InventoryContent(
                        products = uiState.filteredProducts,
                        searchQuery = uiState.searchQuery,
                        onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                        selectedFilter = uiState.selectedFilter,
                        onFilterSelected = { viewModel.selectFilter(it) },
                        onImportDataClick = onInventoryImportedClick,
                        onMilkProductClick = onProductMilkClick,
                        onBiscuitsProductClick = onProductBiscuitsClick
                    )
                    2 -> InsightsContent(
                        onReorderMilkClick = onReorderRecommendationClick,
                        onMoveBiscuitsClick = onProductBiscuitsClick,
                        onTeaPairClick = onRecommendationPairsClick
                    )
                    3 -> ProfileContent(
                        storeProfile = uiState.storeProfile,
                        onShopNameChange = { viewModel.updateShopName(it) },
                        onSettingsClick = onSettingsClick,
                        onSignOutClick = onSignOutClick
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
