package com.k.shavrin.diethelper.domain.usecase.foodentry

import com.k.shavrin.diethelper.domain.repository.FoodEntryRepository
import com.k.shavrin.diethelper.domain.repository.GoalsRepository
import com.k.shavrin.diethelper.domain.usecase.foodentry.GetDailySummaryUseCase.Companion.toSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import javax.inject.Inject

class GetStreakUseCase @Inject constructor(
    private val foodEntryRepository: FoodEntryRepository,
    private val goalsRepository: GoalsRepository
) {
    operator fun invoke(): Flow<Int> {
        val today = LocalDate.now()
        val dates = (0..89).map { today.minusDays(it.toLong()) }

        return combine(
            foodEntryRepository.getEntriesForDates(dates),
            goalsRepository.getDailyGoals()
        ) { entries, goals ->
            val threshold = goals.calories * 0.30f

            val caloriesByDate: Map<LocalDate, Float> = dates.associateWith { day ->
                entries.filter { it.date == day }.toSummary().totalCalories
            }

            var streak = 0

            // Walk backward from yesterday
            var day = today.minusDays(1)
            while (!day.isBefore(today.minusDays(89))) {
                val cal = caloriesByDate[day] ?: 0f
                if (cal >= threshold) {
                    streak++
                    day = day.minusDays(1)
                } else {
                    break
                }
            }

            // Add today if today's calories >= 30% goal
            val todayCal = caloriesByDate[today] ?: 0f
            if (todayCal >= threshold) {
                streak++
            }

            streak
        }
    }
}
