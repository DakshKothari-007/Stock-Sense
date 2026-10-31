package com.simats.stocksense.feature.inventory.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.stocksense.core.util.NumberFormatUtil
import com.simats.stocksense.data.model.AddProductRequest
import com.simats.stocksense.data.model.InventoryProduct
import com.simats.stocksense.data.model.NetworkInventoryProduct
import com.simats.stocksense.data.model.UpdateProductRequest
import com.simats.stocksense.data.repository.InventoryRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI State for the Inventory Screen.
 */
data class InventoryUiState(
    val searchQuery: String = "",
    val selectedFilter: Int = 0, // 0: All, 1: Low stock, 2: Slow moving
    val selectedCategory: String = "All",
    val allProducts: List<InventoryProduct> = emptyList(),
    val filteredProducts: List<InventoryProduct> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val errorMessage: String? = null,
    val shopName: String = "",
    val totalCount: Int = 0,
    val currentPage: Int = 1,
    val hasMorePages: Boolean = false
)

/**
 * ViewModel managing real-time inventory products, 400ms search debouncing,
 * filters, pagination, and backend mutations.
 */
@OptIn(FlowPreview::class)
class InventoryViewModel(
    private val repository: InventoryRepository = InventoryRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(InventoryUiState())
    val uiState: StateFlow<InventoryUiState> = _uiState.asStateFlow()

    private val searchQueryFlow = MutableStateFlow("")

    init {
        // Debounce search typing by 400ms
        viewModelScope.launch {
            searchQueryFlow
                .debounce(400)
                .distinctUntilChanged()
                .collectLatest { debouncedQuery ->
                    loadProducts(
                        search = debouncedQuery,
                        filterIndex = _uiState.value.selectedFilter,
                        category = _uiState.value.selectedCategory,
                        isInitial = false
                    )
                }
        }

        loadProducts(isInitial = true)
    }

    /**
     * Updates search query immediately in UI state (for immediate back arrow toggle)
     * and forwards to 400ms debounce flow.
     */
    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchQueryFlow.value = query
    }

    /**
     * Updates status filter (0: All, 1: Low stock, 2: Slow moving).
     */
    fun selectFilter(filterIndex: Int) {
        _uiState.update { it.copy(selectedFilter = filterIndex) }
        loadProducts(
            search = _uiState.value.searchQuery,
            filterIndex = filterIndex,
            category = _uiState.value.selectedCategory,
            isInitial = false
        )
    }

    /**
     * Updates category filter.
     */
    fun selectCategory(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
        loadProducts(
            search = _uiState.value.searchQuery,
            filterIndex = _uiState.value.selectedFilter,
            category = category,
            isInitial = false
        )
    }

    /**
     * Screen resume or pull-to-refresh.
     */
    fun refresh() {
        loadProducts(
            search = _uiState.value.searchQuery,
            filterIndex = _uiState.value.selectedFilter,
            category = _uiState.value.selectedCategory,
            isInitial = false
        )
    }

    /**
     * Core loader for inventory products.
     */
    fun loadProducts(
        search: String = _uiState.value.searchQuery,
        filterIndex: Int = _uiState.value.selectedFilter,
        category: String = _uiState.value.selectedCategory,
        isInitial: Boolean = false
    ) {
        viewModelScope.launch {
            if (isInitial && _uiState.value.allProducts.isEmpty()) {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            } else {
                _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
            }

            val filterParam = when (filterIndex) {
                1 -> "low_stock"
                2 -> "slow_moving"
                else -> "0"
            }

            val categoryParam = if (category.equals("All", ignoreCase = true)) null else category

            val result = repository.fetchProducts(
                search = search,
                filter = filterParam,
                category = categoryParam,
                page = 1,
                limit = 50
            )

            result.fold(
                onSuccess = { data ->
                    val domainProducts = (data.products ?: emptyList()).map { mapNetworkToDomain(it) }
                    val total = if ((data.totalProducts ?: 0) > 0) (data.totalProducts ?: 0) else (data.totalCount ?: 0)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = null,
                            allProducts = domainProducts,
                            filteredProducts = domainProducts,
                            shopName = data.shopName ?: "",
                            totalCount = if (total > 0) total else domainProducts.size,
                            currentPage = 1,
                            hasMorePages = domainProducts.size < total
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = err.message ?: "Failed to load inventory. Please check your connection and retry."
                        )
                    }
                }
            )
        }
    }

    /**
     * Loads next page when user scrolls to bottom.
     */
    fun loadMore() {
        val currentState = _uiState.value
        if (currentState.isLoading || currentState.isRefreshing || currentState.isLoadingMore || !currentState.hasMorePages) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }
            val nextPage = currentState.currentPage + 1
            val filterParam = when (currentState.selectedFilter) {
                1 -> "low_stock"
                2 -> "slow_moving"
                else -> "0"
            }
            val categoryParam = if (currentState.selectedCategory.equals("All", ignoreCase = true)) null else currentState.selectedCategory

            val result = repository.fetchProducts(
                search = currentState.searchQuery,
                filter = filterParam,
                category = categoryParam,
                page = nextPage,
                limit = 50
            )

            result.fold(
                onSuccess = { data ->
                    val newItems = (data.products ?: emptyList()).map { mapNetworkToDomain(it) }
                    val combined = currentState.allProducts + newItems
                    val total = if ((data.totalProducts ?: 0) > 0) (data.totalProducts ?: 0) else (data.totalCount ?: 0)
                    _uiState.update {
                        it.copy(
                            isLoadingMore = false,
                            allProducts = combined,
                            filteredProducts = combined,
                            currentPage = nextPage,
                            hasMorePages = combined.size < total
                        )
                    }
                },
                onFailure = {
                    _uiState.update { it.copy(isLoadingMore = false) }
                }
            )
        }
    }

    /**
     * Adds product via POST /inventory/add_product.php and displays real validation message.
     */
    fun addProduct(
        request: AddProductRequest,
        onResult: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            val result = repository.addProduct(request)
            result.fold(
                onSuccess = { msg ->
                    refresh()
                    onResult(true, msg)
                },
                onFailure = { err ->
                    onResult(false, err.message ?: "Failed to add product.")
                }
            )
        }
    }

    /**
     * Updates product via POST /inventory/update_product.php and displays real validation message.
     */
    fun updateProduct(
        request: UpdateProductRequest,
        onResult: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            val result = repository.updateProduct(request)
            result.fold(
                onSuccess = { msg ->
                    refresh()
                    onResult(true, msg)
                },
                onFailure = { err ->
                    onResult(false, err.message ?: "Failed to update product.")
                }
            )
        }
    }

    /**
     * Deletes product via POST /inventory/delete_product.php and displays real validation message.
     */
    fun deleteProduct(
        id: Int?,
        sku: String?,
        onResult: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            val result = repository.deleteProduct(id, sku)
            result.fold(
                onSuccess = { msg ->
                    refresh()
                    onResult(true, msg)
                },
                onFailure = { err ->
                    onResult(false, err.message ?: "Failed to delete product.")
                }
            )
        }
    }

    /**
     * Converts a backend NetworkInventoryProduct into a domain InventoryProduct,
     * calculating status from units and threshold if absent, and applying Indian currency formatting.
     */
    private fun mapNetworkToDomain(item: NetworkInventoryProduct): InventoryProduct {
        val rawUnits = item.rawUnits ?: item.units?.toString()?.toIntOrNull() ?: 0
        val threshold = item.minStockThreshold ?: 10
        val rawPrice = item.rawPrice
            ?: item.price?.toString()?.replace("₹", "")?.replace(",", "")?.toDoubleOrNull()
            ?: 0.0

        val formattedPrice = NumberFormatUtil.formatIndianRupees(rawPrice)

        // Work out status if backend returned null or empty
        val rawStatus = item.status?.trim()
        val calculatedStatus: String?
        val calculatedColorHex: Long

        if (!rawStatus.isNullOrEmpty()) {
            calculatedStatus = rawStatus
            calculatedColorHex = item.statusColorHex ?: 0xFF98621A
        } else {
            when {
                rawUnits <= 0 -> {
                    calculatedStatus = "Out of stock"
                    calculatedColorHex = 0xFFD32F2F // Red
                }
                rawUnits <= threshold && threshold > 0 -> {
                    calculatedStatus = "Low stock · Minimum $threshold"
                    calculatedColorHex = 0xFF98621A // Amber
                }
                else -> {
                    calculatedStatus = null
                    calculatedColorHex = 0x00000000
                }
            }
        }

        // Determine icon type
        val iconType = item.iconType ?: when {
            item.name?.contains("Milk", ignoreCase = true) == true -> 0
            item.name?.contains("Biscuit", ignoreCase = true) == true || item.name?.contains("Marie", ignoreCase = true) == true -> 1
            item.name?.contains("Tea", ignoreCase = true) == true -> 3
            else -> 2
        }

        return InventoryProduct(
            id = item.id?.toString() ?: "",
            name = item.name?.takeIf { it.isNotBlank() } ?: "—",
            sku = item.sku?.takeIf { it.isNotBlank() } ?: "—",
            price = formattedPrice,
            status = calculatedStatus,
            statusColorHex = calculatedColorHex,
            units = rawUnits.toString(),
            iconType = iconType,
            category = item.category?.takeIf { it.isNotBlank() } ?: "—",
            minStockThreshold = threshold,
            costPrice = item.costPrice?.toString(),
            rawPrice = rawPrice,
            rawUnits = rawUnits,
            description = item.description
        )
    }
}
