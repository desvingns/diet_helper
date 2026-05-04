package com.k.shavrin.diethelper.domain.usecase.foodentry

import com.k.shavrin.diethelper.domain.model.HistoryItem
import com.k.shavrin.diethelper.domain.repository.FoodEntryRepository
import com.k.shavrin.diethelper.domain.usecase.foodentry.GetDailySummaryUseCase.Companion.toSummary
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetHistoryUseCase @Inject constructor(
    private val repository: FoodEntryRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<List<HistoryItem>> =
        repository.getDistinctDatesDescending().flatMapLatest { dates ->
            if (dates.isEmpty()) {
                flowOf(emptyList())
            } else {
                repository.getEntriesForDates(dates).map { entries ->
                    entries.groupBy { it.date }
                        .map { (date, list) ->
                            HistoryItem(date = date, totalCalories = list.toSummary().totalCalories)
                        }
                        .sortedByDescending { it.date }
                }
            }
        }
}
