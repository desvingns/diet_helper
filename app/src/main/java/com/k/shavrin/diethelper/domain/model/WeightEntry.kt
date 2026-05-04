package com.k.shavrin.diethelper.domain.model

import java.time.LocalDate

data class WeightEntry(
    val id: Long = 0,
    val date: LocalDate,
    val weightKg: Float
)
