package com.k.shavrin.diethelper.presentation.screen.workout

import com.k.shavrin.diethelper.domain.model.WorkoutProgram

data class WorkoutProgramActions(
    val select: (WorkoutProgram) -> Unit,
    val saveWeek: (Int, Map<String, Double>) -> Unit
)
