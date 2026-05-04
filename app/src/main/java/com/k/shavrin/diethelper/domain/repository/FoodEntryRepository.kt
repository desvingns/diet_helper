package com.k.shavrin.diethelper.domain.repository

import com.k.shavrin.diethelper.domain.model.FoodEntry
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface FoodEntryRepository {
    fun getEntriesForDay(date: LocalDate): Flow<List<FoodEntry>>
    fun getDistinctDatesDescending(): Flow<List<LocalDate>>
    fun getEntriesForDates(dates: List<LocalDate>): Flow<List<FoodEntry>>
    suspend fun addEntry(entry: FoodEntry): Long
    suspend fun updateEntry(entry: FoodEntry)
    suspend fun deleteEntry(entry: FoodEntry)
    suspend fun copyEntryToDay(entry: FoodEntry, targetDate: LocalDate)
}
