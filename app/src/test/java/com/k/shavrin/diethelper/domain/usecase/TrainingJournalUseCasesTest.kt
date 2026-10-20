package com.k.shavrin.diethelper.domain.usecase

import com.k.shavrin.diethelper.domain.model.PerformedTrainingSet
import com.k.shavrin.diethelper.domain.model.RecoveryStatus
import com.k.shavrin.diethelper.domain.model.TrainingOutcome
import com.k.shavrin.diethelper.domain.model.TrainingSession
import com.k.shavrin.diethelper.domain.model.TrainingSessionFeedback
import com.k.shavrin.diethelper.domain.model.WorkoutDay
import com.k.shavrin.diethelper.domain.model.WorkoutExercise
import com.k.shavrin.diethelper.domain.model.WorkoutGroup
import com.k.shavrin.diethelper.domain.model.WorkoutPlan
import com.k.shavrin.diethelper.domain.model.WorkoutProgram
import com.k.shavrin.diethelper.domain.model.WorkoutProgress
import com.k.shavrin.diethelper.domain.repository.TrainingJournalRepository
import com.k.shavrin.diethelper.domain.repository.WorkoutRepository
import com.k.shavrin.diethelper.domain.usecase.workout.FinishTrainingSessionUseCase
import com.k.shavrin.diethelper.domain.usecase.workout.RecordTrainingSetUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class TrainingJournalUseCasesTest {
    private val day = WorkoutDay(0, 1, 1, "", listOf(WorkoutExercise(
        4, "Squat", "Heavy", "squat", 1.0, 2.5, 1.0, listOf(WorkoutGroup(3, 2, 0.7))
    )))
    private val journal = JournalFake()
    private val workouts = WorkoutsFake(day)
    private val clock = Clock.fixed(Instant.ofEpochMilli(1000), ZoneId.of("UTC"))
    private val set get() = PerformedTrainingSet(planKey = day.sets().first().key,
        exerciseName = "Squat", actualWeightKg = 70.0, actualReps = 3)

    @Test
    fun `invalid RPE cannot create a session`() = runTest {
        expectRejected { RecordTrainingSetUseCase(journal, workouts, clock)(day, set.copy(rpe = Double.NaN)) }
        assertTrue(journal.sessions.value.isEmpty())
    }

    @Test
    fun `unconfigured references cannot create actual zero load records`() = runTest {
        workouts.progress.value = WorkoutProgress()
        expectRejected { RecordTrainingSetUseCase(journal, workouts, clock)(day, set) }
        assertTrue(journal.sessions.value.isEmpty())
    }

    @Test
    fun `recording a legacy marked set replaces unknown detail rather than double counting it`() = runTest {
        workouts.progress.value = workouts.progress.value.copy(completedSets = setOf(requireNotNull(set.planKey)))
        RecordTrainingSetUseCase(journal, workouts, clock)(day, set)
        assertEquals(0, journal.sessions.value.single().legacySetCount)
        assertEquals(121000L, workouts.progress.value.restDeadline)
    }

    @Test
    fun `partial session can finish with a reason while full completion is rejected`() = runTest {
        RecordTrainingSetUseCase(journal, workouts, clock)(day, set)
        val finish = FinishTrainingSessionUseCase(journal, workouts, clock)
        expectRejected { finish(day, TrainingSessionFeedback(TrainingOutcome.COMPLETED)) }
        expectRejected { finish(day, TrainingSessionFeedback(TrainingOutcome.MODIFIED)) }
        finish(day, TrainingSessionFeedback(TrainingOutcome.MODIFIED, notes = "Less work"))
        assertEquals(setOf(0), workouts.progress.value.completedDays)
        assertEquals(1, journal.sessions.value.single().completedSets.size)
    }

    @Test
    fun `explicit undone records override stale completion flags`() = runTest {
        RecordTrainingSetUseCase(journal, workouts, clock)(day, set)
        val session = journal.sessions.value.single()
        journal.sessions.value = listOf(session.copy(sets = session.sets.map { it.copy(completed = false) }))
        workouts.progress.value = workouts.progress.value.copy(completedSets = day.sets().map { it.key }.toSet())
        expectRejected {
            FinishTrainingSessionUseCase(journal, workouts, clock)(day, TrainingSessionFeedback(TrainingOutcome.COMPLETED))
        }
    }

    @Test
    fun `social minutes cannot exceed the reported gym duration`() = runTest {
        expectRejected {
            FinishTrainingSessionUseCase(journal, workouts, clock)(day,
                TrainingSessionFeedback(TrainingOutcome.STOPPED, gymMinutes = 10, socialMinutes = 20, notes = "Stopped"))
        }
        assertTrue(journal.sessions.value.isEmpty())
    }

    private suspend fun expectRejected(block: suspend () -> Unit) {
        var rejected = false
        try { block() } catch (_: IllegalArgumentException) { rejected = true }
        assertTrue(rejected)
    }
}

private class JournalFake : TrainingJournalRepository {
    val sessions = MutableStateFlow<List<TrainingSession>>(emptyList())
    override fun observeSessions() = sessions
    override suspend fun record(
        day: WorkoutDay,
        set: PerformedTrainingSet,
        legacySetCount: Int,
        now: Long,
        zoneId: String
    ) {
        val session = sessions.value.firstOrNull() ?: TrainingSession("session", day.id, day.cycle, day.day,
            now, zoneId, day.sets().size, legacySetCount)
        sessions.value = listOf(session.copy(sets = session.sets + set.copy(id = "set", sessionId = session.id)))
    }
    override suspend fun undo(sessionId: String, setId: String) = Unit
    override suspend fun finish(
        day: WorkoutDay,
        feedback: TrainingSessionFeedback,
        legacySetCount: Int,
        now: Long,
        zoneId: String
    ) {
        val session = sessions.value.single()
        sessions.value = listOf(session.copy(outcome = feedback.outcome, notes = feedback.notes, finishedAt = now))
    }
    override suspend fun followUp(sessionId: String, recovery: RecoveryStatus, notes: String) = Unit
}

private class WorkoutsFake(private val day: WorkoutDay) : WorkoutRepository {
    override val progress = MutableStateFlow(WorkoutProgress(maxima = mapOf("squat" to 100.0, "bench" to 100.0, "deadlift" to 140.0)))
    override suspend fun getPlan(program: WorkoutProgram) = WorkoutPlan("test", listOf(day), program)
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
        progress.value = progress.value.copy(
            completedSets = if (completed) progress.value.completedSets + key else progress.value.completedSets - key,
            restDeadline = restDeadline
        )
    }
    override suspend fun completeDay(id: Int, program: WorkoutProgram) {
        progress.value = progress.value.copy(completedDays = progress.value.completedDays + id)
    }
    override suspend fun setRest(seconds: Int, deadline: Long, pausedSeconds: Int, program: WorkoutProgram) {
        progress.value = progress.value.copy(restSeconds = seconds, restDeadline = deadline, pausedRestSeconds = pausedSeconds)
    }
}
