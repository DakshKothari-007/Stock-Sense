package com.simats.stocksense.data.model

import com.google.gson.annotations.SerializedName

/**
 * Inventory network response and request models strictly adhering to API_DOCUMENT.md.
 */
data class InventoryProductsResponse(
    @SerializedName("status") val status: Boolean = false,
    @SerializedName("message") val message: String = "",
    @SerializedName("data") val data: InventoryProductsData? = null
)

data class InventoryProductsData(
    @SerializedName("shop_name") val shopName: String? = null,
    @SerializedName("total_products") val totalProducts: Int? = 0,
    @SerializedName("total_count") val totalCount: Int? = 0,
    @SerializedName("page") val page: Int? = 1,
    @SerializedName("limit") val limit: Int? = 0,
    @SerializedName("filter") val filter: String? = null,
    @SerializedName("category") val category: String? = null,
    @SerializedName("search") val search: String? = null,
    @SerializedName("products") val products: List<NetworkInventoryProduct>? = emptyList()
)

data class NetworkInventoryProduct(
    @SerializedName("id") val id: Any? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("sku") val sku: String? = null,
    @SerializedName("category") val category: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("price") val price: Any? = null,
    @SerializedName("raw_price") val rawPrice: Double? = null,
    @SerializedName("cost_price") val costPrice: Any? = null,
    @SerializedName("raw_cost_price") val rawCostPrice: Double? = null,
    @SerializedName("units") val units: Any? = null,
    @SerializedName("raw_units") val rawUnits: Int? = null,
    @SerializedName("min_stock_threshold") val minStockThreshold: Int? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("statusColorHex") val statusColorHex: Long? = null,
    @SerializedName("iconType") val iconType: Int? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null
)

data class AddProductRequest(
    @SerializedName("name") val name: String,
    @SerializedName("sku") val sku: String,
    @SerializedName("category") val category: String? = "General",
    @SerializedName("description") val description: String? = "",
    @SerializedName("price") val price: Double,
    @SerializedName("cost_price") val costPrice: Double = 0.0,
    @SerializedName("units") val units: Int,
    @SerializedName("min_stock_threshold") val minStockThreshold: Int = 10,
    @SerializedName("status") val status: String? = null,
    @SerializedName("icon_type") val iconType: Int = 2
)

data class UpdateProductRequest(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("sku") val sku: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("new_sku") val newSku: String? = null,
    @SerializedName("category") val category: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("price") val price: Double? = null,
    @SerializedName("cost_price") val costPrice: Double? = null,
    @SerializedName("units") val units: Int? = null,
    @SerializedName("min_stock_threshold") val minStockThreshold: Int? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("icon_type") val iconType: Int? = null
)

data class DeleteProductRequest(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("sku") val sku: String? = null
)

data class InventoryActionResponse(
    @SerializedName("status") val status: Boolean = false,
    @SerializedName("message") val message: String = "",
    @SerializedName("data") val data: Any? = null
)

data class ProductDetailResponse(
    @SerializedName("status") val status: Boolean = false,
    @SerializedName("message") val message: String = "",
    @SerializedName("data") val data: ProductDetailData? = null
)

data class ProductDetailData(
    @SerializedName("id") val id: Any? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("sku") val sku: String? = null,
    @SerializedName("category") val category: String? = null,
    @SerializedName("categoryName") val categoryName: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("price") val price: Any? = null,
    @SerializedName("raw_price") val rawPrice: Double? = null,
    @SerializedName("cost_price") val costPrice: Any? = null,
    @SerializedName("raw_cost_price") val rawCostPrice: Double? = null,
    @SerializedName("units") val units: Any? = null,
    @SerializedName("raw_units") val rawUnits: Int? = null,
    @SerializedName("min_stock_threshold") val minStockThreshold: Int? = null,
    @SerializedName("minimumStockThreshold") val minimumStockThreshold: Int? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("statusColorHex") val statusColorHex: Long? = null,
    @SerializedName("iconType") val iconType: Int? = null,
    @SerializedName("stock_cover_days") val stockCoverDays: Double? = null,
    @SerializedName("unitsToReorder") val unitsToReorder: Int? = null,
    @SerializedName("estimatedCost") val estimatedCost: Any? = null,
    @SerializedName("product") val product: ProductDetailData? = null
)

