package com.k.shavrin.diethelper.domain.usecase.workout

import com.k.shavrin.diethelper.domain.model.TrainingTimerAction
import com.k.shavrin.diethelper.domain.model.WorkoutDay
import com.k.shavrin.diethelper.domain.repository.TrainingJournalRepository
import java.time.Clock
import javax.inject.Inject

class FinishTrainingWarmUpUseCase @Inject constructor(
    private val journal: TrainingJournalRepository,
    private val clock: Clock
) {
    suspend operator fun invoke(day: WorkoutDay) {
        journal.advanceTimer(day, TrainingTimerAction.FINISH_WARMUP, clock.millis(), clock.zone.id)
    }
}
