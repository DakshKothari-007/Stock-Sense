package com.simats.stocksense.core.network

import com.simats.stocksense.data.model.*
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Streaming

/**
 * Retrofit service for Inventory operations according to API_DOCUMENT.md Module 3.
 */
interface InventoryApiService {

    @GET("inventory/get_products.php")
    suspend fun getProducts(
        @Query("search") search: String? = null,
        @Query("filter") filter: String? = null,
        @Query("category") category: String? = null,
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null
    ): Response<InventoryProductsResponse>

    @GET("inventory/get_product_detail.php")
    suspend fun getProductDetail(
        @Query("id") id: Int? = null,
        @Query("sku") sku: String? = null
    ): Response<ProductDetailResponse>

    @POST("inventory/add_product.php")
    suspend fun addProduct(
        @Body request: AddProductRequest
    ): Response<AddProductResponse>

    @POST("inventory/update_product.php")
    suspend fun updateProduct(
        @Body request: UpdateProductRequest
    ): Response<InventoryActionResponse>

    @POST("inventory/delete_product.php")
    suspend fun deleteProduct(
        @Body request: DeleteProductRequest
    ): Response<InventoryActionResponse>

    @Streaming
    @GET("inventory/export_products.php")
    suspend fun exportProductsCsv(): Response<ResponseBody>
}

