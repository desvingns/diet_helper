package com.k.shavrin.diethelper.data.local

import android.app.Application
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class TrainingSetTimingMigrationTest {
    @Test
    fun `six to seven preserves complete old records and leaves completion times unknown`() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val helper = FrameworkSQLiteOpenHelperFactory().create(SupportSQLiteOpenHelper.Configuration.builder(context)
            .callback(object : SupportSQLiteOpenHelper.Callback(6) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    TrainingJournalMigration.migrate(db)
                    WorkoutProgramsMigration.migrate(db)
                    TrainingTimersMigration.migrate(db)
                }
                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
            }).build())
        try {
            val db = helper.writableDatabase
            db.execSQL("INSERT INTO training_sessions VALUES ('old', 0, 1, 1, 1000, 'UTC', 3, 1, NULL, " +
                "'ACTIVE', NULL, NULL, NULL, 'UNKNOWN', 'Keep note', 'UNKNOWN', '', 'src7', 1000, 2000, NULL, NULL)")
            db.execSQL("INSERT INTO training_sets VALUES ('set', 'old', '0:1:0:0', 'Squat', 'Squat', 70, 3, 65, 3, 'TOTAL_KG', 6, 'Keep symptom', 'LIKED', 'Keep set', 1)")
            val sessionBefore = row(db, "training_sessions")
            val setBefore = row(db, "training_sets")
            TrainingSetTimingMigration.migrate(db)
            assertEquals(sessionBefore, row(db, "training_sessions"))
            val setAfter = row(db, "training_sets")
            assertEquals(setBefore, setAfter.dropLast(1))
            assertEquals(setBefore.size + 1, setAfter.size)
            assertTrue(setAfter.last() == null)
            db.query("PRAGMA index_list(training_sets)").use { cursor ->
                var uniquePlanIndex = false
                while (cursor.moveToNext()) {
                    if (cursor.getString(cursor.getColumnIndexOrThrow("name")) == "index_training_sets_sessionId_planKey") {
                        uniquePlanIndex = cursor.getInt(cursor.getColumnIndexOrThrow("unique")) == 1
                    }
                }
                assertTrue(uniquePlanIndex)
            }
            db.query("PRAGMA foreign_key_list(training_sets)").use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals("training_sessions", cursor.getString(cursor.getColumnIndexOrThrow("table")))
                assertEquals("CASCADE", cursor.getString(cursor.getColumnIndexOrThrow("on_delete")))
            }
        } finally { helper.close() }
    }

    private fun row(database: SupportSQLiteDatabase, table: String): List<String?> {
        val columns = database.query("PRAGMA table_info($table)").use { cursor ->
            buildList {
                while (cursor.moveToNext()) add(cursor.getString(cursor.getColumnIndexOrThrow("name")))
            }
        }
        // An explicit projection avoids stale SELECT * metadata cached before ALTER TABLE.
        val projection = columns.joinToString(", ") { "\"$it\"" }
        return database.query("SELECT $projection FROM $table").use { cursor ->
            assertTrue(cursor.moveToFirst())
            (0 until cursor.columnCount).map { if (cursor.isNull(it)) null else cursor.getString(it) }
        }
    }
}
