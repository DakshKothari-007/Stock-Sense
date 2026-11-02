package com.simats.stocksense.feature.inventory.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.stocksense.core.util.NumberFormatUtil
import com.simats.stocksense.data.model.InventoryProduct
import com.simats.stocksense.data.model.PurchaseOrderDto
import com.simats.stocksense.data.model.UpdateProductRequest
import com.simats.stocksense.data.repository.InventoryRepository
import com.simats.stocksense.data.repository.ReorderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI State for the Product Detail screen.
 */
data class ProductDetailUiState(
    val isLoading: Boolean = true,
    val isOrdering: Boolean = false,
    val errorMessage: String? = null,
    val product: InventoryProduct = InventoryProduct(),
    val categoryName: String = "—",
    val description: String = "—",
    val minimumStockThreshold: Int = 0,
    val costPrice: String = "—",
    val stockCoverDays: Double? = null,
    val stockCoverDaysText: String = "—",
    val unitsToReorder: Int = 0,
    val estimatedCost: String = "—",
    val lastPlacedOrder: PurchaseOrderDto? = null
)

/**
 * ViewModel for ProductDetailScreen fetching live data via GET /inventory/get_product_detail.php
 * and wiring edit/delete/reorder actions.
 */
class ProductDetailViewModel(
    private val repository: InventoryRepository = InventoryRepository(),
    private val reorderRepository: ReorderRepository = ReorderRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    private var currentProductId: Int = 1

    init {
        loadProduct(1)
    }

    /**
     * Loads product details from GET /inventory/get_product_detail.php by ID
     * synchronized with Reorder module recommendations.
     */
    fun loadProduct(productId: Int = currentProductId) {
        currentProductId = productId
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.fetchProductDetail(id = productId)
            result.fold(
                onSuccess = { data ->
                    val rawUnits = data.rawUnits ?: data.units?.toString()?.toIntOrNull() ?: 0
                    val threshold = data.minimumStockThreshold ?: data.minStockThreshold ?: 10
                    val rawPrice = data.rawPrice
                        ?: data.price?.toString()?.replace("₹", "")?.replace(",", "")?.toDoubleOrNull()
                        ?: 0.0
                    val rawCostPrice = data.rawCostPrice
                        ?: data.costPrice?.toString()?.replace("₹", "")?.replace(",", "")?.toDoubleOrNull()
                        ?: 0.0

                    val formattedPrice = NumberFormatUtil.formatIndianRupees(rawPrice)
                    val formattedCostPrice = NumberFormatUtil.formatIndianRupees(rawCostPrice)

                    // Fetch synchronized reorder recommendation from reorder module
                    val reorderResult = reorderRepository.fetchRecommendations()
                    var unitsToReorder = data.unitsToReorder ?: 0
                    var estCostFormatted = data.estimatedCost?.toString()?.takeIf { it.isNotBlank() }
                        ?: NumberFormatUtil.formatIndianRupees(unitsToReorder * rawCostPrice)
                    var coverDays = data.stockCoverDays

                    if (reorderResult.isSuccess) {
                        val items = reorderResult.getOrNull()?.items.orEmpty()
                        val match = items.firstOrNull {
                            it.product?.id == productId || (it.product?.sku != null && it.product.sku == data.sku)
                        }
                        if (match != null) {
                            unitsToReorder = match.suggestedQuantity ?: 0
                            estCostFormatted = NumberFormatUtil.formatIndianRupees(match.estimatedCost ?: (unitsToReorder * rawCostPrice))
                            if (match.daysOfStockLeft != null) {
                                coverDays = match.daysOfStockLeft
                            }
                        }
                    }

                    val coverText = when {
                        coverDays != null && coverDays > 0 -> "$coverDays days"
                        rawUnits <= 0 -> "0 days (out of stock)"
                        else -> "less than a day"
                    }

                    val rawStatus = data.status?.trim()
                    val calculatedStatus: String?
                    val calculatedColorHex: Long

                    if (!rawStatus.isNullOrEmpty()) {
                        calculatedStatus = rawStatus
                        calculatedColorHex = data.statusColorHex ?: 0xFF98621A
                    } else {
                        when {
                            rawUnits <= 0 -> {
                                calculatedStatus = "Out of stock"
                                calculatedColorHex = 0xFFD32F2F
                            }
                            rawUnits <= threshold && threshold > 0 -> {
                                calculatedStatus = "Low stock · Minimum $threshold"
                                calculatedColorHex = 0xFF98621A
                            }
                            else -> {
                                calculatedStatus = "In stock"
                                calculatedColorHex = 0xFF2E7D32
                            }
                        }
                    }

                    val iconType = data.iconType ?: when {
                        data.name?.contains("Milk", ignoreCase = true) == true -> 0
                        data.name?.contains("Biscuit", ignoreCase = true) == true || data.name?.contains("Marie", ignoreCase = true) == true -> 1
                        data.name?.contains("Tea", ignoreCase = true) == true -> 3
                        else -> 2
                    }

                    val domainProduct = InventoryProduct(
                        id = data.id?.toString() ?: productId.toString(),
                        name = data.name?.takeIf { it.isNotBlank() } ?: "—",
                        sku = data.sku?.takeIf { it.isNotBlank() } ?: "—",
                        price = formattedPrice,
                        status = calculatedStatus,
                        statusColorHex = calculatedColorHex,
                        units = rawUnits.toString(),
                        iconType = iconType,
                        category = (data.category ?: data.categoryName)?.takeIf { it.isNotBlank() } ?: "—",
                        minStockThreshold = threshold,
                        costPrice = formattedCostPrice,
                        rawPrice = rawPrice,
                        rawUnits = rawUnits,
                        description = data.description?.takeIf { it.isNotBlank() } ?: "—"
                    )

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                            product = domainProduct,
                            categoryName = domainProduct.category ?: "—",
                            description = domainProduct.description ?: "—",
                            minimumStockThreshold = threshold,
                            costPrice = formattedCostPrice,
                            stockCoverDays = coverDays,
                            stockCoverDaysText = coverText,
                            unitsToReorder = unitsToReorder,
                            estimatedCost = estCostFormatted
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = err.message ?: "Failed to load product details."
                        )
                    }
                }
            )
        }
    }

    /**
     * Places a purchase order via POST /reorder/create_order.php for this product.
     * Prevents double-tap by checking isOrdering flag.
     */
    fun createReorder(
        onSuccess: (PurchaseOrderDto) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val current = _uiState.value
        if (current.isOrdering) return

        val units = current.unitsToReorder
        if (units <= 0) {
            onError("Item quantity must be a positive integer greater than 0.")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isOrdering = true) }
            val result = reorderRepository.createOrder(productId = currentProductId, quantity = units)
            result.fold(
                onSuccess = { order ->
                    _uiState.update {
                        it.copy(
                            isOrdering = false,
                            lastPlacedOrder = order
                        )
                    }
                    loadProduct(currentProductId)
                    onSuccess(order)
                },
                onFailure = { err ->
                    _uiState.update { it.copy(isOrdering = false) }
                    onError(err.message ?: "Failed to place reorder.")
                }
            )
        }
    }

    /**
     * Edits product details via POST /inventory/update_product.php and shows backend validation message.
     */
    fun editProduct(
        request: UpdateProductRequest,
        onResult: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            val result = repository.updateProduct(request)
            result.fold(
                onSuccess = { msg ->
                    loadProduct(currentProductId)
                    onResult(true, msg)
                },
                onFailure = { err ->
                    onResult(false, err.message ?: "Failed to update product.")
                }
            )
        }
    }

    /**
     * Deletes the current product via POST /inventory/delete_product.php and shows backend validation message.
     */
    fun deleteProduct(
        onResult: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            val result = repository.deleteProduct(
                id = currentProductId,
                sku = _uiState.value.product.sku
            )
            result.fold(
                onSuccess = { msg ->
                    onResult(true, msg)
                },
                onFailure = { err ->
                    onResult(false, err.message ?: "Failed to delete product.")
                }
            )
        }
    }

    /**
     * Updates units to reorder and recalculates estimated cost locally.
     */
    fun updateReorderUnits(units: Int) {
        _uiState.update { current ->
            val costPrice = current.product.rawPrice.takeIf { it > 0 } ?: 27.0
            val cost = units * costPrice
            val formattedCost = NumberFormatUtil.formatIndianRupees(cost)
            current.copy(unitsToReorder = units, estimatedCost = formattedCost)
        }
    }
}
