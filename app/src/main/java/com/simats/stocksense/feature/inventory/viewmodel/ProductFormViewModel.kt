package com.simats.stocksense.feature.inventory.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.stocksense.core.network.NetworkClient
import com.simats.stocksense.core.network.SessionManager
import com.simats.stocksense.data.model.AddProductRequest
import com.simats.stocksense.data.model.UpdateProductRequest
import com.simats.stocksense.data.repository.InventoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI State for New product and Edit product screens.
 * Contains no hardcoded mock or design placeholder values.
 */
data class ProductFormUiState(
    val isEditMode: Boolean = false,
    val productId: Int? = null,
    val originalSku: String = "",
    val name: String = "",
    val sku: String = "",
    val category: String = "",
    val sellingPrice: String = "",
    val purchaseCost: String = "",
    val stock: String = "",
    val minStock: String = "10",

    // Field-level error messages
    val nameError: String? = null,
    val skuError: String? = null,
    val priceError: String? = null,
    val costError: String? = null,
    val stockError: String? = null,
    val minStockError: String? = null,
    val isCostHigherThanPrice: Boolean = false,

    // Category suggestions from shop
    val categories: List<String> = emptyList(),

    // Network states
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val bannerError: String? = null,
    val isSessionExpired: Boolean = false,

    // Dynamic product info for delete confirmation bottom sheet
    val deleteProductName: String = "",
    val deleteProductUnits: String = "0"
)

/**
 * ViewModel handling product creation (add_product.php), update (update_product.php),
 * and deletion (delete_product.php) according to API_DOCUMENT.md Module 3.
 */
