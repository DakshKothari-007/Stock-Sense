package com.simats.stocksense.feature.dashboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.stocksense.core.network.NetworkClient
import com.simats.stocksense.core.network.SessionManager
import com.simats.stocksense.data.model.*
import com.simats.stocksense.data.repository.ProfileRepository
import com.simats.stocksense.data.repository.StockRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MainUiState(
    val selectedTab: Int = 0,
    val searchQuery: String = "",
    val selectedFilter: Int = 0, // 0: All, 1: Low stock, 2: Slow moving
    val products: List<InventoryProduct> = emptyList(),
    val filteredProducts: List<InventoryProduct> = emptyList(),
    val salesSummary: SalesSummary = SalesSummary(),
    val dailySalesPoints: List<DailySalesPoint> = emptyList(),
    val storeProfile: StoreProfile = StoreProfile(),
    val isProfileLoading: Boolean = false,
    val profileErrorMessage: String? = null
)

class MainViewModel(
    private val repository: StockRepository = StockRepository(),
    private val profileRepository: ProfileRepository = ProfileRepository(),
    private val sessionManager: SessionManager? = NetworkClient.getSessionManager()
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        MainUiState(
            products = repository.inventoryProducts.value,
            filteredProducts = repository.inventoryProducts.value,
            salesSummary = repository.salesSummary.value,
            dailySalesPoints = repository.dailySalesPoints.value,
            storeProfile = sessionManager?.getStoreProfile() ?: repository.storeProfile.value
        )
    )
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        refreshProfile()
    }

    /**
     * Synchronizes profile from database via GET auth/profile.php.
     */
    fun refreshProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isProfileLoading = true, profileErrorMessage = null) }
            val result = profileRepository.fetchProfile()
            result.fold(
                onSuccess = { profile ->
                    _uiState.update {
                        it.copy(
                            storeProfile = profile,
                            isProfileLoading = false,
                            profileErrorMessage = null
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isProfileLoading = false,
                            profileErrorMessage = err.message ?: "Failed to load profile. Please try again."
                        )
                    }
                }
            )
        }
    }

    /**
     * Clears all cached user data in the ViewModel so subsequent logins start with a clean state.
     */
    fun clearUserData() {
        _uiState.update {
            it.copy(
                storeProfile = StoreProfile(),
                isProfileLoading = false,
                profileErrorMessage = null
            )
        }
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
        if (index == 3) {
            refreshProfile()
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { currentState ->
            val filtered = filterProducts(currentState.products, query, currentState.selectedFilter)
            currentState.copy(searchQuery = query, filteredProducts = filtered)
        }
    }

    fun selectFilter(filterIndex: Int) {
        _uiState.update { currentState ->
            val filtered = filterProducts(currentState.products, currentState.searchQuery, filterIndex)
            currentState.copy(selectedFilter = filterIndex, filteredProducts = filtered)
        }
    }

    /**
     * Updates shop name optimistically and persists to database via POST auth/update_shop.php.
     */
    fun updateShopName(newName: String, onResult: (Boolean, String?) -> Unit = { _, _ -> }) {
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) {
            onResult(false, "Shop name cannot be empty.")
            return
        }

        // Optimistic update
        val previousName = _uiState.value.storeProfile.name
        _uiState.update { currentState ->
            val updated = currentState.storeProfile.copy(name = trimmed)
            currentState.copy(storeProfile = updated)
        }

        viewModelScope.launch {
            val result = profileRepository.updateShopName(trimmed)
            result.fold(
                onSuccess = { updatedProfile ->
                    _uiState.update { it.copy(storeProfile = updatedProfile) }
                    onResult(true, "Shop name updated successfully.")
                },
                onFailure = { err ->
                    // Revert on failure
                    _uiState.update { currentState ->
                        val reverted = currentState.storeProfile.copy(name = previousName)
                        currentState.copy(storeProfile = reverted)
                    }
                    onResult(false, err.message ?: "Failed to update shop name.")
                }
            )
        }
    }

    /**
     * Updates user full name optimistically and persists to database.
     */
    fun updateUserName(newUserName: String) {
        val currentEmail = _uiState.value.storeProfile.email
        updateUserProfile(newUserName, currentEmail)
    }

    /**
     * Updates user email optimistically and persists to database.
     */
    fun updateEmail(newEmail: String) {
        val currentName = _uiState.value.storeProfile.ownerName
        updateUserProfile(currentName, newEmail)
    }

    /**
     * Updates user details (full name & email) and syncs with MySQL DB via POST auth/update_profile.php.
     */
    fun updateUserProfile(
        newUserName: String,
        newEmail: String,
        onResult: (Boolean, String?) -> Unit = { _, _ -> }
    ) {
        val trimmedName = newUserName.trim()
        val trimmedEmail = newEmail.trim()

        if (trimmedName.isEmpty() || trimmedEmail.isEmpty()) {
            onResult(false, "Name and email cannot be empty.")
            return
        }

        val prevProfile = _uiState.value.storeProfile
        _uiState.update { currentState ->
            val updated = currentState.storeProfile.copy(ownerName = trimmedName, email = trimmedEmail)
            currentState.copy(storeProfile = updated)
        }

        viewModelScope.launch {
            val result = profileRepository.updateUserProfile(trimmedName, trimmedEmail)
            result.fold(
                onSuccess = { updatedProfile ->
                    _uiState.update { it.copy(storeProfile = updatedProfile) }
                    onResult(true, "Profile details updated successfully.")
                },
                onFailure = { err ->
                    _uiState.update { it.copy(storeProfile = prevProfile) }
                    onResult(false, err.message ?: "Failed to update profile.")
                }
            )
        }
    }

    /**
     * Changes password via POST auth/change_password.php.
     */
    fun changePassword(
        currentPass: String,
        newPass: String,
        confirmPass: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val result = profileRepository.changePassword(currentPass, newPass, confirmPass)
            result.fold(
                onSuccess = { msg ->
                    onResult(true, msg)
                },
                onFailure = { err ->
                    onResult(false, err.message ?: "Failed to change password.")
                }
            )
        }
    }

    /**
     * Logs out the user: calls backend to revoke token, clears SharedPreferences session,
     * and calls onComplete to trigger navigation.
     */
    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            clearUserData()
            profileRepository.logout()
            onComplete()
        }
    }

    private fun filterProducts(
        allProducts: List<InventoryProduct>,
        query: String,
        filter: Int
    ): List<InventoryProduct> {
        return allProducts.filter { item ->
            val matchesSearch = query.isEmpty() ||
                    item.name.contains(query, ignoreCase = true) ||
                    item.sku.contains(query, ignoreCase = true)

            val matchesFilter = when (filter) {
                1 -> item.status?.contains("Low stock", ignoreCase = true) == true
                2 -> item.status?.contains("Slow moving", ignoreCase = true) == true
                else -> true
            }

            matchesSearch && matchesFilter
        }
    }
}
