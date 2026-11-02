package com.simats.stocksense.data.model

import com.google.gson.annotations.SerializedName

/**
 * Data models for Module 7: Reorder & Purchase Orders matching API_DOCUMENT.md.
 * All properties are nullable with safe defaults to avoid crashes on missing/null fields.
 */

// --- 1. GET /reorder/list.php ---
data class ReorderListResponse(
    @SerializedName("status") val status: Boolean? = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: ReorderListData? = null
)

data class ReorderListData(
    @SerializedName("reorder_config") val reorderConfig: ReorderConfigDto? = null,
    @SerializedName("history_warning") val historyWarning: Boolean? = false,
    @SerializedName("history_days_available") val historyDaysAvailable: Double? = 0.0,
    @SerializedName("summary") val summary: ReorderSummaryDto? = null,
    @SerializedName("pagination") val pagination: ReorderPaginationDto? = null,
    @SerializedName("items") val items: List<ReorderItemDto>? = emptyList()
)

data class ReorderConfigDto(
    @SerializedName("lead_time_days") val leadTimeDays: Int? = 3,
    @SerializedName("safety_days") val safetyDays: Int? = 2,
    @SerializedName("cover_days") val coverDays: Int? = 7,
    @SerializedName("min_history_days") val minHistoryDays: Int? = 30
)

data class ReorderSummaryDto(
    @SerializedName("total_items_needing_reorder") val totalItemsNeedingReorder: Int? = 0,
    @SerializedName("high_priority_count") val highPriorityCount: Int? = 0,
    @SerializedName("medium_priority_count") val mediumPriorityCount: Int? = 0,
    @SerializedName("low_priority_count") val lowPriorityCount: Int? = 0,
    @SerializedName("total_estimated_cost") val totalEstimatedCost: Double? = 0.0
)

data class ReorderPaginationDto(
    @SerializedName("page") val page: Int? = 1,
    @SerializedName("limit") val limit: Int? = 20,
    @SerializedName("total_matching") val totalMatching: Int? = 0,
    @SerializedName("total_pages") val totalPages: Int? = 1
)

data class ReorderItemDto(
    @SerializedName("product") val product: ReorderProductDto? = null,
    @SerializedName("current_stock") val currentStock: Int? = 0,
    @SerializedName("min_threshold") val minThreshold: Int? = 0,
    @SerializedName("sold_last_30_days") val soldLast30Days: Int? = 0,
    @SerializedName("daily_sales_rate") val dailySalesRate: Double? = 0.0,
    @SerializedName("days_of_stock_left") val daysOfStockLeft: Double? = null,
    @SerializedName("on_order_units") val onOrderUnits: Int? = 0,
    @SerializedName("gross_suggested_quantity") val grossSuggestedQuantity: Int? = null,
    @SerializedName("suggested_quantity") val suggestedQuantity: Int? = null,
    @SerializedName("estimated_cost") val estimatedCost: Double? = null,
    @SerializedName("reason") val reason: String? = null,
    @SerializedName("priority") val priority: String? = null,
    @SerializedName("priority_rule") val priorityRule: String? = null,
    @SerializedName("stock_cover_after_reorder_days") val stockCoverAfterReorderDays: Double? = null
)

data class ReorderProductDto(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("sku") val sku: String? = null,
    @SerializedName("category") val category: String? = null,
    @SerializedName("price") val price: Double? = 0.0,
    @SerializedName("cost_price") val costPrice: Double? = null
)

// --- 2. POST /reorder/create_order.php ---
data class CreatePurchaseOrderRequest(
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("supplier_name") val supplierName: String? = null,
    @SerializedName("items") val items: List<CreatePurchaseOrderItemRequest>
)

data class CreatePurchaseOrderItemRequest(
    @SerializedName("product_id") val productId: Int? = null,
    @SerializedName("sku") val sku: String? = null,
    @SerializedName("quantity") val quantity: Int
)

data class CreatePurchaseOrderResponse(
    @SerializedName("status") val status: Boolean? = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: PurchaseOrderDto? = null
)

data class PurchaseOrderDto(
    @SerializedName("order_id") val orderId: Int? = null,
    @SerializedName("id") val id: Int? = null,
    @SerializedName("order_no") val orderNo: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("total_items") val totalItems: Int? = 0,
    @SerializedName("total_units") val totalUnits: Int? = 0,
    @SerializedName("total_estimated_cost") val totalEstimatedCost: Double? = 0.0,
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("received_at") val receivedAt: String? = null,
    @SerializedName("items") val items: List<PurchaseOrderItemDto>? = emptyList()
)

data class PurchaseOrderItemDto(
    @SerializedName("product_id") val productId: Int? = null,
    @SerializedName("product_name") val productName: String? = null,
    @SerializedName("sku") val sku: String? = null,
    @SerializedName("quantity") val quantity: Int? = 0,
    @SerializedName("cost_price") val costPrice: Double? = null,
    @SerializedName("line_estimated_cost") val lineEstimatedCost: Double? = null
)

// --- 3. GET /reorder/orders.php ---
data class PurchaseOrdersListResponse(
    @SerializedName("status") val status: Boolean? = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: PurchaseOrdersListData? = null
)

data class PurchaseOrdersListData(
    @SerializedName("pagination") val pagination: ReorderPaginationDto? = null,
    @SerializedName("orders") val orders: List<PurchaseOrderDto>? = emptyList()
)

// --- 4. POST /reorder/receive.php & /reorder/cancel.php ---
data class OrderActionRequest(
    @SerializedName("order_id") val orderId: Int? = null,
    @SerializedName("order_no") val orderNo: String? = null
)

data class OrderActionResponse(
    @SerializedName("status") val status: Boolean? = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: Any? = null
)

