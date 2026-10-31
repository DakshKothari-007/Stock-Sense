package com.simats.stocksense.data.repository

import com.simats.stocksense.core.network.DashboardApiService
import com.simats.stocksense.core.network.NetworkClient
import com.simats.stocksense.data.model.DashboardSummaryData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository handling dashboard data aggregation and unread notifications count.
 */
class DashboardRepository(
    private val apiService: DashboardApiService = NetworkClient.createService()
) {

    /**
     * Fetches real-time dashboard summary from GET /dashboard/summary.php.
     */
    suspend fun fetchDashboardSummary(): Result<DashboardSummaryData> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getDashboardSummary()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.status == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message ?: "Failed to retrieve dashboard summary."))
                }
            } else {
                val errorMsg = response.errorBody()?.string()
                Result.failure(Exception(errorMsg ?: "Server returned error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetches unread notifications count from GET /notifications/unread_count.php.
     */
    suspend fun fetchUnreadCount(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getUnreadCount()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.status == true && body.data != null) {
                    Result.success(body.data.unreadCount ?: 0)
                } else {
                    Result.success(0)
                }
            } else {
                Result.success(0)
            }
        } catch (e: Exception) {
            Result.success(0)
        }
    }
}

