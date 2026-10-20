package com.k.shavrin.diethelper.domain.usecase.workout

import com.k.shavrin.diethelper.domain.model.WorkoutProgram
import com.k.shavrin.diethelper.domain.repository.WorkoutRepository
import javax.inject.Inject

class GetWorkoutPlanUseCase @Inject constructor(private val repository: WorkoutRepository) {
    suspend operator fun invoke(program: WorkoutProgram = WorkoutProgram.SRC7) = repository.getPlan(program)
}
