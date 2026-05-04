package com.k.shavrin.diethelper.domain.model

import java.time.LocalDate

data class FoodEntry(
    val id: Long = 0,
    val productId: Long,
    val product: Product,
    val date: LocalDate,
    val mealType: MealType,
    val multiplier: Float
)
