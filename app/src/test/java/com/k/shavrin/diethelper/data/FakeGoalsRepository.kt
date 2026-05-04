package com.k.shavrin.diethelper.data

import com.k.shavrin.diethelper.domain.model.DailyGoals
import com.k.shavrin.diethelper.domain.repository.GoalsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeGoalsRepository(
    initial: DailyGoals = DailyGoals.DEFAULT
) : GoalsRepository {

    private val goals = MutableStateFlow(initial)

    override fun getDailyGoals(): Flow<DailyGoals> = goals

    override suspend fun saveGoals(goals: DailyGoals) {
        this.goals.value = goals
    }

    val current: DailyGoals get() = goals.value
}
