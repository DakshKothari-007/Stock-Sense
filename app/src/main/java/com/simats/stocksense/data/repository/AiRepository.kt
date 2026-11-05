package com.simats.stocksense.data.repository

import com.google.gson.Gson
import com.simats.stocksense.core.network.AiApiService
import com.simats.stocksense.core.network.NetworkClient
import com.simats.stocksense.data.model.AiChatData
import com.simats.stocksense.data.model.AiChatRequest
import com.simats.stocksense.data.model.AiExplainRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for Dukaan Mitra AI Assistant operations adhering to API_DOCUMENT.md Module 11.
 */
class AiRepository(
    private val apiService: AiApiService = NetworkClient.createService(),
    private val gson: Gson = NetworkClient.gson
) {

    /**
     * Send chat query to Dukaan Mitra.
     */
    suspend fun chat(message: String, languageHint: String? = null): Result<AiChatData> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.chat(AiChatRequest(message = message, languageHint = languageHint))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.status && body.data != null) {
                    Result.success(body.data)
                } else {
                    val errorMsg = body?.message?.ifBlank { "Could not get an answer right now." } ?: "Could not get an answer right now."
                    Result.failure(Exception(errorMsg))
                }
            } else {
                handleHttpError(response.code(), response.errorBody()?.string())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Request an explanation for a reorder recommendation, slow-moving item, or product pair.
     */
    suspend fun explain(type: String, productId: Int): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.explain(AiExplainRequest(type = type, productId = productId))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.status && body.data != null) {
                    Result.success(body.data.explanation)
                } else {
                    val errorMsg = body?.message?.ifBlank { "Could not explain right now." } ?: "Could not explain right now."
                    Result.failure(Exception(errorMsg))
                }
            } else {
                handleHttpError(response.code(), response.errorBody()?.string())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetch the 2-line daily AI summary for the home dashboard.
     */
    suspend fun getDailySummary(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getDailySummary()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.status && body.data != null && body.data.summary.isNotBlank()) {
                    Result.success(body.data.summary)
                } else {
                    Result.failure(Exception("No summary available."))
                }
            } else {
                handleHttpError(response.code(), response.errorBody()?.string())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun <T> handleHttpError(code: Int, errorBody: String?): Result<T> {
        if (code == 401) {
            NetworkClient.getSessionManager()?.clearSession()
            return Result.failure(Exception("Session expired. Please log in again."))
        }
        if (code == 429) {
            val custom = extractErrorMessage(errorBody, code)
            return Result.failure(Exception(custom.ifBlank { "Daily question limit reached. Please try again tomorrow." }))
        }
        val msg = extractErrorMessage(errorBody, code)
        return Result.failure(Exception(msg))
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

