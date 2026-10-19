package com.k.shavrin.diethelper.domain.usecase.workout

import com.k.shavrin.diethelper.domain.model.WorkoutPlan
import com.k.shavrin.diethelper.domain.model.WorkoutWaveCycle
import javax.inject.Inject

class AnalyzeWorkoutWavesUseCase @Inject constructor() {
    operator fun invoke(
        plan: WorkoutPlan
    ): List<WorkoutWaveCycle> = plan.days.groupBy { it.cycle }.toSortedMap().map { (cycle, days) ->
        val exercises = days.flatMap { it.exercises }
        val primary = exercises.filter { it.name in listOf("Присед", "Жим лежа", "Становая тяга") }
        WorkoutWaveCycle(
            cycle, days.size, exercises.sumOf { e -> e.groups.sumOf { it.sets } },
            exercises.sumOf { e -> e.groups.sumOf { it.sets * it.reps } },
            primary.maxOfOrNull { e -> e.groups.maxOf { it.percent * e.factor } } ?: 0.0,
            exercises.count { it.load == "Тяжелая" }, exercises.count { it.load == "Средняя" },
            exercises.count { it.load == "Легкая" }
        )
    }
}
