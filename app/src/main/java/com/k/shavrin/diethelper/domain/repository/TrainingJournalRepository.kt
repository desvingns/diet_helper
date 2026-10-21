package com.k.shavrin.diethelper.domain.repository

import com.k.shavrin.diethelper.domain.model.PerformedTrainingSet
import com.k.shavrin.diethelper.domain.model.RecoveryStatus
import com.k.shavrin.diethelper.domain.model.TrainingSession
import com.k.shavrin.diethelper.domain.model.TrainingSessionFeedback
import com.k.shavrin.diethelper.domain.model.TrainingTimerAction
import com.k.shavrin.diethelper.domain.model.WorkoutDay
import kotlinx.coroutines.flow.Flow

interface TrainingJournalRepository {
    fun observeSessions(): Flow<List<TrainingSession>>
    suspend fun advanceTimer(day: WorkoutDay, action: TrainingTimerAction, now: Long, zoneId: String)
    suspend fun record(day: WorkoutDay, set: PerformedTrainingSet, legacySetCount: Int, now: Long, zoneId: String)
    suspend fun undo(sessionId: String, setId: String)
    suspend fun finish(
        day: WorkoutDay,
        feedback: TrainingSessionFeedback,
        legacySetCount: Int,
        now: Long,
        zoneId: String
    )
    suspend fun followUp(sessionId: String, recovery: RecoveryStatus, notes: String)
}
