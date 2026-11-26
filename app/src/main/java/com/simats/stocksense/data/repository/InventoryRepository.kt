package com.simats.stocksense.data.repository

import com.google.gson.Gson
import com.simats.stocksense.core.network.InventoryApiService
import com.simats.stocksense.core.network.NetworkClient
import com.simats.stocksense.data.model.*
import com.simats.stocksense.feature.inventory.util.ExportCsvException
import com.simats.stocksense.feature.inventory.util.ExportErrorResult
import com.simats.stocksense.feature.inventory.util.ProductCsvExportHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ExportedCsvData(
    val fileName: String,
    val bytes: ByteArray
)

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
                if (body != null && body.status == true && body.data != null) {
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
                if (body != null && body.status == true && body.data != null) {
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

    suspend fun addProduct(request: AddProductRequest): Result<AddProductResult> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.addProduct(request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.status) {
                    val returnedSku = body.data?.sku
                    val message = body.message.ifBlank { "Product added to inventory successfully." }
                    Result.success(AddProductResult(message = message, sku = returnedSku))
                } else {
                    Result.failure(ApiException(response.code(), body?.message ?: "Failed to add product."))
                }
            } else {
                val errorMsg = extractErrorMessage(response.errorBody()?.string(), response.code())
                Result.failure(ApiException(response.code(), errorMsg))
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
                if (body != null && body.status == true) {
                    Result.success(body.message?.ifBlank { "Product updated successfully." } ?: "Product updated successfully.")
                } else {
                    Result.failure(ApiException(response.code(), body?.message ?: "Failed to update product."))
                }
            } else {
                val errorMsg = extractErrorMessage(response.errorBody()?.string(), response.code())
                Result.failure(ApiException(response.code(), errorMsg))
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
                if (body != null && body.status == true) {
                    Result.success(body.message?.ifBlank { "Product deleted." } ?: "Product deleted.")
                } else {
                    Result.failure(ApiException(response.code(), body?.message ?: "Failed to delete product."))
                }
            } else {
                val errorMsg = extractErrorMessage(response.errorBody()?.string(), response.code())
                Result.failure(ApiException(response.code(), errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Downloads the shop's full product list as CSV bytes.
     */
    suspend fun exportProductsCsv(): Result<ExportedCsvData> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.exportProductsCsv()
            if (response.isSuccessful) {
                val body = response.body()
                val contentType = body?.contentType()?.toString().orEmpty()
                val bytes = body?.bytes() ?: ByteArray(0)
                if (contentType.contains("application/json", ignoreCase = true)) {
                    val mapped = ProductCsvExportHelper.mapExportError(
                        httpCode = response.code(),
                        errorBody = bytes.toString(Charsets.UTF_8)
                    )
                    return@withContext Result.failure(ExportCsvException(mapped))
                }
                if (bytes.isEmpty()) {
                    return@withContext Result.failure(
                        ExportCsvException(ExportErrorResult("No products to export. Add products to your inventory first."))
                    )
                }
                val fileName = ProductCsvExportHelper.buildExportFileName(
                    contentDisposition = response.headers()["Content-Disposition"]
                )
                Result.success(ExportedCsvData(fileName = fileName, bytes = bytes))
            } else {
                val errorText = response.errorBody()?.string()
                if (response.code() == 401) {
                    return@withContext Result.failure(
                        UnauthorizedException(ProductCsvExportHelper.mapExportError(httpCode = 401, errorBody = errorText).message)
                    )
                }
                val mapped = ProductCsvExportHelper.mapExportError(
                    httpCode = response.code(),
                    errorBody = errorText
                )
                Result.failure(ExportCsvException(mapped))
            }
        } catch (e: Exception) {
            Result.failure(ExportCsvException(ProductCsvExportHelper.mapExportError(throwable = e)))
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

