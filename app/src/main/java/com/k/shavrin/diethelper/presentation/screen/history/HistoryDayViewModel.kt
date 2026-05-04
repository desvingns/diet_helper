package com.k.shavrin.diethelper.presentation.screen.history

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.k.shavrin.diethelper.domain.model.MealType
import com.k.shavrin.diethelper.domain.usecase.foodentry.GetDailySummaryUseCase.Companion.toSummary
import com.k.shavrin.diethelper.domain.usecase.foodentry.GetFoodEntriesForDayUseCase
import com.k.shavrin.diethelper.domain.usecase.goals.GetDailyGoalsUseCase
import com.k.shavrin.diethelper.presentation.navigation.Routes
import com.k.shavrin.diethelper.presentation.screen.today.TodayUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class HistoryDayViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getEntriesForDay: GetFoodEntriesForDayUseCase,
    getGoals: GetDailyGoalsUseCase
) : ViewModel() {

    private val date: LocalDate = LocalDate.parse(
        savedStateHandle.get<String>(Routes.ARG_DATE).orEmpty()
    )

    val uiState: StateFlow<TodayUiState> = combine(
        getEntriesForDay(date),
        getGoals()
    ) { entries, goals ->
        val sections = MealType.values().associateWith { type ->
            entries.filter { it.mealType == type }
        }
        val sectionCalories = sections.mapValues { (_, list) ->
            list.toSummary().totalCalories
        }
        TodayUiState.Success(
            date = date,
            canGoForward = false,
            sections = sections,
            sectionCalories = sectionCalories,
            summary = entries.toSummary(),
            goals = goals
        ) as TodayUiState
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = TodayUiState.Loading
    )
}
