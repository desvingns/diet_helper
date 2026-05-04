package com.k.shavrin.diethelper.domain.model

data class Product(
    val id: Long = 0,
    val name: String,
    val caloriesPer100g: Float,
    val proteinPer100g: Float,
    val fatPer100g: Float,
    val carbsPer100g: Float,
    val isFavorite: Boolean = false
)
