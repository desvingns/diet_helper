package com.k.shavrin.diethelper.data.repository

import com.k.shavrin.diethelper.data.local.dao.SavedMealDao
import com.k.shavrin.diethelper.data.local.entity.SavedMealEntity
import com.k.shavrin.diethelper.data.local.entity.SavedMealItemEntity
import com.k.shavrin.diethelper.data.mapper.toDomain
import com.k.shavrin.diethelper.domain.model.SavedMeal
import com.k.shavrin.diethelper.domain.model.SavedMealItem
import com.k.shavrin.diethelper.domain.repository.SavedMealRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SavedMealRepositoryImpl @Inject constructor(
    private val dao: SavedMealDao
) : SavedMealRepository {

    override fun getSavedMeals(): Flow<List<SavedMeal>> =
        dao.getAllWithItems().map { list -> list.toDomain() }

    override suspend fun saveMeal(name: String, items: List<SavedMealItem>) {
        dao.deleteByName(name)
        val mealId = dao.insertMeal(SavedMealEntity(name = name))
        val entities = items.map { item ->
            SavedMealItemEntity(
                savedMealId = mealId,
                productId = item.productId,
                multiplier = item.multiplier
            )
        }
        dao.insertItems(entities)
    }

    override suspend fun deleteMeal(id: Long) {
        dao.deleteById(id)
    }
}
