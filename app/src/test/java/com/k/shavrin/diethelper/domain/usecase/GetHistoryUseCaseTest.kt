package com.k.shavrin.diethelper.domain.usecase

import app.cash.turbine.test
import com.k.shavrin.diethelper.data.FakeFoodEntryRepository
import com.k.shavrin.diethelper.domain.model.FoodEntry
import com.k.shavrin.diethelper.domain.model.MealType
import com.k.shavrin.diethelper.domain.model.Product
import com.k.shavrin.diethelper.domain.usecase.foodentry.GetHistoryUseCase
import com.k.shavrin.diethelper.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class GetHistoryUseCaseTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeFoodEntryRepository()
    private val useCase = GetHistoryUseCase(repository)

    private val product = Product(
        id = 1, name = "Яблоко",
        caloriesPer100g = 52f, proteinPer100g = 0.4f, fatPer100g = 0.4f, carbsPer100g = 14f
    )
    private val today: LocalDate = LocalDate.of(2025, 3, 10)
    private val yesterday: LocalDate = today.minusDays(1)
    private val twoDaysAgo: LocalDate = today.minusDays(2)

    @Test
    fun `returns empty list when no entries`() = runTest {
        useCase().test {
            val items = awaitItem()
            assertTrue(items.isEmpty())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `returns one item per distinct date sorted descending`() = runTest {
        repository.seed(
            listOf(
                FoodEntry(productId = 1, product = product, date = today, mealType = MealType.BREAKFAST, multiplier = 1f),
                FoodEntry(productId = 1, product = product, date = yesterday, mealType = MealType.LUNCH, multiplier = 2f),
                FoodEntry(productId = 1, product = product, date = twoDaysAgo, mealType = MealType.DINNER, multiplier = 0.5f)
            )
        )

        useCase().test {
            val items = awaitItem()
            assertEquals(3, items.size)
            assertEquals(today, items[0].date)
            assertEquals(yesterday, items[1].date)
            assertEquals(twoDaysAgo, items[2].date)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `aggregates calories for entries on same day`() = runTest {
        repository.seed(
            listOf(
                FoodEntry(productId = 1, product = product, date = today, mealType = MealType.BREAKFAST, multiplier = 1f),
                FoodEntry(productId = 1, product = product, date = today, mealType = MealType.LUNCH, multiplier = 1f)
            )
        )

        useCase().test {
            val items = awaitItem()
            assertEquals(1, items.size)
            assertEquals(today, items[0].date)
            assertEquals(104f, items[0].totalCalories, 0.001f)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `calories per day use multiplier correctly`() = runTest {
        repository.seed(
            listOf(
                FoodEntry(productId = 1, product = product, date = today, mealType = MealType.BREAKFAST, multiplier = 2.5f)
            )
        )

        useCase().test {
            val items = awaitItem()
            assertEquals(1, items.size)
            assertEquals(52f * 2.5f, items[0].totalCalories, 0.001f)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `reacts to new entry added after initial emission`() = runTest {
        useCase().test {
            val empty = awaitItem()
            assertTrue(empty.isEmpty())

            repository.seed(
                listOf(
                    FoodEntry(productId = 1, product = product, date = today, mealType = MealType.SNACK, multiplier = 1f)
                )
            )

            val updated = awaitItem()
            assertEquals(1, updated.size)
            assertEquals(today, updated[0].date)
            cancelAndConsumeRemainingEvents()
        }
    }
}
