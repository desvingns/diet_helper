package com.k.shavrin.diethelper.data.repository

import com.k.shavrin.diethelper.data.local.GoalsDataSource
import com.k.shavrin.diethelper.domain.model.DailyGoals
import com.k.shavrin.diethelper.domain.repository.GoalsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GoalsRepositoryImpl @Inject constructor(
    private val dataSource: GoalsDataSource
) : GoalsRepository {

    override fun getDailyGoals(): Flow<DailyGoals> = dataSource.goalsFlow

    override suspend fun saveGoals(goals: DailyGoals) {
        dataSource.saveGoals(goals)
    }
}
