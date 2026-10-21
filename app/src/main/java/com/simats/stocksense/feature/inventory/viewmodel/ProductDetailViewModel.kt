package com.simats.stocksense.feature.inventory.viewmodel

import androidx.lifecycle.ViewModel
import com.simats.stocksense.data.model.InventoryProduct
import com.simats.stocksense.data.repository.StockRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ProductDetailUiState(
    val product: InventoryProduct = InventoryProduct(
        id = "1",
        name = "Amul Milk",
        sku = "DAI-001",
        price = "₹30",
        status = "Low stock",
        statusColorHex = 0xFF98621A,
        units = "8",
        iconType = 0
    ),
    val categoryName: String = "Dairy",
    val description: String = "500 ml · Fresh daily",
    val minimumStockThreshold: Int = 24,
    val unitsToReorder: Int = 60,
    val estimatedCost: String = "₹1,620"
)

class ProductDetailViewModel(
    private val repository: StockRepository = StockRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    fun updateReorderUnits(units: Int) {
        _uiState.update { current ->
            val cost = units * 27
            current.copy(unitsToReorder = units, estimatedCost = "₹$cost")
        }
    }
}
