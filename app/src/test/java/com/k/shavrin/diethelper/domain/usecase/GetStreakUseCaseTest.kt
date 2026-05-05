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
}
