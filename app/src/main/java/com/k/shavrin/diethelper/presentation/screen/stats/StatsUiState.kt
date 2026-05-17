package com.k.shavrin.diethelper.presentation.screen.stats

import com.k.shavrin.diethelper.domain.model.StatsDayItem
import java.time.LocalDate

sealed interface StatsUiState {
    data object Loading : StatsUiState

    data class Success(
        val from: LocalDate,
        val to: LocalDate,
        val items: List<StatsDayItem>,
        val totalCalories: Float,
        val totalProtein: Float,
        val totalFat: Float,
        val totalCarbs: Float
    ) : StatsUiState

    data class Error(val message: String) : StatsUiState
}
