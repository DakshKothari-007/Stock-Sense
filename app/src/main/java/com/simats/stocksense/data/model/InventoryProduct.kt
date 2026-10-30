package com.simats.stocksense.data.model

/**
 * Model representing an inventory item on the Inventory screen and product detail flows.
 */
data class InventoryProduct(
    val id: String = "",
    val name: String = "",
    val sku: String = "",
    val price: String = "",
    val status: String? = null,
    val statusColorHex: Long = 0xFF98621A,
    val units: String = "0",
    val iconType: Int = 2, // 0: Milk, 1: Cookie, 2: Box, 3: Tea
    val category: String? = null,
    val minStockThreshold: Int = 10,
    val costPrice: String? = null,
    val rawPrice: Double = 0.0,
    val rawUnits: Int = 0,
    val description: String? = null
)
