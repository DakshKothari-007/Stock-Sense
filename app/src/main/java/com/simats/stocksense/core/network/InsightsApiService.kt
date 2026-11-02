package com.simats.stocksense.core.network

import com.simats.stocksense.data.model.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit interface for Module 8: AI Insights & Basket Analysis endpoints.
 */
interface InsightsApiService {

    /**
     * Module 8.1 Slow-Moving Clearance Recommendations
     */
    @GET("insights/slow_moving.php")
    suspend fun getSlowMoving(
        @Query("include_dismissed") includeDismissed: Int = 0
    ): Response<SlowMovingResponse>

    /**
     * Module 8.2 Product Pair Basket Analysis
     */
    @GET("insights/product_pairs.php")
    suspend fun getProductPairs(
        @Query("min_support") minSupport: Double? = 0.05,
        @Query("include_dismissed") includeDismissed: Int = 0
    ): Response<ProductPairsResponse>

    /**
     * Module 8.3 Respond to Recommendation
     */
    @POST("insights/respond.php")
    suspend fun respondToRecommendation(
        @Body request: InsightRespondRequest
    ): Response<InsightRespondResponse>

    /**
     * Module 8.4 Recommendation History Audit
     */
    @GET("insights/history.php")
    suspend fun getHistory(
        @Query("type") type: String? = null,
        @Query("action") action: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<InsightHistoryResponse>
}

