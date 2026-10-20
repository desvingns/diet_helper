package com.k.shavrin.diethelper.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object WorkoutProgramsMigration : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE training_sessions ADD COLUMN programId TEXT NOT NULL DEFAULT 'src7'")
    }
}
