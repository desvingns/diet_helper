package com.k.shavrin.diethelper.domain.usecase.foodentry

import com.k.shavrin.diethelper.domain.model.FoodEntry
import com.k.shavrin.diethelper.domain.repository.FoodEntryRepository
import java.time.LocalDate
import javax.inject.Inject

class CopyFoodEntryToDayUseCase @Inject constructor(
    private val repository: FoodEntryRepository
) {
    suspend operator fun invoke(entry: FoodEntry, targetDate: LocalDate) =
        repository.copyEntryToDay(entry, targetDate)
}
