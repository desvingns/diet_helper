package com.k.shavrin.diethelper.domain.usecase.savedmeal

import com.k.shavrin.diethelper.domain.model.FoodEntry
import com.k.shavrin.diethelper.domain.model.MealType
import com.k.shavrin.diethelper.domain.model.SavedMeal
import com.k.shavrin.diethelper.domain.usecase.foodentry.AddFoodEntryUseCase
import java.time.LocalDate
import javax.inject.Inject

class AddSavedMealEntriesUseCase @Inject constructor(
    private val addFoodEntryUseCase: AddFoodEntryUseCase
) {
    suspend operator fun invoke(meal: SavedMeal, targetDate: LocalDate, targetMealType: MealType) {
        meal.items.forEach { item ->
            addFoodEntryUseCase(
                FoodEntry(
                    id = 0,
                    productId = item.productId,
                    product = item.product,
                    date = targetDate,
                    mealType = targetMealType,
                    multiplier = item.multiplier
                )
            )
        }
    }
}
