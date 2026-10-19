package com.k.shavrin.diethelper.domain.usecase.workout

import com.k.shavrin.diethelper.domain.repository.WorkoutRepository
import javax.inject.Inject

class SaveWorkoutMaximaUseCase @Inject constructor(private val repository: WorkoutRepository) {
    suspend operator fun invoke(maxima: Map<String, Double>) = repository.saveMaxima(maxima)
}
