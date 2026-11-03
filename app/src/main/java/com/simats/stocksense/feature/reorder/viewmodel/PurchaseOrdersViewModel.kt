package com.simats.stocksense.feature.reorder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.stocksense.core.network.NetworkClient
import com.simats.stocksense.data.model.PurchaseOrderDto
import com.simats.stocksense.data.model.ReorderPaginationDto
import com.simats.stocksense.data.repository.ReorderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class PurchaseOrdersUiState(
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isActionRunning: Boolean = false,
    val errorMessage: String? = null,
    val actionSuccessMessage: String? = null,
    val actionErrorMessage: String? = null,
    val isSessionExpired: Boolean = false,

    val shopName: String = "",
    val selectedFilterIndex: Int = 0, // 0: All, 1: Placed, 2: Received, 3: Cancelled
    val orders: List<PurchaseOrderDto> = emptyList(),
    val placedOrdersCount: Int = 0,
    val totalOrdersCount: Int = 0,
    val pagination: ReorderPaginationDto? = null,
    val currentPage: Int = 1,

    // Currently viewed order for Order Detail Screen
    val selectedOrder: PurchaseOrderDto? = null
)

class PurchaseOrdersViewModel(
    private val repository: ReorderRepository = ReorderRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(PurchaseOrdersUiState(isLoading = true))
    val uiState: StateFlow<PurchaseOrdersUiState> = _uiState.asStateFlow()

    init {
        loadShopName()
        loadOrders(resetPage = true)
    }

    private fun loadShopName() {
        val cachedName = NetworkClient.getSessionManager()?.getStoreProfile()?.name?.takeIf { it.isNotBlank() } ?: ""
        _uiState.update { it.copy(shopName = cachedName) }
    }

    fun selectFilter(index: Int) {
        if (_uiState.value.selectedFilterIndex == index) return
        _uiState.update { it.copy(selectedFilterIndex = index) }
        loadOrders(resetPage = true)
    }

    fun refresh() {
        loadShopName()
        loadOrders(resetPage = true)
    }

    fun loadOrders(resetPage: Boolean = false) {
        viewModelScope.launch {
            val pageToLoad = if (resetPage) 1 else _uiState.value.currentPage
            val filterParam = when (_uiState.value.selectedFilterIndex) {
                1 -> "placed"
                2 -> "received"
                3 -> "cancelled"
                else -> null
            }

            if (resetPage) {
                _uiState.update { it.copy(isLoading = it.orders.isEmpty(), errorMessage = null) }
            } else {
                _uiState.update { it.copy(isLoadingMore = true, errorMessage = null) }
            }

            // Fetch orders with pagination
            val result = repository.fetchOrdersData(status = filterParam, page = pageToLoad, limit = 20)
            result.fold(
                onSuccess = { data ->
                    val newOrders = data.orders.orEmpty()
                    val total = data.pagination?.totalOrders ?: data.pagination?.totalMatching ?: newOrders.size
                    val totalPages = data.pagination?.totalPages ?: 1

                    val combinedOrders = if (resetPage) newOrders else (_uiState.value.orders + newOrders)

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            errorMessage = null,
                            orders = combinedOrders,
                            totalOrdersCount = total,
                            pagination = data.pagination,
                            currentPage = pageToLoad,
                            placedOrdersCount = if (filterParam == "placed") total else it.placedOrdersCount
                        )
                    }

                    // If we're on All, compute placedOrdersCount directly from the loaded list or query
                    if (filterParam == null) {
                        val placedCount = combinedOrders.count { it.status.equals("placed", ignoreCase = true) }
                        _uiState.update { it.copy(placedOrdersCount = placedCount) }
                    } else if (_uiState.value.placedOrdersCount == 0 && filterParam != "placed") {
                        // Quick background check for placed count if unknown
                        fetchPlacedCount()
                    }

                    // If a selected order is currently open, refresh its data in case status changed
                    val currentSelected = _uiState.value.selectedOrder
                    if (currentSelected != null) {
                        val updated = combinedOrders.find {
                            (currentSelected.id != null && it.id == currentSelected.id) ||
                            (currentSelected.orderNo != null && it.orderNo == currentSelected.orderNo)
                        }
                        if (updated != null) {
                            _uiState.update { it.copy(selectedOrder = updated) }
                        }
                    }
                },
                onFailure = { err ->
                    val isExpired = err.message?.contains("Session expired", ignoreCase = true) == true
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            errorMessage = err.message ?: "Failed to load purchase orders.",
                            isSessionExpired = isExpired
                        )
                    }
                }
            )
        }
    }

    private fun fetchPlacedCount() {
        viewModelScope.launch {
            repository.fetchOrdersData(status = "placed", page = 1, limit = 1).fold(
                onSuccess = { data ->
                    val placedTotal = data.pagination?.totalOrders ?: data.pagination?.totalMatching ?: data.orders?.size ?: 0
                    _uiState.update { it.copy(placedOrdersCount = placedTotal) }
                },
                onFailure = { /* non-critical */ }
            )
        }
    }

    fun loadNextPage() {
        val currentPagination = _uiState.value.pagination ?: return
        val totalPages = currentPagination.totalPages ?: 1
        val nextPage = _uiState.value.currentPage + 1
        if (nextPage <= totalPages && !_uiState.value.isLoadingMore) {
            _uiState.update { it.copy(currentPage = nextPage) }
            loadOrders(resetPage = false)
        }
    }

    fun selectOrder(order: PurchaseOrderDto) {
        _uiState.update {
            it.copy(
                selectedOrder = order,
                actionSuccessMessage = null,
                actionErrorMessage = null
            )
        }
    }

    fun selectOrderByIdOrNo(orderId: Int? = null, orderNo: String? = null) {
        val found = _uiState.value.orders.find {
            (orderId != null && (it.id == orderId || it.orderId == orderId)) ||
            (!orderNo.isNullOrBlank() && it.orderNo.equals(orderNo, ignoreCase = true))
        }
        if (found != null) {
            _uiState.update {
                it.copy(
                    selectedOrder = found,
                    actionSuccessMessage = null,
                    actionErrorMessage = null
                )
            }
        }
    }

    fun receiveOrder(
        order: PurchaseOrderDto,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val targetId = order.id ?: order.orderId ?: return
        if (_uiState.value.isActionRunning) return

        _uiState.update {
            it.copy(
                isActionRunning = true,
                actionSuccessMessage = null,
                actionErrorMessage = null
            )
        }

        viewModelScope.launch {
            val result = repository.receiveOrder(targetId)
            result.fold(
                onSuccess = { msg ->
                    val nowFormatted = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(Date())
                    val updatedOrder = order.copy(status = "received", receivedAt = nowFormatted)

                    _uiState.update { current ->
                        val updatedList = current.orders.map {
                            if (it.id == targetId || it.orderId == targetId) updatedOrder else it
                        }
                        current.copy(
                            isActionRunning = false,
                            actionSuccessMessage = msg,
                            actionErrorMessage = null,
                            orders = updatedList,
                            selectedOrder = updatedOrder,
                            placedOrdersCount = maxOf(0, current.placedOrdersCount - 1)
                        )
                    }
                    onSuccess()
                },
                onFailure = { err ->
                    val errorMsg = err.message ?: "Failed to receive order."
                    val isExpired = errorMsg.contains("Session expired", ignoreCase = true)
                    _uiState.update {
                        it.copy(
                            isActionRunning = false,
                            actionErrorMessage = errorMsg,
                            isSessionExpired = isExpired
                        )
                    }
                    onError(errorMsg)
                }
            )
        }
    }

    fun cancelOrder(
        order: PurchaseOrderDto,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val targetId = order.id ?: order.orderId ?: return
        if (_uiState.value.isActionRunning) return

        _uiState.update {
            it.copy(
                isActionRunning = true,
                actionSuccessMessage = null,
                actionErrorMessage = null
            )
        }

        viewModelScope.launch {
            val result = repository.cancelOrder(targetId)
            result.fold(
                onSuccess = { msg ->
                    val updatedOrder = order.copy(status = "cancelled")

                    _uiState.update { current ->
                        val updatedList = current.orders.map {
                            if (it.id == targetId || it.orderId == targetId) updatedOrder else it
                        }
                        current.copy(
                            isActionRunning = false,
                            actionSuccessMessage = msg,
                            actionErrorMessage = null,
                            orders = updatedList,
                            selectedOrder = updatedOrder,
                            placedOrdersCount = maxOf(0, current.placedOrdersCount - 1)
                        )
                    }
                    onSuccess()
                },
                onFailure = { err ->
                    val errorMsg = err.message ?: "Failed to cancel order."
                    val isExpired = errorMsg.contains("Session expired", ignoreCase = true)
                    _uiState.update {
                        it.copy(
                            isActionRunning = false,
                            actionErrorMessage = errorMsg,
                            isSessionExpired = isExpired
                        )
                    }
                    onError(errorMsg)
                }
            )
        }
    }

    fun clearActionMessages() {
        _uiState.update { it.copy(actionSuccessMessage = null, actionErrorMessage = null) }
    }

    companion object {
        fun formatOrderCardDate(status: String?, createdAt: String?, receivedAt: String?): String {
            val rawDate = if (status.equals("received", ignoreCase = true) && !receivedAt.isNullOrBlank()) receivedAt else createdAt
            if (rawDate.isNullOrBlank()) return "—"
            val parsedDate = parseDate(rawDate) ?: return rawDate
            val formatted = SimpleDateFormat("d MMM", Locale.ENGLISH).format(parsedDate)
            return when {
                status.equals("received", ignoreCase = true) -> "Received $formatted"
                status.equals("cancelled", ignoreCase = true) -> "Cancelled $formatted"
                else -> "Ordered $formatted"
            }
        }

        fun formatOrderDetailDate(status: String?, createdAt: String?, receivedAt: String?): String {
            val rawDate = if (status.equals("received", ignoreCase = true) && !receivedAt.isNullOrBlank()) receivedAt else createdAt
            if (rawDate.isNullOrBlank()) return "—"
            val parsedDate = parseDate(rawDate) ?: return rawDate
            val formatted = SimpleDateFormat("d MMM yyyy, h:mm a", Locale.ENGLISH).format(parsedDate)
            val prefix = when {
                status.equals("received", ignoreCase = true) -> "Received"
                status.equals("cancelled", ignoreCase = true) -> "Cancelled"
                else -> "Placed"
            }
            return "$prefix · $formatted"
        }

        private fun parseDate(dateStr: String): Date? {
            val formats = listOf(
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd"
            )
            for (fmt in formats) {
                try {
                    val sdf = SimpleDateFormat(fmt, Locale.ENGLISH)
                    return sdf.parse(dateStr)
                } catch (_: Exception) {}
            }
            return null
        }
    }
}

