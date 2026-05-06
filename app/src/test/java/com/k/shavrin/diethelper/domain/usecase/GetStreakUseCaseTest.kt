package com.k.shavrin.diethelper.domain.usecase

import app.cash.turbine.test
import com.k.shavrin.diethelper.data.FakeFoodEntryRepository
import com.k.shavrin.diethelper.data.FakeGoalsRepository
import com.k.shavrin.diethelper.domain.model.DailyGoals
import com.k.shavrin.diethelper.domain.model.FoodEntry
import com.k.shavrin.diethelper.domain.model.MealType
import com.k.shavrin.diethelper.domain.model.Product
import com.k.shavrin.diethelper.domain.usecase.foodentry.GetStreakUseCase
import com.k.shavrin.diethelper.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class GetStreakUseCaseTest {

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
    private val useCase = GetStreakUseCase(foodRepo, goalsRepo)

    // threshold = 2000 * 0.30 = 600 kcal
    // product with 700 kcal per multiplier=1 → 700 * 1 = 700 ≥ 600 → counts for streak
    private val product = Product(
        id = 1, name = "Тест",
        caloriesPer100g = 700f,
        proteinPer100g = 10f,
        fatPer100g = 6f,
        carbsPer100g = 25f
    )

    private fun entryFor(date: LocalDate) = FoodEntry(
        productId = 1, product = product, date = date,
        mealType = MealType.BREAKFAST, multiplier = 1f
    )

    @Test
    fun `streak is zero when no entries`() = runTest {
        useCase().test {
            assertEquals(0, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `streak counts consecutive days backward from yesterday plus today`() = runTest {
        val today = LocalDate.now()
        foodRepo.seed(
            listOf(
                entryFor(today),
                entryFor(today.minusDays(1)),
                entryFor(today.minusDays(2))
            )
        )
        useCase().test {
            assertEquals(3, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `streak breaks on missing day`() = runTest {
        val today = LocalDate.now()
        // today and 2 days ago logged, yesterday NOT logged → streak = 1 (only today)
        foodRepo.seed(
            listOf(
                entryFor(today),
                entryFor(today.minusDays(2))
            )
        )
        useCase().test {
            assertEquals(1, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `streak is 1 when only today is logged`() = runTest {
        val today = LocalDate.now()
        foodRepo.seed(listOf(entryFor(today)))
        useCase().test {
            assertEquals(1, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `streak is 1 when only yesterday is logged and today is not`() = runTest {
        val today = LocalDate.now()
        foodRepo.seed(listOf(entryFor(today.minusDays(1))))
        useCase().test {
            assertEquals(1, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `streak does not count today when today calories are below threshold`() = runTest {
        val today = LocalDate.now()
        // Low-calorie product: 100 kcal * 1 = 100 < 600 threshold
        val lowProduct = Product(
            id = 2, name = "Малокалорийный",
            caloriesPer100g = 100f,
            proteinPer100g = 1f, fatPer100g = 1f, carbsPer100g = 5f
        )
        val lowEntry = FoodEntry(
            productId = 2, product = lowProduct, date = today,
            mealType = MealType.BREAKFAST, multiplier = 1f
        )
        // yesterday is logged above threshold, today is below threshold
        foodRepo.seed(listOf(entryFor(today.minusDays(1)), lowEntry))
        useCase().test {
            // yesterday contributes streak=1, today does NOT add because 100 < 600
            assertEquals(1, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `streak adds today when today calories exactly meet threshold`() = runTest {
        val today = LocalDate.now()
        // 600 kcal exactly = 2000 * 0.30 → threshold met
        val thresholdProduct = Product(
            id = 3, name = "Граница",
            caloriesPer100g = 600f,
            proteinPer100g = 5f, fatPer100g = 5f, carbsPer100g = 20f
        )
        val thresholdEntry = FoodEntry(
            productId = 3, product = thresholdProduct, date = today,
            mealType = MealType.BREAKFAST, multiplier = 1f
        )
        foodRepo.seed(listOf(thresholdEntry))
        useCase().test {
            assertEquals(1, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `streak is zero when yesterday calories are below threshold and today is empty`() = runTest {
        val today = LocalDate.now()
        val lowProduct = Product(
            id = 2, name = "Малокалорийный",
            caloriesPer100g = 100f,
            proteinPer100g = 1f, fatPer100g = 1f, carbsPer100g = 5f
        )
        val lowEntry = FoodEntry(
            productId = 2, product = lowProduct, date = today.minusDays(1),
            mealType = MealType.BREAKFAST, multiplier = 1f
        )
        // yesterday below threshold, today empty → streak = 0
        foodRepo.seed(listOf(lowEntry))
        useCase().test {
            assertEquals(0, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }
}
