package com.simats.stocksense.core.network

import com.simats.stocksense.data.model.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit interface for Module 7: Reorder & Purchase Orders endpoints.
 */
interface ReorderApiService {

    /**
     * Module 7.1 Reorder Recommendations List
     */
    @GET("reorder/list.php")
    suspend fun getReorderList(
        @Query("priority") priority: String? = null,
        @Query("category") category: String? = null,
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null
    ): Response<ReorderListResponse>

    /**
     * Module 7.2 Create Purchase Order
     */
    @POST("reorder/create_order.php")
    suspend fun createPurchaseOrder(
        @Body request: CreatePurchaseOrderRequest
    ): Response<CreatePurchaseOrderResponse>

    /**
     * Module 7.3 List Purchase Orders
     */
    @GET("reorder/orders.php")
    suspend fun getPurchaseOrders(
        @Query("status") status: String? = null,
        @Query("page") page: Int? = 1,
        @Query("limit") limit: Int? = 20
    ): Response<PurchaseOrdersListResponse>

    /**
     * Module 7.4 Receive Purchase Order
     */
    @POST("reorder/receive.php")
    suspend fun receiveOrder(
        @Body request: OrderActionRequest
    ): Response<OrderActionResponse>

    /**
     * Module 7.5 Cancel Purchase Order
     */
    @POST("reorder/cancel.php")
    suspend fun cancelOrder(
        @Body request: OrderActionRequest
    ): Response<OrderActionResponse>
}

