package com.simats.stocksense.data.model

import com.google.gson.annotations.SerializedName

// -------------------------------------------------------------
// Module 8.1: Slow-Moving Recommendations
// -------------------------------------------------------------

data class SlowMovingResponse(
    @SerializedName("status") val status: Boolean? = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: SlowMovingData? = null
)

data class SlowMovingData(
    @SerializedName("has_enough_sales_history") val hasEnoughSalesHistory: Boolean? = false,
    @SerializedName("sales_history_days") val salesHistoryDays: Int? = 0,
    @SerializedName("total_slow_moving_count") val totalSlowMovingCount: Int? = 0,
    @SerializedName("total_tied_up_capital") val totalTiedUpCapital: Double? = 0.0,
    @SerializedName("config") val config: SlowMovingConfigDto? = null,
    @SerializedName("items") val items: List<SlowMovingItemDto>? = emptyList()
)

data class SlowMovingConfigDto(
    @SerializedName("stock_cover_days_threshold") val stockCoverDaysThreshold: Int? = 60,
    @SerializedName("single_discount_pct") val singleDiscountPct: Int? = 10,
    @SerializedName("bundle_discount_pct") val bundleDiscountPct: Int? = 15
)

data class SlowMovingItemDto(
    @SerializedName("product_id") val productId: Int? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("sku") val sku: String? = null,
    @SerializedName("category") val category: String? = null,
    @SerializedName("current_stock") val currentStock: Int? = null,
    @SerializedName("units") val units: Int? = null,
    @SerializedName("units_sold_30_days") val unitsSold30Days: Int? = 0,
    @SerializedName("daily_sales_rate") val dailySalesRate: Double? = 0.0,
    @SerializedName("stock_cover_days") val stockCoverDays: Double? = null,
    @SerializedName("selling_price") val sellingPrice: Double? = null,
    @SerializedName("cost_price") val costPrice: Double? = null,
    @SerializedName("tied_up_capital") val tiedUpCapital: Double? = 0.0,
    @SerializedName("status") val status: String? = null,
    @SerializedName("rule_triggered") val ruleTriggered: String? = null,
    @SerializedName("suggested_discount_percent") val suggestedDiscountPercent: Int? = null,
    @SerializedName("clearance_price") val clearancePrice: Double? = null,
    @SerializedName("margin_status") val marginStatus: String? = null,
    @SerializedName("clearance_suggestion") val clearanceSuggestion: ClearanceSuggestionDto? = null,
    @SerializedName("response") val response: RecommendationResponseDto? = null
) {
    val effectiveStock: Int
        get() = currentStock ?: units ?: 0

    val effectiveDiscountPct: Int
        get() = clearanceSuggestion?.discountPct ?: suggestedDiscountPercent ?: 10

    val effectiveSuggestedPrice: Double?
        get() = clearanceSuggestion?.suggestedPrice ?: clearancePrice ?: (
            if (sellingPrice != null) sellingPrice * (1.0 - (effectiveDiscountPct / 100.0)) else null
        )

    val effectiveMarginStatus: String?
        get() = clearanceSuggestion?.marginStatus ?: marginStatus
}

data class ClearanceSuggestionDto(
    @SerializedName("action_type") val actionType: String? = null,
    @SerializedName("discount_pct") val discountPct: Int? = 10,
    @SerializedName("suggested_price") val suggestedPrice: Double? = null,
    @SerializedName("margin_status") val marginStatus: String? = null,
    @SerializedName("warning") val warning: String? = null,
    @SerializedName("bundle_suggestion") val bundleSuggestion: BundleSuggestionDto? = null
)

data class BundleSuggestionDto(
    @SerializedName("action_type") val actionType: String? = null,
    @SerializedName("discount_pct") val discountPct: Int? = 15,
    @SerializedName("suggested_price") val suggestedPrice: Double? = null,
    @SerializedName("margin_status") val marginStatus: String? = null,
    @SerializedName("warning") val warning: String? = null
)

data class RecommendationResponseDto(
    @SerializedName("is_responded") val isResponded: Boolean? = false,
    @SerializedName("action") val action: String? = null,
    @SerializedName("responded_at") val respondedAt: String? = null
)

// -------------------------------------------------------------
// Module 8.2: Product Pairs Basket Analysis
// -------------------------------------------------------------

data class ProductPairsResponse(
    @SerializedName("status") val status: Boolean? = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: ProductPairsData? = null
)

data class ProductPairsData(
    @SerializedName("has_enough_sales_history") val hasEnoughSalesHistory: Boolean? = false,
    @SerializedName("total_bills_analyzed") val totalBillsAnalyzed: Int? = 0,
    @SerializedName("total_pairs_count") val totalPairsCount: Int? = 0,
    @SerializedName("config") val config: PairsConfigDto? = null,
    @SerializedName("items") val items: List<ProductPairItemDto>? = emptyList(),
    @SerializedName("pairs") val pairs: List<ProductPairItemDto>? = emptyList()
) {
    val effectiveItems: List<ProductPairItemDto>
        get() = if (!items.isNullOrEmpty()) items else pairs.orEmpty()
}

