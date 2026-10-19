package com.k.shavrin.diethelper.domain.repository

import com.k.shavrin.diethelper.domain.model.TrainingSession

interface TrainingJournalExporter {
    suspend fun export(sessions: List<TrainingSession>): List<String>
}
