package com.simats.stocksense.core.network

import com.simats.stocksense.data.model.AiChatRequest
import com.simats.stocksense.data.model.AiChatResponse
import com.simats.stocksense.data.model.AiDailySummaryResponse
import com.simats.stocksense.data.model.AiExplainRequest
import com.simats.stocksense.data.model.AiExplainResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Retrofit API service for Dukaan Mitra AI Assistant (Module 11).
 */
interface AiApiService {

    @POST("ai/chat.php")
    suspend fun chat(
        @Body request: AiChatRequest
    ): Response<AiChatResponse>

    @POST("ai/explain.php")
    suspend fun explain(
        @Body request: AiExplainRequest
    ): Response<AiExplainResponse>

    @GET("ai/daily_summary.php")
    suspend fun getDailySummary(): Response<AiDailySummaryResponse>
}