data class PairsConfigDto(
    @SerializedName("window_days") val windowDays: Int? = 90,
    @SerializedName("min_total_bills") val minTotalBills: Int? = 30,
    @SerializedName("min_bills_together") val minBillsTogether: Int? = 3,
    @SerializedName("min_support") val minSupport: Double? = 0.05
)

data class ProductPairItemDto(
    @SerializedName("pair_id") val pairId: String? = null,
    @SerializedName("product_1") val product1: PairProductDto? = null,
    @SerializedName("product_2") val product2: PairProductDto? = null,
    @SerializedName("metrics") val metrics: PairMetricsDto? = null,
    @SerializedName("headline") val headline: String? = null,
    @SerializedName("support") val support: Double? = null,
    @SerializedName("lift") val lift: Double? = null,
    @SerializedName("suggested_action") val suggestedAction: SuggestedPairActionDto? = null,
    @SerializedName("response") val response: RecommendationResponseDto? = null
) {
    val effectiveSupport: Double
        get() = metrics?.support ?: support ?: 0.0

    val effectiveLift: Double
        get() = metrics?.lift ?: lift ?: 1.0
}

data class PairProductDto(
    @SerializedName("product_id") val productId: Int? = null,
    @SerializedName("id") val id: Int? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("sku") val sku: String? = null,
    @SerializedName("category") val category: String? = null,
    @SerializedName("price") val price: Double? = null,
    @SerializedName("current_stock") val currentStock: Int? = 0,
    @SerializedName("units") val units: Int? = 0,
    @SerializedName("bills_count") val billsCount: Int? = 0
) {
    val effectiveId: Int?
        get() = productId ?: id

    val effectiveStock: Int
        get() = currentStock ?: units ?: 0
}

data class PairMetricsDto(
    @SerializedName("bills_together") val billsTogether: Int? = 0,
    @SerializedName("total_store_bills") val totalStoreBills: Int? = 0,
    @SerializedName("support") val support: Double? = 0.0,
    @SerializedName("confidence_1_to_2") val confidence1To2: Double? = 0.0,
    @SerializedName("confidence_2_to_1") val confidence2To1: Double? = 0.0,
    @SerializedName("lift") val lift: Double? = 1.0
)

data class SuggestedPairActionDto(
    @SerializedName("action_type") val actionType: String? = null,
    @SerializedName("bundle_discount_pct") val bundleDiscountPct: Int? = 10,
    @SerializedName("combined_regular_price") val combinedRegularPrice: Double? = null,
    @SerializedName("bundle_price") val bundlePrice: Double? = null,
    @SerializedName("placement_tip") val placementTip: String? = null
)

// -------------------------------------------------------------
// Module 8.3: Respond to Recommendation
// -------------------------------------------------------------

data class InsightRespondRequest(
    @SerializedName("recommendation_type") val recommendationType: String,
    @SerializedName("product_id_1") val productId1: Int,
    @SerializedName("product_id_2") val productId2: Int = 0,
    @SerializedName("action") val action: String,
    @SerializedName("notes") val notes: String? = null
)

data class InsightRespondResponse(
    @SerializedName("status") val status: Boolean? = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: InsightRespondData? = null
)

data class InsightRespondData(
    @SerializedName("recommendation_type") val recommendationType: String? = null,
    @SerializedName("product_id_1") val productId1: Int? = null,
    @SerializedName("product_id_2") val productId2: Int? = null,
    @SerializedName("action") val action: String? = null,
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("pair_id") val pairId: String? = null
)

// -------------------------------------------------------------
// Module 8.4: Recommendation History Audit
// -------------------------------------------------------------

data class InsightHistoryResponse(
    @SerializedName("status") val status: Boolean? = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: InsightHistoryData? = null
)

data class InsightHistoryData(
    @SerializedName("items") val items: List<InsightHistoryItemDto>? = emptyList(),
    @SerializedName("pagination") val pagination: InsightPaginationDto? = null
)

data class InsightHistoryItemDto(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("recommendation_type") val recommendationType: String? = null,
    @SerializedName("pair_id") val pairId: String? = null,
    @SerializedName("product_1") val product1: InsightHistoryProductDto? = null,
    @SerializedName("product_2") val product2: InsightHistoryProductDto? = null,
    @SerializedName("action") val action: String? = null,
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null
)

data class InsightHistoryProductDto(
    @SerializedName("product_id") val productId: Int? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("sku") val sku: String? = null,
    @SerializedName("price") val price: Double? = null
)

data class InsightPaginationDto(
    @SerializedName("page") val page: Int? = 1,
    @SerializedName("limit") val limit: Int? = 20,
    @SerializedName("total_items") val totalItems: Int? = 0,
    @SerializedName("total_pages") val totalPages: Int? = 1
)

