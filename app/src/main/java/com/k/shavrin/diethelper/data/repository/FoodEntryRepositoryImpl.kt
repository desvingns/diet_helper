package com.k.shavrin.diethelper.data.repository

import com.k.shavrin.diethelper.data.local.dao.FoodEntryDao
import com.k.shavrin.diethelper.data.mapper.toDomain
import com.k.shavrin.diethelper.data.mapper.toEntity
import com.k.shavrin.diethelper.domain.model.FoodEntry
import com.k.shavrin.diethelper.domain.repository.FoodEntryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class FoodEntryRepositoryImpl @Inject constructor(
    private val foodEntryDao: FoodEntryDao
) : FoodEntryRepository {

    override fun getEntriesForDay(date: LocalDate): Flow<List<FoodEntry>> =
        foodEntryDao.getEntriesForDate(date).map { rows -> rows.toDomain() }

    override fun getDistinctDatesDescending(): Flow<List<LocalDate>> =
        foodEntryDao.getDistinctDatesDescending()

    override fun getEntriesForDates(dates: List<LocalDate>): Flow<List<FoodEntry>> =
        foodEntryDao.getEntriesForDates(dates).map { rows -> rows.toDomain() }

    override suspend fun addEntry(entry: FoodEntry): Long =
        foodEntryDao.insertEntry(entry.toEntity().copy(id = 0))

    override suspend fun updateEntry(entry: FoodEntry) {
        foodEntryDao.updateEntry(entry.toEntity())
    }

    override suspend fun deleteEntry(entry: FoodEntry) {
        foodEntryDao.deleteEntry(entry.toEntity())
    }

    override suspend fun copyEntryToDay(entry: FoodEntry, targetDate: LocalDate) {
        foodEntryDao.insertEntry(
            entry.toEntity().copy(id = 0, date = targetDate)
        )
    }
}
