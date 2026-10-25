package com.k.shavrin.diethelper.presentation.screen.workout

import com.k.shavrin.diethelper.domain.model.WorkoutDay
import com.k.shavrin.diethelper.domain.model.WorkoutExercise
import com.k.shavrin.diethelper.domain.model.WorkoutGroup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TrainingSessionTimersTest {
    private val squat = WorkoutExercise(4, "Присед", "Тяжелая", "squat", 1.0, 2.5, 1.0,
        listOf(WorkoutGroup(3, 2, 0.7), WorkoutGroup(2, 2, 0.8)))

    @Test
    fun `following preview crosses groups and skips completed sets in the same exercise`() {
        val sets = WorkoutDay(0, 1, 1, "", listOf(squat)).sets()
        assertEquals(sets[1], followingTrainingSet(sets, emptySet()))
        assertEquals(sets[2], followingTrainingSet(sets, setOf(sets[1].key)))
        assertEquals(sets[3], followingTrainingSet(sets, setOf(sets[0].key, sets[2].key)))
    }

    @Test
    fun `following preview stops at another exercise even when names match`() {
        val oneSet = squat.copy(groups = listOf(WorkoutGroup(3, 1, 0.7)))
        val sets = WorkoutDay(0, 1, 1, "", listOf(oneSet, oneSet.copy(id = 5))).sets()
        assertNull(followingTrainingSet(sets, emptySet()))
        assertNull(followingTrainingSet(sets, setOf(sets.first().key)))
        assertNull(followingTrainingSet(sets, sets.map { it.key }.toSet()))
        assertNull(followingTrainingSet(emptyList(), emptySet()))
    }

    @Test
    fun `following preview never jumps over an intervening different exercise`() {
        val oneSet = squat.copy(groups = listOf(WorkoutGroup(3, 1, 0.7)))
        val first = WorkoutDay(0, 1, 1, "", listOf(oneSet)).sets().single()
        val other = first.copy(key = "other", exercise = oneSet.copy(id = 5))
        val later = first.copy(key = "later")
        assertNull(followingTrainingSet(listOf(first, other, later), emptySet()))
    }
}
