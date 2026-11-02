package com.simats.stocksense.data.model

import com.google.gson.annotations.SerializedName

/**
 * Response models for sales backend endpoints.
 * All properties are nullable with safe defaults to prevent crashes on null JSON values.
 */

// --- 1. sales/summary.php ---
data class SalesSummaryResponse(
    @SerializedName("status") val status: Boolean? = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: SalesSummaryData? = null
)

data class SalesSummaryData(
    @SerializedName("date_range") val dateRange: SalesDateRange? = null,
    @SerializedName("total_sales_amount") val totalSalesAmount: Double? = 0.0,
    @SerializedName("total_bills_count") val totalBillsCount: Int? = 0,
    @SerializedName("average_bill_amount") val averageBillAmount: Double? = 0.0,
    @SerializedName("daily_sales_points") val dailySalesPoints: List<DailySalesPointDto>? = emptyList(),
    @SerializedName("top_selling_products") val topSellingProducts: List<TopSellingProductDto>? = emptyList(),
    @SerializedName("category_breakdown") val categoryBreakdown: List<CategoryBreakdownDto>? = emptyList(),
    @SerializedName("hourly_sales_today") val hourlySalesToday: HourlySalesTodayDto? = null
)

data class SalesDateRange(
    @SerializedName("from") val from: String? = null,
    @SerializedName("to") val to: String? = null
)

data class DailySalesPointDto(
    @SerializedName("date") val date: String? = null,
    @SerializedName("day_label") val dayLabel: String? = null,
    @SerializedName("sales_amount") val salesAmount: Double? = 0.0,
    @SerializedName("bills_count") val billsCount: Int? = 0,
    @SerializedName("height_ratio") val heightRatio: Float? = 0f,
    @SerializedName("is_current_day") val isCurrentDay: Boolean? = false
)

data class TopSellingProductDto(
    @SerializedName("product_id") val productId: Int? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("sku") val sku: String? = null,
    @SerializedName("category") val category: String? = null,
    @SerializedName("units_sold") val unitsSold: Int? = 0,
    @SerializedName("revenue") val revenue: Double? = 0.0
)

data class CategoryBreakdownDto(
    @SerializedName("category") val category: String? = null,
    @SerializedName("units_sold") val unitsSold: Int? = 0,
    @SerializedName("revenue") val revenue: Double? = 0.0,
    @SerializedName("percentage") val percentage: Double? = 0.0
)

data class HourlySalesTodayDto(
    @SerializedName("has_real_timestamps") val hasRealTimestamps: Boolean? = false,
    @SerializedName("peak_hour") val peakHour: String? = null,
    @SerializedName("peak_amount") val peakAmount: Double? = 0.0,
    @SerializedName("hourly_points") val hourlyPoints: List<HourlyPointDto>? = emptyList()
)

data class HourlyPointDto(
    @SerializedName("hour") val hour: Int? = null,
    @SerializedName("label") val label: String? = null,
    @SerializedName("amount") val amount: Double? = 0.0,
    @SerializedName("bills_count") val billsCount: Int? = 0
)

// --- 2. sales/list.php ---
data class SalesListResponse(
    @SerializedName("status") val status: Boolean? = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: SalesListData? = null
)

data class SalesListData(
    @SerializedName("pagination") val pagination: SalesPagination? = null,
    @SerializedName("bills") val bills: List<SaleBillDto>? = emptyList()
)

data class SalesPagination(
    @SerializedName("page") val page: Int? = 1,
    @SerializedName("limit") val limit: Int? = 20,
    @SerializedName("total_bills") val totalBills: Int? = 0,
    @SerializedName("total_pages") val totalPages: Int? = 1
)

data class SaleBillDto(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("bill_no") val billNo: String? = null,
    @SerializedName("sale_date") val saleDate: String? = null,
    @SerializedName("total_amount") val totalAmount: Double? = 0.0,
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("item_count") val itemCount: Int? = 0,
    @SerializedName("total_quantity") val totalQuantity: Int? = 0,
    @SerializedName("items") val items: List<SaleBillItemDto>? = emptyList()
)

data class SaleBillItemDto(
    @SerializedName("item_id") val itemId: Int? = null,
    @SerializedName("product_id") val productId: Int? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("sku") val sku: String? = null,
    @SerializedName("category") val category: String? = null,
    @SerializedName("quantity") val quantity: Int? = 0,
    @SerializedName("unit_price") val unitPrice: Double? = 0.0,
    @SerializedName("line_total") val lineTotal: Double? = 0.0
)

// --- 3. sales/create.php ---
data class CreateSaleRequest(
    @SerializedName("bill_no") val billNo: String,
    @SerializedName("sale_date") val saleDate: String? = null,
    @SerializedName("customer_name") val customerName: String? = null,
    @SerializedName("customer_phone") val customerPhone: String? = null,
    @SerializedName("payment_method") val paymentMethod: String? = "CASH",
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("items") val items: List<CreateSaleItemRequest>
)

data class CreateSaleItemRequest(
    @SerializedName("product_id") val productId: Int,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("unit_price") val unitPrice: Double? = null
)

data class CreateSaleResponse(
    @SerializedName("status") val status: Boolean? = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: CreateSaleData? = null
)

data class CreateSaleData(
    @SerializedName("sale_id") val saleId: Int? = null,
    @SerializedName("bill_no") val billNo: String? = null,
    @SerializedName("sale_date") val saleDate: String? = null,
    @SerializedName("total_items") val totalItems: Int? = 0,
    @SerializedName("total_quantity") val totalQuantity: Int? = 0,
    @SerializedName("total_amount") val totalAmount: Double? = 0.0,
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("items") val items: List<SaleBillItemDto>? = emptyList()
)

// --- 4. sales/import.php ---
data class ImportSalesResponse(
    @SerializedName("status") val status: Boolean? = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: ImportSalesData? = null
)

data class ImportSalesData(
    @SerializedName("imported_bills") val importedBills: Int? = 0,
    @SerializedName("imported_items") val importedItems: Int? = 0
)

