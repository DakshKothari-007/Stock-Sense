package com.simats.stocksense.data.repository

import com.google.gson.Gson
import com.simats.stocksense.core.network.InventoryApiService
import com.simats.stocksense.core.network.NetworkClient
import com.simats.stocksense.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for inventory operations, mapping backend responses and extracting server error messages.
 */
class InventoryRepository(
    private val apiService: InventoryApiService = NetworkClient.createService(),
    private val gson: Gson = NetworkClient.gson
) {

    suspend fun fetchProducts(
        search: String? = null,
        filter: String? = null,
        category: String? = null,
        page: Int? = null,
        limit: Int? = null
    ): Result<InventoryProductsData> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getProducts(
                search = search?.takeIf { it.isNotBlank() },
                filter = filter?.takeIf { it.isNotBlank() },
                category = category?.takeIf { it.isNotBlank() },
                page = page,
                limit = limit
            )

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.status && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message ?: "Failed to retrieve inventory products."))
                }
            } else {
                val errorMsg = extractErrorMessage(response.errorBody()?.string(), response.code())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchProductDetail(id: Int? = null, sku: String? = null): Result<ProductDetailData> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getProductDetail(id = id, sku = sku)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.status && body.data != null) {
                    val resolved = body.data.product ?: body.data
                    Result.success(resolved)
                } else {
                    Result.failure(Exception(body?.message ?: "Failed to retrieve product details."))
                }
            } else {
                val errorMsg = extractErrorMessage(response.errorBody()?.string(), response.code())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addProduct(request: AddProductRequest): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.addProduct(request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.status) {
                    Result.success(body.message.ifBlank { "Product added successfully." })
                } else {
                    Result.failure(Exception(body?.message ?: "Failed to add product."))
                }
            } else {
                val errorMsg = extractErrorMessage(response.errorBody()?.string(), response.code())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProduct(request: UpdateProductRequest): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.updateProduct(request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.status) {
                    Result.success(body.message.ifBlank { "Product updated successfully." })
                } else {
                    Result.failure(Exception(body?.message ?: "Failed to update product."))
                }
            } else {
                val errorMsg = extractErrorMessage(response.errorBody()?.string(), response.code())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteProduct(id: Int?, sku: String?): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteProduct(DeleteProductRequest(id = id, sku = sku))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.status) {
                    Result.success(body.message.ifBlank { "Product deleted successfully." })
                } else {
                    Result.failure(Exception(body?.message ?: "Failed to delete product."))
                }
            } else {
                val errorMsg = extractErrorMessage(response.errorBody()?.string(), response.code())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun extractErrorMessage(errorBody: String?, fallbackCode: Int): String {
        if (errorBody.isNullOrBlank()) return "Server returned error code $fallbackCode"
        return try {
            val map = gson.fromJson(errorBody, Map::class.java)
            map["message"]?.toString() ?: errorBody
        } catch (e: Exception) {
            errorBody
        }
    }
}

