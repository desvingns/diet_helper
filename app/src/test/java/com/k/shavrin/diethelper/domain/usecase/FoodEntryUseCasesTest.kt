package com.k.shavrin.diethelper.domain.usecase

import app.cash.turbine.test
import com.k.shavrin.diethelper.data.FakeFoodEntryRepository
import com.k.shavrin.diethelper.domain.model.FoodEntry
import com.k.shavrin.diethelper.domain.model.MealType
import com.k.shavrin.diethelper.domain.model.Product
import com.k.shavrin.diethelper.domain.usecase.foodentry.CopyFoodEntryToDayUseCase
import com.k.shavrin.diethelper.domain.usecase.foodentry.DeleteFoodEntryUseCase
import com.k.shavrin.diethelper.domain.usecase.foodentry.UpdateFoodEntryUseCase
import com.k.shavrin.diethelper.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class FoodEntryUseCasesTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repo = FakeFoodEntryRepository()

    private val product = Product(
        id = 1, name = "Тест",
        caloriesPer100g = 100f, proteinPer100g = 5f, fatPer100g = 2f, carbsPer100g = 15f
    )
    private val date = LocalDate.of(2025, 4, 1)
    private val entry = FoodEntry(
        id = 1, productId = 1, product = product,
        date = date, mealType = MealType.BREAKFAST, multiplier = 1f
    )

    // ── UpdateFoodEntryUseCase ───────────────────────────────────────────────

    @Test
    fun `UpdateFoodEntryUseCase updates multiplier`() = runTest {
        repo.seed(listOf(entry))
        UpdateFoodEntryUseCase(repo)(entry.copy(multiplier = 2f))

        repo.getEntriesForDay(date).test {
            assertEquals(2f, awaitItem().first().multiplier, 0.001f)
            cancelAndConsumeRemainingEvents()
        }
    }

    // ── DeleteFoodEntryUseCase ───────────────────────────────────────────────

    @Test
    fun `DeleteFoodEntryUseCase removes the entry`() = runTest {
        repo.seed(listOf(entry))
        DeleteFoodEntryUseCase(repo)(entry)

        repo.getEntriesForDay(date).test {
            assertTrue(awaitItem().isEmpty())
            cancelAndConsumeRemainingEvents()
        }
    }

    // ── CopyFoodEntryToDayUseCase ────────────────────────────────────────────

    @Test
    fun `CopyFoodEntryToDayUseCase creates entry on target date`() = runTest {
        repo.seed(listOf(entry))
        val targetDate = date.plusDays(1)
        CopyFoodEntryToDayUseCase(repo)(entry, targetDate)

        repo.getEntriesForDay(targetDate).test {
            val list = awaitItem()
            assertEquals(1, list.size)
            assertEquals(targetDate, list[0].date)
            assertEquals(entry.multiplier, list[0].multiplier, 0.001f)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `CopyFoodEntryToDayUseCase does not remove source entry`() = runTest {
        repo.seed(listOf(entry))
        CopyFoodEntryToDayUseCase(repo)(entry, date.plusDays(1))

        repo.getEntriesForDay(date).test {
            assertEquals(1, awaitItem().size)
            cancelAndConsumeRemainingEvents()
        }
    }
}
