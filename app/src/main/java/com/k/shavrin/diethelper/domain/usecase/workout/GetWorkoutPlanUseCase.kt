package com.k.shavrin.diethelper.domain.usecase.workout

import com.k.shavrin.diethelper.domain.repository.WorkoutRepository
import javax.inject.Inject

class GetWorkoutPlanUseCase @Inject constructor(private val repository: WorkoutRepository) {
    suspend operator fun invoke() = repository.getPlan()
}
