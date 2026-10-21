@file:Suppress("LongParameterList")

package com.k.shavrin.diethelper.domain.model

enum class TrainingOutcome { ACTIVE, COMPLETED, MODIFIED, STOPPED }
enum class RecoveryStatus { UNKNOWN, NORMAL, DISCOMFORT, ILLNESS }
enum class ExercisePreference { NEUTRAL, LIKED, DISLIKED }
enum class TrainingLoadBasis { TOTAL_KG, PER_HAND_KG, BODYWEIGHT }
enum class TrainingTimerAction { START_WARMUP, FINISH_WARMUP, START_COOLDOWN, FINISH_COOLDOWN }

data class TrainingSession(
    val id: String,
    val planDayId: Int,
    val cycle: Int,
    val day: Int,
    val startedAt: Long,
    val zoneId: String,
    val plannedSetCount: Int,
    val legacySetCount: Int = 0,
    val finishedAt: Long? = null,
    val outcome: TrainingOutcome = TrainingOutcome.ACTIVE,
    val gymMinutes: Int? = null,
    val socialMinutes: Int? = null,
    val sessionRpe: Double? = null,
    val recovery: RecoveryStatus = RecoveryStatus.UNKNOWN,
    val notes: String = "",
    val nextDayRecovery: RecoveryStatus = RecoveryStatus.UNKNOWN,
    val nextDayNotes: String = "",
    val sets: List<PerformedTrainingSet> = emptyList(),
    val program: WorkoutProgram = WorkoutProgram.SRC7,
    val warmUpStartedAt: Long? = null,
    val warmUpFinishedAt: Long? = null,
    val coolDownStartedAt: Long? = null,
    val coolDownFinishedAt: Long? = null
) {
    val trainingMinutes: Int? get() = gymMinutes?.let { total -> socialMinutes?.let { total - it } }
    val completedSets: List<PerformedTrainingSet> get() = sets.filter { it.completed }
    fun elapsedMillis(now: Long): Long = ((coolDownFinishedAt ?: finishedAt ?: now) - startedAt).coerceAtLeast(0)
    fun warmUpMillis(
        now: Long
    ): Long? = warmUpStartedAt?.let { ((warmUpFinishedAt ?: finishedAt ?: now) - it).coerceAtLeast(0) }
    fun coolDownMillis(
        now: Long
    ): Long? = coolDownStartedAt?.let { ((coolDownFinishedAt ?: finishedAt ?: now) - it).coerceAtLeast(0) }
}

data class PerformedTrainingSet(
    val id: String = "",
    val sessionId: String = "",
    val planKey: String? = null,
    val exerciseName: String,
    val plannedExerciseName: String? = null,
    val plannedWeightKg: Double? = null,
    val plannedReps: Int? = null,
    val actualWeightKg: Double? = null,
    val actualReps: Int,
    val loadBasis: TrainingLoadBasis = TrainingLoadBasis.TOTAL_KG,
    val rpe: Double? = null,
    val symptomNote: String = "",
    val preference: ExercisePreference = ExercisePreference.NEUTRAL,
    val notes: String = "",
    val completed: Boolean = true
)

data class TrainingSessionFeedback(
    val outcome: TrainingOutcome,
    val gymMinutes: Int? = null,
    val socialMinutes: Int? = null,
    val sessionRpe: Double? = null,
    val recovery: RecoveryStatus = RecoveryStatus.UNKNOWN,
    val notes: String = ""
)

data class WorkoutWaveCycle(
    val cycle: Int,
    val days: Int,
    val sets: Int,
    val repetitions: Int,
    val primaryPeak: Double,
    val heavyExercises: Int,
    val mediumExercises: Int,
    val lightExercises: Int
)

fun completedTrainingPlanKeys(legacyKeys: Set<String>, records: List<PerformedTrainingSet>): Set<String> =
    (legacyKeys + records.filter { it.completed }.mapNotNull { it.planKey }) -
        records.filter { !it.completed }.mapNotNull { it.planKey }.toSet()
