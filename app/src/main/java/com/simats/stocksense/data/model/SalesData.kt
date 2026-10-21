package com.simats.stocksense.data.model

data class SalesSummary(
    val todaySalesAmount: String = "₹18,450",
    val todayBillsCount: Int = 96,
    val todayAvgBillAmount: String = "₹192.19",
    val weeklySalesTotal: String = "₹1,20,750",
    val inventoryValue: String = "₹1,48,620",
    val activeProductsCount: Int = 128
)

data class DailySalesPoint(
    val dayLabel: String,
    val heightRatio: Float,
    val isCurrentDay: Boolean = false
)

data class CategoryBreakdown(
    val name: String,
    val percentage: Float,
    val amountText: String,
    val percentLabel: String
)

data class BestSellerItem(
    val nameWithUnits: String,
    val revenueText: String
)
