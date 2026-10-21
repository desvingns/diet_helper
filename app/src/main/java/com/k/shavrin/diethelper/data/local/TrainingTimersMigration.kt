package com.k.shavrin.diethelper.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object TrainingTimersMigration : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        listOf("warmUpStartedAt", "warmUpFinishedAt", "coolDownStartedAt", "coolDownFinishedAt").forEach { column ->
            db.execSQL("ALTER TABLE training_sessions ADD COLUMN $column INTEGER")
        }
    }
}
