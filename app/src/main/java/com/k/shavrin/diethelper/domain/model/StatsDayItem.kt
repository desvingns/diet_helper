package com.k.shavrin.diethelper.domain.model

import java.time.LocalDate

data class StatsDayItem(
    val date: LocalDate,
    val calories: Float,
    val protein: Float,
    val fat: Float,
    val carbs: Float
)
