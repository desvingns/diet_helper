package com.k.shavrin.diethelper.data.repository

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.k.shavrin.diethelper.data.local.DietHelperDatabase
import com.k.shavrin.diethelper.domain.model.PerformedTrainingSet
import com.k.shavrin.diethelper.domain.model.WorkoutDay
import com.k.shavrin.diethelper.domain.model.WorkoutExercise
import com.k.shavrin.diethelper.domain.model.WorkoutGroup
import com.k.shavrin.diethelper.domain.model.WorkoutProgram
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class TrainingJournalRepositoryImplTest {
    private val context get() = ApplicationProvider.getApplicationContext<Application>()
    private val day = WorkoutDay(0, 1, 1, "", listOf(WorkoutExercise(
        4, "Присед", "Тяжелая", "squat", 1.0, 2.5, 1.0, listOf(WorkoutGroup(3, 2, 0.7))
    )))
    private fun set(index: Int = 0) = PerformedTrainingSet(
        planKey = day.sets()[index].key, exerciseName = "Присед", actualWeightKg = 70.0, actualReps = 3
    )

    @Test
    fun `timestamps survive disk reopen`() =
        // mp-real-io: persistence is verified against a real SQLite file across database instances.
        runBlocking {
            // Robolectric already isolates each test; short names also fit native Windows SQLite paths.
            val name = "timing.db"
            val first = Room.databaseBuilder(context, DietHelperDatabase::class.java, name).build()
            val id = try {
                val repository = TrainingJournalRepositoryImpl(first)
                repository.record(day, set(), 0, 1000, "UTC")
                repository.observeSessions().first().single().sets.single().id
            } finally { first.close() }
            assertTrue(context.getDatabasePath(name).isFile)
            val second = Room.databaseBuilder(context, DietHelperDatabase::class.java, name).build()
            try {
                val restored = TrainingJournalRepositoryImpl(second).observeSessions().first().single()
                assertEquals(id, restored.sets.single().id)
                assertEquals(1000L, restored.sets.single().completedAt)
                assertEquals(1000L, restored.lastCompletedSetAt)
            } finally { second.close() }
        }

    @Test
    fun `editing an earlier completed set preserves its timestamp and latest set clock`() =
        // mp-real-io: repository transactions execute on Room's real database executor.
        runBlocking {
            val database = Room.inMemoryDatabaseBuilder(context, DietHelperDatabase::class.java).build()
            try {
                val repository = TrainingJournalRepositoryImpl(database)
                repository.record(day, set(), 0, 1000, "UTC")
                repository.record(day, set(1), 0, 2000, "UTC")
                repository.record(day, set().copy(actualReps = 2, completedAt = 9999), 0, 3000, "UTC")
                val session = repository.observeSessions().first().single()
                assertEquals(1000L, session.sets.single { it.planKey == set().planKey }.completedAt)
                assertEquals(2, session.sets.single { it.planKey == set().planKey }.actualReps)
                assertEquals(2000L, session.lastCompletedSetAt)
            } finally { database.close() }
        }

    @Test
    fun `undo removes the last clock source and completing it again records a fresh time`() =
        // mp-real-io: repository transactions execute on Room's real database executor.
        runBlocking {
            val database = Room.inMemoryDatabaseBuilder(context, DietHelperDatabase::class.java).build()
            try {
                val repository = TrainingJournalRepositoryImpl(database)
                repository.record(day, set(), 0, 1000, "UTC")
                repository.record(day, set(1), 0, 2000, "UTC")
                val before = repository.observeSessions().first().single()
                val last = before.sets.single { it.planKey == set(1).planKey }
                repository.undo(before.id, last.id)
                assertEquals(1000L, repository.observeSessions().first().single().lastCompletedSetAt)
                repository.record(day, set(1), 0, 5000, "UTC")
                val after = repository.observeSessions().first().single()
                assertEquals(last.id, after.sets.single { it.planKey == last.planKey }.id)
                assertEquals(5000L, after.lastCompletedSetAt)
            } finally { database.close() }
        }

    @Test
    fun `editing a historical completed set leaves its unknown time unknown`() =
        // mp-real-io: the legacy null timestamp is read back through the real DAO and mapper.
        runBlocking {
            val database = Room.inMemoryDatabaseBuilder(context, DietHelperDatabase::class.java).build()
            try {
                val repository = TrainingJournalRepositoryImpl(database)
                repository.record(day, set(), 0, 1000, "UTC")
                database.openHelper.writableDatabase.execSQL("UPDATE training_sets SET completedAt = NULL")
                repository.record(day, set().copy(actualWeightKg = 75.0), 0, 3000, "UTC")
                val session = repository.observeSessions().first().single()
                assertNull(session.sets.single().completedAt)
                assertNull(session.lastCompletedSetAt)
                assertEquals(75.0, session.sets.single().actualWeightKg!!, 0.001)
            } finally { database.close() }
        }

    @Test
    fun `last completion time stays independent for equal day ids in different programs`() =
        // mp-real-io: program scope is checked through real persisted sessions.
        runBlocking {
            val database = Room.inMemoryDatabaseBuilder(context, DietHelperDatabase::class.java).build()
            try {
                val repository = TrainingJournalRepositoryImpl(database)
                repository.record(day, set(), 0, 1000, "UTC")
                repository.record(day.copy(program = WorkoutProgram.RETURN), set(), 0, 5000, "UTC")
                val sessions = repository.observeSessions().first()
                assertEquals(1000L, sessions.single { it.program == WorkoutProgram.SRC7 }.lastCompletedSetAt)
                assertEquals(5000L, sessions.single { it.program == WorkoutProgram.RETURN }.lastCompletedSetAt)
            } finally { database.close() }
        }
}
