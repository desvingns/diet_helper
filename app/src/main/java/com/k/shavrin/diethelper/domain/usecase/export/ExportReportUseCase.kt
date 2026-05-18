package com.k.shavrin.diethelper.domain.usecase.export

import com.k.shavrin.diethelper.domain.model.DailySummary
import com.k.shavrin.diethelper.domain.model.ExportConfig
import com.k.shavrin.diethelper.domain.model.ExportMode
import com.k.shavrin.diethelper.domain.model.FoodEntry
import com.k.shavrin.diethelper.domain.model.MealType
import com.k.shavrin.diethelper.domain.model.ReportData
import com.k.shavrin.diethelper.domain.model.ReportDay
import com.k.shavrin.diethelper.domain.model.ReportEntry
import com.k.shavrin.diethelper.domain.model.ReportStats
import com.k.shavrin.diethelper.domain.repository.FoodEntryRepository
import com.k.shavrin.diethelper.domain.repository.GoalsRepository
import com.k.shavrin.diethelper.domain.repository.ReportRenderer
import com.k.shavrin.diethelper.domain.usecase.foodentry.GetDailySummaryUseCase.Companion.toSummary
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.roundToInt

private const val LOWER_TOLERANCE = 0.9f
private const val UPPER_TOLERANCE = 1.1f
private const val PERCENT_SCALE = 100f

class ExportReportUseCase @Inject constructor(
    private val foodEntryRepository: FoodEntryRepository,
    private val goalsRepository: GoalsRepository,
    private val renderer: ReportRenderer
) {
    suspend operator fun invoke(config: ExportConfig): String {
        val (start, end) = orderRange(config.from, config.to)
        val allDates = expandDates(start, end)

        val entries = foodEntryRepository.getEntriesForDates(allDates).first()
        val goals = goalsRepository.getDailyGoals().first()

        val entriesByDate: Map<LocalDate, List<FoodEntry>> = entries.groupBy { it.date }

        val days = allDates.map { date ->
            val dayEntries = entriesByDate[date].orEmpty()
            val summary = dayEntries.toSummary()
            val entriesByMeal = if (config.mode == ExportMode.SUMMARY_ONLY) {
                emptyMap()
            } else {
                buildEntriesByMeal(dayEntries)
            }
            ReportDay(date = date, summary = summary, entriesByMeal = entriesByMeal)
        }

        val stats = if (config.includeStats) buildStats(days, goals.calories) else null

        val data = ReportData(
            config = config.copy(from = start, to = end),
            goals = goals,
            days = days,
            stats = stats
        )
        return renderer.render(data)
    }

    private fun orderRange(from: LocalDate, to: LocalDate): Pair<LocalDate, LocalDate> =
        if (from.isAfter(to)) to to from else from to to

    private fun expandDates(start: LocalDate, end: LocalDate): List<LocalDate> =
        generateSequence(start) { current ->
            if (current.isBefore(end)) current.plusDays(1) else null
        }.toList()

    private fun buildEntriesByMeal(entries: List<FoodEntry>): Map<MealType, List<ReportEntry>> {
        if (entries.isEmpty()) return emptyMap()
        val grouped = entries.groupBy { it.mealType }
        val result = linkedMapOf<MealType, List<ReportEntry>>()
        for (mealType in MealType.values()) {
            val mealEntries = grouped[mealType].orEmpty().map { it.toReportEntry() }
            if (mealEntries.isNotEmpty()) {
                result[mealType] = mealEntries
            }
        }
        return result
    }

    private fun FoodEntry.toReportEntry(): ReportEntry {
        val m = multiplier
        return ReportEntry(
            productName = product.name,
            grams = (m * 100f).roundToInt(),
            calories = product.caloriesPer100g * m,
            protein = product.proteinPer100g * m,
            fat = product.fatPer100g * m,
            carbs = product.carbsPer100g * m
        )
    }

    private fun buildStats(days: List<ReportDay>, goalCalories: Float): ReportStats {
        val daysCount = days.size
        val nonEmptyDays = days.filter { it.summary.totalCalories > 0f }
        val nonEmptyCount = nonEmptyDays.size

        val avgCal: Float
        val avgPro: Float
        val avgFat: Float
        val avgCarb: Float
        if (nonEmptyCount == 0) {
            avgCal = 0f
            avgPro = 0f
            avgFat = 0f
            avgCarb = 0f
        } else {
            avgCal = nonEmptyDays.sumOf { it.summary.totalCalories.toDouble() }.toFloat() / nonEmptyCount
            avgPro = nonEmptyDays.sumOf { it.summary.totalProtein.toDouble() }.toFloat() / nonEmptyCount
            avgFat = nonEmptyDays.sumOf { it.summary.totalFat.toDouble() }.toFloat() / nonEmptyCount
            avgCarb = nonEmptyDays.sumOf { it.summary.totalCarbs.toDouble() }.toFloat() / nonEmptyCount
        }

        val hitCount = if (goalCalories > 0f) {
            days.count { dayHitGoal(it.summary, goalCalories) }
        } else 0

        val hitPercent = if (daysCount == 0) 0f else hitCount * PERCENT_SCALE / daysCount

        return ReportStats(
            daysCount = daysCount,
            nonEmptyDaysCount = nonEmptyCount,
            averageCalories = avgCal,
            averageProtein = avgPro,
            averageFat = avgFat,
            averageCarbs = avgCarb,
            daysHitCaloriePercent = hitPercent
        )
    }

    private fun dayHitGoal(summary: DailySummary, goal: Float): Boolean {
        val lo = goal * LOWER_TOLERANCE
        val hi = goal * UPPER_TOLERANCE
        return summary.totalCalories in lo..hi
    }
}
