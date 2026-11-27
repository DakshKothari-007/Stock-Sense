package com.simats.stocksense.feature.offers.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.stocksense.data.model.BundleOfferDto
import com.simats.stocksense.data.repository.OffersRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OffersUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val activeOffers: List<BundleOfferDto> = emptyList(),
    val endedOffers: List<BundleOfferDto> = emptyList(),
    val endingOfferId: Int? = null
)

class OffersViewModel(
    private val repository: OffersRepository = OffersRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(OffersUiState())
    val uiState: StateFlow<OffersUiState> = _uiState.asStateFlow()

    private var lastLoadedAtMs: Long = 0L
    private var isLoadInFlight: Boolean = false
    private val cacheTtlMs: Long = 30_000L

    init {
        refresh(force = true)
    }

    fun refresh(force: Boolean = false) {
        val hasCached = _uiState.value.activeOffers.isNotEmpty() || _uiState.value.endedOffers.isNotEmpty()
        val isFresh = System.currentTimeMillis() - lastLoadedAtMs < cacheTtlMs
        if (!force && (isLoadInFlight || (hasCached && isFresh))) {
            return
        }

        viewModelScope.launch {
            isLoadInFlight = true
            try {
                val isInitial = !hasCached
                _uiState.update {
                    it.copy(
                        isLoading = isInitial,
                        isRefreshing = !isInitial,
                        errorMessage = null
                    )
                }

                val result = repository.getOffers()
                result.fold(
                    onSuccess = { data ->
                        lastLoadedAtMs = System.currentTimeMillis()
                        val active = data.offers.filter { it.status.equals("active", ignoreCase = true) }
                        val ended = data.offers.filter { !it.status.equals("active", ignoreCase = true) }
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                errorMessage = null,
                                activeOffers = active,
                                endedOffers = ended
                            )
                        }
                    },
                    onFailure = { err ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                errorMessage = err.message ?: "Failed to load bundle offers."
                            )
                        }
                    }
                )
            } finally {
                isLoadInFlight = false
            }
        }
    }

    fun endOffer(
        offerId: Int,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (_uiState.value.endingOfferId != null) return

        viewModelScope.launch {
            _uiState.update { it.copy(endingOfferId = offerId) }
            val result = repository.endOffer(offerId)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(endingOfferId = null) }
                    refresh(force = true)
                    onSuccess()
                },
                onFailure = { err ->
                    _uiState.update { it.copy(endingOfferId = null) }
                    onError(err.message ?: "Failed to end offer.")
                }
            )
        }
    }
}
