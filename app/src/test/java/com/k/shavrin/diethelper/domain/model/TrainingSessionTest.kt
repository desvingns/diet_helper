package com.k.shavrin.diethelper.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TrainingSessionTest {
    private val session = TrainingSession("session", 0, 1, 1, 1000, "UTC", 3)

    @Test
    fun `last completion uses the latest actual timestamp regardless of list order`() {
        val sets = listOf(
            PerformedTrainingSet(exerciseName = "Extra", actualReps = 4, completedAt = 5000),
            PerformedTrainingSet(planKey = "first", exerciseName = "Squat", actualReps = 3, completedAt = 2000),
            PerformedTrainingSet(exerciseName = "Undone", actualReps = 3, completedAt = 9000, completed = false),
            PerformedTrainingSet(exerciseName = "Legacy", actualReps = 3)
        )
        assertEquals(5000L, session.copy(sets = sets).lastCompletedSetAt)
    }

    @Test
    fun `no completed timestamps leaves last completion unknown`() {
        assertNull(session.lastCompletedSetAt)
        assertNull(session.copy(sets = listOf(
            PerformedTrainingSet(exerciseName = "Legacy", actualReps = 3),
            PerformedTrainingSet(exerciseName = "Undone", actualReps = 3, completed = false, completedAt = 9000)
        )).lastCompletedSetAt)
    }
}
