package com.k.shavrin.diethelper.domain.usecase

import app.cash.turbine.test
import com.k.shavrin.diethelper.data.FakeFoodEntryRepository
import com.k.shavrin.diethelper.domain.model.FoodEntry
import com.k.shavrin.diethelper.domain.model.MealType
import com.k.shavrin.diethelper.domain.model.Product
import com.k.shavrin.diethelper.domain.usecase.foodentry.AddFoodEntryUseCase
import com.k.shavrin.diethelper.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class AddFoodEntryUseCaseTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeFoodEntryRepository()
    private val useCase = AddFoodEntryUseCase(repository)

    @Test
    fun `addEntry inserts and returns generated id`() = runTest {
        val product = Product(id = 1, name = "Apple", caloriesPer100g = 52f, proteinPer100g = 0.3f, fatPer100g = 0.2f, carbsPer100g = 14f)
        val date = LocalDate.of(2025, 1, 15)

        val newId = useCase(
            FoodEntry(productId = 1, product = product, date = date, mealType = MealType.SNACK, multiplier = 1.5f)
        )

        assertTrue(newId > 0)
        repository.getEntriesForDay(date).test {
            val list = awaitItem()
            assertEquals(1, list.size)
            assertEquals(1.5f, list[0].multiplier, 0.001f)
            cancelAndConsumeRemainingEvents()
        }
    }
}
