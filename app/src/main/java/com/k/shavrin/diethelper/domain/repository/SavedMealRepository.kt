package com.k.shavrin.diethelper.domain.repository

import com.k.shavrin.diethelper.domain.model.SavedMeal
import com.k.shavrin.diethelper.domain.model.SavedMealItem
import kotlinx.coroutines.flow.Flow

interface SavedMealRepository {
    fun getSavedMeals(): Flow<List<SavedMeal>>
    suspend fun saveMeal(name: String, items: List<SavedMealItem>)
    suspend fun deleteMeal(id: Long)
}
