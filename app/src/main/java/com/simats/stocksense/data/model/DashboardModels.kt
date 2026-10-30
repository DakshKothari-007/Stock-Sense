package com.simats.stocksense.data.model

import com.google.gson.annotations.SerializedName

/**
 * Data models strictly adhering to API_DOCUMENT.md and backend dashboard/summary.php.
 */
data class DashboardSummaryResponse(
    @SerializedName("status") val status: Boolean = false,
    @SerializedName("message") val message: String = "",
    @SerializedName("data") val data: DashboardSummaryData? = null
)

data class DashboardSummaryData(
    @SerializedName("store") val store: DashboardStoreInfo? = null,
    @SerializedName("date_range") val dateRange: DashboardDateRange? = null,
    @SerializedName("sales") val sales: DashboardSalesInfo? = null,
    @SerializedName("inventory") val inventory: DashboardInventoryInfo? = null,
    @SerializedName("top_selling_products") val topSellingProducts: List<DashboardTopProduct>? = emptyList(),
    @SerializedName("alerts") val alerts: DashboardAlerts? = null,
    @SerializedName("ai_priorities") val aiPriorities: List<DashboardAiPriority>? = emptyList(),
    @SerializedName("category_breakdown") val categoryBreakdown: List<DashboardCategoryItem>? = emptyList(),
    @SerializedName("last_import") val lastImport: DashboardLastImport? = null
)

data class DashboardStoreInfo(
    @SerializedName("name") val name: String? = null,
    @SerializedName("location") val location: String? = null,
    @SerializedName("currency") val currency: String? = null
)

data class DashboardDateRange(
    @SerializedName("from") val from: String? = null,
    @SerializedName("to") val to: String? = null
)

data class DashboardSalesInfo(
    @SerializedName("today_sales_amount") val todaySalesAmount: Double? = 0.0,
    @SerializedName("today_bills_count") val todayBillsCount: Int? = 0,
    @SerializedName("weekly_sales_total") val weeklySalesTotal: Double? = 0.0,
    @SerializedName("daily_sales_points") val dailySalesPoints: List<DashboardDailySalesPoint>? = emptyList()
)

data class DashboardDailySalesPoint(
    @SerializedName("date") val date: String? = null,
    @SerializedName("day_label") val dayLabel: String? = null,
    @SerializedName("amount") val amount: Double? = 0.0,
    @SerializedName("bills_count") val billsCount: Int? = 0,
    @SerializedName("is_current_day") val isCurrentDay: Boolean? = false
)

data class DashboardInventoryInfo(
    @SerializedName("total_active_products") val totalActiveProducts: Int? = 0,
    @SerializedName("total_stock_value") val totalStockValue: Double? = 0.0,
    @SerializedName("low_stock_count") val lowStockCount: Int? = 0,
    @SerializedName("out_of_stock_count") val outOfStockCount: Int? = 0,
    @SerializedName("slow_moving_count") val slowMovingCount: Int? = 0
)

data class DashboardAlerts(
    @SerializedName("low_stock_items") val lowStockItems: List<DashboardLowStockItem>? = emptyList(),
    @SerializedName("out_of_stock_items") val outOfStockItems: List<DashboardOutOfStockItem>? = emptyList(),
    @SerializedName("slow_moving_items") val slowMovingItems: List<DashboardSlowMovingItem>? = emptyList(),
    @SerializedName("slow_moving_metadata") val slowMovingMetadata: DashboardSlowMovingMetadata? = null
)

data class DashboardLowStockItem(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("name") val name: String = "",
    @SerializedName("sku") val sku: String? = null,
    @SerializedName("category") val category: String? = null,
    @SerializedName("units") val units: Int = 0,
    @SerializedName("min_stock_threshold") val minStockThreshold: Int = 0,
    @SerializedName("price") val price: Double? = 0.0,
    @SerializedName("cost_price") val costPrice: Double? = 0.0,
    @SerializedName("status") val status: String? = null
)

