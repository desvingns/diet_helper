package com.k.shavrin.diethelper.domain.model

data class SavedMealItem(
    val id: Long = 0,
    val savedMealId: Long,
    val productId: Long,
    val product: Product,
    val multiplier: Float
)
