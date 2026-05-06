package com.k.shavrin.diethelper.data

import com.k.shavrin.diethelper.domain.model.SavedMeal
import com.k.shavrin.diethelper.domain.model.SavedMealItem
import com.k.shavrin.diethelper.domain.repository.SavedMealRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeSavedMealRepository : SavedMealRepository {

    private val meals = MutableStateFlow<List<SavedMeal>>(emptyList())
    private var nextId = 1L

    override fun getSavedMeals(): Flow<List<SavedMeal>> =
        meals.map { list -> list.sortedBy { it.name.lowercase() } }

    override suspend fun saveMeal(name: String, items: List<SavedMealItem>) {
        meals.update { list -> list.filterNot { it.name.equals(name, ignoreCase = false) } }
        val id = nextId++
        meals.update { list ->
            list + SavedMeal(
                id = id,
                name = name,
                items = items.mapIndexed { index, item ->
                    item.copy(id = (nextId + index), savedMealId = id)
                }
            )
        }
    }

    override suspend fun deleteMeal(id: Long) {
        meals.update { list -> list.filterNot { it.id == id } }
    }
}
