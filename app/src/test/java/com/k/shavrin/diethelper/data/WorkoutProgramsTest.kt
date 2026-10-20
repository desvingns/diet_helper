package com.k.shavrin.diethelper.data

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import com.k.shavrin.diethelper.data.export.TrainingJournalJsonCodec
import com.k.shavrin.diethelper.data.local.DietHelperDatabase
import com.k.shavrin.diethelper.data.local.TrainingJournalMigration
import com.k.shavrin.diethelper.data.local.WorkoutProgramsMigration
import com.k.shavrin.diethelper.data.repository.TrainingJournalRepositoryImpl
import com.k.shavrin.diethelper.data.repository.WorkoutRepositoryImpl
import com.k.shavrin.diethelper.domain.model.PerformedTrainingSet
import com.k.shavrin.diethelper.domain.model.ReturnToTrainingPlan
import com.k.shavrin.diethelper.domain.model.TrainingOutcome
import com.k.shavrin.diethelper.domain.model.TrainingSession
import com.k.shavrin.diethelper.domain.model.TrainingSessionFeedback
import com.k.shavrin.diethelper.domain.model.WorkoutProgram
import com.k.shavrin.diethelper.domain.repository.TrainingJournalExporter
import com.k.shavrin.diethelper.domain.usecase.workout.ExportTrainingJournalUseCase
import com.k.shavrin.diethelper.domain.usecase.workout.RecordTrainingSetUseCase
import com.k.shavrin.diethelper.domain.usecase.workout.SaveReturnWeekLoadsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Clock

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class WorkoutProgramsTest {
    private val context get() = ApplicationProvider.getApplicationContext<Application>()
    private lateinit var database: DietHelperDatabase
    private lateinit var journal: TrainingJournalRepositoryImpl
    private val preferences = ProgramPreferencesFake()
    private val workouts get() = WorkoutRepositoryImpl(context, preferences)
    private val loads = mapOf("squat" to 100.0, "bench" to 80.0, "deadlift" to 60.0)
    private val returnDay = ReturnToTrainingPlan.create().days.first()
    private val set get() = PerformedTrainingSet(planKey = returnDay.sets().first().key,
        exerciseName = "Squat", plannedWeightKg = 100.0, actualWeightKg = 100.0, actualReps = 3)

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(context, DietHelperDatabase::class.java).build()
        journal = TrainingJournalRepositoryImpl(database)
    }

    @After
    fun close() { database.close() }

    @Test
    fun `switching programs preserves old progress maxima and both independent timers`() = runTest {
        val repo = workouts
        repo.saveMaxima(loads.mapValues { it.value * 2 })
        repo.setCompleted("0:1:0:0", true, 5000)
        repo.completeDay(0)
        repo.setRest(180, 9000, 20)
        val source = repo.progress.first()
        repo.selectProgram(WorkoutProgram.RETURN)
        assertTrue(repo.progress.first().completedDays.isEmpty())
        repo.saveReturnWeekLoads(1, loads)
        repo.setCompleted("0:1:0:0", true, 7000, WorkoutProgram.RETURN)
        repo.completeDay(1, WorkoutProgram.RETURN)
        repo.setRest(90, 8000, 15, WorkoutProgram.RETURN)
        val restarted = workouts
        assertEquals(WorkoutProgram.RETURN, restarted.progress.first().program)
        assertEquals(setOf(1), restarted.progress.first().completedDays)
        restarted.selectProgram(WorkoutProgram.SRC7)
        val restored = restarted.progress.first()
        assertEquals(source.completedDays, restored.completedDays)
        assertEquals(source.completedSets, restored.completedSets)
        assertEquals(source.maxima, restored.maxima)
        assertEquals(source.restDeadline, restored.restDeadline)
        assertEquals(source.pausedRestSeconds, restored.pausedRestSeconds)
        restarted.selectProgram(WorkoutProgram.RETURN)
        assertEquals(8000L, restarted.progress.first().restDeadline)
        assertEquals(15, restarted.progress.first().pausedRestSeconds)
    }

    @Test
    fun `new week has no inferred weights and source maxima cannot configure return training`() = runTest {
        val repo = workouts
        repo.saveMaxima(loads)
        repo.selectProgram(WorkoutProgram.RETURN)
        assertFalse(repo.progress.first().configuredFor(returnDay))
        SaveReturnWeekLoadsUseCase(repo)(1, loads)
        val progress = repo.progress.first()
        assertTrue(progress.configuredFor(returnDay))
        assertFalse(progress.configuredFor(ReturnToTrainingPlan.create().days[5]))
        assertTrue(progress.loadsFor(ReturnToTrainingPlan.create().days[5]).isEmpty())
        repo.saveMaxima(loads.mapValues { it.value * 3 })
        assertEquals(loads, repo.progress.first().loadsFor(returnDay))
    }

    @Test
    fun `same day and set ids in different programs produce independent active sessions`() = runTest {
        val sourceDay = returnDay.copy(program = WorkoutProgram.SRC7)
        journal.record(sourceDay, set, 0, 1000, "UTC")
        journal.record(returnDay, set.copy(actualWeightKg = 60.0), 0, 2000, "UTC")
        assertEquals(2, journal.observeSessions().first().size)
        journal.finish(returnDay, TrainingSessionFeedback(TrainingOutcome.MODIFIED, notes = "Return only"), 0, 3000, "UTC")
        val sessions = journal.observeSessions().first()
        assertEquals(TrainingOutcome.ACTIVE, sessions.single { it.program == WorkoutProgram.SRC7 }.outcome)
        assertEquals(TrainingOutcome.MODIFIED, sessions.single { it.program == WorkoutProgram.RETURN }.outcome)
        assertEquals(100.0, sessions.single { it.program == WorkoutProgram.SRC7 }.sets.single().actualWeightKg!!, 0.001)
        val json = TrainingJournalJsonCodec.encode(sessions)
        assertEquals(2, json.getInt("schema_version"))
        val exported = json.getJSONArray("sessions")
        assertEquals(setOf("src7", "return"), (0 until exported.length()).map { exported.getJSONObject(it).getString("program_id") }.toSet())
    }

    @Test
    fun `stale program or unreviewed week cannot record a set`() = runTest {
        val repo = workouts
        repo.saveMaxima(loads)
        val record = RecordTrainingSetUseCase(journal, repo, Clock.systemUTC())
        rejected { record(returnDay, set) }
        repo.selectProgram(WorkoutProgram.RETURN)
        rejected { record(returnDay, set) }
        repo.saveReturnWeekLoads(1, loads)
        val nextWeek = ReturnToTrainingPlan.create().days[5]
        rejected { record(nextWeek, set.copy(planKey = nextWeek.sets().first().key)) }
        assertTrue(journal.observeSessions().first().isEmpty())
        record(returnDay, set)
        assertEquals(WorkoutProgram.RETURN, journal.observeSessions().first().single().program)
    }

    @Test
    fun `return weight edits do not rewrite recorded snapshots`() = runTest {
        val repo = workouts
        repo.selectProgram(WorkoutProgram.RETURN)
        repo.saveReturnWeekLoads(1, loads)
        val record = RecordTrainingSetUseCase(journal, repo, Clock.systemUTC())
        record(returnDay, set)
        repo.saveReturnWeekLoads(1, loads.mapValues { it.value + 10 })
        record(returnDay, set.copy(plannedWeightKg = 110.0, actualWeightKg = 105.0))
        val logged = journal.observeSessions().first().single().sets.single()
        assertEquals(100.0, logged.plannedWeightKg!!, 0.001)
        assertEquals(105.0, logged.actualWeightKg!!, 0.001)
    }

    @Test
    fun `export contains only selected program when explicitly scoped`() = runTest {
        journal.record(returnDay, set, 0, 1000, "UTC")
        journal.record(returnDay.copy(program = WorkoutProgram.SRC7), set, 0, 2000, "UTC")
        var exported = emptyList<TrainingSession>()
        val exporter = object : TrainingJournalExporter {
            override suspend fun export(sessions: List<TrainingSession>): List<String> {
                exported = sessions
                return listOf("report.md", "report.json")
            }
        }
        ExportTrainingJournalUseCase(journal, exporter)(WorkoutProgram.RETURN)
        assertEquals(listOf(WorkoutProgram.RETURN), exported.map { it.program })
    }

    @Test
    fun `invalid weekly loads cannot create a reviewed week`() = runTest {
        val save = SaveReturnWeekLoadsUseCase(workouts)
        rejected { save(0, loads) }
        rejected { save(7, loads) }
        rejected { save(2, loads - "bench") }
        rejected { save(2, loads + ("bench" to Double.NaN)) }
        rejected { save(2, loads + ("squat" to 0.0)) }
        assertTrue(workouts.progress.first().returnWeekLoads.isEmpty())
    }

    @Test
    fun `program migration assigns existing journal rows to source without losing set data`() {
        val helper = FrameworkSQLiteOpenHelperFactory().create(SupportSQLiteOpenHelper.Configuration.builder(context)
            .callback(object : SupportSQLiteOpenHelper.Callback(4) {
                override fun onCreate(db: SupportSQLiteDatabase) { TrainingJournalMigration.migrate(db) }
                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
            }).build())
        try {
            val db = helper.writableDatabase
            db.execSQL("INSERT INTO training_sessions VALUES ('old', 0, 1, 1, 1000, 'UTC', 3, 1, NULL, 'ACTIVE', NULL, NULL, NULL, 'UNKNOWN', 'Keep note', 'UNKNOWN', '')")
            db.execSQL("INSERT INTO training_sets VALUES ('set', 'old', '0:1:0:0', 'Squat', 'Squat', 70, 3, 65, 3, 'TOTAL_KG', 6, '', 'NEUTRAL', '', 1)")
            WorkoutProgramsMigration.migrate(db)
            db.query("SELECT programId, notes, legacySetCount FROM training_sessions WHERE id = 'old'").use {
                assertTrue(it.moveToFirst())
                assertEquals("src7", it.getString(0))
                assertEquals("Keep note", it.getString(1))
                assertEquals(1, it.getInt(2))
            }
            db.query("SELECT actualWeightKg, completed FROM training_sets WHERE id = 'set'").use {
                assertTrue(it.moveToFirst())
                assertEquals(65.0, it.getDouble(0), 0.001)
                assertEquals(1, it.getInt(1))
            }
        } finally { helper.close() }
    }

    private suspend fun rejected(block: suspend () -> Unit) {
        var failed = false
        try { block() } catch (_: IllegalArgumentException) { failed = true }
        assertTrue(failed)
    }
}

private class ProgramPreferencesFake : DataStore<Preferences> {
    override val data = MutableStateFlow(emptyPreferences())
    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences =
        transform(data.value).also { data.value = it }
}
