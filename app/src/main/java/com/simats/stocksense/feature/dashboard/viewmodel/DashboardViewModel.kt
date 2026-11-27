package com.simats.stocksense.feature.dashboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.stocksense.core.util.NumberFormatUtil
import com.simats.stocksense.data.model.DashboardSummaryData
import com.simats.stocksense.data.repository.AiRepository
import com.simats.stocksense.data.repository.DashboardRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date

/**
 * UI State for the Dashboard screen.
 */
data class DashboardUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val dashboardData: DashboardSummaryData? = null,
    val unreadCount: Int = 0,
    val lastUpdatedText: String = "",
    val dailySummary: String? = null
)

/**
 * ViewModel responsible for loading Dashboard metrics and notifications unread count.
 */
class DashboardViewModel(
    private val repository: DashboardRepository = DashboardRepository(),
    private val aiRepository: AiRepository = AiRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var lastLoadedAtMs: Long = 0L
    private var isLoadInFlight: Boolean = false
    private val cacheTtlMs: Long = 30_000L

    init {
        loadDashboard(isInitialLoad = true)
        refreshDailySummary()
    }

    /**
     * Loads summary metrics and unread notifications count in parallel.
     */
    fun loadDashboard(isInitialLoad: Boolean = false) {
        viewModelScope.launch {
            isLoadInFlight = true
            try {
                if (isInitialLoad && _uiState.value.dashboardData == null) {
                    _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                } else {
                    _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
                }

                val summaryDeferred = async { repository.fetchDashboardSummary() }
                val unreadDeferred = async { repository.fetchUnreadCount() }

                val summaryResult = summaryDeferred.await()
                val unreadResult = unreadDeferred.await()

                val unreadCount = unreadResult.getOrDefault(0)

                summaryResult.fold(
                    onSuccess = { data ->
                        lastLoadedAtMs = System.currentTimeMillis()
                        val updatedTime = NumberFormatUtil.formatLastUpdatedTime(Date())
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                errorMessage = null,
                                dashboardData = data,
                                unreadCount = unreadCount,
                                lastUpdatedText = updatedTime
                            )
                        }
                    },
                    onFailure = { err ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                errorMessage = err.message ?: "Failed to load dashboard data. Please check your connection and retry.",
                                unreadCount = unreadCount
                            )
                        }
                    }
                )
            } finally {
                isLoadInFlight = false
            }
        }
    }

    /**
     * Refreshes dashboard data. Automatic resume/tab-switch calls use [force] = false
     * so cached data within TTL is reused without redundant network calls.
     */
    fun refresh(force: Boolean = false) {
        val invalidated = DashboardRepository.consumeStaleFlag()
        val hasCachedData = _uiState.value.dashboardData != null
        val isFresh = System.currentTimeMillis() - lastLoadedAtMs < cacheTtlMs
        if (!force && !invalidated && (isLoadInFlight || (hasCachedData && isFresh))) {
            return
        }
        loadDashboard(isInitialLoad = false)
        refreshDailySummary()
    }

    /**
     * Refreshes the daily summary on resume.
     */
    fun refreshDailySummary() {
        viewModelScope.launch {
            val res = aiRepository.getDailySummary()
            _uiState.update { current ->
                current.copy(dailySummary = res.getOrNull())
            }
        }
    }
}

