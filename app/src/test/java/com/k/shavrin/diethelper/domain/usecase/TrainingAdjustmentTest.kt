package com.k.shavrin.diethelper.domain.usecase

import com.k.shavrin.diethelper.domain.model.PerformedTrainingSet
import com.k.shavrin.diethelper.domain.model.RecoveryStatus
import com.k.shavrin.diethelper.domain.model.TrainingOutcome
import com.k.shavrin.diethelper.domain.model.TrainingSession
import com.k.shavrin.diethelper.domain.usecase.workout.SuggestTrainingAdjustmentUseCase
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TrainingAdjustmentTest {
    private val suggest = SuggestTrainingAdjustmentUseCase()
    private val session = TrainingSession("id", 0, 1, 1, 1000, "UTC", 2, outcome = TrainingOutcome.COMPLETED)

    @Test
    fun `low effort cannot override unusual sensations`() {
        val symptomatic = session.copy(sets = listOf(PerformedTrainingSet(
            exerciseName = "Front squat", actualWeightKg = 50.0, actualReps = 3, rpe = 5.0, symptomNote = "Unusual sensation"
        )), nextDayRecovery = RecoveryStatus.NORMAL)
        assertTrue(suggest(symptomatic).startsWith("Не повышай"))
    }

    @Test
    fun `missing follow up does not become normal recovery`() {
        assertTrue(suggest(session.copy(sessionRpe = 5.0)).contains("ещё не отмечено"))
    }

    @Test
    fun `illness has priority over a normal follow up and low effort`() {
        assertTrue(suggest(session.copy(recovery = RecoveryStatus.ILLNESS, nextDayRecovery = RecoveryStatus.NORMAL,
            sessionRpe = 5.0)).startsWith("Отмечена болезнь"))
    }

    @Test
    fun `total duration cannot imply a known training duration when breaks are unknown`() {
        assertNull(session.copy(gymMinutes = 90).trainingMinutes)
    }
}
