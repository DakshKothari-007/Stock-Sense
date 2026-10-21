package com.simats.stocksense.feature.reorder.viewmodel

import androidx.lifecycle.ViewModel
import com.simats.stocksense.data.model.ReorderRecommendation
import com.simats.stocksense.data.repository.StockRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ReorderUiState(
    val recommendation: ReorderRecommendation = ReorderRecommendation(),
    val isAccepted: Boolean = false,
    val isDismissed: Boolean = false
)

class ReorderViewModel(
    private val repository: StockRepository = StockRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ReorderUiState(
            recommendation = repository.reorderRecommendation.value
        )
    )
    val uiState: StateFlow<ReorderUiState> = _uiState.asStateFlow()

    fun acceptReorder() {
        _uiState.update { it.copy(isAccepted = true) }
    }

    fun dismissReorder() {
        _uiState.update { it.copy(isDismissed = true) }
    }
}
