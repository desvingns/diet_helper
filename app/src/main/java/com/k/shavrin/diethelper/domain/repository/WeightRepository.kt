package com.k.shavrin.diethelper.domain.repository

import com.k.shavrin.diethelper.domain.model.WeightEntry
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface WeightRepository {
    fun getAllEntries(): Flow<List<WeightEntry>>
    suspend fun getEntryByDate(date: LocalDate): WeightEntry?
    suspend fun upsertEntry(date: LocalDate, weightKg: Float)
    suspend fun deleteEntry(entry: WeightEntry)
}
