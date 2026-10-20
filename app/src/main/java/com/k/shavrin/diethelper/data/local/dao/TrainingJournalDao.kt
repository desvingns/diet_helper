package com.k.shavrin.diethelper.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.k.shavrin.diethelper.data.local.entity.TrainingSessionEntity
import com.k.shavrin.diethelper.data.local.entity.TrainingSessionWithSets
import com.k.shavrin.diethelper.data.local.entity.TrainingSetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrainingJournalDao {
    @Transaction
    @Query("SELECT * FROM training_sessions ORDER BY startedAt DESC, id DESC")
    fun observeSessions(): Flow<List<TrainingSessionWithSets>>

    @Query("SELECT * FROM training_sessions WHERE outcome = 'ACTIVE' AND programId = :programId LIMIT 1")
    suspend fun activeSession(programId: String = "src7"): TrainingSessionEntity?

    @Query("SELECT * FROM training_sessions WHERE planDayId = :dayId AND programId = :programId ORDER BY startedAt DESC LIMIT 1")
    suspend fun latestForDay(dayId: Int, programId: String = "src7"): TrainingSessionEntity?

    @Query("SELECT * FROM training_sessions WHERE id = :id")
    suspend fun session(id: String): TrainingSessionEntity?

    @Query("SELECT * FROM training_sets WHERE sessionId = :sessionId AND planKey = :key LIMIT 1")
    suspend fun plannedSet(sessionId: String, key: String): TrainingSetEntity?

    @Upsert
    suspend fun saveSession(session: TrainingSessionEntity)

    @Upsert
    suspend fun saveSet(set: TrainingSetEntity)

    @Query("UPDATE training_sets SET completed = 0 WHERE sessionId = :sessionId AND id = :setId")
    suspend fun undo(sessionId: String, setId: String)
}
