package com.k.shavrin.diethelper.domain.usecase.workout

import com.k.shavrin.diethelper.domain.model.WorkoutDay
import com.k.shavrin.diethelper.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class RecordWorkoutSetUseCase @Inject constructor(private val repository: WorkoutRepository) {
    suspend operator fun invoke(day: WorkoutDay, key: String, completed: Boolean, now: Long) {
        val progress = repository.progress.first()
        require(progress.configured && day.id !in progress.completedDays)
        require(day.sets().any { it.key == key })
        val deadline = if (completed) now + progress.restSeconds * 1000L else 0L
        repository.setCompleted(key, completed, deadline)
    }
}
