package com.k.shavrin.diethelper.domain.usecase.workout

import com.k.shavrin.diethelper.domain.model.RecoveryStatus
import com.k.shavrin.diethelper.domain.repository.TrainingJournalRepository
import javax.inject.Inject

class SaveTrainingFollowUpUseCase @Inject constructor(private val repository: TrainingJournalRepository) {
    suspend operator fun invoke(id: String, recovery: RecoveryStatus, notes: String) {
        require(notes.length <= 4000)
        repository.followUp(id, recovery, notes)
    }
}
