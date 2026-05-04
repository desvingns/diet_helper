package com.k.shavrin.diethelper.domain.model

data class DailyGoals(
    val calories: Float,
    val protein: Float,
    val fat: Float,
    val carbs: Float
) {
    companion object {
        val DEFAULT = DailyGoals(
            calories = 2000f,
            protein = 150f,
            fat = 67f,
            carbs = 250f
        )
    }
}
