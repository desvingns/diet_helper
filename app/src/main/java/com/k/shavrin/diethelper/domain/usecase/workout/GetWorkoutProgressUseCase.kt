package com.k.shavrin.diethelper.domain.usecase.workout

import com.k.shavrin.diethelper.domain.repository.WorkoutRepository
import javax.inject.Inject

class GetWorkoutProgressUseCase @Inject constructor(private val repository: WorkoutRepository) {
    operator fun invoke() = repository.progress
}
