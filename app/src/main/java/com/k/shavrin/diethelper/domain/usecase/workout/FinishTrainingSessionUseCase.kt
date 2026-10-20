package com.k.shavrin.diethelper.domain.usecase.workout

import com.k.shavrin.diethelper.domain.model.TrainingOutcome
import com.k.shavrin.diethelper.domain.model.TrainingSessionFeedback
import com.k.shavrin.diethelper.domain.model.WorkoutDay
import com.k.shavrin.diethelper.domain.model.completedTrainingPlanKeys
import com.k.shavrin.diethelper.domain.repository.TrainingJournalRepository
import com.k.shavrin.diethelper.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.first
import java.time.Clock
import javax.inject.Inject

class FinishTrainingSessionUseCase @Inject constructor(
    private val journal: TrainingJournalRepository,
    private val workouts: WorkoutRepository,
    private val clock: Clock
) {
    suspend operator fun invoke(day: WorkoutDay, feedback: TrainingSessionFeedback) {
        require(feedback.outcome != TrainingOutcome.ACTIVE)
        require(feedback.notes.length <= 4000)
        require(feedback.outcome == TrainingOutcome.COMPLETED || feedback.notes.isNotBlank()) { "Укажите причину изменения занятия" }
        require(feedback.gymMinutes == null || feedback.gymMinutes in 1..1440)
        require(feedback.socialMinutes == null || feedback.socialMinutes in 0..1440)
        require(feedback.gymMinutes == null || feedback.socialMinutes == null || feedback.socialMinutes <= feedback.gymMinutes)
        require(feedback.sessionRpe == null || feedback.sessionRpe.isFinite() && feedback.sessionRpe in 0.0..10.0)
        val progress = workouts.progress.first()
        require(progress.program == day.program)
        val sessions = journal.observeSessions().first().filter { it.program == day.program && it.planDayId == day.id }
        val known = sessions.flatMap { it.sets }.mapNotNull { it.planKey }.toSet()
        val completed = completedTrainingPlanKeys(progress.completedSets, sessions.flatMap { it.sets })
        require(feedback.outcome != TrainingOutcome.COMPLETED || day.sets().all { it.key in completed })
        val legacyCount = day.sets().count { it.key in progress.completedSets && it.key !in known }
        journal.finish(day, feedback, legacyCount, clock.millis(), clock.zone.id)
        workouts.completeDay(day.id, day.program)
    }
}
