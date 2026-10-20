package com.k.shavrin.diethelper.domain.model

import kotlin.math.floor

data class WorkoutPlan(val title: String, val days: List<WorkoutDay>, val program: WorkoutProgram = WorkoutProgram.SRC7)

data class WorkoutDay(
    val id: Int,
    val cycle: Int,
    val day: Int,
    val notes: String,
    val exercises: List<WorkoutExercise>,
    val program: WorkoutProgram = WorkoutProgram.SRC7,
    val title: String = ""
) {
    fun sets(): List<WorkoutSet> = exercises.flatMap { exercise ->
        exercise.groups.flatMapIndexed { groupIndex, group ->
            List(group.sets) { index ->
                WorkoutSet("$id:${exercise.id}:$groupIndex:$index", exercise, group, index + 1)
            }
        }
    }
}

data class WorkoutExercise(
    val id: Int,
    val name: String,
    val load: String,
    val base: String,
    val factor: Double,
    val rounding: Double,
    val equipmentMultiplier: Double,
    val groups: List<WorkoutGroup>,
    val loadBasis: TrainingLoadBasis = TrainingLoadBasis.TOTAL_KG
) {
    fun weight(maxima: Map<String, Double>, percent: Double): Double {
        val maximum = maxima[base] ?: 0.0
        return floor(maximum * factor * percent / rounding + 0.5 + 1e-9) * rounding
    }
}

data class WorkoutGroup(val reps: Int, val sets: Int, val percent: Double)
data class WorkoutSet(val key: String, val exercise: WorkoutExercise, val group: WorkoutGroup, val number: Int)

data class WorkoutProgress(
    val maxima: Map<String, Double> = emptyMap(),
    val completedDays: Set<Int> = emptySet(),
    val completedSets: Set<String> = emptySet(),
    val restSeconds: Int = 120,
    val restDeadline: Long = 0,
    val pausedRestSeconds: Int = 0,
    val program: WorkoutProgram = WorkoutProgram.SRC7,
    val returnWeekLoads: Map<Int, Map<String, Double>> = emptyMap()
) {
    val configured: Boolean get() = listOf("squat", "bench", "deadlift").all { (maxima[it] ?: 0.0) > 0 }

    fun loadsFor(day: WorkoutDay): Map<String, Double> =
        if (day.program == WorkoutProgram.RETURN) returnWeekLoads[day.cycle].orEmpty() else maxima

    fun configuredFor(day: WorkoutDay): Boolean = program == day.program &&
        listOf("squat", "bench", "deadlift").all { (loadsFor(day)[it] ?: 0.0) > 0 }
}
