package com.k.shavrin.diethelper.domain.usecase.goals

import com.k.shavrin.diethelper.domain.model.DailyGoals
import com.k.shavrin.diethelper.domain.repository.GoalsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDailyGoalsUseCase @Inject constructor(
    private val repository: GoalsRepository
) {
    operator fun invoke(): Flow<DailyGoals> = repository.getDailyGoals()
}
