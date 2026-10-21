package com.simats.stocksense.data.repository

import com.simats.stocksense.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class StockRepository {

    private val _inventoryProducts = MutableStateFlow(
        listOf(
            InventoryProduct(
                id = "1",
                name = "Amul Milk · 500 ml",
                sku = "DAI-001",
                price = "₹30",
                status = "Low stock · Minimum 24",
                statusColorHex = 0xFF98621A,
                units = "8",
                iconType = 0
            ),
            InventoryProduct(
                id = "2",
                name = "Britannia Marie · 150 g",
                sku = "SNK-014",
                price = "₹30",
                status = "Slow moving · 6 sold in 30 days",
                statusColorHex = 0xFF98621A,
                units = "96",
                iconType = 1
            ),
            InventoryProduct(
                id = "3",
                name = "Aashirvaad Atta - 5 kg",
                sku = "STP-003",
                price = "₹260",
                status = null,
                statusColorHex = 0x00000000,
                units = "24",
                iconType = 2
            ),
            InventoryProduct(
                id = "4",
                name = "Tata Tea - 250 g",
                sku = "BEV-008",
                price = "₹120",
                status = null,
                statusColorHex = 0x00000000,
                units = "42",
                iconType = 3
            ),
            InventoryProduct(
                id = "5",
                name = "Parle-G · 80 g",
                sku = "SNK-002",
                price = "₹10",
                status = null,
                statusColorHex = 0x00000000,
                units = "120",
                iconType = 1
            )
        )
    )
    val inventoryProducts: StateFlow<List<InventoryProduct>> = _inventoryProducts.asStateFlow()

    private val _salesSummary = MutableStateFlow(SalesSummary())
    val salesSummary: StateFlow<SalesSummary> = _salesSummary.asStateFlow()

    private val _dailySalesPoints = MutableStateFlow(
        listOf(
            DailySalesPoint("9", 0.45f),
            DailySalesPoint("10", 0.52f),
            DailySalesPoint("11", 0.45f),
            DailySalesPoint("12", 0.60f),
            DailySalesPoint("13", 0.85f),
            DailySalesPoint("14", 0.55f),
            DailySalesPoint("15", 0.70f, isCurrentDay = true)
        )
    )
    val dailySalesPoints: StateFlow<List<DailySalesPoint>> = _dailySalesPoints.asStateFlow()

    private val _categoryBreakdowns = MutableStateFlow(
        listOf(
            CategoryBreakdown("Dairy", 0.32f, "₹38,640", "32%"),
            CategoryBreakdown("Staples", 0.28f, "₹33,810", "28%"),
            CategoryBreakdown("Snacks", 0.22f, "₹26,565", "22%"),
            CategoryBreakdown("Personal care", 0.12f, "₹14,490", "12%"),
            CategoryBreakdown("Beverages", 0.06f, "₹7,245", "6%")
        )
    )
    val categoryBreakdowns: StateFlow<List<CategoryBreakdown>> = _categoryBreakdowns.asStateFlow()

    private val _bestSellers = MutableStateFlow(
        listOf(
            BestSellerItem("Aashirvaad Atta · 48 units", "₹12,480"),
            BestSellerItem("Fortune Oil · 72 units", "₹10,080"),
            BestSellerItem("Amul Paneer · 60 units", "₹5,400")
        )
    )
    val bestSellers: StateFlow<List<BestSellerItem>> = _bestSellers.asStateFlow()

    private val _reorderRecommendation = MutableStateFlow(ReorderRecommendation())
    val reorderRecommendation: StateFlow<ReorderRecommendation> = _reorderRecommendation.asStateFlow()

    private val _storeProfile = MutableStateFlow(StoreProfile())
    val storeProfile: StateFlow<StoreProfile> = _storeProfile.asStateFlow()
}
