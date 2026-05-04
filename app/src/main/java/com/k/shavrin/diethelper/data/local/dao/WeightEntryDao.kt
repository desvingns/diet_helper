package com.k.shavrin.diethelper.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.k.shavrin.diethelper.data.local.entity.WeightEntryEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface WeightEntryDao {

    @Query("SELECT * FROM weight_entries ORDER BY date DESC")
    fun getAllEntries(): Flow<List<WeightEntryEntity>>

    @Query("SELECT * FROM weight_entries WHERE date = :date LIMIT 1")
    suspend fun getEntryByDate(date: LocalDate): WeightEntryEntity?

    @Upsert
    suspend fun upsertEntry(entry: WeightEntryEntity): Long

    @Delete
    suspend fun deleteEntry(entry: WeightEntryEntity)
}
