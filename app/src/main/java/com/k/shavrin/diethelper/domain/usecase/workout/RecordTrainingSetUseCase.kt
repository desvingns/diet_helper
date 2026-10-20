package com.k.shavrin.diethelper.domain.usecase.workout

import com.k.shavrin.diethelper.domain.model.PerformedTrainingSet
import com.k.shavrin.diethelper.domain.model.TrainingLoadBasis
import com.k.shavrin.diethelper.domain.model.WorkoutDay
import com.k.shavrin.diethelper.domain.repository.TrainingJournalRepository
import com.k.shavrin.diethelper.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.first
import java.time.Clock
import javax.inject.Inject

class RecordTrainingSetUseCase @Inject constructor(
    private val journal: TrainingJournalRepository,
    private val workouts: WorkoutRepository,
    private val clock: Clock
) {
    suspend operator fun invoke(day: WorkoutDay, set: PerformedTrainingSet) {
        val progress = workouts.progress.first()
        require(progress.configuredFor(day) && day.id !in progress.completedDays) { "Сначала настройте веса этой недели" }
        require(set.planKey == null || day.sets().any { it.key == set.planKey })
        require(set.exerciseName.isNotBlank() && set.exerciseName.length <= 120)
        require(set.actualReps in 1..1000)
        require(set.rpe == null || set.rpe.isFinite() && set.rpe in 1.0..10.0)
        require(set.symptomNote.length <= 2000 && set.notes.length <= 2000)
        if (set.loadBasis == TrainingLoadBasis.BODYWEIGHT) {
            require(set.actualWeightKg == null)
        } else {
            require(set.actualWeightKg?.let { it.isFinite() && it in 0.0..1500.0 } == true)
        }
        val existingKeys = journal.observeSessions().first().filter { it.program == day.program && it.planDayId == day.id }
            .flatMap { it.sets }.mapNotNull { it.planKey }.toSet() + listOfNotNull(set.planKey)
        val legacyCount = day.sets().count { it.key in progress.completedSets && it.key !in existingKeys }
        val now = clock.millis()
        journal.record(day, set.copy(completed = true), legacyCount, now, clock.zone.id)
        val deadline = now + progress.restSeconds * 1000L
        if (set.planKey != null) workouts.setCompleted(set.planKey, true, deadline, day.program)
        else workouts.setRest(progress.restSeconds, deadline, program = day.program)
    }
}
