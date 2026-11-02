package com.simats.stocksense.feature.reorder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.stocksense.core.util.NumberFormatUtil
import com.simats.stocksense.data.model.*
import com.simats.stocksense.data.repository.ReorderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

data class ReorderUiState(
    val isLoading: Boolean = false,
    val isOrdering: Boolean = false,
    val errorMessage: String? = null,
    val items: List<ReorderItemDto> = emptyList(),
    val currentIndex: Int = 0,
    val recommendation: ReorderRecommendation? = null,
    val isAccepted: Boolean = false,
    val isDismissed: Boolean = false,
    val lastPlacedOrder: PurchaseOrderDto? = null
)

class ReorderViewModel(
    private val repository: ReorderRepository = ReorderRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReorderUiState(isLoading = true))
    val uiState: StateFlow<ReorderUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    /**
     * Refreshes recommendations from GET /reorder/list.php.
     */
    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = it.items.isEmpty(), errorMessage = null) }
            val result = repository.fetchRecommendations()
            result.fold(
                onSuccess = { data ->
                    val rawItems = data.items.orEmpty()
                    val idx = if (_uiState.value.currentIndex < rawItems.size) _uiState.value.currentIndex else 0
                    val currentItem = rawItems.getOrNull(idx)
                    val domainRec = currentItem?.let { mapToDomainRecommendation(it) }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                            items = rawItems,
                            currentIndex = idx,
                            recommendation = domainRec
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = err.message ?: "Failed to load reorder recommendations."
                        )
                    }
                }
            )
        }
    }

    /**
     * Places a purchase order for the currently active recommendation.
     * Prevents double-taps by verifying isOrdering.
     */
    fun acceptReorder(
        onSuccess: (PurchaseOrderDto) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val current = _uiState.value
        if (current.isOrdering) return

        val rec = current.recommendation
        val productId = rec?.productId
        val qty = rec?.recommendedUnits ?: 0

        if (productId == null || productId <= 0) {
            onError("Invalid product for reorder.")
            return
        }

        if (qty <= 0) {
            onError("Item quantity must be a positive integer greater than 0.")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isOrdering = true) }
            val result = repository.createOrder(productId = productId, quantity = qty)
            result.fold(
                onSuccess = { order ->
                    _uiState.update {
                        it.copy(
                            isOrdering = false,
                            isAccepted = true,
                            lastPlacedOrder = order
                        )
                    }
                    // Refresh recommendations list so ordered product updates or disappears
                    refresh()
                    onSuccess(order)
                },
                onFailure = { err ->
                    _uiState.update { it.copy(isOrdering = false) }
                    onError(err.message ?: "Failed to place purchase order.")
                }
            )
        }
    }

    /**
     * Dismisses current recommendation and moves to next item if available.
     */
    fun dismissReorder() {
        val current = _uiState.value
        val nextIdx = current.currentIndex + 1
        if (nextIdx < current.items.size) {
            val nextItem = current.items[nextIdx]
            _uiState.update {
                it.copy(
                    currentIndex = nextIdx,
                    recommendation = mapToDomainRecommendation(nextItem),
                    isDismissed = false
                )
            }
        } else {
            _uiState.update { it.copy(isDismissed = true) }
        }
    }

    /**
     * Stores order details when an order is placed from outside (e.g. ProductDetailScreen).
     */
    fun setLastPlacedOrder(order: PurchaseOrderDto) {
        _uiState.update { it.copy(lastPlacedOrder = order) }
    }

    private fun mapToDomainRecommendation(item: ReorderItemDto): ReorderRecommendation {
        val prod = item.product
        val name = prod?.name?.takeIf { it.isNotBlank() } ?: "—"
        val sku = prod?.sku?.takeIf { it.isNotBlank() } ?: "—"
        val displayName = if (sku != "—") "$name · $sku" else name

        val recUnits = item.suggestedQuantity ?: 0
        val costPrice = prod?.costPrice ?: 0.0
        val estCostFormatted = NumberFormatUtil.formatIndianRupees(item.estimatedCost ?: (recUnits * costPrice))
        val unitCostFormatted = NumberFormatUtil.formatIndianRupees(costPrice)
        val purchaseText = "Estimated purchase $estCostFormatted · $unitCostFormatted/unit"

        val currentStock = item.currentStock ?: 0
        val minThreshold = item.minThreshold ?: 0
        val stockText = "$currentStock / $minThreshold units"

        val sold30 = item.soldLast30Days ?: 0
        val soldText = "${NumberFormatUtil.formatIndianNumber(sold30)} units (30d)"

        val demandRate = item.dailySalesRate ?: 0.0
        val demandText = "${String.format(Locale.US, "%.1f", demandRate)} units/day"

        val daysLeft = item.daysOfStockLeft
        val coverNowText = when {
            daysLeft != null && daysLeft > 0 -> "$daysLeft days"
            currentStock <= 0 -> "0 days (out of stock)"
            else -> "less than a day"
        }

        val onOrder = item.onOrderUnits ?: 0
        val projectedTotal = currentStock + recUnits + onOrder
        val coverAfterDays = item.stockCoverAfterReorderDays
        val coverAfterText = if (coverAfterDays != null) {
            "$projectedTotal units · $coverAfterDays days"
        } else {
            "$projectedTotal units · —"
        }

        val reasonText = item.reason?.takeIf { it.isNotBlank() }
            ?: item.priorityRule?.takeIf { it.isNotBlank() }
            ?: "Stock is critically low. Replenish to prevent stockout."

        val priorityRaw = item.priority?.lowercase(Locale.ENGLISH) ?: "high"
        val priorityText = when (priorityRaw) {
            "high" -> "High priority - Low stock"
            "medium" -> "Medium priority - Buffer alert"
            "low" -> "Low priority - Cycle review"
            else -> "Reorder recommended"
        }

        val fullTitle = "Keep $name on the shelf"

        return ReorderRecommendation(
            productId = prod?.id,
            title = fullTitle,
            productName = displayName,
            recommendedUnits = recUnits,
            estimatedPurchaseText = purchaseText,
            currentStockText = stockText,
            salesLast7DaysText = soldText,
            avgDailyDemandText = demandText,
            stockCoverNowText = coverNowText,
            stockCoverAfterText = coverAfterText,
            demandDescription = reasonText,
            priorityText = priorityText,
            priority = priorityRaw,
            estimatedCost = item.estimatedCost,
            unitCost = costPrice
        )
    }
}
