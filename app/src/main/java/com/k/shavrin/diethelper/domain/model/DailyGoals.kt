package com.k.shavrin.diethelper.domain.model

data class DailyGoals(
    val calories: Float,
    val proteinMin: Float,
    val proteinMax: Float,
    val fatMin: Float,
    val fatMax: Float,
    val carbsMin: Float,
    val carbsMax: Float
) {
    companion object {
        val DEFAULT = DailyGoals(
            calories = 2000f,
            proteinMin = 120f,
            proteinMax = 180f,
            fatMin = 55f,
            fatMax = 80f,
            carbsMin = 200f,
            carbsMax = 280f
        )
    }
}
