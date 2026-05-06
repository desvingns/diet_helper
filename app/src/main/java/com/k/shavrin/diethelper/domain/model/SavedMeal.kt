package com.k.shavrin.diethelper.domain.model

data class SavedMeal(
    val id: Long = 0,
    val name: String,
    val items: List<SavedMealItem>
)
