package com.k.shavrin.diethelper.domain.model

import java.time.LocalDate

data class ReportEntry(
    val productName: String,
    val grams: Int,
    val calories: Float,
    val protein: Float,
    val fat: Float,
    val carbs: Float
)

data class ReportDay(
    val date: LocalDate,
    val summary: DailySummary,
    val entriesByMeal: Map<MealType, List<ReportEntry>>
)

data class ReportStats(
    val daysCount: Int,
    val nonEmptyDaysCount: Int,
    val averageCalories: Float,
    val averageProtein: Float,
    val averageFat: Float,
    val averageCarbs: Float,
    val daysHitCaloriePercent: Float
)

data class ReportData(
    val config: ExportConfig,
    val goals: DailyGoals,
    val days: List<ReportDay>,
    val stats: ReportStats?
)
