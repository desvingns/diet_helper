package com.k.shavrin.diethelper.domain.usecase.foodentry

import com.k.shavrin.diethelper.domain.model.DailyGoals
import com.k.shavrin.diethelper.domain.model.DayStatus
import com.k.shavrin.diethelper.domain.model.FoodEntry
import com.k.shavrin.diethelper.domain.repository.FoodEntryRepository
import com.k.shavrin.diethelper.domain.repository.GoalsRepository
import com.k.shavrin.diethelper.domain.usecase.foodentry.GetDailySummaryUseCase.Companion.toSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject

class GetWeekDayStatusesUseCase @Inject constructor(
    private val foodEntryRepository: FoodEntryRepository,
    private val goalsRepository: GoalsRepository
) {
    operator fun invoke(date: LocalDate): Flow<List<Pair<LocalDate, DayStatus>>> {
        val weekStart = date.with(DayOfWeek.MONDAY)
        val weekDays = (0..6).map { weekStart.plusDays(it.toLong()) }

        return combine(
            foodEntryRepository.getEntriesForDates(weekDays),
            goalsRepository.getDailyGoals()
        ) { entries, goals ->
            weekDays.map { day ->
                val dayEntries = entries.filter { it.date == day }
                day to computeDayStatus(day, dayEntries, goals)
            }
        }
    }

    companion object {
        fun computeDayStatus(
            day: LocalDate,
            entries: List<FoodEntry>,
            goals: DailyGoals
        ): DayStatus {
            val today = LocalDate.now()
            if (day.isAfter(today)) return DayStatus.FUTURE

            val summary = entries.toSummary()
            val calories = summary.totalCalories
            val protein = summary.totalProtein
            val fat = summary.totalFat
            val carbs = summary.totalCarbs

            if (calories < goals.calories * 0.30f) return DayStatus.GRAY_LOGGED

            val isGreen = calories < goals.calories &&
                protein >= goals.proteinMin && protein <= goals.proteinMax &&
                fat >= goals.fatMin && fat <= goals.fatMax &&
                carbs >= goals.carbsMin && carbs <= goals.carbsMax

            val isYellow = !isGreen &&
                calories < goals.calories * 1.25f &&
                protein >= goals.proteinMin * 0.75f && protein <= goals.proteinMax * 1.25f &&
                fat >= goals.fatMin * 0.75f && fat <= goals.fatMax * 1.25f &&
                carbs >= goals.carbsMin * 0.75f && carbs <= goals.carbsMax * 1.25f

            return when {
                isGreen -> DayStatus.GREEN
                isYellow -> DayStatus.YELLOW
                else -> DayStatus.RED
            }
        }
    }
}
