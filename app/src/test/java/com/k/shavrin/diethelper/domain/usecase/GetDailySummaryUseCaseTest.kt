package com.k.shavrin.diethelper.domain.usecase

import app.cash.turbine.test
import com.k.shavrin.diethelper.data.FakeFoodEntryRepository
import com.k.shavrin.diethelper.domain.model.FoodEntry
import com.k.shavrin.diethelper.domain.model.MealType
import com.k.shavrin.diethelper.domain.model.Product
import com.k.shavrin.diethelper.domain.usecase.foodentry.GetDailySummaryUseCase
import com.k.shavrin.diethelper.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class GetDailySummaryUseCaseTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeFoodEntryRepository()
    private val useCase = GetDailySummaryUseCase(repository)

    private val product = Product(
        id = 1, name = "Гречка",
        caloriesPer100g = 100f, proteinPer100g = 4f, fatPer100g = 1f, carbsPer100g = 20f
    )
    private val date: LocalDate = LocalDate.of(2025, 1, 15)

    @Test
    fun `summary sums nutrients by multiplier`() = runTest {
        repository.seed(
            listOf(
                FoodEntry(productId = 1, product = product, date = date, mealType = MealType.BREAKFAST, multiplier = 1.5f),
                FoodEntry(productId = 1, product = product, date = date, mealType = MealType.DINNER, multiplier = 0.5f)
            )
        )

        useCase(date).test {
            val summary = awaitItem()
            assertEquals(200f, summary.totalCalories, 0.001f)
            assertEquals(8f, summary.totalProtein, 0.001f)
            assertEquals(2f, summary.totalFat, 0.001f)
            assertEquals(40f, summary.totalCarbs, 0.001f)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `summary is zero when no entries`() = runTest {
        useCase(date).test {
            val summary = awaitItem()
            assertEquals(0f, summary.totalCalories, 0f)
            assertEquals(0f, summary.totalProtein, 0f)
            assertEquals(0f, summary.totalFat, 0f)
            assertEquals(0f, summary.totalCarbs, 0f)
            cancelAndConsumeRemainingEvents()
        }
    }
}
