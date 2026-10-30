package com.simats.stocksense.core.network

import com.simats.stocksense.data.model.DashboardSummaryResponse
import com.simats.stocksense.data.model.UnreadCountResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit service for Dashboard summary and notification badge unread count.
 * Matches endpoints specified in API_DOCUMENT.md sections 5.1 and 10.2.
 */
interface DashboardApiService {

    @GET("dashboard/summary.php")
    suspend fun getDashboardSummary(
        @Query("from") from: String? = null,
        @Query("to") to: String? = null
    ): Response<DashboardSummaryResponse>

    @GET("notifications/unread_count.php")
    suspend fun getUnreadCount(): Response<UnreadCountResponse>
}

