package com.k.shavrin.diethelper.domain.repository

import com.k.shavrin.diethelper.domain.model.WorkoutPlan
import com.k.shavrin.diethelper.domain.model.WorkoutProgram
import com.k.shavrin.diethelper.domain.model.WorkoutProgress
import kotlinx.coroutines.flow.Flow

interface WorkoutRepository {
    val progress: Flow<WorkoutProgress>
    suspend fun getPlan(program: WorkoutProgram = WorkoutProgram.SRC7): WorkoutPlan
    suspend fun selectProgram(program: WorkoutProgram)
    suspend fun saveReturnWeekLoads(week: Int, loads: Map<String, Double>)
    suspend fun saveMaxima(maxima: Map<String, Double>)
    suspend fun setCompleted(
        key: String,
        completed: Boolean,
        restDeadline: Long,
        program: WorkoutProgram = WorkoutProgram.SRC7
    )
    suspend fun completeDay(id: Int, program: WorkoutProgram = WorkoutProgram.SRC7)
    suspend fun setRest(
        seconds: Int,
        deadline: Long,
        pausedSeconds: Int = 0,
        program: WorkoutProgram = WorkoutProgram.SRC7
    )
}
