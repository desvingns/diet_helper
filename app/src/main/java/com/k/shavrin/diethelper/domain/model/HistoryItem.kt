package com.k.shavrin.diethelper.domain.model

import java.time.LocalDate

data class HistoryItem(
    val date: LocalDate,
    val totalCalories: Float
)
