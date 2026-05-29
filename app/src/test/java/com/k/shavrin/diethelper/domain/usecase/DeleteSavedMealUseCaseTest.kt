package com.k.shavrin.diethelper.domain.usecase

import app.cash.turbine.test
import com.k.shavrin.diethelper.data.FakeSavedMealRepository
import com.k.shavrin.diethelper.domain.model.Product
import com.k.shavrin.diethelper.domain.model.SavedMealItem
import com.k.shavrin.diethelper.domain.usecase.savedmeal.DeleteSavedMealUseCase
import com.k.shavrin.diethelper.domain.usecase.savedmeal.GetSavedMealsUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DeleteSavedMealUseCaseTest {

    private val repository = FakeSavedMealRepository()
    private val deleteUseCase = DeleteSavedMealUseCase(repository)
    private val getUseCase = GetSavedMealsUseCase(repository)

    private val product = Product(
        id = 1,
        name = "Banana",
        caloriesPer100g = 89f,
        proteinPer100g = 1.1f,
        fatPer100g = 0.3f,
        carbsPer100g = 23f
    )

    @Test
    fun `deleteMeal removes meal by id`() = runTest {
        repository.saveMeal(
            "Завтрак",
            listOf(SavedMealItem(savedMealId = 0, productId = 1, product = product, multiplier = 1f))
        )

        val savedId = getUseCase().let { flow ->
            var id = 0L
            flow.test {
                id = awaitItem()[0].id
                cancelAndConsumeRemainingEvents()
            }
            id
        }

        deleteUseCase(savedId)

        getUseCase().test {
            val list = awaitItem()
            assertTrue(list.isEmpty())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `deleteMeal removes only the targeted meal`() = runTest {
        repository.saveMeal("Завтрак", emptyList())
        repository.saveMeal("Обед", emptyList())

        var breakfastId = 0L
        getUseCase().test {
            breakfastId = awaitItem().first { it.name == "Завтрак" }.id
            cancelAndConsumeRemainingEvents()
        }

        deleteUseCase(breakfastId)

        getUseCase().test {
            val list = awaitItem()
            assertEquals(1, list.size)
            assertEquals("Обед", list[0].name)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `deleteMeal on nonexistent id does nothing`() = runTest {
        repository.saveMeal("Ужин", emptyList())

        deleteUseCase(99999L)

        getUseCase().test {
            val list = awaitItem()
            assertEquals(1, list.size)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `deleteMeal on empty repository does nothing`() = runTest {
        deleteUseCase(1L)

        getUseCase().test {
            assertTrue(awaitItem().isEmpty())
            cancelAndConsumeRemainingEvents()
        }
    }
}
