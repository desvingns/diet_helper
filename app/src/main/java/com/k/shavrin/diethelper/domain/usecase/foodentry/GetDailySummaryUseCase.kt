package com.k.shavrin.diethelper.domain.usecase.foodentry

import com.k.shavrin.diethelper.domain.model.DailySummary
import com.k.shavrin.diethelper.domain.model.FoodEntry
import com.k.shavrin.diethelper.domain.repository.FoodEntryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class GetDailySummaryUseCase @Inject constructor(
    private val repository: FoodEntryRepository
) {
    operator fun invoke(date: LocalDate): Flow<DailySummary> =
        repository.getEntriesForDay(date).map { entries -> entries.toSummary() }

    companion object {
        fun List<FoodEntry>.toSummary(): DailySummary {
            var calories = 0f
            var protein = 0f
            var fat = 0f
            var carbs = 0f
            for (entry in this) {
                val m = entry.multiplier
                calories += entry.product.caloriesPer100g * m
                protein += entry.product.proteinPer100g * m
                fat += entry.product.fatPer100g * m
                carbs += entry.product.carbsPer100g * m
            }
            return DailySummary(calories, protein, fat, carbs)
        }
    }
}
