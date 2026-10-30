package com.simats.stocksense.feature.dashboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.stocksense.core.util.NumberFormatUtil
import com.simats.stocksense.data.model.DashboardSummaryData
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
    val lastUpdatedText: String = ""
)

/**
 * ViewModel responsible for loading Dashboard metrics and notifications unread count.
 */
class DashboardViewModel(
    private val repository: DashboardRepository = DashboardRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboard(isInitialLoad = true)
    }

    /**
     * Loads summary metrics and unread notifications count in parallel.
     */
    fun loadDashboard(isInitialLoad: Boolean = false) {
        viewModelScope.launch {
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
        }
    }

    /**
     * Manual refresh triggered by pull-to-refresh or retry.
     */
    fun refresh() {
        loadDashboard(isInitialLoad = false)
    }
}

