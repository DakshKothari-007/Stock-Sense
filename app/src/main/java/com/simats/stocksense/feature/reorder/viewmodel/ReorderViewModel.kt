package com.simats.stocksense.feature.reorder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.stocksense.core.util.NumberFormatUtil
import com.simats.stocksense.data.model.*
import com.simats.stocksense.data.repository.ReorderRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

data class ReorderUiState(
    val isLoading: Boolean = false,
    val isOrdering: Boolean = false,
    val isAwaitingSelection: Boolean = false,
    val errorMessage: String? = null,
    val items: List<ReorderItemDto> = emptyList(),
    val recommendation: ReorderRecommendation? = null,
    val selectedTargetProductId: Int? = null,
    val selectedTargetSku: String? = null,
    val selectionNotice: String? = null,
    val isAccepted: Boolean = false,
    val isDismissed: Boolean = false,
    val lastPlacedOrder: PurchaseOrderDto? = null
)

class ReorderViewModel(
    private val repository: ReorderRepository = ReorderRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReorderUiState(isLoading = true))
    val uiState: StateFlow<ReorderUiState> = _uiState.asStateFlow()

    private var activeRefreshJob: Job? = null

    init {
        refresh(force = true)
    }

    /**
     * Selects a reorder item by product ID first, then SKU.
     * Waits for loading to finish if recommendations are not loaded or need refresh.
     */
    fun selectReorderProduct(productId: Int? = null, sku: String? = null) {
        var shouldFetch = false
        _uiState.update { current ->
            val (nextState, needsFetch) = applySelectProduct(current, productId, sku)
            shouldFetch = needsFetch
            nextState
        }
        if (shouldFetch) {
            refresh(force = true)
        }
    }

    /**
     * Clears any active product selection and notices when leaving the screen.
     */
    fun clearSelection() {
        _uiState.update { applyClearSelection(it) }
    }

    /**
     * Refreshes recommendations from GET /reorder/list.php.
     */
    fun refresh(force: Boolean = false) {
        if (!force && activeRefreshJob?.isActive == true) {
            return
        }
        activeRefreshJob?.cancel()
        activeRefreshJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.fetchRecommendations()
            result.fold(
                onSuccess = { data ->
                    val rawItems = data.items.orEmpty()
                    _uiState.update { current ->
                        applyLoadedItems(current, rawItems)
                    }
                },
                onFailure = { err ->
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            isAwaitingSelection = false,
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
                        applyClearSelection(it).copy(
                            isOrdering = false,
                            isAccepted = true,
                            lastPlacedOrder = order
                        )
                    }
                    // Refresh recommendations list so ordered product updates or disappears
                    refresh(force = true)
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
     * Dismisses current recommendation and advances to the next available item by product identity.
     */
    fun dismissReorder() {
        _uiState.update { applyDismissCurrent(it) }
    }

    /**
     * Stores order details when an order is placed from outside (e.g. ProductDetailScreen).
     */
    fun setLastPlacedOrder(order: PurchaseOrderDto) {
        _uiState.update { it.copy(lastPlacedOrder = order) }
    }

    data class SelectionMatch(
        val selectedItem: ReorderItemDto?,
        val itemNotFound: Boolean
    )

    companion object {
        const val ITEM_NOT_FOUND_NOTICE =
            "That item is no longer in your reorder list. Showing the first available recommendation."

        /**
         * Resolves a target reorder item by product ID first, then SKU.
         * Never selects by list position.
         */
        fun resolveSelection(
            items: List<ReorderItemDto>,
            targetProductId: Int?,
            targetSku: String?
        ): SelectionMatch {
            if (items.isEmpty()) {
                return SelectionMatch(selectedItem = null, itemNotFound = false)
            }

            val validId = targetProductId?.takeIf { it > 0 }
            val validSku = targetSku?.trim()?.takeIf { it.isNotBlank() }

            if (validId == null && validSku == null) {
                return SelectionMatch(selectedItem = items.firstOrNull(), itemNotFound = false)
            }

            val byId = if (validId != null) {
                items.firstOrNull { it.product?.id == validId }
            } else null
            if (byId != null) {
                return SelectionMatch(selectedItem = byId, itemNotFound = false)
            }

            val bySku = if (validSku != null) {
                items.firstOrNull { it.product?.sku?.trim().equals(validSku, ignoreCase = true) }
            } else null
            if (bySku != null) {
                return SelectionMatch(selectedItem = bySku, itemNotFound = false)
            }

            return SelectionMatch(selectedItem = items.firstOrNull(), itemNotFound = true)
        }

        fun applySelectProduct(
            current: ReorderUiState,
            productId: Int?,
            sku: String?
        ): Pair<ReorderUiState, Boolean> {
            val validId = productId?.takeIf { it > 0 }
            val validSku = sku?.trim()?.takeIf { it.isNotBlank() }

            if (validId == null && validSku == null) {
                val firstRec = current.items.firstOrNull()?.let { mapToDomainRecommendation(it) }
                return current.copy(
                    selectedTargetProductId = null,
                    selectedTargetSku = null,
                    isAwaitingSelection = false,
                    selectionNotice = null,
                    isDismissed = false,
                    recommendation = firstRec
                ) to false
            }

            if (current.isLoading && current.items.isEmpty()) {
                return current.copy(
                    selectedTargetProductId = validId,
                    selectedTargetSku = validSku,
                    isAwaitingSelection = true,
                    selectionNotice = null,
                    isDismissed = false
                ) to false
            }

            val match = resolveSelection(current.items, validId, validSku)
            return if (current.items.isNotEmpty() && !match.itemNotFound && match.selectedItem != null) {
                current.copy(
                    selectedTargetProductId = match.selectedItem.product?.id ?: validId,
                    selectedTargetSku = match.selectedItem.product?.sku?.trim()?.takeIf { it.isNotBlank() } ?: validSku,
                    isAwaitingSelection = false,
                    selectionNotice = null,
                    isDismissed = false,
                    recommendation = mapToDomainRecommendation(match.selectedItem)
                ) to false
            } else {
                current.copy(
                    selectedTargetProductId = validId,
                    selectedTargetSku = validSku,
                    isAwaitingSelection = true,
                    selectionNotice = null,
                    isDismissed = false
                ) to true
            }
        }

        fun applyLoadedItems(
            current: ReorderUiState,
            rawItems: List<ReorderItemDto>
        ): ReorderUiState {
            if (rawItems.isEmpty()) {
                return current.copy(
                    isLoading = false,
                    isAwaitingSelection = false,
                    errorMessage = null,
                    items = emptyList(),
                    recommendation = null,
                    selectionNotice = null
                )
            }

            val hasTarget = (current.selectedTargetProductId != null && current.selectedTargetProductId > 0) ||
                !current.selectedTargetSku.isNullOrBlank()
            val match = resolveSelection(rawItems, current.selectedTargetProductId, current.selectedTargetSku)
            val selected = match.selectedItem ?: rawItems.first()
            val showNotFoundNotice = hasTarget && match.itemNotFound

            return current.copy(
                isLoading = false,
                isAwaitingSelection = false,
                errorMessage = null,
                items = rawItems,
                recommendation = mapToDomainRecommendation(selected),
                selectedTargetProductId = if (showNotFoundNotice) current.selectedTargetProductId else (selected.product?.id ?: current.selectedTargetProductId),
                selectedTargetSku = if (showNotFoundNotice) current.selectedTargetSku else (selected.product?.sku ?: current.selectedTargetSku),
                selectionNotice = if (showNotFoundNotice) ITEM_NOT_FOUND_NOTICE else null,
                isDismissed = false
            )
        }

        fun applyDismissCurrent(current: ReorderUiState): ReorderUiState {
            if (current.items.isEmpty()) {
                return current.copy(isDismissed = true, selectionNotice = null)
            }
            val activeId = current.recommendation?.productId?.takeIf { it > 0 }
            val activeSku = current.recommendation?.sku?.trim()?.takeIf { it.isNotBlank() }

            val currentPos = current.items.indexOfFirst { item ->
                (activeId != null && item.product?.id == activeId) ||
                    (activeId == null && activeSku != null && item.product?.sku?.trim().equals(activeSku, ignoreCase = true))
            }

            val nextItem = if (currentPos >= 0 && currentPos + 1 < current.items.size) {
                current.items[currentPos + 1]
            } else {
                null
            }

            return if (nextItem != null) {
                current.copy(
                    recommendation = mapToDomainRecommendation(nextItem),
                    selectedTargetProductId = nextItem.product?.id,
                    selectedTargetSku = nextItem.product?.sku,
                    selectionNotice = null,
                    isDismissed = false
                )
            } else {
                current.copy(
                    isDismissed = true,
                    selectionNotice = null
                )
            }
        }

        fun applyClearSelection(current: ReorderUiState): ReorderUiState {
            return current.copy(
                selectedTargetProductId = null,
                selectedTargetSku = null,
                isAwaitingSelection = false,
                selectionNotice = null,
                isDismissed = false,
                recommendation = current.items.firstOrNull()?.let { mapToDomainRecommendation(it) }
            )
        }

        fun mapToDomainRecommendation(item: ReorderItemDto): ReorderRecommendation {
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
        val demandFormatted = if (demandRate % 1.0 == 0.0) "${demandRate.toInt()}" else String.format(Locale.US, "%.1f", demandRate)
        val demandText = "$demandFormatted units/day"

        val daysLeft = item.daysOfStockLeft
        val coverNowText = when {
            daysLeft != null && daysLeft > 0 -> {
                val rounded = Math.round(daysLeft).toInt()
                if (rounded <= 0) "less than a day" else "$rounded days"
            }
            currentStock <= 0 -> "0 days (out of stock)"
            else -> "less than a day"
        }

        val onOrder = item.onOrderUnits ?: 0
        val projectedTotal = currentStock + recUnits + onOrder
        val coverAfterDays = item.stockCoverAfterReorderDays
        val coverAfterText = if (coverAfterDays != null) {
            val rounded = Math.round(coverAfterDays).toInt()
            val daysStr = if (rounded <= 0) "less than a day" else "$rounded days"
            "$projectedTotal units · $daysStr"
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
            sku = prod?.sku?.trim()?.takeIf { it.isNotBlank() },
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
}
