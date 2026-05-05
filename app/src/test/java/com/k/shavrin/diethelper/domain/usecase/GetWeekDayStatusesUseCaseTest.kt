package com.k.shavrin.diethelper.domain.usecase

import app.cash.turbine.test
import com.k.shavrin.diethelper.data.FakeFoodEntryRepository
import com.k.shavrin.diethelper.data.FakeGoalsRepository
import com.k.shavrin.diethelper.domain.model.DailyGoals
import com.k.shavrin.diethelper.domain.model.DayStatus
import com.k.shavrin.diethelper.domain.model.FoodEntry
import com.k.shavrin.diethelper.domain.model.MealType
import com.k.shavrin.diethelper.domain.model.Product
import com.k.shavrin.diethelper.domain.usecase.foodentry.GetWeekDayStatusesUseCase
import com.k.shavrin.diethelper.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class GetWeekDayStatusesUseCaseTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val foodRepo = FakeFoodEntryRepository()
    private val goalsRepo = FakeGoalsRepository(
        DailyGoals(
            calories = 2000f,
            proteinMin = 80f, proteinMax = 120f,
            fatMin = 50f, fatMax = 70f,
            carbsMin = 200f, carbsMax = 300f
        )
    )
    private val useCase = GetWeekDayStatusesUseCase(foodRepo, goalsRepo)

    // Monday 2025-04-28
    private val monday = LocalDate.of(2025, 4, 28)

    private val product = Product(
        id = 1, name = "Тест",
        caloriesPer100g = 100f,
        proteinPer100g = 10f,
        fatPer100g = 6f,
        carbsPer100g = 25f
    )

    @Test
    fun `returns 7 days starting from Monday of the given date's week`() = runTest {
        useCase(monday).test {
            val statuses = awaitItem()
            assertEquals(7, statuses.size)
            assertEquals(DayOfWeek.MONDAY, statuses.first().first.dayOfWeek)
            assertEquals(DayOfWeek.SUNDAY, statuses.last().first.dayOfWeek)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `future days have FUTURE status`() = runTest {
        // Monday 2025-04-28 is in the past; use a date far in the future
        val futureMonday = LocalDate.now().plusWeeks(1).with(DayOfWeek.MONDAY)
        useCase(futureMonday).test {
            val statuses = awaitItem()
            statuses.forEach { (_, status) ->
                assertEquals(DayStatus.FUTURE, status)
            }
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `day with no entries gets GRAY_LOGGED status for past date`() = runTest {
        useCase(monday).test {
            val statuses = awaitItem()
            // Monday 2025-04-28 is in the past, no entries → 0 calories < 30% of 2000 → GRAY_LOGGED
            val mondayStatus = statuses.first { it.first == monday }.second
            assertEquals(DayStatus.GRAY_LOGGED, mondayStatus)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `day with green macros gets GREEN status`() = runTest {
        // 15 multiplier * 100 kcal = 1500 kcal (< 2000)
        // protein = 15 * 10 = 150 → not in [80,120], so this won't be GREEN
        // Use multiplier to get: calories=1500, protein=100, fat=60, carbs=250 all in range
        // product: cal=100, prot=10, fat=6, carbs=25
        // multiplier = 10 → cal=1000 (< 2000), prot=100 ∈ [80,120], fat=60 ∈ [50,70], carbs=250 ∈ [200,300]
        foodRepo.seed(
            listOf(
                FoodEntry(
                    productId = 1, product = product, date = monday,
                    mealType = MealType.BREAKFAST, multiplier = 10f
                )
            )
        )
        useCase(monday).test {
            val statuses = awaitItem()
            val mondayStatus = statuses.first { it.first == monday }.second
            assertEquals(DayStatus.GREEN, mondayStatus)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `day with exceeded calories gets RED status`() = runTest {
        // multiplier = 30 → cal = 3000 > 2000 * 1.25 = 2500 → RED
        foodRepo.seed(
            listOf(
                FoodEntry(
                    productId = 1, product = product, date = monday,
                    mealType = MealType.BREAKFAST, multiplier = 30f
                )
            )
        )
        useCase(monday).test {
            val statuses = awaitItem()
            val mondayStatus = statuses.first { it.first == monday }.second
            assertEquals(DayStatus.RED, mondayStatus)
            cancelAndConsumeRemainingEvents()
        }
    }
}
