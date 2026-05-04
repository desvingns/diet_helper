package com.k.shavrin.diethelper.data

import com.k.shavrin.diethelper.domain.model.FoodEntry
import com.k.shavrin.diethelper.domain.repository.FoodEntryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.time.LocalDate

class FakeFoodEntryRepository : FoodEntryRepository {

    private val entries = MutableStateFlow<List<FoodEntry>>(emptyList())
    private var nextId = 1L

    fun seed(initial: List<FoodEntry>) {
        entries.value = initial.mapIndexed { index, entry ->
            if (entry.id == 0L) entry.copy(id = (index + 1).toLong()) else entry
        }
        nextId = (entries.value.maxOfOrNull { it.id } ?: 0L) + 1
    }

    override fun getEntriesForDay(date: LocalDate): Flow<List<FoodEntry>> = entries.map { list ->
        list.filter { it.date == date }
    }

    override fun getDistinctDatesDescending(): Flow<List<LocalDate>> = entries.map { list ->
        list.map { it.date }.distinct().sortedDescending()
    }

    override fun getEntriesForDates(dates: List<LocalDate>): Flow<List<FoodEntry>> = entries.map { list ->
        list.filter { it.date in dates }
    }

    override suspend fun addEntry(entry: FoodEntry): Long {
        val id = nextId++
        entries.update { it + entry.copy(id = id) }
        return id
    }

    override suspend fun updateEntry(entry: FoodEntry) {
        entries.update { list -> list.map { if (it.id == entry.id) entry else it } }
    }

    override suspend fun deleteEntry(entry: FoodEntry) {
        entries.update { list -> list.filterNot { it.id == entry.id } }
    }

    override suspend fun copyEntryToDay(entry: FoodEntry, targetDate: LocalDate) {
        addEntry(entry.copy(id = 0L, date = targetDate))
    }
}
