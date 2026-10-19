package com.k.shavrin.diethelper.domain.usecase.workout

import com.k.shavrin.diethelper.domain.model.WorkoutDay
import com.k.shavrin.diethelper.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class CompleteWorkoutUseCase @Inject constructor(private val repository: WorkoutRepository) {
    suspend operator fun invoke(day: WorkoutDay) {
        val progress = repository.progress.first()
        require(day.sets().isNotEmpty() && day.sets().all { it.key in progress.completedSets })
        repository.completeDay(day.id)
    }
}
