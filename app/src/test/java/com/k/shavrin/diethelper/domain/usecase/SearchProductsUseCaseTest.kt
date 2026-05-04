package com.k.shavrin.diethelper.domain.usecase

import app.cash.turbine.test
import com.k.shavrin.diethelper.data.FakeProductRepository
import com.k.shavrin.diethelper.domain.model.Product
import com.k.shavrin.diethelper.domain.usecase.product.SearchProductsUseCase
import com.k.shavrin.diethelper.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchProductsUseCaseTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeProductRepository()
    private val useCase = SearchProductsUseCase(repository)

    @Test
    fun `query is trimmed before search`() = runTest {
        repository.seed(
            listOf(
                Product(name = "Apple", caloriesPer100g = 52f, proteinPer100g = 0.3f, fatPer100g = 0.2f, carbsPer100g = 14f),
                Product(name = "Banana", caloriesPer100g = 89f, proteinPer100g = 1.1f, fatPer100g = 0.3f, carbsPer100g = 23f)
            )
        )

        useCase("  app  ").test {
            val list = awaitItem()
            assertEquals(1, list.size)
            assertEquals("Apple", list[0].name)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `empty query returns all products`() = runTest {
        repository.seed(
            listOf(
                Product(name = "Apple", caloriesPer100g = 52f, proteinPer100g = 0.3f, fatPer100g = 0.2f, carbsPer100g = 14f, isFavorite = true),
                Product(name = "Banana", caloriesPer100g = 89f, proteinPer100g = 1.1f, fatPer100g = 0.3f, carbsPer100g = 23f)
            )
        )

        useCase("").test {
            val list = awaitItem()
            assertEquals(2, list.size)
            assertEquals("Apple", list[0].name)
            cancelAndConsumeRemainingEvents()
        }
    }
}
