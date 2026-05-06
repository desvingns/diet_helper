package com.k.shavrin.diethelper.domain.usecase.savedmeal

import com.k.shavrin.diethelper.domain.model.SavedMeal
import com.k.shavrin.diethelper.domain.repository.SavedMealRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSavedMealsUseCase @Inject constructor(
    private val repository: SavedMealRepository
) {
    operator fun invoke(): Flow<List<SavedMeal>> = repository.getSavedMeals()
}
