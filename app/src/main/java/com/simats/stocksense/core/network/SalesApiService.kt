package com.simats.stocksense.core.network

import com.simats.stocksense.data.model.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit API interface for Sales module endpoints matching API_DOCUMENT.md Section 6.
 */
interface SalesApiService {

    /**
     * Module 6.1 Create Sale Bill
     * Records a sale bill, atomically deducts stock.
     */
    @POST("sales/create.php")
    suspend fun createSale(
        @Body request: CreateSaleRequest
    ): Response<CreateSaleResponse>

    /**
     * Module 6.2 Get Sales List
     * Returns paginated list of bills with embedded line items.
     */
    @GET("sales/list.php")
    suspend fun getSalesList(
        @Query("page") page: Int? = 1,
        @Query("limit") limit: Int? = 20,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null,
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null
    ): Response<SalesListResponse>

    /**
     * Module 6.3 Sales Summary & Analytics
     * Period totals, daily sales chart points, top selling products, category breakdown, hourly breakdown.
     */
    @GET("sales/summary.php")
    suspend fun getSalesSummary(
        @Query("period") period: String? = null,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null
    ): Response<SalesSummaryResponse>

    /**
     * Module 6.4 Import Sales History
     * Bulk imports past sales with optional stock deduction.
     */
    @Multipart
    @POST("sales/import.php")
    suspend fun importSalesFile(
        @Part file: MultipartBody.Part,
        @Query("deduct_stock") deductStock: Boolean = false
    ): Response<ImportSalesResponse>

    /**
     * Module 6.5 Product 7-Day Sales History
     * Returns last 7 days of daily units sold per day for a specific product.
     */
    @GET("sales/product_history.php")
    suspend fun getProductSalesHistory(
        @Query("id") id: Int? = null,
        @Query("sku") sku: String? = null
    ): Response<ProductHistoryResponse>
}

