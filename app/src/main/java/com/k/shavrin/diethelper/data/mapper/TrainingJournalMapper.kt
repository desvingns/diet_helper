package com.k.shavrin.diethelper.data.mapper

import com.k.shavrin.diethelper.data.local.entity.TrainingSessionEntity
import com.k.shavrin.diethelper.data.local.entity.TrainingSessionWithSets
import com.k.shavrin.diethelper.data.local.entity.TrainingSetEntity
import com.k.shavrin.diethelper.domain.model.ExercisePreference
import com.k.shavrin.diethelper.domain.model.PerformedTrainingSet
import com.k.shavrin.diethelper.domain.model.RecoveryStatus
import com.k.shavrin.diethelper.domain.model.TrainingLoadBasis
import com.k.shavrin.diethelper.domain.model.TrainingOutcome
import com.k.shavrin.diethelper.domain.model.TrainingSession
import com.k.shavrin.diethelper.domain.model.WorkoutProgram

fun TrainingSessionWithSets.toDomain() = session.toDomain(sets.map { it.toDomain() })

fun TrainingSessionEntity.toDomain(sets: List<PerformedTrainingSet> = emptyList()) = TrainingSession(
    id, planDayId, cycle, day, startedAt, zoneId, plannedSetCount, legacySetCount, finishedAt,
    TrainingOutcome.entries.firstOrNull { it.name == outcome } ?: TrainingOutcome.STOPPED,
    gymMinutes, socialMinutes, sessionRpe,
    RecoveryStatus.entries.firstOrNull { it.name == recovery } ?: RecoveryStatus.UNKNOWN,
    notes, RecoveryStatus.entries.firstOrNull { it.name == nextDayRecovery } ?: RecoveryStatus.UNKNOWN,
    nextDayNotes, sets, WorkoutProgram.fromId(programId),
    warmUpStartedAt, warmUpFinishedAt, coolDownStartedAt, coolDownFinishedAt
)

fun TrainingSession.toEntity() = TrainingSessionEntity(
    id, planDayId, cycle, day, startedAt, zoneId, plannedSetCount, legacySetCount, finishedAt,
    outcome.name, gymMinutes, socialMinutes, sessionRpe, recovery.name, notes, nextDayRecovery.name, nextDayNotes, program.id,
    warmUpStartedAt, warmUpFinishedAt, coolDownStartedAt, coolDownFinishedAt
)

fun TrainingSetEntity.toDomain() = PerformedTrainingSet(
    id, sessionId, planKey, exerciseName, plannedExerciseName, plannedWeightKg, plannedReps,
    actualWeightKg, actualReps,
    TrainingLoadBasis.entries.firstOrNull { it.name == loadBasis } ?: TrainingLoadBasis.TOTAL_KG,
    rpe, symptomNote, ExercisePreference.entries.firstOrNull { it.name == preference } ?: ExercisePreference.NEUTRAL,
    notes, completed, completedAt
)

fun PerformedTrainingSet.toEntity() = TrainingSetEntity(
    id, sessionId, planKey, exerciseName, plannedExerciseName, plannedWeightKg, plannedReps,
    actualWeightKg, actualReps, loadBasis.name, rpe, symptomNote, preference.name, notes, completed, completedAt
)
