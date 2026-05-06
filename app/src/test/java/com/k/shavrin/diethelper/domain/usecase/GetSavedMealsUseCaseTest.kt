package com.k.shavrin.diethelper.domain.usecase

import app.cash.turbine.test
import com.k.shavrin.diethelper.data.FakeSavedMealRepository
import com.k.shavrin.diethelper.domain.model.Product
import com.k.shavrin.diethelper.domain.model.SavedMealItem
import com.k.shavrin.diethelper.domain.usecase.savedmeal.GetSavedMealsUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GetSavedMealsUseCaseTest {

    private val repository = FakeSavedMealRepository()
    private val useCase = GetSavedMealsUseCase(repository)

    private val product = Product(
        id = 1,
        name = "Apple",
        caloriesPer100g = 52f,
        proteinPer100g = 0.3f,
        fatPer100g = 0.2f,
        carbsPer100g = 14f
    )

    @Test
    fun `returns empty list when repository is empty`() = runTest {
        useCase().test {
            val list = awaitItem()
            assertTrue(list.isEmpty())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `returns saved meals after they are stored`() = runTest {
        repository.saveMeal(
            "Завтрак",
            listOf(SavedMealItem(savedMealId = 0, productId = 1, product = product, multiplier = 1f))
        )

        useCase().test {
            val list = awaitItem()
            assertEquals(1, list.size)
            assertEquals("Завтрак", list[0].name)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `returns meals sorted alphabetically`() = runTest {
        repository.saveMeal("Ужин", emptyList())
        repository.saveMeal("Завтрак", emptyList())
        repository.saveMeal("Обед", emptyList())

        useCase().test {
            val list = awaitItem()
            assertEquals(3, list.size)
            assertEquals("Завтрак", list[0].name)
            assertEquals("Обед", list[1].name)
            assertEquals("Ужин", list[2].name)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `emits updated list when a new meal is added`() = runTest {
        useCase().test {
            val empty = awaitItem()
            assertTrue(empty.isEmpty())

            repository.saveMeal(
                "Обед",
                listOf(SavedMealItem(savedMealId = 0, productId = 1, product = product, multiplier = 2f))
            )

            val updated = awaitItem()
            assertEquals(1, updated.size)
            assertEquals("Обед", updated[0].name)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `returns items with correct multiplier`() = runTest {
        repository.saveMeal(
            "Перекус",
            listOf(SavedMealItem(savedMealId = 0, productId = 1, product = product, multiplier = 1.5f))
        )

        useCase().test {
            val list = awaitItem()
            assertEquals(1.5f, list[0].items[0].multiplier, 0.001f)
            cancelAndConsumeRemainingEvents()
        }
    }
}
