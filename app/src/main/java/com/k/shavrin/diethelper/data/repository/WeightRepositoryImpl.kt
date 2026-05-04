package com.k.shavrin.diethelper.data.repository

import com.k.shavrin.diethelper.data.local.dao.WeightEntryDao
import com.k.shavrin.diethelper.data.local.entity.WeightEntryEntity
import com.k.shavrin.diethelper.data.mapper.toDomain
import com.k.shavrin.diethelper.data.mapper.toEntity
import com.k.shavrin.diethelper.domain.model.WeightEntry
import com.k.shavrin.diethelper.domain.repository.WeightRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class WeightRepositoryImpl @Inject constructor(
    private val weightEntryDao: WeightEntryDao
) : WeightRepository {

    override fun getAllEntries(): Flow<List<WeightEntry>> =
        weightEntryDao.getAllEntries().map { entities -> entities.toDomain() }

    override suspend fun getEntryByDate(date: LocalDate): WeightEntry? =
        weightEntryDao.getEntryByDate(date)?.toDomain()

    override suspend fun upsertEntry(date: LocalDate, weightKg: Float) {
        val existing = weightEntryDao.getEntryByDate(date)
        val entity = existing?.copy(weightKg = weightKg)
            ?: WeightEntryEntity(date = date, weightKg = weightKg)
        weightEntryDao.upsertEntry(entity)
    }

    override suspend fun deleteEntry(entry: WeightEntry) {
        weightEntryDao.deleteEntry(entry.toEntity())
    }
}
