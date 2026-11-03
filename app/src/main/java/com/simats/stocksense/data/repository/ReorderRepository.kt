package com.simats.stocksense.data.repository

import com.google.gson.Gson
import com.simats.stocksense.core.network.NetworkClient
import com.simats.stocksense.core.network.ReorderApiService
import com.simats.stocksense.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository handling Module 7: Reorder & Purchase Orders.
 */
class ReorderRepository(
    private val apiService: ReorderApiService = NetworkClient.createService(),
    private val gson: Gson = NetworkClient.gson
) {

    /**
     * Fetches reorder recommendations list from GET /reorder/list.php.
     */
    suspend fun fetchRecommendations(
        priority: String? = null,
        category: String? = null,
        page: Int = 1,
        limit: Int = 20
    ): Result<ReorderListData> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getReorderList(
                priority = priority,
                category = category,
                page = page,
                limit = limit
            )
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.status == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message ?: "Failed to retrieve reorder recommendations."))
                }
            } else {
                val errorMsg = extractErrorMessage(response.errorBody()?.string(), response.code())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Creates a purchase order for a single product.
     */
    suspend fun createOrder(
        productId: Int,
        quantity: Int,
        notes: String? = null
    ): Result<PurchaseOrderDto> {
        val request = CreatePurchaseOrderRequest(
            notes = notes,
            items = listOf(CreatePurchaseOrderItemRequest(productId = productId, quantity = quantity))
        )
        return createOrder(request)
    }

    /**
     * Creates a purchase order with multiple items from POST /reorder/create_order.php.
     */
    suspend fun createOrder(request: CreatePurchaseOrderRequest): Result<PurchaseOrderDto> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.createPurchaseOrder(request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.status == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message ?: "Failed to place purchase order."))
                }
            } else {
                val errorMsg = extractErrorMessage(response.errorBody()?.string(), response.code())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetches existing purchase orders from GET /reorder/orders.php.
     */
    suspend fun fetchOrders(
        status: String? = null,
        page: Int = 1,
        limit: Int = 20
    ): Result<List<PurchaseOrderDto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getPurchaseOrders(status = status, page = page, limit = limit)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.status == true && body.data != null) {
                    Result.success(body.data.orders.orEmpty())
                } else {
                    Result.failure(Exception(body?.message ?: "Failed to fetch orders."))
                }
            } else {
                val errorMsg = extractErrorMessage(response.errorBody()?.string(), response.code())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchOrdersData(
        status: String? = null,
        page: Int = 1,
        limit: Int = 20
    ): Result<PurchaseOrdersListData> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getPurchaseOrders(status = status, page = page, limit = limit)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.status == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message ?: "Failed to fetch orders."))
                }
            } else {
                val errorMsg = extractErrorMessage(response.errorBody()?.string(), response.code())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Receives a purchase order via POST /reorder/receive.php.
     */
    suspend fun receiveOrder(orderId: Int): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.receiveOrder(OrderActionRequest(orderId = orderId))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.status == true) {
                    Result.success(body.message ?: "Order received successfully.")
                } else {
                    Result.failure(Exception(body?.message ?: "Failed to receive order."))
                }
            } else {
                val errorMsg = extractErrorMessage(response.errorBody()?.string(), response.code())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Cancels an open purchase order via POST /reorder/cancel.php.
     */
    suspend fun cancelOrder(orderId: Int): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.cancelOrder(OrderActionRequest(orderId = orderId))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.status == true) {
                    Result.success(body.message ?: "Order cancelled successfully.")
                } else {
                    Result.failure(Exception(body?.message ?: "Failed to cancel order."))
                }
            } else {
                val errorMsg = extractErrorMessage(response.errorBody()?.string(), response.code())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun extractErrorMessage(errorBody: String?, httpCode: Int): String {
        if (errorBody.isNullOrBlank()) {
            return when (httpCode) {
                400 -> "Bad request. Please verify the input."
                401 -> "Session expired. Please log in again."
                404 -> "Product or purchase order not found."
                409 -> "Conflict occurred. Open order already exists."
                500 -> "Internal server error. Please try again."
                else -> "Request failed with HTTP status $httpCode."
            }
        }
        return try {
            val json = gson.fromJson(errorBody, Map::class.java)
            (json["message"] as? String)?.takeIf { it.isNotBlank() }
                ?: (json["error"] as? String)?.takeIf { it.isNotBlank() }
                ?: errorBody
        } catch (e: Exception) {
            errorBody
        }
    }
}

