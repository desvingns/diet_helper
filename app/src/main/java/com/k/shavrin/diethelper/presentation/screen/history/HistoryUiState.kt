package com.k.shavrin.diethelper.presentation.screen.history

import com.k.shavrin.diethelper.domain.model.HistoryItem

sealed interface HistoryUiState {
    data object Loading : HistoryUiState
    data class Success(val items: List<HistoryItem>) : HistoryUiState
    data class Error(val message: String) : HistoryUiState
}
