package com.simats.stocksense.feature.dashboard.viewmodel

import androidx.lifecycle.ViewModel
import com.simats.stocksense.data.model.*
import com.simats.stocksense.data.repository.StockRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class MainUiState(
    val selectedTab: Int = 0,
    val searchQuery: String = "",
    val selectedFilter: Int = 0, // 0: All, 1: Low stock, 2: Slow moving
    val products: List<InventoryProduct> = emptyList(),
    val filteredProducts: List<InventoryProduct> = emptyList(),
    val salesSummary: SalesSummary = SalesSummary(),
    val dailySalesPoints: List<DailySalesPoint> = emptyList(),
    val storeProfile: StoreProfile = StoreProfile()
)

class MainViewModel(
    private val repository: StockRepository = StockRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        MainUiState(
            products = repository.inventoryProducts.value,
            filteredProducts = repository.inventoryProducts.value,
            salesSummary = repository.salesSummary.value,
            dailySalesPoints = repository.dailySalesPoints.value,
            storeProfile = repository.storeProfile.value
        )
    )
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { currentState ->
            val filtered = filterProducts(currentState.products, query, currentState.selectedFilter)
            currentState.copy(searchQuery = query, filteredProducts = filtered)
        }
    }

    fun selectFilter(filterIndex: Int) {
        _uiState.update { currentState ->
            val filtered = filterProducts(currentState.products, currentState.searchQuery, filterIndex)
            currentState.copy(selectedFilter = filterIndex, filteredProducts = filtered)
        }
    }

    fun updateShopName(newName: String) {
        _uiState.update { currentState ->
            val updatedProfile = currentState.storeProfile.copy(name = newName)
            currentState.copy(storeProfile = updatedProfile)
        }
    }

    private fun filterProducts(
        allProducts: List<InventoryProduct>,
        query: String,
        filter: Int
    ): List<InventoryProduct> {
        return allProducts.filter { item ->
            val matchesSearch = query.isEmpty() ||
                    item.name.contains(query, ignoreCase = true) ||
                    item.sku.contains(query, ignoreCase = true)

            val matchesFilter = when (filter) {
                1 -> item.status?.contains("Low stock", ignoreCase = true) == true
                2 -> item.status?.contains("Slow moving", ignoreCase = true) == true
                else -> true
            }

            matchesSearch && matchesFilter
        }
    }
}
