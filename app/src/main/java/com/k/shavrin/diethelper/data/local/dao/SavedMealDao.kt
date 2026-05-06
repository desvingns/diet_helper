package com.k.shavrin.diethelper.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.k.shavrin.diethelper.data.local.entity.SavedMealEntity
import com.k.shavrin.diethelper.data.local.entity.SavedMealItemEntity
import com.k.shavrin.diethelper.data.local.entity.SavedMealWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedMealDao {

    @Transaction
    @Query("SELECT * FROM saved_meals ORDER BY name COLLATE NOCASE ASC")
    fun getAllWithItems(): Flow<List<SavedMealWithItems>>

    @Query("SELECT * FROM saved_meals WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): SavedMealEntity?

    @Insert
    suspend fun insertMeal(entity: SavedMealEntity): Long

    @Insert
    suspend fun insertItems(items: List<SavedMealItemEntity>)

    @Query("DELETE FROM saved_meals WHERE name = :name")
    suspend fun deleteByName(name: String)

    @Query("DELETE FROM saved_meals WHERE id = :id")
    suspend fun deleteById(id: Long)
}
