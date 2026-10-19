package com.k.shavrin.diethelper.domain.usecase.workout

import com.k.shavrin.diethelper.domain.repository.TrainingJournalRepository
import javax.inject.Inject

class GetTrainingJournalUseCase @Inject constructor(private val repository: TrainingJournalRepository) {
    operator fun invoke() = repository.observeSessions()
}
