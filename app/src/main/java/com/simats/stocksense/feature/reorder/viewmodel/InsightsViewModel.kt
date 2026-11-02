package com.simats.stocksense.feature.reorder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.stocksense.data.model.*
import com.simats.stocksense.data.repository.InsightsRepository
import com.simats.stocksense.data.repository.ReorderRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class InsightsUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isResponding: Boolean = false,
    val errorMessage: String? = null,
    val respondErrorMessage: String? = null,
    val slowMovingData: SlowMovingData? = null,
    val slowMovingItems: List<SlowMovingItemDto> = emptyList(),
    val slowMovingHistoryMessage: String? = null,
    val hasEnoughSlowMovingHistory: Boolean = true,
    val productPairsData: ProductPairsData? = null,
    val productPairItems: List<ProductPairItemDto> = emptyList(),
    val productPairsHistoryMessage: String? = null,
    val hasEnoughPairsHistory: Boolean = true,
    val reorderItems: List<ReorderItemDto> = emptyList(),
    val selectedSlowMovingIndex: Int = 0,
    val selectedPairIndex: Int = 0
) {
    val totalOpportunities: Int
        get() = reorderItems.size + slowMovingItems.size + productPairItems.size

    val activeSlowMovingItem: SlowMovingItemDto?
        get() = slowMovingItems.getOrNull(selectedSlowMovingIndex) ?: slowMovingItems.firstOrNull()

    val activePairItem: ProductPairItemDto?
        get() = productPairItems.getOrNull(selectedPairIndex) ?: productPairItems.firstOrNull()
}

class InsightsViewModel(
    private val repository: InsightsRepository = InsightsRepository(),
    private val reorderRepository: ReorderRepository = ReorderRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(InsightsUiState(isLoading = true))
    val uiState: StateFlow<InsightsUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun selectSlowMovingIndex(index: Int) {
        _uiState.update { it.copy(selectedSlowMovingIndex = index) }
    }

    fun selectPairIndex(index: Int) {
        _uiState.update { it.copy(selectedPairIndex = index) }
    }

    /**
     * Refreshes slow-moving recommendations, product pairs, and reorders in parallel.
     */
    fun refresh() {
        viewModelScope.launch {
            val isInitial = _uiState.value.slowMovingData == null && _uiState.value.productPairsData == null
            if (isInitial) {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            } else {
                _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
            }

            val slowMovingDeferred = async { repository.getSlowMoving() }
            val productPairsDeferred = async { repository.getProductPairs() }
            val reorderDeferred = async { reorderRepository.fetchRecommendations() }

            val slowMovingRes = slowMovingDeferred.await()
            val productPairsRes = productPairsDeferred.await()
            val reorderRes = reorderDeferred.await()

            var newErrorMessage: String? = null

            // Slow Moving
            var newSlowData: SlowMovingData? = _uiState.value.slowMovingData
            var newSlowItems = _uiState.value.slowMovingItems
            var hasEnoughSlowHist = true
            var slowHistMsg: String? = null

            slowMovingRes.fold(
                onSuccess = { data ->
                    newSlowData = data
                    hasEnoughSlowHist = data.hasEnoughSalesHistory ?: true
                    newSlowItems = if (hasEnoughSlowHist) data.items.orEmpty() else emptyList()
                    slowHistMsg = if (!hasEnoughSlowHist) {
                        "Need at least 30 days of sales history to calculate slow-moving recommendations accurately."
                    } else null
                },
                onFailure = { err ->
                    newErrorMessage = err.message
                }
            )

            // Product Pairs
            var newPairsData: ProductPairsData? = _uiState.value.productPairsData
            var newPairItems = _uiState.value.productPairItems
            var hasEnoughPairsHist = true
            var pairsHistMsg: String? = null

            productPairsRes.fold(
                onSuccess = { data ->
                    newPairsData = data
                    hasEnoughPairsHist = data.hasEnoughSalesHistory ?: true
                    newPairItems = if (hasEnoughPairsHist) data.effectiveItems else emptyList()
                    pairsHistMsg = if (!hasEnoughPairsHist) {
                        "Store needs at least 30 bills in the last 90 days to identify reliable product pairs."
                    } else null
                },
                onFailure = { err ->
                    if (newErrorMessage == null) {
                        newErrorMessage = err.message
                    }
                }
            )

            // Reorders
            var newReorderItems = _uiState.value.reorderItems
            reorderRes.fold(
                onSuccess = { data ->
                    newReorderItems = data.items.orEmpty()
                },
                onFailure = { }
            )

            _uiState.update {
                it.copy(
                    isLoading = false,
                    isRefreshing = false,
                    errorMessage = newErrorMessage,
                    slowMovingData = newSlowData,
                    slowMovingItems = newSlowItems,
                    hasEnoughSlowMovingHistory = hasEnoughSlowHist,
                    slowMovingHistoryMessage = slowHistMsg,
                    productPairsData = newPairsData,
                    productPairItems = newPairItems,
                    hasEnoughPairsHistory = hasEnoughPairsHist,
                    productPairsHistoryMessage = pairsHistMsg,
                    reorderItems = newReorderItems
                )
            }
        }
    }

    /**
     * Responds to a slow-moving recommendation (accept or dismiss).
     * Prevents double-taps via isResponding.
     */
    fun respondSlowMoving(
        productId: Int,
        action: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (_uiState.value.isResponding) return

        viewModelScope.launch {
            _uiState.update { it.copy(isResponding = true, respondErrorMessage = null) }
            val result = repository.respond(
                recommendationType = "slow_moving",
                productId1 = productId,
                productId2 = 0,
                action = action
            )
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isResponding = false) }
                    refresh()
                    onSuccess()
                },
                onFailure = { err ->
                    val msg = err.message ?: "Failed to record response."
                    _uiState.update { it.copy(isResponding = false, respondErrorMessage = msg) }
                    onError(msg)
                }
            )
        }
    }

    /**
     * Responds to a product pair recommendation (accept or dismiss).
     * Prevents double-taps via isResponding.
     */
    fun respondProductPair(
        productId1: Int,
        productId2: Int,
        action: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (_uiState.value.isResponding) return

        viewModelScope.launch {
            _uiState.update { it.copy(isResponding = true, respondErrorMessage = null) }
            val result = repository.respond(
                recommendationType = "product_pair",
                productId1 = productId1,
                productId2 = productId2,
                action = action
            )
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isResponding = false) }
                    refresh()
                    onSuccess()
                },
                onFailure = { err ->
                    val msg = err.message ?: "Failed to record response."
                    _uiState.update { it.copy(isResponding = false, respondErrorMessage = msg) }
                    onError(msg)
                }
            )
        }
    }
}

