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
import com.k.shavrin.diethelper.data.local.TrainingTimersMigration
import com.k.shavrin.diethelper.data.local.WorkoutProgramsMigration
import com.k.shavrin.diethelper.data.repository.TrainingJournalRepositoryImpl
import com.k.shavrin.diethelper.data.repository.WorkoutRepositoryImpl
import com.k.shavrin.diethelper.domain.model.PerformedTrainingSet
import com.k.shavrin.diethelper.domain.model.RecoveryStatus
import com.k.shavrin.diethelper.domain.model.TrainingLoadBasis
import com.k.shavrin.diethelper.domain.model.TrainingOutcome
import com.k.shavrin.diethelper.domain.model.TrainingSessionFeedback
import com.k.shavrin.diethelper.domain.model.TrainingTimerAction
import com.k.shavrin.diethelper.domain.model.WorkoutDay
import com.k.shavrin.diethelper.domain.model.WorkoutExercise
import com.k.shavrin.diethelper.domain.model.WorkoutGroup
import com.k.shavrin.diethelper.domain.model.WorkoutProgram
import com.k.shavrin.diethelper.domain.usecase.workout.AnalyzeWorkoutWavesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class TrainingJournalPersistenceTest {
    private val context get() = ApplicationProvider.getApplicationContext<Application>()
    private lateinit var database: DietHelperDatabase
    private lateinit var journal: TrainingJournalRepositoryImpl
    private val exercise = WorkoutExercise(4, "Присед", "Тяжелая", "squat", 1.0, 2.5, 1.0, listOf(WorkoutGroup(3, 2, 0.7)))
    private val day = WorkoutDay(0, 1, 1, "", listOf(exercise))
    private val set get() = PerformedTrainingSet(
        planKey = day.sets().first().key, exerciseName = "Присед", plannedExerciseName = "Присед",
        plannedWeightKg = 100.0, plannedReps = 3, actualWeightKg = 95.0, actualReps = 3, rpe = 6.0
    )

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(context, DietHelperDatabase::class.java).build()
        journal = TrainingJournalRepositoryImpl(database)
    }

    @After
    fun close() { database.close() }

    @Test
    fun `warmup starts an empty session and independent timers survive repository recreation`() = runTest {
        journal.advanceTimer(day, TrainingTimerAction.START_WARMUP, 1000, "UTC")
        journal.advanceTimer(day, TrainingTimerAction.START_WARMUP, 2000, "UTC")
        val warming = TrainingJournalRepositoryImpl(database).observeSessions().first().single()
        assertEquals(1000L, warming.startedAt)
        assertEquals(1000L, warming.warmUpStartedAt)
        assertTrue(warming.sets.isEmpty())
        assertEquals(9000L, warming.warmUpMillis(10000))
        journal.advanceTimer(day, TrainingTimerAction.FINISH_WARMUP, 11000, "UTC")
        journal.record(day, set, 0, 12000, "UTC")
        journal.advanceTimer(day, TrainingTimerAction.START_COOLDOWN, 61000, "UTC")
        journal.advanceTimer(day, TrainingTimerAction.FINISH_COOLDOWN, 71000, "UTC")
        journal.finish(day, TrainingSessionFeedback(TrainingOutcome.MODIFIED, notes = "Shortened"), 0, 91000, "UTC")
        val finished = TrainingJournalRepositoryImpl(database).observeSessions().first().single()
        assertEquals(71000L, finished.finishedAt)
        assertEquals(70000L, finished.elapsedMillis(200000))
        assertEquals(10000L, finished.warmUpMillis(200000))
        assertEquals(10000L, finished.coolDownMillis(200000))
        assertEquals(1, finished.sets.size)
        val exported = TrainingJournalJsonCodec.encode(listOf(finished)).getJSONArray("sessions").getJSONObject(0)
        assertEquals("warmup_start", exported.getString("start_basis"))
        assertEquals(70, exported.getInt("timed_total_seconds"))
        assertEquals(10, exported.getInt("warmup_seconds"))
    }

    @Test
    fun `timers stay independent for both programs and cannot steal a day`() = runTest {
        journal.advanceTimer(day, TrainingTimerAction.START_WARMUP, 1000, "UTC")
        journal.advanceTimer(day.copy(program = WorkoutProgram.RETURN), TrainingTimerAction.START_WARMUP, 5000, "UTC")
        assertEquals(2, journal.observeSessions().first().size)
        var rejected = false
        try {
            journal.advanceTimer(day.copy(id = 99), TrainingTimerAction.FINISH_WARMUP, 6000, "UTC")
        } catch (_: IllegalArgumentException) {
            rejected = true
        }
        assertTrue(rejected)
        assertTrue(journal.observeSessions().first().all { it.warmUpFinishedAt == null })
    }

    @Test
    fun `sets cannot be recorded during warmup or cooldown`() = runTest {
        journal.advanceTimer(day, TrainingTimerAction.START_WARMUP, 1000, "UTC")
        var rejected = 0
        try { journal.record(day, set, 0, 2000, "UTC") } catch (_: IllegalArgumentException) { rejected++ }
        journal.advanceTimer(day, TrainingTimerAction.FINISH_WARMUP, 3000, "UTC")
        journal.record(day, set, 0, 4000, "UTC")
        journal.advanceTimer(day, TrainingTimerAction.START_COOLDOWN, 5000, "UTC")
        try { journal.record(day, set, 0, 6000, "UTC") } catch (_: IllegalArgumentException) { rejected++ }
        assertEquals(2, rejected)
        assertEquals(1, journal.observeSessions().first().single().sets.size)
    }

    @Test
    fun `timer migration preserves existing session and set values without inventing timings`() {
        val helper = FrameworkSQLiteOpenHelperFactory().create(SupportSQLiteOpenHelper.Configuration.builder(context)
            .callback(object : SupportSQLiteOpenHelper.Callback(5) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    TrainingJournalMigration.migrate(db)
                    WorkoutProgramsMigration.migrate(db)
                }
                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
            }).build())
        try {
            val db = helper.writableDatabase
            db.execSQL("INSERT INTO training_sessions VALUES ('old', 0, 1, 1, 1000, 'UTC', 3, 1, NULL, 'ACTIVE', NULL, NULL, NULL, 'UNKNOWN', 'Keep note', 'UNKNOWN', '', 'src7')")
            db.execSQL("INSERT INTO training_sets VALUES ('set', 'old', '0:1:0:0', 'Squat', 'Squat', 70, 3, 65, 3, 'TOTAL_KG', 6, '', 'NEUTRAL', '', 1)")
            TrainingTimersMigration.migrate(db)
            db.query("SELECT notes, startedAt, warmUpStartedAt, coolDownFinishedAt FROM training_sessions WHERE id = 'old'").use {
                assertTrue(it.moveToFirst())
                assertEquals("Keep note", it.getString(0))
                assertEquals(1000L, it.getLong(1))
                assertTrue(it.isNull(2))
                assertTrue(it.isNull(3))
            }
            db.query("SELECT actualWeightKg FROM training_sets WHERE id = 'set'").use {
                assertTrue(it.moveToFirst())
                assertEquals(65.0, it.getDouble(0), 0.001)
            }
        } finally { helper.close() }
    }

    @Test
    fun `editing actual load preserves the original plan snapshot and set identity`() = runTest {
        journal.record(day, set, 0, 1000, "Europe/Budapest")
        val before = journal.observeSessions().first().single().sets.single()
        journal.record(day, set.copy(plannedWeightKg = 500.0, actualWeightKg = 97.5), 0, 2000, "Europe/Budapest")
        val reopened = TrainingJournalRepositoryImpl(database).observeSessions().first().single()
        assertEquals(before.id, reopened.sets.single().id)
        assertEquals(100.0, reopened.sets.single().plannedWeightKg!!, 0.001)
        assertEquals(97.5, reopened.sets.single().actualWeightKg!!, 0.001)
        assertEquals(1000L, reopened.startedAt)
    }

    @Test
    fun `partial finish preserves performed sets and follow up remains unknown until supplied`() = runTest {
        journal.record(day, set, 0, 1000, "Europe/Budapest")
        journal.finish(day, TrainingSessionFeedback(TrainingOutcome.MODIFIED, 90, 10, 6.0, notes = "Shortened"), 0, 2000, "Europe/Budapest")
        val session = journal.observeSessions().first().single()
        assertEquals(TrainingOutcome.MODIFIED, session.outcome)
        assertEquals(80, session.trainingMinutes)
        assertEquals(1, session.completedSets.size)
        assertEquals(RecoveryStatus.UNKNOWN, session.nextDayRecovery)
        journal.followUp(session.id, RecoveryStatus.NORMAL, "No change")
        assertEquals("No change", journal.observeSessions().first().single().nextDayNotes)
    }

    @Test
    fun `undo retains an audit row and closed sessions cannot be undone`() = runTest {
        journal.record(day, set, 0, 1000, "UTC")
        val session = journal.observeSessions().first().single()
        journal.undo(session.id, session.sets.single().id)
        assertFalse(journal.observeSessions().first().single().sets.single().completed)
        journal.finish(day, TrainingSessionFeedback(TrainingOutcome.STOPPED, notes = "Stopped"), 0, 2000, "UTC")
        var rejected = false
        try {
            journal.undo(session.id, session.sets.single().id)
        } catch (_: IllegalArgumentException) {
            rejected = true
        }
        assertTrue(rejected)
    }

    @Test
    fun `another day cannot steal the active session`() = runTest {
        journal.record(day, set, 0, 1000, "UTC")
        var rejected = false
        try {
            journal.record(day.copy(id = 1, day = 2), set.copy(planKey = null), 0, 2000, "UTC")
        } catch (_: IllegalArgumentException) {
            rejected = true
        }
        assertTrue(rejected)
        assertEquals(1, journal.observeSessions().first().size)
    }

    @Test
    fun `legacy marks and bodyweight extras are exported without invented loads`() = runTest {
        journal.record(day, PerformedTrainingSet(exerciseName = "Подтягивания", actualReps = 4,
            loadBasis = TrainingLoadBasis.BODYWEIGHT), 1, 1000, "Europe/Budapest")
        journal.record(day, PerformedTrainingSet(exerciseName = "Подтягивания", actualReps = 4,
            loadBasis = TrainingLoadBasis.BODYWEIGHT), 1, 2000, "Europe/Budapest")
        val session = journal.observeSessions().first().single()
        assertEquals(2, session.sets.size)
        assertEquals(1, session.legacySetCount)
        assertNull(session.trainingMinutes)
        val exported = TrainingJournalJsonCodec.encode(listOf(session)).getJSONArray("sessions").getJSONObject(0)
        assertTrue(exported.isNull("training_minutes_including_set_rests"))
        assertEquals("UNKNOWN", exported.getString("next_day_recovery"))
        assertTrue(exported.getJSONArray("sets").getJSONObject(0).isNull("actual_weight_kg"))
        assertEquals(1, exported.getInt("legacy_set_count_without_actual_details"))
    }

    @Test
    fun `all source waves retain their actual set counts and light intensity need not mean low reps`() = runTest {
        val preferences = object : DataStore<Preferences> {
            override val data = MutableStateFlow(emptyPreferences())
            override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
                return transform(data.value).also { data.value = it }
            }
        }
        val waves = AnalyzeWorkoutWavesUseCase()(WorkoutRepositoryImpl(context, preferences).getPlan())
        assertEquals(12, waves.size)
        assertEquals(126, waves[0].sets)
        assertEquals(430, waves[0].repetitions)
        assertEquals(74, waves[4].sets)
        assertEquals(4, waves[4].days)
        assertEquals(1, waves[7].heavyExercises)
        assertEquals(481, waves[7].repetitions)
        assertEquals(3, waves.last().days)
    }

    @Test
    fun `migration adds journal tables while preserving old products and leaving journal empty`() {
        val helper = FrameworkSQLiteOpenHelperFactory().create(SupportSQLiteOpenHelper.Configuration.builder(context)
            .callback(object : SupportSQLiteOpenHelper.Callback(3) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.execSQL("CREATE TABLE products (id INTEGER NOT NULL PRIMARY KEY, name TEXT NOT NULL)")
                    db.execSQL("INSERT INTO products VALUES (17, 'Keep me')")
                }
                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
            }).build())
        try {
            val db = helper.writableDatabase
            TrainingJournalMigration.migrate(db)
            db.query("SELECT name FROM products WHERE id = 17").use {
                assertTrue(it.moveToFirst())
                assertEquals("Keep me", it.getString(0))
            }
            db.query("SELECT COUNT(*) FROM training_sessions").use {
                assertTrue(it.moveToFirst())
                assertEquals(0, it.getInt(0))
            }
            db.query("PRAGMA index_list(training_sets)").use {
                var unique = false
                while (it.moveToNext()) {
                    if (it.getString(it.getColumnIndexOrThrow("name")) == "index_training_sets_sessionId_planKey") {
                        unique = it.getInt(it.getColumnIndexOrThrow("unique")) == 1
                    }
                }
                assertTrue(unique)
            }
        } finally { helper.close() }
    }
}
