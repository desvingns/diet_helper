@file:Suppress("LongParameterList")

package com.k.shavrin.diethelper.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "training_sessions")
data class TrainingSessionEntity(
    @PrimaryKey val id: String,
    val planDayId: Int,
    val cycle: Int,
    val day: Int,
    val startedAt: Long,
    val zoneId: String,
    val plannedSetCount: Int,
    val legacySetCount: Int,
    val finishedAt: Long?,
    val outcome: String,
    val gymMinutes: Int?,
    val socialMinutes: Int?,
    val sessionRpe: Double?,
    val recovery: String,
    val notes: String,
    val nextDayRecovery: String,
    val nextDayNotes: String,
    @ColumnInfo(defaultValue = "'src7'") val programId: String = "src7",
    val warmUpStartedAt: Long? = null,
    val warmUpFinishedAt: Long? = null,
    val coolDownStartedAt: Long? = null,
    val coolDownFinishedAt: Long? = null
)

@Entity(
    tableName = "training_sets",
    foreignKeys = [ForeignKey(
        entity = TrainingSessionEntity::class,
        parentColumns = ["id"], childColumns = ["sessionId"], onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("sessionId"), Index(value = ["sessionId", "planKey"], unique = true)]
)
data class TrainingSetEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val planKey: String?,
    val exerciseName: String,
    val plannedExerciseName: String?,
    val plannedWeightKg: Double?,
    val plannedReps: Int?,
    val actualWeightKg: Double?,
    val actualReps: Int,
    val loadBasis: String,
    val rpe: Double?,
    val symptomNote: String,
    val preference: String,
    val notes: String,
    val completed: Boolean,
    val completedAt: Long? = null
)

data class TrainingSessionWithSets(
    @Embedded val session: TrainingSessionEntity,
    @Relation(parentColumn = "id", entityColumn = "sessionId") val sets: List<TrainingSetEntity>
)
