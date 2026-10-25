package com.k.shavrin.diethelper.data.mapper

import com.k.shavrin.diethelper.data.local.entity.TrainingSessionWithSets
import com.k.shavrin.diethelper.domain.model.ExercisePreference
import com.k.shavrin.diethelper.domain.model.PerformedTrainingSet
import com.k.shavrin.diethelper.domain.model.TrainingLoadBasis
import com.k.shavrin.diethelper.domain.model.TrainingSession
import com.k.shavrin.diethelper.domain.model.WorkoutProgram
import org.junit.Assert.assertEquals
import org.junit.Test

class TrainingJournalMapperTest {
    @Test
    fun `session and set round trip preserves known and unknown completion times`() {
        val known = PerformedTrainingSet("set", "session", "0:4:0:0", "Squat", "Squat",
            70.0, 3, 67.5, 2, TrainingLoadBasis.TOTAL_KG, 7.0, "Symptom", ExercisePreference.LIKED,
            "Note", true, 5000)
        val unknown = known.copy(id = "legacy", planKey = "0:4:0:1", completedAt = null)
        val session = TrainingSession("session", 0, 1, 1, 1000, "UTC", 3,
            sets = listOf(known, unknown), program = WorkoutProgram.RETURN,
            warmUpStartedAt = 1000, warmUpFinishedAt = 2000)
        val persisted = TrainingSessionWithSets(session.toEntity(), session.sets.map { it.toEntity() })
        assertEquals(session, persisted.toDomain())
        assertEquals(known.toEntity(), known.toEntity().toDomain().toEntity())
        assertEquals(unknown.toEntity(), unknown.toEntity().toDomain().toEntity())
    }
}
