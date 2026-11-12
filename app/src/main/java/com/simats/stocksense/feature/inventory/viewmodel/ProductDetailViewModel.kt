package com.simats.stocksense.feature.inventory.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.stocksense.core.util.NumberFormatUtil
import com.simats.stocksense.data.model.InventoryProduct
import com.simats.stocksense.data.model.PurchaseOrderDto
import com.simats.stocksense.data.model.UpdateProductRequest
import com.simats.stocksense.data.repository.InventoryRepository
import com.simats.stocksense.data.repository.ReorderRepository
import com.simats.stocksense.data.repository.SalesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Data point for the 7-day sales history bar chart.
 */
data class SalesBarPoint(
    val dayNumber: String = "",
    val dayLabel: String = "",
    val date: String = "",
    val unitsSold: Int = 0,
    val revenue: Double = 0.0,
    val heightRatio: Float = 0.0f,
    val isToday: Boolean = false
)

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
    val onOrderUnits: Int = 0,
    val estimatedCost: String = "—",
    val lastPlacedOrder: PurchaseOrderDto? = null,
    val salesHistory: List<SalesBarPoint> = emptyList(),
    val has7DaySales: Boolean = false,
    val total7DayUnits: Int = 0,
    val lastUpdatedDateText: String = ""
)

/**
 * ViewModel for ProductDetailScreen fetching live data via GET /inventory/get_product_detail.php
 * and 7-day sales history via GET /sales/product_history.php.
 */
class ProductDetailViewModel(
    private val repository: InventoryRepository = InventoryRepository(),
    private val reorderRepository: ReorderRepository = ReorderRepository(),
    private val salesRepository: SalesRepository = SalesRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    private var currentProductId: Int? = null
    private var currentProductSku: String? = "DAI-001"

    init {
        loadProduct(productId = null, sku = "DAI-001")
    }

    /**
     * Loads product details from GET /inventory/get_product_detail.php
     * and 7-day sales history from GET /sales/product_history.php.
     */
    fun loadProduct(productId: Int? = null, sku: String? = null) {
        val targetId = when {
            productId != null -> productId
            sku != null -> null
            else -> currentProductId
        }
        val targetSku = when {
            sku != null -> sku
            productId != null -> null
            else -> currentProductSku
        }
        currentProductId = targetId
        currentProductSku = targetSku

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.fetchProductDetail(id = targetId, sku = targetSku)
            result.fold(
                onSuccess = { data ->
                    val resolvedId = data.id?.toString()?.toIntOrNull() ?: targetId ?: 0
                    val resolvedSku = data.sku?.takeIf { it.isNotBlank() } ?: targetSku ?: ""
                    currentProductId = if (resolvedId > 0) resolvedId else null
                    currentProductSku = resolvedSku.takeIf { it.isNotBlank() }

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

                    val onOrderUnits = data.onOrderUnits ?: 0

                    // Fetch synchronized reorder recommendation from reorder module
                    val reorderResult = reorderRepository.fetchRecommendations()
                    var unitsToReorder = data.suggestedReorderUnits ?: data.unitsToReorder ?: 0
                    var estCostFormatted = data.estimatedCost?.toString()?.takeIf { it.isNotBlank() }
                        ?: NumberFormatUtil.formatIndianRupees(unitsToReorder * rawCostPrice)
                    var coverDays = data.stockCoverDays

                    if (reorderResult.isSuccess) {
                        val items = reorderResult.getOrNull()?.items.orEmpty()
                        val match = items.firstOrNull {
                            (resolvedId > 0 && it.product?.id == resolvedId) ||
                            (!it.product?.sku.isNullOrBlank() && it.product?.sku == resolvedSku)
                        }
                        if (match != null) {
                            unitsToReorder = match.suggestedQuantity ?: 0
                            estCostFormatted = NumberFormatUtil.formatIndianRupees(match.estimatedCost ?: (unitsToReorder * rawCostPrice))
                            if (match.daysOfStockLeft != null) {
                                coverDays = match.daysOfStockLeft
                            }
                        } else if (onOrderUnits > 0) {
                            unitsToReorder = 0
                        }
                    }

                    // Fetch 7-day sales history for the bar chart
                    val historyResult = salesRepository.fetchProductSalesHistory(
                        id = if (resolvedId > 0) resolvedId else null,
                        sku = resolvedSku.takeIf { it.isNotBlank() }
                    )
                    var salesPoints: List<SalesBarPoint> = emptyList()
                    var has7DaySales = false
                    var total7DayUnits = 0

                    if (historyResult.isSuccess) {
                        val histData = historyResult.getOrNull()
                        if (histData != null) {
                            val mappedPoints = (histData.history ?: emptyList()).map { pt ->
                                SalesBarPoint(
                                    dayNumber = pt.dayNumber ?: "",
                                    dayLabel = pt.dayLabel ?: "",
                                    date = pt.date ?: "",
                                    unitsSold = pt.unitsSold ?: 0,
                                    revenue = pt.revenue ?: 0.0,
                                    heightRatio = pt.heightRatio ?: 0.0f,
                                    isToday = pt.isToday == true
                                )
                            }
                            salesPoints = mappedPoints
                            total7DayUnits = histData.totalUnitsSold ?: 0
                            has7DaySales = histData.hasSales == true && mappedPoints.any { it.unitsSold > 0 || it.revenue > 0.0 }
                        }
                    }

                    val rawUpdatedStr = data.updatedAt?.takeIf { it.isNotBlank() }
                        ?: data.createdAt?.takeIf { it.isNotBlank() }
                        ?: salesPoints.lastOrNull { it.date.isNotBlank() }?.date
                    val lastUpdatedDateText = NumberFormatUtil.formatProductDetailLastUpdated(rawUpdatedStr)

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
                        id = data.id?.toString() ?: resolvedId.toString(),
                        name = data.name?.takeIf { it.isNotBlank() } ?: "—",
                        sku = resolvedSku.takeIf { it.isNotBlank() } ?: "—",
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
                            onOrderUnits = onOrderUnits,
                            estimatedCost = estCostFormatted,
                            salesHistory = salesPoints,
                            has7DaySales = has7DaySales,
                            total7DayUnits = total7DayUnits,
                            lastUpdatedDateText = lastUpdatedDateText
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

        val pid = currentProductId ?: current.product.id.toIntOrNull() ?: 0
        if (pid <= 0) {
            onError("Invalid product id.")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isOrdering = true) }
            val result = reorderRepository.createOrder(productId = pid, quantity = units)
            result.fold(
                onSuccess = { order ->
                    _uiState.update {
                        it.copy(
                            isOrdering = false,
                            lastPlacedOrder = order
                        )
                    }
                    loadProduct(currentProductId, currentProductSku)
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
                    loadProduct(currentProductId, currentProductSku)
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
        val pid = currentProductId ?: _uiState.value.product.id.toIntOrNull()
        viewModelScope.launch {
            val result = repository.deleteProduct(
                id = pid,
                sku = _uiState.value.product.sku.takeIf { it.isNotBlank() && it != "—" } ?: currentProductSku
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
