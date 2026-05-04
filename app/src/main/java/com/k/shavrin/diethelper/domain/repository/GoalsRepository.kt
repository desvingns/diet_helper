package com.k.shavrin.diethelper.domain.repository

import com.k.shavrin.diethelper.domain.model.DailyGoals
import kotlinx.coroutines.flow.Flow

interface GoalsRepository {
    fun getDailyGoals(): Flow<DailyGoals>
    suspend fun saveGoals(goals: DailyGoals)
}
