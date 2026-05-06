package com.k.shavrin.diethelper.presentation.screen.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.k.shavrin.diethelper.domain.model.FoodEntry
import com.k.shavrin.diethelper.domain.model.MealType
import com.k.shavrin.diethelper.domain.model.SavedMealItem
import com.k.shavrin.diethelper.domain.usecase.foodentry.AddFoodEntryUseCase
import com.k.shavrin.diethelper.domain.usecase.foodentry.CopyFoodEntryToDayUseCase
import com.k.shavrin.diethelper.domain.usecase.foodentry.DeleteFoodEntryUseCase
import com.k.shavrin.diethelper.domain.usecase.foodentry.GetDailySummaryUseCase.Companion.toSummary
import com.k.shavrin.diethelper.domain.usecase.foodentry.GetFoodEntriesForDayUseCase
import com.k.shavrin.diethelper.domain.usecase.foodentry.GetStreakUseCase
import com.k.shavrin.diethelper.domain.usecase.foodentry.GetWeekDayStatusesUseCase
import com.k.shavrin.diethelper.domain.usecase.foodentry.UpdateFoodEntryUseCase
import com.k.shavrin.diethelper.domain.usecase.goals.GetDailyGoalsUseCase
import com.k.shavrin.diethelper.domain.usecase.savedmeal.SaveMealUseCase
import com.k.shavrin.diethelper.presentation.util.ClipboardSnapshot
import com.k.shavrin.diethelper.presentation.util.InMemoryMealClipboard
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val getEntriesForDay: GetFoodEntriesForDayUseCase,
    private val getGoals: GetDailyGoalsUseCase,
    private val updateEntryUseCase: UpdateFoodEntryUseCase,
    private val deleteEntryUseCase: DeleteFoodEntryUseCase,
    private val copyEntryUseCase: CopyFoodEntryToDayUseCase,
    private val getWeekDayStatuses: GetWeekDayStatusesUseCase,
    private val getStreak: GetStreakUseCase,
    private val addFoodEntryUseCase: AddFoodEntryUseCase,
    private val saveMealUseCase: SaveMealUseCase,
    private val clipboard: InMemoryMealClipboard
) : ViewModel() {

    private val _currentDate = MutableStateFlow(LocalDate.now())
    val currentDate: StateFlow<LocalDate> = _currentDate.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<TodayUiState> = _currentDate.flatMapLatest { date ->
        combine(
            getEntriesForDay(date),
            getGoals(),
            getWeekDayStatuses(date),
            getStreak(),
            clipboard.state
        ) { entries, goals, weekStatuses, streak, clipboardSnapshot ->
            val sections = MealType.entries.associateWith { type ->
                entries.filter { it.mealType == type }
            }
            val sectionCalories = sections.mapValues { (_, list) ->
                list.toSummary().totalCalories
            }
            val sectionProtein = sections.mapValues { (_, list) ->
                list.sumOf { (it.product.proteinPer100g * it.multiplier).toDouble() }.toFloat()
            }
            val sectionFat = sections.mapValues { (_, list) ->
                list.sumOf { (it.product.fatPer100g * it.multiplier).toDouble() }.toFloat()
            }
            val sectionCarbs = sections.mapValues { (_, list) ->
                list.sumOf { (it.product.carbsPer100g * it.multiplier).toDouble() }.toFloat()
            }
            TodayUiState.Success(
                date = date,
                sections = sections,
                sectionCalories = sectionCalories,
                sectionProtein = sectionProtein,
                sectionFat = sectionFat,
                sectionCarbs = sectionCarbs,
                summary = entries.toSummary(),
                goals = goals,
                weekStatuses = weekStatuses,
                streak = streak,
                clipboard = clipboardSnapshot
            ) as TodayUiState
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = TodayUiState.Loading
    )

    fun goToDate(date: LocalDate) {
        if (!date.isAfter(LocalDate.now())) {
            _currentDate.value = date
        }
    }

    fun goToToday() {
        _currentDate.value = LocalDate.now()
    }

    fun goToPreviousDay() {
        _currentDate.value = _currentDate.value.minusDays(1)
    }

    fun goToNextDay() {
        val candidate = _currentDate.value.plusDays(1)
        if (!candidate.isAfter(LocalDate.now())) {
            _currentDate.value = candidate
        }
    }

    fun updateMultiplier(entry: FoodEntry, newMultiplier: Float) {
        viewModelScope.launch {
            updateEntryUseCase(entry.copy(multiplier = newMultiplier))
        }
    }

    fun deleteEntry(entry: FoodEntry) {
        viewModelScope.launch { deleteEntryUseCase(entry) }
    }

    fun copyEntryToDay(entry: FoodEntry, targetDate: LocalDate) {
        viewModelScope.launch { copyEntryUseCase(entry, targetDate) }
    }

    fun copyMeal(mealType: MealType) {
        val state = uiState.value as? TodayUiState.Success ?: return
        val entries = state.sections[mealType] ?: return
        if (entries.isEmpty()) return
        clipboard.copy(ClipboardSnapshot(entries, mealType, state.date))
    }

    fun pasteMeal(targetMealType: MealType) {
        val snapshot = clipboard.state.value ?: return
        viewModelScope.launch {
            snapshot.entries.forEach { entry ->
                addFoodEntryUseCase(
                    entry.copy(id = 0, date = _currentDate.value, mealType = targetMealType)
                )
            }
        }
    }

    fun clearClipboard() {
        clipboard.clear()
    }

    fun saveMeal(name: String, mealType: MealType) {
        val state = uiState.value as? TodayUiState.Success ?: return
        val entries = state.sections[mealType] ?: return
        viewModelScope.launch {
            saveMealUseCase(
                name,
                entries.map { entry ->
                    SavedMealItem(
                        savedMealId = 0,
                        productId = entry.productId,
                        product = entry.product,
                        multiplier = entry.multiplier
                    )
                }
            )
        }
    }
}
