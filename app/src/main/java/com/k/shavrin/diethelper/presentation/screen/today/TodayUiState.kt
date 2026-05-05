package com.k.shavrin.diethelper.presentation.screen.today

import com.k.shavrin.diethelper.domain.model.DailyGoals
import com.k.shavrin.diethelper.domain.model.DailySummary
import com.k.shavrin.diethelper.domain.model.FoodEntry
import com.k.shavrin.diethelper.domain.model.MealType
import java.time.LocalDate

sealed interface TodayUiState {
    data object Loading : TodayUiState

    data class Success(
        val date: LocalDate,
        val canGoForward: Boolean,
        val sections: Map<MealType, List<FoodEntry>>,
        val sectionCalories: Map<MealType, Float>,
        val sectionProtein: Map<MealType, Float>,
        val sectionFat: Map<MealType, Float>,
        val sectionCarbs: Map<MealType, Float>,
        val summary: DailySummary,
        val goals: DailyGoals
    ) : TodayUiState

    data class Error(val message: String) : TodayUiState
}
