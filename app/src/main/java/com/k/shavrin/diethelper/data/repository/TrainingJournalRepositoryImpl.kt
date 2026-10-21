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
import com.k.shavrin.diethelper.domain.model.TrainingTimerAction
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

    override suspend fun advanceTimer(day: WorkoutDay, action: TrainingTimerAction, now: Long, zoneId: String) {
        database.withTransaction {
            val session = if (action == TrainingTimerAction.START_WARMUP) activeOrNew(day, 0, now, zoneId)
                else requireNotNull(dao.activeSession(day.program.id)?.toDomain()) { "Сначала начните разминку" }
            require(session.planDayId == day.id)
            val updated = when (action) {
                TrainingTimerAction.START_WARMUP -> {
                    require(session.coolDownStartedAt == null)
                    session.copy(warmUpStartedAt = session.warmUpStartedAt ?: now)
                }
                TrainingTimerAction.FINISH_WARMUP -> {
                    require(session.warmUpStartedAt != null && session.coolDownStartedAt == null)
                    session.copy(warmUpFinishedAt = session.warmUpFinishedAt ?: now)
                }
                TrainingTimerAction.START_COOLDOWN -> {
                    require(session.warmUpStartedAt == null || session.warmUpFinishedAt != null) { "Сначала завершите разминку" }
                    session.copy(coolDownStartedAt = session.coolDownStartedAt ?: now)
                }
                TrainingTimerAction.FINISH_COOLDOWN -> {
                    require(session.coolDownStartedAt != null)
                    session.copy(coolDownFinishedAt = session.coolDownFinishedAt ?: now)
                }
            }
            dao.saveSession(updated.toEntity())
        }
    }

    override suspend fun record(day: WorkoutDay, set: PerformedTrainingSet, legacySetCount: Int, now: Long, zoneId: String) {
        database.withTransaction {
            val session = activeOrNew(day, legacySetCount, now, zoneId)
            require(session.coolDownStartedAt == null) { "Силовая часть завершена; идёт заминка" }
            require(session.warmUpStartedAt == null || session.warmUpFinishedAt != null) { "Сначала завершите разминку" }
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
                finishedAt = session.finishedAt ?: session.coolDownFinishedAt ?: now, outcome = feedback.outcome,
                warmUpFinishedAt = session.warmUpFinishedAt ?: session.warmUpStartedAt?.let { now },
                coolDownFinishedAt = session.coolDownFinishedAt ?: session.coolDownStartedAt?.let { now },
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
