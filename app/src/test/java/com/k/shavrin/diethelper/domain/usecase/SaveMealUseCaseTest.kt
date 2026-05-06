package com.k.shavrin.diethelper.domain.usecase

import app.cash.turbine.test
import com.k.shavrin.diethelper.data.FakeSavedMealRepository
import com.k.shavrin.diethelper.domain.model.Product
import com.k.shavrin.diethelper.domain.model.SavedMealItem
import com.k.shavrin.diethelper.domain.usecase.savedmeal.SaveMealUseCase
import com.k.shavrin.diethelper.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SaveMealUseCaseTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeSavedMealRepository()
    private val useCase = SaveMealUseCase(repository)

    private val product = Product(
        id = 1,
        name = "Apple",
        caloriesPer100g = 52f,
        proteinPer100g = 0.3f,
        fatPer100g = 0.2f,
        carbsPer100g = 14f
    )

    @Test
    fun `saveMeal stores meal and items`() = runTest {
        val items = listOf(
            SavedMealItem(savedMealId = 0, productId = 1, product = product, multiplier = 1.5f)
        )

        useCase("Завтрак", items)

        repository.getSavedMeals().test {
            val list = awaitItem()
            assertEquals(1, list.size)
            assertEquals("Завтрак", list[0].name)
            assertEquals(1, list[0].items.size)
            assertEquals(1.5f, list[0].items[0].multiplier, 0.001f)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `saveMeal overwrites meal with same name`() = runTest {
        val items = listOf(
            SavedMealItem(savedMealId = 0, productId = 1, product = product, multiplier = 1f)
        )
        useCase("Завтрак", items)

        val newItems = listOf(
            SavedMealItem(savedMealId = 0, productId = 1, product = product, multiplier = 2f)
        )
        useCase("Завтрак", newItems)

        repository.getSavedMeals().test {
            val list = awaitItem()
            assertEquals(1, list.size)
            assertEquals(2f, list[0].items[0].multiplier, 0.001f)
            cancelAndConsumeRemainingEvents()
        }
    }
}
