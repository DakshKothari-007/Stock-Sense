package com.simats.stocksense.data.repository

import com.simats.stocksense.core.network.InsightsApiService
import com.simats.stocksense.core.network.NetworkClient
import com.simats.stocksense.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class InsightsRepository(
    private val apiService: InsightsApiService = NetworkClient.createService<InsightsApiService>()
) {

    suspend fun getSlowMoving(includeDismissed: Boolean = false): Result<SlowMovingData> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getSlowMoving(includeDismissed = if (includeDismissed) 1 else 0)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.status == true) {
                    Result.success(body.data ?: SlowMovingData(hasEnoughSalesHistory = false))
                } else {
                    Result.failure(Exception(body?.message ?: "Failed to fetch slow-moving recommendations."))
                }
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProductPairs(minSupport: Double = 0.05, includeDismissed: Boolean = false): Result<ProductPairsData> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getProductPairs(
                minSupport = minSupport,
                includeDismissed = if (includeDismissed) 1 else 0
            )
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.status == true) {
                    Result.success(body.data ?: ProductPairsData(hasEnoughSalesHistory = false))
                } else {
                    Result.failure(Exception(body?.message ?: "Failed to fetch product pair recommendations."))
                }
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun respond(
        recommendationType: String,
        productId1: Int,
        productId2: Int = 0,
        action: String,
        notes: String? = null
    ): Result<InsightRespondData> = withContext(Dispatchers.IO) {
        try {
            val req = InsightRespondRequest(
                recommendationType = recommendationType,
                productId1 = productId1,
                productId2 = productId2,
                action = action,
                notes = notes
            )
            val response = apiService.respondToRecommendation(req)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.status == true) {
                    Result.success(body.data ?: InsightRespondData(action = action))
                } else {
                    Result.failure(Exception(body?.message ?: "Failed to record recommendation response."))
                }
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getHistory(
        type: String? = null,
        action: String? = null,
        page: Int = 1,
        limit: Int = 20
    ): Result<InsightHistoryData> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getHistory(type = type, action = action, page = page, limit = limit)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.status == true) {
                    Result.success(body.data ?: InsightHistoryData())
                } else {
                    Result.failure(Exception(body?.message ?: "Failed to fetch recommendation history."))
                }
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseErrorMessage(errorBody: String?): String {
        if (errorBody.isNullOrBlank()) return "Server error occurred. Please try again."
        return try {
            val json = JSONObject(errorBody)
            json.optString("message", "Server error occurred. Please try again.")
        } catch (e: Exception) {
            "Server error occurred. Please try again."
        }
    }
}

