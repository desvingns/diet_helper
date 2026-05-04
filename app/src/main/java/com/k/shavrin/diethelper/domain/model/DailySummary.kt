package com.k.shavrin.diethelper.domain.model

data class DailySummary(
    val totalCalories: Float,
    val totalProtein: Float,
    val totalFat: Float,
    val totalCarbs: Float
) {
    companion object {
        val EMPTY = DailySummary(0f, 0f, 0f, 0f)
    }
}
