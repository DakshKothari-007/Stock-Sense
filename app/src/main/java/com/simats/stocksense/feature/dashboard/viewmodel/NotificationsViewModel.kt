package com.simats.stocksense.feature.dashboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.stocksense.core.network.NetworkClient
import com.simats.stocksense.data.model.NotificationItem
import com.simats.stocksense.data.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotificationsUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val shopName: String = "",
    val unreadCount: Int = 0,
    val totalCount: Int = 0,
    val notifications: List<NotificationItem> = emptyList(),
    val isSessionExpired: Boolean = false
)

/**
 * ViewModel managing notifications state, auto-refresh, and optimistic actions with rollback.
 * Adheres strictly to API_DOCUMENT.md Module 10.
 */
class NotificationsViewModel(
    private val repository: NotificationRepository = NotificationRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init {
        loadNotifications()
    }

    fun loadNotifications(isRefresh: Boolean = false) {
        viewModelScope.launch {
            val cachedShopName = NetworkClient.getSessionManager()?.getStoreProfile()?.name?.takeIf { it.isNotBlank() } ?: "Green Basket"
            if (isRefresh) {
                _uiState.update { it.copy(isRefreshing = true, errorMessage = null, shopName = cachedShopName) }
            } else if (_uiState.value.notifications.isEmpty()) {
                _uiState.update { it.copy(isLoading = true, errorMessage = null, shopName = cachedShopName) }
            } else {
                _uiState.update { it.copy(shopName = cachedShopName) }
            }

            val result = repository.fetchNotifications()
            result.fold(
                onSuccess = { data ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = null,
                            unreadCount = data.unreadCount ?: 0,
                            totalCount = data.totalCount ?: 0,
                            notifications = data.items ?: emptyList(),
                            shopName = cachedShopName
                        )
                    }
                },
                onFailure = { err ->
                    val isExpired = err.message?.contains("Session expired", ignoreCase = true) == true
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = err.message ?: "Failed to retrieve notifications.",
                            isSessionExpired = isExpired
                        )
                    }
                }
            )
        }
    }

    fun refresh() {
        loadNotifications(isRefresh = true)
    }

    fun markAsRead(key: String, onComplete: () -> Unit = {}) {
        if (key.isBlank()) {
            onComplete()
            return
        }

        val current = _uiState.value
        val target = current.notifications.find { it.key == key }
        if (target == null || target.isRead == true) {
            onComplete()
            return
        }

        val previousNotifications = current.notifications
        val previousUnreadCount = current.unreadCount

        // Optimistic UI update
        _uiState.update { state ->
            val updated = state.notifications.map { item ->
                if (item.key == key) item.copy(isRead = true) else item
            }
            state.copy(
                notifications = updated,
                unreadCount = maxOf(0, state.unreadCount - 1)
            )
        }

        viewModelScope.launch {
            val result = repository.markRead(key)
            result.fold(
                onSuccess = {
                    onComplete()
                },
                onFailure = { err ->
                    val isExpired = err.message?.contains("Session expired", ignoreCase = true) == true
                    // Roll back optimistic update
                    _uiState.update { state ->
                        state.copy(
                            notifications = previousNotifications,
                            unreadCount = previousUnreadCount,
                            errorMessage = err.message ?: "Failed to mark notification as read.",
                            isSessionExpired = isExpired
                        )
                    }
                    onComplete()
                }
            )
        }
    }

    fun markAllAsRead() {
        val current = _uiState.value
        if (current.unreadCount == 0) return

        val previousNotifications = current.notifications
        val previousUnreadCount = current.unreadCount

        // Optimistic UI update
        _uiState.update { state ->
            val updated = state.notifications.map { it.copy(isRead = true) }
            state.copy(
                notifications = updated,
                unreadCount = 0
            )
        }

        viewModelScope.launch {
            val result = repository.markAllRead()
            result.fold(
                onSuccess = { /* Keep optimistic state */ },
                onFailure = { err ->
                    val isExpired = err.message?.contains("Session expired", ignoreCase = true) == true
                    // Roll back optimistic update
                    _uiState.update { state ->
                        state.copy(
                            notifications = previousNotifications,
                            unreadCount = previousUnreadCount,
                            errorMessage = err.message ?: "Failed to mark all notifications as read.",
                            isSessionExpired = isExpired
                        )
                    }
                }
            )
        }
    }

    fun dismissNotification(key: String) {
        if (key.isBlank()) return

        val current = _uiState.value
        val itemToDismiss = current.notifications.find { it.key == key } ?: return
        val wasUnread = itemToDismiss.isRead != true

        val previousNotifications = current.notifications
        val previousUnreadCount = current.unreadCount
        val previousTotalCount = current.totalCount

        // Optimistic UI update
        _uiState.update { state ->
            val updated = state.notifications.filterNot { it.key == key }
            state.copy(
                notifications = updated,
                unreadCount = if (wasUnread) maxOf(0, state.unreadCount - 1) else state.unreadCount,
                totalCount = maxOf(0, state.totalCount - 1)
            )
        }

        viewModelScope.launch {
            val result = repository.dismiss(key)
            result.fold(
                onSuccess = { /* Keep optimistic removal */ },
                onFailure = { err ->
                    val isExpired = err.message?.contains("Session expired", ignoreCase = true) == true
                    // Roll back optimistic update
                    _uiState.update { state ->
                        state.copy(
                            notifications = previousNotifications,
                            unreadCount = previousUnreadCount,
                            totalCount = previousTotalCount,
                            errorMessage = err.message ?: "Failed to dismiss notification.",
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
}