class ProductFormViewModel(
    private val repository: InventoryRepository = InventoryRepository(),
    private val sessionManager: SessionManager? = NetworkClient.getSessionManager()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductFormUiState())
    val uiState: StateFlow<ProductFormUiState> = _uiState.asStateFlow()

    /**
     * Initializes the state for creating a new product.
     */
    fun initNewProduct() {
        _uiState.value = ProductFormUiState(
            isEditMode = false,
            name = "",
            sku = "",
            category = "",
            sellingPrice = "",
            purchaseCost = "",
            stock = "",
            minStock = "10"
        )
        loadCategories()
    }

    /**
     * Initializes the state for editing an existing product by loading live details.
     */
    fun initEditProduct(productId: Int?, sku: String?) {
        _uiState.value = ProductFormUiState(
            isEditMode = true,
            productId = productId,
            originalSku = sku.orEmpty(),
            isLoading = true
        )
        loadCategories()

        viewModelScope.launch {
            val result = repository.fetchProductDetail(id = productId, sku = sku)
            result.fold(
                onSuccess = { data ->
                    val pPrice = data.rawPrice ?: data.price?.toString()?.replace("₹", "")?.replace(",", "")?.toDoubleOrNull() ?: 0.0
                    val pCost = data.rawCostPrice ?: data.costPrice?.toString()?.replace("₹", "")?.replace(",", "")?.toDoubleOrNull() ?: 0.0
                    val pUnits = data.rawUnits ?: data.units?.toString()?.toIntOrNull() ?: 0
                    val pMin = data.minimumStockThreshold ?: data.minStockThreshold ?: 10

                    val priceStr = if (pPrice % 1.0 == 0.0) pPrice.toInt().toString() else pPrice.toString()
                    val costStr = if (pCost % 1.0 == 0.0) pCost.toInt().toString() else pCost.toString()

                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            productId = data.id?.toString()?.toIntOrNull() ?: productId,
                            originalSku = data.sku.orEmpty(),
                            name = data.name.orEmpty(),
                            sku = data.sku.orEmpty(),
                            category = (data.category ?: data.categoryName).orEmpty(),
                            sellingPrice = priceStr,
                            purchaseCost = costStr,
                            stock = pUnits.toString(),
                            minStock = pMin.toString(),
                            deleteProductName = data.name.orEmpty(),
                            deleteProductUnits = pUnits.toString(),
                            isCostHigherThanPrice = pCost > pPrice && pPrice > 0.0
                        )
                    }
                },
                onFailure = { err ->
                    val errMsg = err.message ?: "Failed to load product details."
                    val is401 = errMsg.contains("401", ignoreCase = true) || errMsg.contains("unauthorized", ignoreCase = true)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            bannerError = errMsg,
                            isSessionExpired = is401
                        )
                    }
                }
            )
        }
    }

    /**
     * Fetches shop's distinct categories to suggest in the category dropdown.
     */
    private fun loadCategories() {
        viewModelScope.launch {
            val result = repository.fetchProducts(limit = 100)
            if (result.isSuccess) {
                val fetched = result.getOrNull()?.products.orEmpty()
                    .mapNotNull { it.category?.trim() }
                    .filter { it.isNotBlank() }
                    .distinct()
                    .sorted()

                val defaultCategories = listOf("Beverages", "Dairy", "Bakery", "Snacks", "Staples", "Packaged Food", "Personal Care", "Household")
                val combined = (fetched + defaultCategories).distinct().sorted()

                _uiState.update { it.copy(categories = combined) }
            }
        }
    }

    fun updateName(value: String) {
        _uiState.update { current ->
            current.copy(
                name = value,
                nameError = if (value.isNotBlank()) null else current.nameError
            )
        }
    }

    fun updateSku(value: String) {
        _uiState.update { current ->
            current.copy(
                sku = value,
                skuError = null
            )
        }
    }

    fun updateCategory(value: String) {
        _uiState.update { it.copy(category = value) }
    }

    fun updateSellingPrice(value: String) {
        _uiState.update { current ->
            val price = value.toDoubleOrNull()
            val cost = current.purchaseCost.toDoubleOrNull()
            val costHigher = price != null && cost != null && cost > price && price > 0.0
            current.copy(
                sellingPrice = value,
                priceError = if (price != null && price >= 0) null else current.priceError,
                isCostHigherThanPrice = costHigher
            )
        }
    }

    fun updatePurchaseCost(value: String) {
        _uiState.update { current ->
            val cost = value.toDoubleOrNull()
            val price = current.sellingPrice.toDoubleOrNull()
            val costHigher = price != null && cost != null && cost > price && price > 0.0
            current.copy(
                purchaseCost = value,
                costError = if (cost != null && cost >= 0) null else current.costError,
                isCostHigherThanPrice = costHigher
            )
        }
    }

    fun updateStock(value: String) {
        _uiState.update { current ->
            val stock = value.toIntOrNull()
            current.copy(
                stock = value,
                stockError = if (stock != null && stock >= 0) null else current.stockError,
                deleteProductUnits = if (stock != null) stock.toString() else current.deleteProductUnits
            )
        }
    }

    fun updateMinStock(value: String) {
        _uiState.update { current ->
            val minStock = value.toIntOrNull()
            current.copy(
                minStock = value,
                minStockError = if (minStock != null && minStock >= 0) null else current.minStockError
            )
        }
    }

    fun clearBannerError() {
        _uiState.update { it.copy(bannerError = null) }
    }

    /**
     * Validates form inputs before dispatching save request to backend.
     */
    fun validate(): Boolean {
        val state = _uiState.value
        var valid = true

        var nameErr: String? = null
        if (state.name.trim().isBlank()) {
            nameErr = "Product name is required"
            valid = false
        } else if (state.name.length > 150) {
            nameErr = "Product name must be 150 characters or less"
            valid = false
        }

        var priceErr: String? = null
        val parsedPrice = state.sellingPrice.toDoubleOrNull()
        if (state.sellingPrice.isBlank() || parsedPrice == null || parsedPrice < 0) {
            priceErr = "Valid selling price (>= 0) is required"
            valid = false
        }

        var costErr: String? = null
        val parsedCost = if (state.purchaseCost.isBlank()) 0.0 else state.purchaseCost.toDoubleOrNull()
        if (parsedCost == null || parsedCost < 0) {
            costErr = "Valid purchase cost (>= 0) is required"
            valid = false
        }

        var stockErr: String? = null
        val parsedStock = state.stock.toIntOrNull()
        if (state.stock.isBlank() || parsedStock == null || parsedStock < 0) {
            stockErr = "Valid whole number (>= 0) is required"
            valid = false
        }

        var minStockErr: String? = null
        val parsedMinStock = state.minStock.toIntOrNull()
        if (state.minStock.isBlank() || parsedMinStock == null || parsedMinStock < 0) {
            minStockErr = "Valid minimum stock (>= 0) is required"
            valid = false
        }

        val costHigher = parsedCost != null && parsedPrice != null && parsedCost > parsedPrice && parsedPrice > 0.0

        _uiState.update {
            it.copy(
                nameError = nameErr,
                priceError = priceErr,
                costError = costErr,
                stockError = stockErr,
                minStockError = minStockErr,
                isCostHigherThanPrice = costHigher
            )
        }

        return valid
    }

    /**
     * Saves the product by calling add_product.php or update_product.php.
     * Returns the final SKU (server-generated or user-provided) and message on success.
     */
    fun saveProduct(onSuccess: (returnedSku: String, message: String) -> Unit) {
        if (!validate()) return

        val state = _uiState.value
        _uiState.update { it.copy(isSaving = true, bannerError = null, skuError = null) }

        viewModelScope.launch {
            val parsedPrice = state.sellingPrice.toDoubleOrNull() ?: 0.0
            val parsedCost = if (state.purchaseCost.isBlank()) 0.0 else (state.purchaseCost.toDoubleOrNull() ?: 0.0)
            val parsedStock = state.stock.toIntOrNull() ?: 0
            val parsedMinStock = state.minStock.toIntOrNull() ?: 10

            if (!state.isEditMode) {
                // New Product: leave sku empty/null if user left it blank so server auto-generates stable unique SKU
                val userSku = state.sku.trim().uppercase().takeIf { it.isNotBlank() }

                val request = AddProductRequest(
                    name = state.name.trim(),
                    sku = userSku,
                    category = state.category.trim().ifBlank { "General" },
                    price = parsedPrice,
                    costPrice = parsedCost,
                    units = parsedStock,
                    minStockThreshold = parsedMinStock
                )

                val result = repository.addProduct(request)
                result.fold(
                    onSuccess = { res ->
                        val finalSku = res.sku ?: userSku ?: ""
                        _uiState.update { it.copy(isSaving = false, sku = finalSku) }
                        onSuccess(finalSku, res.message)
                    },
                    onFailure = { err ->
                        handleSaveError(err.message ?: "Failed to create product.")
                    }
                )
            } else {
                // Edit Product
                val newSku = if (state.sku.trim().isNotBlank() && !state.sku.trim().equals(state.originalSku.trim(), ignoreCase = true)) {
                    state.sku.trim().uppercase()
                } else null

                val request = UpdateProductRequest(
                    id = state.productId,
                    sku = state.originalSku.takeIf { it.isNotBlank() },
                    name = state.name.trim(),
                    newSku = newSku,
                    category = state.category.trim().ifBlank { "General" },
                    price = parsedPrice,
                    costPrice = parsedCost,
                    units = parsedStock,
                    minStockThreshold = parsedMinStock
                )

                val result = repository.updateProduct(request)
                result.fold(
                    onSuccess = { msg ->
                        _uiState.update { it.copy(isSaving = false) }
                        onSuccess(newSku ?: state.sku, msg)
                    },
                    onFailure = { err ->
                        handleSaveError(err.message ?: "Failed to update product.")
                    }
                )
            }
        }
    }

    /**
     * Deletes the product via delete_product.php.
     * If product is linked to sales, the backend 400 Bad Request message is displayed
     * and the product is preserved without navigating away.
     */
    fun deleteProduct(onSuccess: (String) -> Unit) {
        val state = _uiState.value
        _uiState.update { it.copy(isDeleting = true, bannerError = null) }

        viewModelScope.launch {
            val result = repository.deleteProduct(
                id = state.productId,
                sku = state.originalSku.takeIf { it.isNotBlank() }
            )
            result.fold(
                onSuccess = { msg ->
                    _uiState.update { it.copy(isDeleting = false) }
                    onSuccess(msg)
                },
                onFailure = { err ->
                    val errMsg = err.message ?: "Failed to delete product."
                    val is401 = errMsg.contains("401", ignoreCase = true) || errMsg.contains("unauthorized", ignoreCase = true)
                    _uiState.update {
                        it.copy(
                            isDeleting = false,
                            bannerError = errMsg,
                            isSessionExpired = is401
                        )
                    }
                }
            )
        }
    }

    private fun handleSaveError(errMsg: String) {
        val is401 = errMsg.contains("401", ignoreCase = true) || errMsg.contains("unauthorized", ignoreCase = true)
        if (is401) {
            _uiState.update { it.copy(isSaving = false, isSessionExpired = true) }
            return
        }

        if (errMsg.contains("SKU", ignoreCase = true) || errMsg.contains("already exists", ignoreCase = true) || errMsg.contains("assigned", ignoreCase = true)) {
            _uiState.update { it.copy(isSaving = false, skuError = errMsg, bannerError = null) }
        } else if (errMsg.contains("name", ignoreCase = true)) {
            _uiState.update { it.copy(isSaving = false, nameError = errMsg, bannerError = null) }
        } else {
            _uiState.update { it.copy(isSaving = false, bannerError = errMsg) }
        }
    }
}
