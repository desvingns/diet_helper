package com.k.shavrin.diethelper.domain.usecase

import com.k.shavrin.diethelper.domain.model.WorkoutDay
import com.k.shavrin.diethelper.domain.model.WorkoutExercise
import com.k.shavrin.diethelper.domain.model.WorkoutGroup
import com.k.shavrin.diethelper.domain.model.WorkoutPlan
import com.k.shavrin.diethelper.domain.model.WorkoutProgram
import com.k.shavrin.diethelper.domain.model.WorkoutProgress
import com.k.shavrin.diethelper.domain.repository.WorkoutRepository
import com.k.shavrin.diethelper.domain.usecase.workout.CompleteWorkoutUseCase
import com.k.shavrin.diethelper.domain.usecase.workout.RecordWorkoutSetUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkoutUseCasesTest {
    private val exercise = WorkoutExercise(4, "Присед", "Тяжелая", "squat", 1.0, 2.5, 1.0,
        listOf(WorkoutGroup(5, 2, 0.54)))
    private val day = WorkoutDay(0, 1, 1, "", listOf(exercise))

    @Test
    fun `rounding matches spreadsheet and does not multiply dumbbell weight twice`() {
        assertEquals(125.0, exercise.weight(mapOf("squat" to 230.0), 0.54), 0.001)
        val dumbbell = exercise.copy(base = "bench", factor = 0.32, rounding = 1.0, equipmentMultiplier = 2.0)
        assertEquals(20.0, dumbbell.weight(mapOf("bench" to 160.0), 0.4), 0.001)
        assertEquals(0.0, exercise.weight(emptyMap(), 0.8), 0.001)
    }

    @Test
    fun `record starts a persistent deadline and undo clears the mark`() = runTest {
        val repository = FakeWorkoutRepository(day)
        val useCase = RecordWorkoutSetUseCase(repository)
        val key = day.sets().first().key
        useCase(day, key, true, 1_000)
        assertEquals(setOf(key), repository.progress.value.completedSets)
        assertEquals(121_000L, repository.progress.value.restDeadline)
        useCase(day, key, false, 2_000)
        assertTrue(repository.progress.value.completedSets.isEmpty())
        assertEquals(0L, repository.progress.value.restDeadline)
    }

    @Test
    fun `completion requires every set and preserves completion history`() = runTest {
        val repository = FakeWorkoutRepository(day)
        val complete = CompleteWorkoutUseCase(repository)
        var rejected = false
        try { complete(day) } catch (_: IllegalArgumentException) { rejected = true }
        assertTrue(rejected)
        val record = RecordWorkoutSetUseCase(repository)
        day.sets().forEach { record(day, it.key, true, 1_000) }
        complete(day)
        assertEquals(setOf(0), repository.progress.value.completedDays)
        assertEquals(day.sets().size, repository.progress.value.completedSets.size)
    }

    @Test
    fun `unconfigured maxima cannot log a fabricated zero weight set`() = runTest {
        val repository = FakeWorkoutRepository(day)
        repository.progress.value = WorkoutProgress()
        var rejected = false
        try {
            RecordWorkoutSetUseCase(repository)(day, day.sets().first().key, true, 0)
        } catch (_: IllegalArgumentException) {
            rejected = true
        }
        assertTrue(rejected)
        assertTrue(repository.progress.value.completedSets.isEmpty())
    }
}

private class FakeWorkoutRepository(private val day: WorkoutDay) : WorkoutRepository {
    override val progress = MutableStateFlow(WorkoutProgress(maxima = mapOf("squat" to 230.0, "bench" to 160.0, "deadlift" to 240.0)))
    override suspend fun getPlan(program: WorkoutProgram) = WorkoutPlan("Test", listOf(day), program)
    override suspend fun selectProgram(program: WorkoutProgram) {
        progress.value = progress.value.copy(program = program)
    }
    override suspend fun saveReturnWeekLoads(week: Int, loads: Map<String, Double>) {
        progress.value = progress.value.copy(returnWeekLoads = progress.value.returnWeekLoads + (week to loads))
    }
    override suspend fun saveMaxima(maxima: Map<String, Double>) {
        progress.value = progress.value.copy(maxima = maxima)
    }
    override suspend fun setCompleted(key: String, completed: Boolean, restDeadline: Long, program: WorkoutProgram) {
        progress.value = progress.value.copy(completedSets = if (completed) progress.value.completedSets + key else progress.value.completedSets - key,
            restDeadline = restDeadline)
    }
    override suspend fun completeDay(id: Int, program: WorkoutProgram) {
        progress.value = progress.value.copy(completedDays = progress.value.completedDays + id)
    }
    override suspend fun setRest(seconds: Int, deadline: Long, pausedSeconds: Int, program: WorkoutProgram) {
        progress.value = progress.value.copy(restSeconds = seconds, restDeadline = deadline, pausedRestSeconds = pausedSeconds)
    }
}
