package com.k.shavrin.diethelper.domain.usecase

import app.cash.turbine.test
import com.k.shavrin.diethelper.data.FakeFoodEntryRepository
import com.k.shavrin.diethelper.domain.model.MealType
import com.k.shavrin.diethelper.domain.model.Product
import com.k.shavrin.diethelper.domain.model.SavedMeal
import com.k.shavrin.diethelper.domain.model.SavedMealItem
import com.k.shavrin.diethelper.domain.usecase.foodentry.AddFoodEntryUseCase
import com.k.shavrin.diethelper.domain.usecase.savedmeal.AddSavedMealEntriesUseCase
import com.k.shavrin.diethelper.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class AddSavedMealEntriesUseCaseTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val foodEntryRepository = FakeFoodEntryRepository()
    private val addFoodEntryUseCase = AddFoodEntryUseCase(foodEntryRepository)
    private val useCase = AddSavedMealEntriesUseCase(addFoodEntryUseCase)

    private val product = Product(
        id = 1,
        name = "Banana",
        caloriesPer100g = 89f,
        proteinPer100g = 1.1f,
        fatPer100g = 0.3f,
        carbsPer100g = 23f
    )

    @Test
    fun `addSavedMealEntries creates food entries for target date and mealType`() = runTest {
        val meal = SavedMeal(
            id = 1,
            name = "My Breakfast",
            items = listOf(
                SavedMealItem(savedMealId = 1, productId = 1, product = product, multiplier = 2f)
            )
        )
        val targetDate = LocalDate.of(2025, 5, 1)

        useCase(meal, targetDate, MealType.LUNCH)

        foodEntryRepository.getEntriesForDay(targetDate).test {
            val entries = awaitItem()
            assertEquals(1, entries.size)
            assertEquals(MealType.LUNCH, entries[0].mealType)
            assertEquals(2f, entries[0].multiplier, 0.001f)
            assertEquals(targetDate, entries[0].date)
            cancelAndConsumeRemainingEvents()
        }
    }
}
