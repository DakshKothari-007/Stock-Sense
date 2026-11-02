package com.simats.stocksense.feature.analytics.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.stocksense.core.network.NetworkClient
import com.simats.stocksense.core.util.NumberFormatUtil
import com.simats.stocksense.data.model.*
import com.simats.stocksense.data.repository.SalesRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SalesAnalyticsUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val selectedPeriodIndex: Int = 1, // 0: Today, 1: 7 days, 2: 30 days
    val periodHeaderSubtitle: String = "—",
    val sales7DaysTotal: String = "₹0",
    val todaySalesTotal: String = "₹0",
    val todayBillsText: String = "0 bills · Avg ₹0",
    val sales30DaysTotal: String = "₹0",
    val sales30DaysBillsText: String = "0 bills · Avg ₹0",
    val activeSalesTotal: String = "₹0",
    val chartTitle: String = "Daily sales (7 days)",
    val chartSubtitle: String = "Daily sales in ₹",
    val dailySalesPoints: List<DailySalesPoint> = emptyList(),
    val highestDayLabel: String = "Highest · —",
    val highestDayAmount: String = "₹0",
    val categoryBreakdowns: List<CategoryBreakdown> = emptyList(),
    val bestSellers: List<BestSellerItem> = emptyList(),
    val bills: List<SaleBillDto> = emptyList(),
    val isBillsLoading: Boolean = false,
    val billsPage: Int = 1,
    val billsTotalPages: Int = 1
)

