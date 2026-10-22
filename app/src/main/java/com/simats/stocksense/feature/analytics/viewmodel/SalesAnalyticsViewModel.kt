package com.simats.stocksense.feature.analytics.viewmodel

import androidx.lifecycle.ViewModel
import com.simats.stocksense.data.model.*
import com.simats.stocksense.data.repository.StockRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SalesAnalyticsUiState(
    val selectedPeriodIndex: Int = 1, // 0: Today, 1: 7 days, 2: 30 days
    val periodHeaderSubtitle: String = "Green Basket · 9–15 Oct 2026",
    val sales7DaysTotal: String = "₹1,20,750",
    val todaySalesTotal: String = "₹18,450",
    val todayBillsText: String = "96 bills · Avg ₹192.19",
    val sales30DaysTotal: String = "₹4,85,200",
    val sales30DaysBillsText: String = "2,840 bills · Avg ₹170.84",
    val activeSalesTotal: String = "₹1,20,750",
    val chartTitle: String = "Daily sales (7 days)",
    val chartSubtitle: String = "9–15 Oct 2026 · Daily sales in ₹",
    val dailySalesPoints: List<DailySalesPoint> = emptyList(),
    val highestDayLabel: String = "Highest · 13 Oct",
    val highestDayAmount: String = "₹22,377",
    val categoryBreakdowns: List<CategoryBreakdown> = emptyList(),
    val bestSellers: List<BestSellerItem> = emptyList()
)

class SalesAnalyticsViewModel(
    private val repository: StockRepository = StockRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(getUiStateForPeriod(1))
    val uiState: StateFlow<SalesAnalyticsUiState> = _uiState.asStateFlow()

    fun selectPeriod(index: Int) {
        _uiState.value = getUiStateForPeriod(index)
    }

    private fun getUiStateForPeriod(index: Int): SalesAnalyticsUiState {
        return when (index) {
            0 -> SalesAnalyticsUiState(
                selectedPeriodIndex = 0,
                periodHeaderSubtitle = "Green Basket · Today, 15 Oct 2026",
                sales7DaysTotal = repository.salesSummary.value.weeklySalesTotal,
                todaySalesTotal = repository.salesSummary.value.todaySalesAmount,
                todayBillsText = "96 bills · Avg ₹192.19",
                sales30DaysTotal = "₹4,85,200",
                activeSalesTotal = "₹18,450",
                chartTitle = "Hourly sales (Today)",
                chartSubtitle = "15 Oct 2026 · Today's hourly sales in ₹",
                dailySalesPoints = listOf(
                    DailySalesPoint("9 AM", 0.30f),
                    DailySalesPoint("11 AM", 0.55f),
                    DailySalesPoint("1 PM", 0.90f, isCurrentDay = true),
                    DailySalesPoint("3 PM", 0.65f),
                    DailySalesPoint("5 PM", 0.80f),
                    DailySalesPoint("7 PM", 0.70f),
                    DailySalesPoint("9 PM", 0.40f)
                ),
                highestDayLabel = "Peak hour · 1 PM–2 PM",
                highestDayAmount = "₹4,250",
                categoryBreakdowns = listOf(
                    CategoryBreakdown("Dairy", 0.34f, "₹6,273", "34%"),
                    CategoryBreakdown("Staples", 0.26f, "₹4,797", "26%"),
                    CategoryBreakdown("Snacks", 0.21f, "₹3,874", "21%"),
                    CategoryBreakdown("Personal care", 0.12f, "₹2,214", "12%"),
                    CategoryBreakdown("Beverages", 0.07f, "₹1,292", "7%")
                ),
                bestSellers = listOf(
                    BestSellerItem("Amul Milk · 24 units", "₹1,440"),
                    BestSellerItem("Britannia Marie · 18 units", "₹540"),
                    BestSellerItem("Parle-G · 35 units", "₹350")
                )
            )

            1 -> SalesAnalyticsUiState(
                selectedPeriodIndex = 1,
                periodHeaderSubtitle = "Green Basket · 9–15 Oct 2026",
                sales7DaysTotal = repository.salesSummary.value.weeklySalesTotal,
                todaySalesTotal = repository.salesSummary.value.todaySalesAmount,
                todayBillsText = "96 bills · Avg ₹192.19",
                sales30DaysTotal = "₹4,85,200",
                activeSalesTotal = "₹1,20,750",
                chartTitle = "Daily sales (7 days)",
                chartSubtitle = "9–15 Oct 2026 · Daily sales in ₹",
                dailySalesPoints = repository.dailySalesPoints.value,
                highestDayLabel = "Highest · 13 Oct",
                highestDayAmount = "₹22,377",
                categoryBreakdowns = repository.categoryBreakdowns.value,
                bestSellers = repository.bestSellers.value
            )

            2 -> SalesAnalyticsUiState(
                selectedPeriodIndex = 2,
                periodHeaderSubtitle = "Green Basket · Sep 16 – Oct 15 2026 (30 days)",
                sales7DaysTotal = repository.salesSummary.value.weeklySalesTotal,
                todaySalesTotal = repository.salesSummary.value.todaySalesAmount,
                todayBillsText = "96 bills · Avg ₹192.19",
                sales30DaysTotal = "₹4,85,200",
                activeSalesTotal = "₹4,85,200",
                chartTitle = "30-day sales overview",
                chartSubtitle = "Sep 16 – Oct 15 2026 · Weekly sales in ₹",
                dailySalesPoints = listOf(
                    DailySalesPoint("W1", 0.65f),
                    DailySalesPoint("W2", 0.90f),
                    DailySalesPoint("W3", 0.75f),
                    DailySalesPoint("W4", 0.85f, isCurrentDay = true)
                ),
                highestDayLabel = "Peak week · Week 2",
                highestDayAmount = "₹1,32,400",
                categoryBreakdowns = listOf(
                    CategoryBreakdown("Dairy", 0.32f, "₹1,55,264", "32%"),
                    CategoryBreakdown("Staples", 0.28f, "₹1,35,856", "28%"),
                    CategoryBreakdown("Snacks", 0.22f, "₹1,06,744", "22%"),
                    CategoryBreakdown("Personal care", 0.12f, "₹58,224", "12%"),
                    CategoryBreakdown("Beverages", 0.06f, "₹29,112", "6%")
                ),
                bestSellers = listOf(
                    BestSellerItem("Aashirvaad Atta · 192 units", "₹49,920"),
                    BestSellerItem("Fortune Oil · 288 units", "₹40,320"),
                    BestSellerItem("Amul Paneer · 240 units", "₹21,600"),
                    BestSellerItem("Britannia Marie · 120 units", "₹3,600")
                )
            )

            else -> SalesAnalyticsUiState()
        }
    }
}
