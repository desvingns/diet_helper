package com.k.shavrin.diethelper.data.repository

import androidx.room.withTransaction
import com.k.shavrin.diethelper.data.local.DietHelperDatabase
import com.k.shavrin.diethelper.data.mapper.toDomain
import com.k.shavrin.diethelper.data.mapper.toEntity
import com.k.shavrin.diethelper.domain.model.PerformedTrainingSet
import com.k.shavrin.diethelper.domain.model.RecoveryStatus
import com.k.shavrin.diethelper.domain.model.TrainingOutcome
import com.k.shavrin.diethelper.domain.model.TrainingSession
import com.k.shavrin.diethelper.domain.model.TrainingSessionFeedback
import com.k.shavrin.diethelper.domain.model.WorkoutDay
import com.k.shavrin.diethelper.domain.repository.TrainingJournalRepository
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrainingJournalRepositoryImpl @Inject constructor(
    private val database: DietHelperDatabase
) : TrainingJournalRepository {
    private val dao get() = database.trainingJournalDao()
    override fun observeSessions() = dao.observeSessions().map { entries -> entries.map { it.toDomain() } }

    override suspend fun record(day: WorkoutDay, set: PerformedTrainingSet, legacySetCount: Int, now: Long, zoneId: String) {
        database.withTransaction {
            val session = activeOrNew(day, legacySetCount, now, zoneId)
            dao.saveSession(session.copy(legacySetCount = legacySetCount).toEntity())
            val previous = set.planKey?.let { dao.plannedSet(session.id, it) }
            val stable = set.copy(
                id = previous?.id ?: UUID.randomUUID().toString(), sessionId = session.id,
                plannedWeightKg = previous?.plannedWeightKg ?: set.plannedWeightKg,
                plannedReps = previous?.plannedReps ?: set.plannedReps,
                plannedExerciseName = previous?.plannedExerciseName ?: set.plannedExerciseName
            )
            dao.saveSet(stable.toEntity())
        }
    }

    override suspend fun undo(sessionId: String, setId: String) {
        database.withTransaction {
            require(dao.session(sessionId)?.outcome == TrainingOutcome.ACTIVE.name)
            dao.undo(sessionId, setId)
        }
    }

    override suspend fun finish(day: WorkoutDay, feedback: TrainingSessionFeedback, legacySetCount: Int, now: Long, zoneId: String) {
        database.withTransaction {
            val active = dao.activeSession(day.program.id)
            require(active == null || active.planDayId == day.id) { "Сначала завершите начатую тренировку" }
            val session = active?.toDomain() ?: dao.latestForDay(day.id, day.program.id)?.toDomain()
                ?: activeOrNew(day, legacySetCount, now, zoneId)
            dao.saveSession(session.copy(
                finishedAt = session.finishedAt ?: now, outcome = feedback.outcome,
                gymMinutes = feedback.gymMinutes, socialMinutes = feedback.socialMinutes,
                sessionRpe = feedback.sessionRpe, recovery = feedback.recovery, notes = feedback.notes.trim()
            ).toEntity())
        }
    }

    override suspend fun followUp(sessionId: String, recovery: RecoveryStatus, notes: String) {
        database.withTransaction {
            val session = requireNotNull(dao.session(sessionId))
            require(session.outcome != TrainingOutcome.ACTIVE.name)
            dao.saveSession(session.toDomain().copy(nextDayRecovery = recovery, nextDayNotes = notes.trim()).toEntity())
        }
    }

    private suspend fun activeOrNew(day: WorkoutDay, legacySetCount: Int, now: Long, zoneId: String): TrainingSession {
        val active = dao.activeSession(day.program.id)
        require(active == null || active.planDayId == day.id) { "Сначала завершите начатую тренировку" }
        if (active != null) return active.toDomain()
        require(dao.latestForDay(day.id, day.program.id)?.outcome.let { it == null || it == TrainingOutcome.ACTIVE.name }) {
            "Это занятие уже сохранено"
        }
        val session = TrainingSession(
            UUID.randomUUID().toString(), day.id, day.cycle, day.day, now, zoneId, day.sets().size, legacySetCount,
            program = day.program
        )
        dao.saveSession(session.toEntity())
        return session
    }
}