class SalesAnalyticsViewModel(
    private val repository: SalesRepository = SalesRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SalesAnalyticsUiState(isLoading = true))
    val uiState: StateFlow<SalesAnalyticsUiState> = _uiState.asStateFlow()

    private var cachedTodayData: SalesSummaryData? = null
    private var cached7DaysData: SalesSummaryData? = null
    private var cached30DaysData: SalesSummaryData? = null

    init {
        refresh()
    }

    /**
     * Refreshes summary data for Today, 7 days, and 30 days from real backend API.
     */
    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = cached7DaysData == null && cachedTodayData == null && cached30DaysData == null,
                isRefreshing = true,
                errorMessage = null
            )

            try {
                // Fetch all 3 periods in parallel to populate the top summary cards and active period
                val todayDeferred = async { repository.fetchSalesSummary(period = "today") }
                val days7Deferred = async { repository.fetchSalesSummary(period = "7_days") }
                val days30Deferred = async { repository.fetchSalesSummary(period = "30_days") }

                val todayResult = todayDeferred.await()
                val days7Result = days7Deferred.await()
                val days30Result = days30Deferred.await()

                if (todayResult.isSuccess || days7Result.isSuccess || days30Result.isSuccess) {
                    cachedTodayData = todayResult.getOrNull() ?: cachedTodayData
                    cached7DaysData = days7Result.getOrNull() ?: cached7DaysData
                    cached30DaysData = days30Result.getOrNull() ?: cached30DaysData

                    applyCurrentPeriodState(_uiState.value.selectedPeriodIndex)
                } else {
                    val errorMsg = days7Result.exceptionOrNull()?.message
                        ?: todayResult.exceptionOrNull()?.message
                        ?: days30Result.exceptionOrNull()?.message
                        ?: "Failed to load sales analytics."
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = errorMsg
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    errorMessage = e.message ?: "Network error. Please try again."
                )
            }
        }
    }

    /**
     * User switches period tab: 0 = Today, 1 = 7 days, 2 = 30 days.
     */
    fun selectPeriod(index: Int) {
        if (_uiState.value.selectedPeriodIndex == index && cachedTodayData != null) return
        _uiState.value = _uiState.value.copy(selectedPeriodIndex = index)
        applyCurrentPeriodState(index)
    }

    private fun applyCurrentPeriodState(selectedIndex: Int) {
        val today = cachedTodayData
        val days7 = cached7DaysData
        val days30 = cached30DaysData

        val shopName = NetworkClient.getSessionManager()?.getStoreProfile()?.name?.takeIf { it.isNotBlank() }
            ?: "Store"

        // Format top card summaries
        val todayTotalText = NumberFormatUtil.formatIndianRupees(today?.totalSalesAmount ?: 0.0)
        val todayBillsCount = today?.totalBillsCount ?: 0
        val todayAvgBill = NumberFormatUtil.formatIndianRupees(today?.averageBillAmount ?: 0.0)
        val todayBillsText = "${NumberFormatUtil.formatIndianNumber(todayBillsCount)} bills · Avg $todayAvgBill"

        val sales7DaysTotal = NumberFormatUtil.formatIndianRupees(days7?.totalSalesAmount ?: 0.0)

        val sales30DaysTotal = NumberFormatUtil.formatIndianRupees(days30?.totalSalesAmount ?: 0.0)
        val sales30BillsCount = days30?.totalBillsCount ?: 0
        val sales30AvgBill = NumberFormatUtil.formatIndianRupees(days30?.averageBillAmount ?: 0.0)
        val sales30DaysBillsText = "${NumberFormatUtil.formatIndianNumber(sales30BillsCount)} bills · Avg $sales30AvgBill"

        val dateFormat = SimpleDateFormat("d MMM yyyy", Locale.ENGLISH)
        val shortDateFormat = SimpleDateFormat("d MMM", Locale.ENGLISH)
        val now = Date()

        when (selectedIndex) {
            0 -> {
                // Period: TODAY
                val data = today
                val headerSubtitle = "$shopName · Today, ${dateFormat.format(now)}"
                val activeTotal = todayTotalText

                val hourly = data?.hourlySalesToday
                val hourlyPoints = hourly?.hourlyPoints.orEmpty()

                val chartPoints: List<DailySalesPoint>
                val peakLabel: String
                val peakAmount: String

                if (hourlyPoints.isNotEmpty()) {
                    val maxAmount = hourlyPoints.maxOfOrNull { it.amount ?: 0.0 } ?: 0.0
                    chartPoints = hourlyPoints.map { hp ->
                        val ratio = if (maxAmount > 0.0) ((hp.amount ?: 0.0) / maxAmount).toFloat() else 0.06f
                        DailySalesPoint(
                            dayLabel = hp.label ?: "—",
                            heightRatio = ratio.coerceIn(0.06f, 1f),
                            isCurrentDay = false
                        )
                    }
                    peakLabel = if (!hourly?.peakHour.isNullOrBlank()) "Peak hour · ${hourly?.peakHour}" else "Peak hour · —"
                    peakAmount = NumberFormatUtil.formatIndianRupees(hourly?.peakAmount ?: 0.0)
                } else {
                    // Fallback if no hourly distribution available
                    val dp = data?.dailySalesPoints?.firstOrNull()
                    chartPoints = if (dp != null) {
                        listOf(
                            DailySalesPoint(
                                dayLabel = dp.dayLabel ?: shortDateFormat.format(now),
                                heightRatio = if ((dp.salesAmount ?: 0.0) > 0.0) 0.85f else 0.06f,
                                isCurrentDay = true
                            )
                        )
                    } else {
                        emptyList()
                    }
                    peakLabel = "Peak hour · —"
                    peakAmount = "₹0"
                }

                val categories = mapCategories(data?.categoryBreakdown)
                val bestSellers = mapBestSellers(data?.topSellingProducts)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    errorMessage = null,
                    selectedPeriodIndex = 0,
                    periodHeaderSubtitle = headerSubtitle,
                    todaySalesTotal = todayTotalText,
                    todayBillsText = todayBillsText,
                    sales7DaysTotal = sales7DaysTotal,
                    sales30DaysTotal = sales30DaysTotal,
                    sales30DaysBillsText = sales30DaysBillsText,
                    activeSalesTotal = activeTotal,
                    chartTitle = "Hourly sales (Today)",
                    chartSubtitle = "${dateFormat.format(now)} · Today's hourly sales in ₹",
                    dailySalesPoints = chartPoints,
                    highestDayLabel = peakLabel,
                    highestDayAmount = peakAmount,
                    categoryBreakdowns = categories,
                    bestSellers = bestSellers
                )
            }

            1 -> {
                // Period: 7 DAYS
                val data = days7
                val rawPoints = data?.dailySalesPoints.orEmpty()

                val fromDateStr = formatRangeDate(data?.dateRange?.from, fallbackDaysAgo = 6)
                val toDateStr = formatRangeDate(data?.dateRange?.to, fallbackDaysAgo = 0)
                val headerSubtitle = "$shopName · $fromDateStr – $toDateStr"

                val maxDailyAmount = rawPoints.maxOfOrNull { it.salesAmount ?: 0.0 } ?: 0.0
                val highestPoint = rawPoints.maxByOrNull { it.salesAmount ?: 0.0 }

                val chartPoints = rawPoints.map { dp ->
                    val ratio = if (maxDailyAmount > 0.0) ((dp.salesAmount ?: 0.0) / maxDailyAmount).toFloat() else 0.06f
                    DailySalesPoint(
                        dayLabel = dp.dayLabel?.takeIf { it.isNotBlank() } ?: "—",
                        heightRatio = ratio.coerceIn(0.06f, 1f),
                        isCurrentDay = dp.isCurrentDay == true
                    )
                }

                val highestDayLabel = if (highestPoint != null && (highestPoint.salesAmount ?: 0.0) > 0.0) {
                    "Highest · ${highestPoint.dayLabel ?: "—"}"
                } else {
                    "Highest · —"
                }
                val highestDayAmount = NumberFormatUtil.formatIndianRupees(highestPoint?.salesAmount ?: 0.0)

                val categories = mapCategories(data?.categoryBreakdown)
                val bestSellers = mapBestSellers(data?.topSellingProducts)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    errorMessage = null,
                    selectedPeriodIndex = 1,
                    periodHeaderSubtitle = headerSubtitle,
                    todaySalesTotal = todayTotalText,
                    todayBillsText = todayBillsText,
                    sales7DaysTotal = sales7DaysTotal,
                    sales30DaysTotal = sales30DaysTotal,
                    sales30DaysBillsText = sales30DaysBillsText,
                    activeSalesTotal = sales7DaysTotal,
                    chartTitle = "Daily sales (7 days)",
                    chartSubtitle = "$fromDateStr – $toDateStr · Daily sales in ₹",
                    dailySalesPoints = chartPoints,
                    highestDayLabel = highestDayLabel,
                    highestDayAmount = highestDayAmount,
                    categoryBreakdowns = categories,
                    bestSellers = bestSellers
                )
            }

            2 -> {
                // Period: 30 DAYS
                val data = days30
                val rawPoints = data?.dailySalesPoints.orEmpty()

                val fromDateStr = formatRangeDate(data?.dateRange?.from, fallbackDaysAgo = 29)
                val toDateStr = formatRangeDate(data?.dateRange?.to, fallbackDaysAgo = 0)
                val headerSubtitle = "$shopName · $fromDateStr – $toDateStr (30 days)"

                // Group 30 daily points into 4 weekly buckets for clean chart rendering
                val chartPoints: List<DailySalesPoint>
                val peakLabel: String
                val peakAmount: String

                if (rawPoints.size >= 14) {
                    val chunkSize = (rawPoints.size + 3) / 4
                    val weeks = rawPoints.chunked(chunkSize)
                    val weekTotals = weeks.map { wk -> wk.sumOf { it.salesAmount ?: 0.0 } }
                    val maxWeek = weekTotals.maxOrNull() ?: 0.0
                    val peakIdx = weekTotals.indexOf(maxWeek)

                    chartPoints = weeks.mapIndexed { idx, _ ->
                        val wAmt = weekTotals.getOrElse(idx) { 0.0 }
                        val ratio = if (maxWeek > 0.0) (wAmt / maxWeek).toFloat() else 0.06f
                        DailySalesPoint(
                            dayLabel = "W${idx + 1}",
                            heightRatio = ratio.coerceIn(0.06f, 1f),
                            isCurrentDay = (idx == weeks.size - 1)
                        )
                    }
                    peakLabel = if (maxWeek > 0.0) "Peak week · Week ${peakIdx + 1}" else "Peak week · —"
                    peakAmount = NumberFormatUtil.formatIndianRupees(maxWeek)
                } else if (rawPoints.isNotEmpty()) {
                    val maxAmt = rawPoints.maxOfOrNull { it.salesAmount ?: 0.0 } ?: 0.0
                    val highestPoint = rawPoints.maxByOrNull { it.salesAmount ?: 0.0 }
                    chartPoints = rawPoints.map { dp ->
                        val ratio = if (maxAmt > 0.0) ((dp.salesAmount ?: 0.0) / maxAmt).toFloat() else 0.06f
                        DailySalesPoint(
                            dayLabel = dp.dayLabel ?: "—",
                            heightRatio = ratio.coerceIn(0.06f, 1f),
                            isCurrentDay = dp.isCurrentDay == true
                        )
                    }
                    peakLabel = if (highestPoint != null && (highestPoint.salesAmount ?: 0.0) > 0.0) {
                        "Highest · ${highestPoint.dayLabel ?: "—"}"
                    } else {
                        "Highest · —"
                    }
                    peakAmount = NumberFormatUtil.formatIndianRupees(highestPoint?.salesAmount ?: 0.0)
                } else {
                    chartPoints = emptyList()
                    peakLabel = "Peak week · —"
                    peakAmount = "₹0"
                }

                val categories = mapCategories(data?.categoryBreakdown)
                val bestSellers = mapBestSellers(data?.topSellingProducts)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    errorMessage = null,
                    selectedPeriodIndex = 2,
                    periodHeaderSubtitle = headerSubtitle,
                    todaySalesTotal = todayTotalText,
                    todayBillsText = todayBillsText,
                    sales7DaysTotal = sales7DaysTotal,
                    sales30DaysTotal = sales30DaysTotal,
                    sales30DaysBillsText = sales30DaysBillsText,
                    activeSalesTotal = sales30DaysTotal,
                    chartTitle = "30-day sales overview",
                    chartSubtitle = "$fromDateStr – $toDateStr · Weekly sales in ₹",
                    dailySalesPoints = chartPoints,
                    highestDayLabel = peakLabel,
                    highestDayAmount = peakAmount,
                    categoryBreakdowns = categories,
                    bestSellers = bestSellers
                )
            }
        }
    }

    private fun mapCategories(raw: List<CategoryBreakdownDto>?): List<CategoryBreakdown> {
        if (raw.isNullOrEmpty()) return emptyList()
        return raw.map { cat ->
            val pctRaw = cat.percentage ?: 0.0
            val pctFloat = (pctRaw / 100.0).toFloat().coerceIn(0f, 1f)
            CategoryBreakdown(
                name = cat.category?.takeIf { it.isNotBlank() } ?: "—",
                percentage = pctFloat,
                amountText = NumberFormatUtil.formatIndianRupees(cat.revenue ?: 0.0),
                percentLabel = "${String.format(Locale.US, "%.0f", pctRaw)}%"
            )
        }
    }

    private fun mapBestSellers(raw: List<TopSellingProductDto>?): List<BestSellerItem> {
        if (raw.isNullOrEmpty()) return emptyList()
        return raw.map { item ->
            val name = item.name?.takeIf { it.isNotBlank() } ?: "—"
            val units = item.unitsSold ?: 0
            BestSellerItem(
                nameWithUnits = "$name · ${NumberFormatUtil.formatIndianNumber(units)} units",
                revenueText = NumberFormatUtil.formatIndianRupees(item.revenue ?: 0.0)
            )
        }
    }

    private fun formatRangeDate(rawYmd: String?, fallbackDaysAgo: Int): String {
        return try {
            if (!rawYmd.isNullOrBlank()) {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                val parsed = inputFormat.parse(rawYmd)
                if (parsed != null) {
                    val outFormat = SimpleDateFormat("d MMM", Locale.ENGLISH)
                    return outFormat.format(parsed)
                }
            }
            val cal = java.util.Calendar.getInstance()
            cal.add(java.util.Calendar.DAY_OF_YEAR, -fallbackDaysAgo)
            SimpleDateFormat("d MMM", Locale.ENGLISH).format(cal.time)
        } catch (e: Exception) {
            rawYmd ?: "—"
        }
    }

    /**
     * Paginated bills list fetching.
     */
    fun loadBills(page: Int = 1) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isBillsLoading = true)
            val result = repository.fetchSalesList(page = page, limit = 20)
            result.onSuccess { data ->
                val newBills = if (page == 1) {
                    data.bills.orEmpty()
                } else {
                    _uiState.value.bills + data.bills.orEmpty()
                }
                _uiState.value = _uiState.value.copy(
                    isBillsLoading = false,
                    bills = newBills,
                    billsPage = page,
                    billsTotalPages = data.pagination?.totalPages ?: 1
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(isBillsLoading = false)
            }
        }
    }

    /**
     * Load next page of bills on scroll.
     */
    fun loadMoreBills() {
        val current = _uiState.value
        if (!current.isBillsLoading && current.billsPage < current.billsTotalPages) {
            loadBills(current.billsPage + 1)
        }
    }

    /**
     * Record a new sale bill with atomic stock reduction.
     */
    fun recordSale(
        request: CreateSaleRequest,
        onSuccess: (CreateSaleData) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = repository.createSale(request)
            result.onSuccess { data ->
                refresh()
                onSuccess(data)
            }.onFailure { error ->
                onError(error.message ?: "Failed to record sale.")
            }
        }
    }
}