data class DashboardOutOfStockItem(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("name") val name: String = "",
    @SerializedName("sku") val sku: String? = null,
    @SerializedName("category") val category: String? = null,
    @SerializedName("units") val units: Int = 0,
    @SerializedName("min_stock_threshold") val minStockThreshold: Int = 0,
    @SerializedName("price") val price: Double? = 0.0,
    @SerializedName("cost_price") val costPrice: Double? = 0.0,
    @SerializedName("status") val status: String? = null
)

data class DashboardSlowMovingItem(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("name") val name: String = "",
    @SerializedName("sku") val sku: String? = null,
    @SerializedName("category") val category: String? = null,
    @SerializedName("current_stock") val currentStock: Int = 0,
    @SerializedName("units") val units: Int = 0,
    @SerializedName("sold_last_30_days") val soldLast30Days: Int = 0,
    @SerializedName("revenue_last_30_days") val revenueLast30Days: Double? = 0.0,
    @SerializedName("cash_tied_up_at_cost") val cashTiedUpAtCost: Double? = 0.0,
    @SerializedName("stock_cover_days") val stockCoverDays: Int? = null,
    @SerializedName("price") val price: Double? = 0.0,
    @SerializedName("cost_price") val costPrice: Double? = 0.0,
    @SerializedName("status") val status: String? = null
)

data class DashboardSlowMovingMetadata(
    @SerializedName("has_enough_sales_history") val hasEnoughSalesHistory: Boolean? = false,
    @SerializedName("history_days_available") val historyDaysAvailable: Double? = 0.0,
    @SerializedName("rule") val rule: String? = null,
    @SerializedName("message") val message: String? = null
)

data class DashboardAiPriority(
    @SerializedName("type") val type: String? = null, // "reorder" or "clearance"
    @SerializedName("title") val title: String? = null,
    @SerializedName("subtitle") val subtitle: String? = null,
    @SerializedName("product_id") val productId: Int? = null,
    @SerializedName("sku") val sku: String? = null,
    @SerializedName("units_to_order") val unitsToOrder: Int? = null,
    @SerializedName("estimated_cost") val estimatedCost: Double? = null,
    @SerializedName("units") val units: Int? = null,
    @SerializedName("cash_tied_up") val cashTiedUp: Double? = null,
    @SerializedName("rule") val rule: String? = null
)

data class DashboardTopProduct(
    @SerializedName("product_id") val productId: Int = 0,
    @SerializedName("name") val name: String = "",
    @SerializedName("sku") val sku: String? = null,
    @SerializedName("category") val category: String? = null,
    @SerializedName("units_sold") val unitsSold: Int = 0,
    @SerializedName("revenue") val revenue: Double = 0.0
)

data class DashboardCategoryItem(
    @SerializedName("category") val category: String = "",
    @SerializedName("product_count") val productCount: Int = 0,
    @SerializedName("total_units") val totalUnits: Int = 0,
    @SerializedName("category_value") val categoryValue: Double = 0.0,
    @SerializedName("value_percentage") val valuePercentage: Double = 0.0
)

data class DashboardLastImport(
    @SerializedName("file_name") val fileName: String? = null,
    @SerializedName("file_size") val fileSize: Int? = null,
    @SerializedName("total_rows") val totalRows: Int? = null,
    @SerializedName("success_count") val successCount: Int? = null,
    @SerializedName("failed_count") val failedCount: Int? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("date") val date: String? = null
)

/**
 * Notifications unread count model adhering to API_DOCUMENT.md section 10.2.
 */
data class UnreadCountResponse(
    @SerializedName("status") val status: Boolean = false,
    @SerializedName("message") val message: String = "",
    @SerializedName("data") val data: UnreadCountData? = null
)

data class UnreadCountData(
    @SerializedName("unread_count") val unreadCount: Int = 0
)

