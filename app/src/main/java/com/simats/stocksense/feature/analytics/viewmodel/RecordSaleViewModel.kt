package com.simats.stocksense.feature.analytics.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.stocksense.core.network.NetworkClient
import com.simats.stocksense.data.model.CreateSaleData
import com.simats.stocksense.data.model.CreateSaleItemRequest
import com.simats.stocksense.data.model.CreateSaleRequest
import com.simats.stocksense.data.model.NetworkInventoryProduct
import com.simats.stocksense.data.repository.InventoryRepository
import com.simats.stocksense.data.repository.SalesRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Bill item in the active sales cart.
 */
data class BillItem(
    val productId: Int,
    val name: String,
    val sku: String,
    val category: String = "",
    val unitPrice: Double,
    val availableStock: Int,
    val quantity: Int = 1
) {
    val lineTotal: Double get() = quantity * unitPrice
}

/**
 * State for Record Sale and Add Product screens.
 */
data class RecordSaleUiState(
    val billNo: String = "",
    val storeName: String = "",
    val items: List<BillItem> = emptyList(),
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val savedSaleData: CreateSaleData? = null,
    val isSessionExpired: Boolean = false,

    // Add Product Screen state
    val searchQuery: String = "",
    val selectedFilterIndex: Int = 0, // 0: All, 1: Fast selling, 2: Low stock
    val searchResults: List<NetworkInventoryProduct> = emptyList(),
    val totalShopProductsCount: Int = 0,
    val isSearching: Boolean = false,
    val searchErrorMessage: String? = null
) {
    val totalItemsCount: Int get() = items.size
    val totalUnitsCount: Int get() = items.sumOf { it.quantity }
    val totalAmount: Double get() = items.sumOf { it.lineTotal }
}

