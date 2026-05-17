package com.k.shavrin.diethelper.domain.usecase.stats

import com.k.shavrin.diethelper.domain.model.StatsDayItem
import com.k.shavrin.diethelper.domain.repository.FoodEntryRepository
import com.k.shavrin.diethelper.domain.usecase.foodentry.GetDailySummaryUseCase.Companion.toSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class GetStatsRangeUseCase @Inject constructor(
    private val repository: FoodEntryRepository
) {
    operator fun invoke(from: LocalDate, to: LocalDate): Flow<List<StatsDayItem>> {
        val start = if (from.isAfter(to)) to else from
        val end = if (from.isAfter(to)) from else to
        val dates = generateSequence(start) { current ->
            if (current.isBefore(end)) current.plusDays(1) else null
        }.toList()

        if (dates.isEmpty()) return flowOf(emptyList())

        return repository.getEntriesForDates(dates).map { entries ->
            entries
                .groupBy { it.date }
                .map { (date, list) ->
                    val summary = list.toSummary()
                    StatsDayItem(
                        date = date,
                        calories = summary.totalCalories,
                        protein = summary.totalProtein,
                        fat = summary.totalFat,
                        carbs = summary.totalCarbs
                    )
                }
                .filter { it.protein + it.fat + it.carbs > 0f }
                .sortedBy { it.date }
        }
    }
}
