package com.k.shavrin.diethelper.data

import com.k.shavrin.diethelper.domain.model.WeightEntry
import com.k.shavrin.diethelper.domain.repository.WeightRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.time.LocalDate

class FakeWeightRepository : WeightRepository {

    private val entries = MutableStateFlow<List<WeightEntry>>(emptyList())
    private var nextId = 1L

    fun seed(initial: List<WeightEntry>) {
        entries.value = initial.mapIndexed { index, entry ->
            if (entry.id == 0L) entry.copy(id = (index + 1).toLong()) else entry
        }
        nextId = (entries.value.maxOfOrNull { it.id } ?: 0L) + 1
    }

    override fun getAllEntries(): Flow<List<WeightEntry>> = entries.map { list ->
        list.sortedByDescending { it.date }
    }

    override suspend fun getEntryByDate(date: LocalDate): WeightEntry? =
        entries.value.firstOrNull { it.date == date }

    override suspend fun upsertEntry(date: LocalDate, weightKg: Float) {
        val existing = entries.value.firstOrNull { it.date == date }
        if (existing != null) {
            entries.update { list ->
                list.map { if (it.id == existing.id) it.copy(weightKg = weightKg) else it }
            }
        } else {
            val id = nextId++
            entries.update { it + WeightEntry(id = id, date = date, weightKg = weightKg) }
        }
    }

    override suspend fun deleteEntry(entry: WeightEntry) {
        entries.update { list -> list.filterNot { it.id == entry.id } }
    }
}
