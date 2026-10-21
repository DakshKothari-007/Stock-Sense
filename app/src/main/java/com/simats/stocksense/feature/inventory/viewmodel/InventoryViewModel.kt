package com.simats.stocksense.feature.inventory.viewmodel

import androidx.lifecycle.ViewModel
import com.simats.stocksense.data.model.InventoryProduct
import com.simats.stocksense.data.repository.StockRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class InventoryUiState(
    val searchQuery: String = "",
    val selectedFilter: Int = 0, // 0: All, 1: Low stock, 2: Slow moving
    val allProducts: List<InventoryProduct> = emptyList(),
    val filteredProducts: List<InventoryProduct> = emptyList()
)

class InventoryViewModel(
    private val repository: StockRepository = StockRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        InventoryUiState(
            allProducts = repository.inventoryProducts.value,
            filteredProducts = repository.inventoryProducts.value
        )
    )
    val uiState: StateFlow<InventoryUiState> = _uiState.asStateFlow()

    fun updateSearchQuery(query: String) {
        _uiState.update { current ->
            val filtered = filterProducts(current.allProducts, query, current.selectedFilter)
            current.copy(searchQuery = query, filteredProducts = filtered)
        }
    }

    fun selectFilter(filterIndex: Int) {
        _uiState.update { current ->
            val filtered = filterProducts(current.allProducts, current.searchQuery, filterIndex)
            current.copy(selectedFilter = filterIndex, filteredProducts = filtered)
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
