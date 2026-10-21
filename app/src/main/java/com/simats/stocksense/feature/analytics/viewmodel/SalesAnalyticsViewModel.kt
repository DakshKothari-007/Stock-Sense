package com.simats.stocksense.feature.analytics.viewmodel

import androidx.lifecycle.ViewModel
import com.simats.stocksense.data.model.*
import com.simats.stocksense.data.repository.StockRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SalesAnalyticsUiState(
    val selectedPeriodIndex: Int = 1, // 0: Today, 1: 7 days, 2: 30 days
    val sales7DaysTotal: String = "₹1,20,750",
    val todaySalesTotal: String = "₹18,450",
    val todayBillsText: String = "96 bills · Avg ₹192.19",
    val dailySalesPoints: List<DailySalesPoint> = emptyList(),
    val highestDayLabel: String = "Highest · 13 Oct",
    val highestDayAmount: String = "₹22,377",
    val categoryBreakdowns: List<CategoryBreakdown> = emptyList(),
    val bestSellers: List<BestSellerItem> = emptyList()
)

class SalesAnalyticsViewModel(
    private val repository: StockRepository = StockRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SalesAnalyticsUiState(
            sales7DaysTotal = repository.salesSummary.value.weeklySalesTotal,
            todaySalesTotal = repository.salesSummary.value.todaySalesAmount,
            dailySalesPoints = repository.dailySalesPoints.value,
            categoryBreakdowns = repository.categoryBreakdowns.value,
            bestSellers = repository.bestSellers.value
        )
    )
    val uiState: StateFlow<SalesAnalyticsUiState> = _uiState.asStateFlow()

    fun selectPeriod(index: Int) {
        _uiState.update { currentState ->
            currentState.copy(selectedPeriodIndex = index)
        }
    }
}
