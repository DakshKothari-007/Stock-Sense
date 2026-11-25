package com.simats.stocksense.data.model

data class ReorderRecommendation(
    val productId: Int? = null,
    val sku: String? = null,
    val title: String = "",
    val productName: String = "",
    val recommendedUnits: Int = 0,
    val estimatedPurchaseText: String = "",
    val currentStockText: String = "",
    val salesLast7DaysText: String = "",
    val avgDailyDemandText: String = "",
    val stockCoverNowText: String = "",
    val stockCoverAfterText: String = "",
    val demandDescription: String = "",
    val priorityText: String = "",
    val priority: String = "",
    val estimatedCost: Double? = null,
    val unitCost: Double? = null
)
