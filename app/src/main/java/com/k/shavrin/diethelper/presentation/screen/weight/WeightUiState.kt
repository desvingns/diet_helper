package com.k.shavrin.diethelper.presentation.screen.weight

import com.k.shavrin.diethelper.domain.model.WeightEntry

data class WeightEntryWithDelta(
    val entry: WeightEntry,
    val deltaKg: Float?
)

sealed interface WeightUiState {
    data object Loading : WeightUiState
    data class Success(val items: List<WeightEntryWithDelta>) : WeightUiState
    data class Error(val message: String) : WeightUiState
}
