package com.simats.stocksense.data.model

import com.google.gson.annotations.SerializedName
import java.util.UUID

/**
 * Data models for StockSense AI Assistant (Dukaan Mitra).
 */

data class AiHistoryItem(
    @SerializedName("role") val role: String,
    @SerializedName("text") val text: String
)

data class AiChatRequest(
    @SerializedName("message") val message: String,
    @SerializedName("language_hint") val languageHint: String? = null,
    @SerializedName("history") val history: List<AiHistoryItem>? = null
)

data class AiChatResponse(
    @SerializedName("status") val status: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: AiChatData?
)

data class AiChatData(
    @SerializedName("answer") val answer: String,
    @SerializedName("cards") val cards: List<AiProductCard>? = null,
    @SerializedName("actions") val actions: List<AiAction>? = null,
    @SerializedName("suggestions") val suggestions: List<AiSuggestion>? = null
)

data class AiProductCard(
    @SerializedName("product_id") val productId: Int? = null,
    @SerializedName("name") val name: String,
    @SerializedName("sku") val sku: String? = null,
    @SerializedName("units_left") val unitsLeft: Int = 0,
    @SerializedName("avg_daily_sales") val avgDailySales: Double = 0.0,
    @SerializedName("status") val status: String? = null,
    @SerializedName("price") val price: Double? = null,
    @SerializedName("min_stock") val minStock: Int? = null
)

data class AiAction(
    @SerializedName("label") val label: String,
    @SerializedName("target") val target: String,
    @SerializedName("product_id") val productId: Int? = null
)

data class AiSuggestion(
    @SerializedName("label") val label: String,
    @SerializedName("message") val message: String
)

data class AiExplainRequest(
    @SerializedName("type") val type: String,
    @SerializedName("product_id") val productId: Int
)

data class AiExplainResponse(
    @SerializedName("status") val status: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: AiExplainData?
)

data class AiExplainData(
    @SerializedName("explanation") val explanation: String
)

data class AiDailySummaryResponse(
    @SerializedName("status") val status: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: AiDailySummaryData?
)

data class AiDailySummaryData(
    @SerializedName("summary") val summary: String
)

/**
 * Local chat message representation in UI state.
 */
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val isUser: Boolean,
    val text: String,
    val cards: List<AiProductCard> = emptyList(),
    val actions: List<AiAction> = emptyList(),
    val suggestions: List<AiSuggestion> = emptyList(),
    val isError: Boolean = false,
    val isTyping: Boolean = false,
    val retryQuestion: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

