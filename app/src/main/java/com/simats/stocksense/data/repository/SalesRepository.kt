package com.simats.stocksense.data.repository

import com.google.gson.Gson
import com.simats.stocksense.core.network.NetworkClient
import com.simats.stocksense.core.network.SalesApiService
import com.simats.stocksense.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository handling all Sales API requests, error parsing, and backend validation messages.
 */
class SalesRepository(
    private val apiService: SalesApiService = NetworkClient.createService(),
    private val gson: Gson = NetworkClient.gson
) {

    /**
     * Fetches sales summary and analytics for a given period ("today", "7_days", "30_days", etc.)
     * or custom date range.
     */
    suspend fun fetchSalesSummary(
        period: String? = null,
        from: String? = null,
        to: String? = null
    ): Result<SalesSummaryData> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getSalesSummary(period = period, from = from, to = to)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.status == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message ?: "Failed to retrieve sales summary."))
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
     * Fetches paginated sales bills list.
     */
    suspend fun fetchSalesList(
        page: Int = 1,
        limit: Int = 20,
        from: String? = null,
        to: String? = null,
        startDate: String? = null,
        endDate: String? = null
    ): Result<SalesListData> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getSalesList(
                page = page,
                limit = limit,
                from = from,
                to = to,
                startDate = startDate,
                endDate = endDate
            )
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.status == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message ?: "Failed to retrieve sales list."))
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
     * Records a new sale bill with atomic stock deduction.
     */
    suspend fun createSale(request: CreateSaleRequest): Result<CreateSaleData> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.createSale(request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.status == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message ?: "Failed to record sale bill."))
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
                404 -> "Requested product or sale not found."
                409 -> "Duplicate bill number."
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

