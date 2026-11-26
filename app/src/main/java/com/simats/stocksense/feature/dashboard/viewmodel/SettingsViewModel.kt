package com.simats.stocksense.feature.dashboard.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.stocksense.core.avatar.AvatarCode
import com.simats.stocksense.core.network.NetworkClient
import com.simats.stocksense.core.theme.ThemeMode
import com.simats.stocksense.core.theme.ThemeState
import com.simats.stocksense.data.model.SettingsData
import com.simats.stocksense.data.model.SettingsNotificationsData
import com.simats.stocksense.data.repository.InventoryRepository
import com.simats.stocksense.data.repository.SettingsRepository
import com.simats.stocksense.data.repository.UnauthorizedException
import com.simats.stocksense.feature.inventory.util.ExportCsvException
import com.simats.stocksense.feature.inventory.util.ProductCsvExportHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val settingsData: SettingsData? = null,
    val avatarCode: AvatarCode? = null,
    val isSessionExpired: Boolean = false,
    val isExportingCsv: Boolean = false,
    val exportErrorMessage: String? = null
)

/**
 * ViewModel managing Store & System Settings (API_DOCUMENT.md Module 9).
 * Supports optimistic updates with automatic rollback on error.
 */
class SettingsViewModel(
    private val repository: SettingsRepository = SettingsRepository(),
    private val inventoryRepository: InventoryRepository = InventoryRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            avatarCode = NetworkClient.getSessionManager()?.getAvatar()
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            NetworkClient.getSessionManager()?.avatarState?.collect { avatar ->
                _uiState.update { it.copy(avatarCode = avatar) }
            }
        }
        loadSettings()
    }

    fun loadSettings(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
            } else if (_uiState.value.settingsData == null) {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            }

            val result = repository.fetchSettings()
            result.fold(
                onSuccess = { data ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = null,
                            settingsData = data
                        )
                    }
                },
                onFailure = { err ->
                    val isExpired = err.message?.contains("Session expired", ignoreCase = true) == true
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = err.message ?: "Failed to retrieve settings.",
                            isSessionExpired = isExpired
                        )
                    }
                }
            )
        }
    }

    fun updateThemeMode(newMode: ThemeMode) {
        val previousMode = ThemeState.themeMode.value
        ThemeState.setThemeMode(newMode)

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val result = repository.updateSettings(mapOf("theme_mode" to newMode.name))
            result.fold(
                onSuccess = { updatedData ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            settingsData = updatedData,
                            errorMessage = null
                        )
                    }
                },
                onFailure = { err ->
                    // Revert theme state back to previous mode on failure
                    ThemeState.setThemeMode(previousMode)
                    val isExpired = err.message?.contains("Session expired", ignoreCase = true) == true
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = err.message ?: "Failed to update theme mode.",
                            isSessionExpired = isExpired
                        )
                    }
                }
            )
        }
    }

    fun updateNotificationSetting(key: String, value: Boolean) {
        val currentSettings = _uiState.value.settingsData ?: return
        val currentNotifications = currentSettings.notifications ?: SettingsNotificationsData()

        val previousValue = when (key) {
            "low_stock_alerts" -> currentNotifications.lowStockAlerts ?: true
            "ai_recommendations" -> currentNotifications.aiRecommendations ?: true
            "daily_sales_summary" -> currentNotifications.dailySalesSummary ?: true
            "weekly_email_digest" -> currentNotifications.weeklyEmailDigest ?: false
            else -> false
        }

        // Optimistically apply update
        val updatedNotifications = when (key) {
            "low_stock_alerts" -> currentNotifications.copy(lowStockAlerts = value)
            "ai_recommendations" -> currentNotifications.copy(aiRecommendations = value)
            "daily_sales_summary" -> currentNotifications.copy(dailySalesSummary = value)
            "weekly_email_digest" -> currentNotifications.copy(weeklyEmailDigest = value)
            else -> currentNotifications
        }

        _uiState.update {
            it.copy(
                isSaving = true,
                settingsData = currentSettings.copy(notifications = updatedNotifications)
            )
        }

        viewModelScope.launch {
            val result = repository.updateSettings(mapOf(key to value))
            result.fold(
                onSuccess = { updatedData ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            settingsData = updatedData,
                            errorMessage = null
                        )
                    }
                },
                onFailure = { err ->
                    // Rollback control to previous value on failure
                    val revertedNotifications = when (key) {
                        "low_stock_alerts" -> updatedNotifications.copy(lowStockAlerts = previousValue)
                        "ai_recommendations" -> updatedNotifications.copy(aiRecommendations = previousValue)
                        "daily_sales_summary" -> updatedNotifications.copy(dailySalesSummary = previousValue)
                        "weekly_email_digest" -> updatedNotifications.copy(weeklyEmailDigest = previousValue)
                        else -> updatedNotifications
                    }
                    val isExpired = err.message?.contains("Session expired", ignoreCase = true) == true
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            settingsData = currentSettings.copy(notifications = revertedNotifications),
                            errorMessage = err.message ?: "Failed to update notification setting.",
                            isSessionExpired = isExpired
                        )
                    }
                }
            )
        }
    }

    fun resetDefaults() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val result = repository.resetDefaults()
            result.fold(
                onSuccess = { updatedData ->
                    ThemeState.setThemeMode(ThemeMode.SYSTEM)
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            settingsData = updatedData,
                            errorMessage = null
                        )
                    }
                },
                onFailure = { err ->
                    val isExpired = err.message?.contains("Session expired", ignoreCase = true) == true
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = err.message ?: "Failed to reset settings to defaults.",
                            isSessionExpired = isExpired
                        )
                    }
                }
            )
        }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun exportProductsCsv(context: Context) {
        if (_uiState.value.isExportingCsv) return

        _uiState.update { it.copy(isExportingCsv = true, exportErrorMessage = null) }
        val appContext = context.applicationContext

        viewModelScope.launch {
            val result = ProductCsvExportHelper.downloadAndShare(appContext, inventoryRepository)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isExportingCsv = false, exportErrorMessage = null) }
                },
                onFailure = { err ->
                    val expired = err is UnauthorizedException ||
                        (err as? ExportCsvException)?.error?.isSessionExpired == true
                    val mapped = ProductCsvExportHelper.mapExportError(throwable = err)
                    _uiState.update {
                        it.copy(
                            isExportingCsv = false,
                            isSessionExpired = it.isSessionExpired || expired,
                            exportErrorMessage = if (expired) null else mapped.message
                        )
                    }
                }
            )
        }
    }
}

