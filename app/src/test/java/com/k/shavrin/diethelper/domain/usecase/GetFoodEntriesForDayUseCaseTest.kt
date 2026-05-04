package com.k.shavrin.diethelper.domain.usecase

import app.cash.turbine.test
import com.k.shavrin.diethelper.data.FakeFoodEntryRepository
import com.k.shavrin.diethelper.domain.model.FoodEntry
import com.k.shavrin.diethelper.domain.model.MealType
import com.k.shavrin.diethelper.domain.model.Product
import com.k.shavrin.diethelper.domain.usecase.foodentry.GetFoodEntriesForDayUseCase
import com.k.shavrin.diethelper.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class GetFoodEntriesForDayUseCaseTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeFoodEntryRepository()
    private val useCase = GetFoodEntriesForDayUseCase(repository)

    private val product = Product(id = 1, name = "Apple", caloriesPer100g = 52f, proteinPer100g = 0.3f, fatPer100g = 0.2f, carbsPer100g = 14f)
    private val today: LocalDate = LocalDate.of(2025, 1, 15)
    private val yesterday: LocalDate = today.minusDays(1)

    @Test
    fun `returns only entries on the requested date`() = runTest {
        repository.seed(
            listOf(
                FoodEntry(productId = 1, product = product, date = today, mealType = MealType.BREAKFAST, multiplier = 1f),
                FoodEntry(productId = 1, product = product, date = today, mealType = MealType.LUNCH, multiplier = 2f),
                FoodEntry(productId = 1, product = product, date = yesterday, mealType = MealType.DINNER, multiplier = 1f)
            )
        )

        useCase(today).test {
            val list = awaitItem()
            assertEquals(2, list.size)
            cancelAndConsumeRemainingEvents()
        }
    }
}
