package com.k.shavrin.diethelper.domain.usecase.goals

import com.k.shavrin.diethelper.domain.model.DailyGoals
import com.k.shavrin.diethelper.domain.repository.GoalsRepository
import javax.inject.Inject

class SaveDailyGoalsUseCase @Inject constructor(
    private val repository: GoalsRepository
) {
    suspend operator fun invoke(goals: DailyGoals) = repository.saveGoals(goals)
}
