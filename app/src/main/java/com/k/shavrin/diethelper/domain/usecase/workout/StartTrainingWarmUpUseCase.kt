package com.k.shavrin.diethelper.domain.usecase.workout

import com.k.shavrin.diethelper.domain.model.TrainingTimerAction
import com.k.shavrin.diethelper.domain.model.WorkoutDay
import com.k.shavrin.diethelper.domain.repository.TrainingJournalRepository
import com.k.shavrin.diethelper.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.first
import java.time.Clock
import javax.inject.Inject

class StartTrainingWarmUpUseCase @Inject constructor(
    private val journal: TrainingJournalRepository,
    private val workouts: WorkoutRepository,
    private val clock: Clock
) {
    suspend operator fun invoke(day: WorkoutDay) {
        val progress = workouts.progress.first()
        require(progress.program == day.program && progress.configuredFor(day) && day.id !in progress.completedDays)
        journal.advanceTimer(day, TrainingTimerAction.START_WARMUP, clock.millis(), clock.zone.id)
    }
}
