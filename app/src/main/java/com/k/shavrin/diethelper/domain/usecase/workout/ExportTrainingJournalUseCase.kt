package com.k.shavrin.diethelper.domain.usecase.workout

import com.k.shavrin.diethelper.domain.repository.TrainingJournalExporter
import com.k.shavrin.diethelper.domain.repository.TrainingJournalRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ExportTrainingJournalUseCase @Inject constructor(
    private val repository: TrainingJournalRepository,
    private val exporter: TrainingJournalExporter
) {
    suspend operator fun invoke(): List<String> {
        val sessions = repository.observeSessions().first()
        require(sessions.isNotEmpty()) { "Пока нет записанных занятий" }
        return exporter.export(sessions)
    }
}
