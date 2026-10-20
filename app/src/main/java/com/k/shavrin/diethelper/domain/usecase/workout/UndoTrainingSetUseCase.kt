package com.k.shavrin.diethelper.domain.usecase.workout

import com.k.shavrin.diethelper.domain.model.PerformedTrainingSet
import com.k.shavrin.diethelper.domain.repository.TrainingJournalRepository
import com.k.shavrin.diethelper.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class UndoTrainingSetUseCase @Inject constructor(
    private val journal: TrainingJournalRepository,
    private val workouts: WorkoutRepository
) {
    suspend operator fun invoke(set: PerformedTrainingSet) {
        val session = requireNotNull(journal.observeSessions().first().find { it.id == set.sessionId })
        journal.undo(set.sessionId, set.id)
        set.planKey?.let { workouts.setCompleted(it, false, 0, session.program) }
    }
}
