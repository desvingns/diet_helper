package com.k.shavrin.diethelper.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object TrainingJournalMigration : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS training_sessions (" +
                "id TEXT NOT NULL PRIMARY KEY, planDayId INTEGER NOT NULL, cycle INTEGER NOT NULL, day INTEGER NOT NULL, " +
                "startedAt INTEGER NOT NULL, zoneId TEXT NOT NULL, plannedSetCount INTEGER NOT NULL, legacySetCount INTEGER NOT NULL, " +
                "finishedAt INTEGER, outcome TEXT NOT NULL, gymMinutes INTEGER, socialMinutes INTEGER, sessionRpe REAL, " +
                "recovery TEXT NOT NULL, notes TEXT NOT NULL, nextDayRecovery TEXT NOT NULL, nextDayNotes TEXT NOT NULL)"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS training_sets (" +
                "id TEXT NOT NULL PRIMARY KEY, sessionId TEXT NOT NULL, planKey TEXT, exerciseName TEXT NOT NULL, " +
                "plannedExerciseName TEXT, plannedWeightKg REAL, plannedReps INTEGER, actualWeightKg REAL, actualReps INTEGER NOT NULL, " +
                "loadBasis TEXT NOT NULL, rpe REAL, symptomNote TEXT NOT NULL, preference TEXT NOT NULL, notes TEXT NOT NULL, completed INTEGER NOT NULL, " +
                "FOREIGN KEY(sessionId) REFERENCES training_sessions(id) ON UPDATE NO ACTION ON DELETE CASCADE)"
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_training_sets_sessionId ON training_sets(sessionId)")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_training_sets_sessionId_planKey ON training_sets(sessionId, planKey)")
    }
}
