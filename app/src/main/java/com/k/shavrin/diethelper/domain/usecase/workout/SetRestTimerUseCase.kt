package com.k.shavrin.diethelper.domain.usecase.workout

import com.k.shavrin.diethelper.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SetRestTimerUseCase @Inject constructor(private val repository: WorkoutRepository) {
    suspend operator fun invoke(seconds: Int, deadline: Long, pausedSeconds: Int = 0) =
        repository.setRest(seconds, deadline, pausedSeconds, repository.progress.first().program)
}
