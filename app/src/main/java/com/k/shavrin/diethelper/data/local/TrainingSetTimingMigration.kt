package com.k.shavrin.diethelper.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object TrainingSetTimingMigration : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Existing sets have no verified completion time; preserve that unknown value.
        db.execSQL("ALTER TABLE training_sets ADD COLUMN completedAt INTEGER")
    }
}
