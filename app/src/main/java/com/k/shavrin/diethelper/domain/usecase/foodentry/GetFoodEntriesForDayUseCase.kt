package com.k.shavrin.diethelper.domain.usecase.foodentry

import com.k.shavrin.diethelper.domain.model.FoodEntry
import com.k.shavrin.diethelper.domain.repository.FoodEntryRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class GetFoodEntriesForDayUseCase @Inject constructor(
    private val repository: FoodEntryRepository
) {
    operator fun invoke(date: LocalDate): Flow<List<FoodEntry>> =
        repository.getEntriesForDay(date)
}
