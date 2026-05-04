package com.k.shavrin.diethelper.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.k.shavrin.diethelper.data.local.entity.FoodEntryEntity
import com.k.shavrin.diethelper.data.local.entity.FoodEntryWithProduct
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface FoodEntryDao {

    @Transaction
    @Query("SELECT * FROM food_entries WHERE date = :date")
    fun getEntriesForDate(date: LocalDate): Flow<List<FoodEntryWithProduct>>

    @Transaction
    @Query("SELECT * FROM food_entries WHERE date IN (:dates)")
    fun getEntriesForDates(dates: List<LocalDate>): Flow<List<FoodEntryWithProduct>>

    @Query("SELECT DISTINCT date FROM food_entries ORDER BY date DESC")
    fun getDistinctDatesDescending(): Flow<List<LocalDate>>

    @Insert
    suspend fun insertEntry(entry: FoodEntryEntity): Long

    @Update
    suspend fun updateEntry(entry: FoodEntryEntity)

    @Delete
    suspend fun deleteEntry(entry: FoodEntryEntity)
}
