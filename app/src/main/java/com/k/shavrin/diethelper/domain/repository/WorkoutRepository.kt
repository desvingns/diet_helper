package com.k.shavrin.diethelper.domain.repository

import com.k.shavrin.diethelper.domain.model.WorkoutPlan
import com.k.shavrin.diethelper.domain.model.WorkoutProgress
import kotlinx.coroutines.flow.Flow

interface WorkoutRepository {
    val progress: Flow<WorkoutProgress>
    suspend fun getPlan(): WorkoutPlan
    suspend fun saveMaxima(maxima: Map<String, Double>)
    suspend fun setCompleted(key: String, completed: Boolean, restDeadline: Long)
    suspend fun completeDay(id: Int)
    suspend fun setRest(seconds: Int, deadline: Long, pausedSeconds: Int = 0)
}
