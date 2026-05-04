package com.k.shavrin.diethelper.domain.usecase.foodentry

import com.k.shavrin.diethelper.domain.model.FoodEntry
import com.k.shavrin.diethelper.domain.repository.FoodEntryRepository
import javax.inject.Inject

class AddFoodEntryUseCase @Inject constructor(
    private val repository: FoodEntryRepository
) {
    suspend operator fun invoke(entry: FoodEntry): Long = repository.addEntry(entry)
}
