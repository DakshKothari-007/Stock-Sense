package com.simats.stocksense.data.model

data class ReorderRecommendation(
    val title: String = "Keep milk on the shelf",
    val productName: String = "Amul Milk · 500 ml",
    val recommendedUnits: Int = 60,
    val estimatedPurchaseText: String = "Estimated purchase ₹1,620 · ₹27/unit",
    val currentStockText: String = "8 / 24 units",
    val salesLast7DaysText: String = "84 units",
    val avgDailyDemandText: String = "12 units",
    val stockCoverNowText: String = "0.7 days",
    val stockCoverAfterText: String = "68 units · 5.7 days",
    val demandDescription: String = "Demand has stayed steady at 10–14 units a day. Replenish soon and receive milk in daily batches to keep it fresh."
)
