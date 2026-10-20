package com.k.shavrin.diethelper.domain.usecase.workout

import com.k.shavrin.diethelper.domain.repository.WorkoutRepository
import javax.inject.Inject

class SaveReturnWeekLoadsUseCase @Inject constructor(private val repository: WorkoutRepository) {
    suspend operator fun invoke(week: Int, loads: Map<String, Double>) {
        require(week in 1..6)
        require(listOf("squat", "bench", "deadlift").all { key ->
            loads[key]?.let { it.isFinite() && it > 0 && it <= 1500 } == true
        })
        repository.saveReturnWeekLoads(week, loads)
    }
}
