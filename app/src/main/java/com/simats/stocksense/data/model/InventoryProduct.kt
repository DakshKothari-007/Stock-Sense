package com.simats.stocksense.data.model

data class InventoryProduct(
    val id: String,
    val name: String,
    val sku: String,
    val price: String,
    val status: String?,
    val statusColorHex: Long = 0xFF98621A,
    val units: String,
    val iconType: Int // 0: Milk, 1: Cookie, 2: Box, 3: Tea
)
