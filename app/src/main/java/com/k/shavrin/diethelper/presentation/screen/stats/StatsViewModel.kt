package com.k.shavrin.diethelper.presentation.screen.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.k.shavrin.diethelper.domain.usecase.stats.GetStatsRangeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val getStatsRangeUseCase: GetStatsRangeUseCase
) : ViewModel() {

    private val _from = MutableStateFlow(LocalDate.now().minusDays(6))
    private val _to = MutableStateFlow(LocalDate.now())

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState = combine(_from, _to) { from, to -> from to to }
        .flatMapLatest { (from, to) ->
            getStatsRangeUseCase(from, to).map { items ->
                StatsUiState.Success(
                    from = from,
                    to = to,
                    items = items,
                    totalCalories = items.sumOf { it.calories.toDouble() }.toFloat(),
                    totalProtein = items.sumOf { it.protein.toDouble() }.toFloat(),
                    totalFat = items.sumOf { it.fat.toDouble() }.toFloat(),
                    totalCarbs = items.sumOf { it.carbs.toDouble() }.toFloat()
                ) as StatsUiState
            }
        }
        .catch { e -> emit(StatsUiState.Error(e.message ?: "Ошибка загрузки")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = StatsUiState.Loading
        )

    fun setFrom(date: LocalDate) {
        if (date.isAfter(_to.value)) {
            _from.value = _to.value
            _to.value = date
        } else {
            _from.value = date
        }
    }

    fun setTo(date: LocalDate) {
        if (date.isBefore(_from.value)) {
            _to.value = _from.value
            _from.value = date
        } else {
            _to.value = date
        }
    }

    fun selectPreset(days: Int) {
        _to.value = LocalDate.now()
        _from.value = LocalDate.now().minusDays((days - 1).toLong())
    }

    fun selectCurrentMonth() {
        _to.value = LocalDate.now()
        _from.value = LocalDate.now().withDayOfMonth(1)
    }
}