@OptIn(FlowPreview::class)
class RecordSaleViewModel(
    private val salesRepository: SalesRepository = SalesRepository(),
    private val inventoryRepository: InventoryRepository = InventoryRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecordSaleUiState())
    val uiState: StateFlow<RecordSaleUiState> = _uiState.asStateFlow()

    private val _searchFlow = MutableStateFlow("")

    init {
        val cachedStoreName = NetworkClient.getSessionManager()?.getStoreProfile()?.name?.takeIf { it.isNotBlank() } ?: ""
        _uiState.update {
            it.copy(
                billNo = generateBillNumber(),
                storeName = cachedStoreName
            )
        }

        // Set up 400ms search query debounce
        viewModelScope.launch {
            _searchFlow
                .debounce(400)
                .distinctUntilChanged()
                .collectLatest { query ->
                    executeSearch(query, _uiState.value.selectedFilterIndex)
                }
        }

        // Initial fetch of products for the Add Product screen
        loadProducts()
    }

    private fun generateBillNumber(): String {
        val datePart = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.ENGLISH).format(Date())
        val randomSuffix = (1..4).map { (('A'..'Z') + ('0'..'9')).random() }.joinToString("")
        return "APP-$datePart-$randomSuffix"
    }

    fun loadProducts() {
        executeSearch(_uiState.value.searchQuery, _uiState.value.selectedFilterIndex)
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        _searchFlow.value = query
    }

    fun selectFilter(filterIndex: Int) {
        _uiState.update { it.copy(selectedFilterIndex = filterIndex) }
        executeSearch(_uiState.value.searchQuery, filterIndex)
    }

    private fun executeSearch(query: String, filterIndex: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true, searchErrorMessage = null) }

            // Backend inventory/get_products.php filter param:
            // 0: All (null), 1: Fast selling (backend does not have fast_selling flag, defaults to all), 2: Low stock ("low_stock")
            val filterParam = when (filterIndex) {
                2 -> "low_stock"
                else -> null
            }

            val result = inventoryRepository.fetchProducts(
                search = query.trim().takeIf { it.isNotBlank() },
                filter = filterParam
            )

            result.fold(
                onSuccess = { data ->
                    val products = data.products ?: emptyList()
                    val totalCount = data.totalCount ?: data.totalProducts ?: products.size
                    val returnedShopName = data.shopName?.takeIf { it.isNotBlank() }
                    _uiState.update {
                        it.copy(
                            isSearching = false,
                            searchResults = products,
                            totalShopProductsCount = if (it.totalShopProductsCount == 0 || query.isBlank()) totalCount else it.totalShopProductsCount,
                            storeName = returnedShopName ?: it.storeName.takeIf { s -> s.isNotBlank() } ?: "",
                            searchErrorMessage = null
                        )
                    }
                },
                onFailure = { err ->
                    val isExpired = err.message?.contains("Session expired", ignoreCase = true) == true
                    _uiState.update {
                        it.copy(
                            isSearching = false,
                            searchErrorMessage = err.message ?: "Failed to load products.",
                            isSessionExpired = isExpired
                        )
                    }
                }
            )
        }
    }

    fun addProductToBill(product: NetworkInventoryProduct) {
        val prodId = when (val id = product.id) {
            is Number -> id.toInt()
            is String -> id.toDoubleOrNull()?.toInt() ?: return
            else -> return
        }

        val availableStock = when (val u = product.units) {
            is Number -> u.toInt()
            is String -> u.toDoubleOrNull()?.toInt() ?: product.rawUnits ?: 0
            else -> product.rawUnits ?: 0
        }

        if (availableStock <= 0) return // Out of stock items cannot be added

        val unitPrice = when (val p = product.price) {
            is Number -> p.toDouble()
            is String -> p.replace("₹", "").trim().toDoubleOrNull() ?: product.rawPrice ?: 0.0
            else -> product.rawPrice ?: 0.0
        }

        val currentItems = _uiState.value.items.toMutableList()
        val existingIndex = currentItems.indexOfFirst { it.productId == prodId }

        if (existingIndex >= 0) {
            val existing = currentItems[existingIndex]
            if (existing.quantity < availableStock) {
                currentItems[existingIndex] = existing.copy(quantity = existing.quantity + 1)
            }
        } else {
            currentItems.add(
                BillItem(
                    productId = prodId,
                    name = product.name ?: "—",
                    sku = product.sku ?: "—",
                    category = product.category ?: "",
                    unitPrice = unitPrice,
                    availableStock = availableStock,
                    quantity = 1
                )
            )
        }

        _uiState.update { it.copy(items = currentItems, errorMessage = null) }
    }

    fun incrementQuantity(productId: Int) {
        val currentItems = _uiState.value.items.toMutableList()
        val index = currentItems.indexOfFirst { it.productId == productId }
        if (index >= 0) {
            val item = currentItems[index]
            if (item.quantity < item.availableStock) {
                currentItems[index] = item.copy(quantity = item.quantity + 1)
                _uiState.update { it.copy(items = currentItems, errorMessage = null) }
            }
        }
    }

    fun decrementQuantity(productId: Int) {
        val currentItems = _uiState.value.items.toMutableList()
        val index = currentItems.indexOfFirst { it.productId == productId }
        if (index >= 0) {
            val item = currentItems[index]
            if (item.quantity > 1) {
                currentItems[index] = item.copy(quantity = item.quantity - 1)
            } else {
                // Minimum is 1; minus at 1 removes the item from the bill
                currentItems.removeAt(index)
            }
            _uiState.update { it.copy(items = currentItems, errorMessage = null) }
        }
    }

    fun removeItem(productId: Int) {
        val currentItems = _uiState.value.items.filterNot { it.productId == productId }
        _uiState.update { it.copy(items = currentItems, errorMessage = null) }
    }

    fun clearBill(generateNewBillNo: Boolean = true) {
        _uiState.update {
            it.copy(
                items = emptyList(),
                billNo = if (generateNewBillNo) generateBillNumber() else it.billNo,
                errorMessage = null
            )
        }
    }

    fun saveSale(onSuccess: (CreateSaleData) -> Unit = {}) {
        val currentState = _uiState.value
        if (currentState.items.isEmpty() || currentState.isSaving) return

        val activeBillNo = currentState.billNo.ifBlank { generateBillNumber() }
        _uiState.update { it.copy(isSaving = true, errorMessage = null, billNo = activeBillNo) }

        viewModelScope.launch {
            // Note: sale_date is omitted per requirement 3 so server defaults to current server time in Asia/Kolkata
            val request = CreateSaleRequest(
                billNo = activeBillNo,
                saleDate = null,
                items = currentState.items.map {
                    CreateSaleItemRequest(
                        productId = it.productId,
                        quantity = it.quantity,
                        unitPrice = it.unitPrice
                    )
                }
            )

            val result = salesRepository.createSale(request)
            result.fold(
                onSuccess = { data ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            savedSaleData = data,
                            items = emptyList(),
                            errorMessage = null
                        )
                    }
                    onSuccess(data)
                },
                onFailure = { err ->
                    val errorMsg = err.message ?: "Failed to record sale bill."
                    val isDuplicate = errorMsg.contains("duplicate bill number", ignoreCase = true) ||
                            errorMsg.contains("already exists", ignoreCase = true)
                    val isExpired = errorMsg.contains("Session expired", ignoreCase = true)

                    if (isDuplicate) {
                        // Requirement 3: If backend returns duplicate bill number for a retry, treat it as already saved!
                        val fallbackSavedData = CreateSaleData(
                            billNo = activeBillNo,
                            totalAmount = currentState.totalAmount,
                            totalItems = currentState.totalItemsCount,
                            totalQuantity = currentState.totalUnitsCount
                        )
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                savedSaleData = fallbackSavedData,
                                items = emptyList(),
                                errorMessage = null
                            )
                        }
                        onSuccess(fallbackSavedData)
                    } else if (isExpired) {
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                isSessionExpired = true,
                                errorMessage = "Session expired. Please log in again."
                            )
                        }
                    } else {
                        // Requirement 5: Keep the bill intact so user does not lose items, and show real backend message
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                errorMessage = errorMsg
                            )
                        }
                    }
                }
            )
        }
    }

    fun dismissSuccessState() {
        _uiState.update {
            it.copy(
                savedSaleData = null,
                billNo = generateBillNumber()
            )
        }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

