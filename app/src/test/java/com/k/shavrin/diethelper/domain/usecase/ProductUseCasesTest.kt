package com.k.shavrin.diethelper.domain.usecase

import app.cash.turbine.test
import com.k.shavrin.diethelper.data.FakeProductRepository
import com.k.shavrin.diethelper.domain.model.Product
import com.k.shavrin.diethelper.domain.usecase.product.AddProductUseCase
import com.k.shavrin.diethelper.domain.usecase.product.GetAllProductsUseCase
import com.k.shavrin.diethelper.domain.usecase.product.ToggleFavoriteUseCase
import com.k.shavrin.diethelper.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProductUseCasesTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repo = FakeProductRepository()

    private val apple = Product(
        name = "Apple", caloriesPer100g = 52f,
        proteinPer100g = 0.3f, fatPer100g = 0.2f, carbsPer100g = 14f
    )

    // ── AddProductUseCase ────────────────────────────────────────────────────

    @Test
    fun `AddProductUseCase inserts product and returns positive id`() = runTest {
        val id = AddProductUseCase(repo)(apple)
        assertTrue(id > 0)
        assertEquals("Apple", repo.getProductById(id)!!.name)
    }

    // ── GetAllProductsUseCase ────────────────────────────────────────────────

    @Test
    fun `GetAllProductsUseCase emits all products`() = runTest {
        repo.seed(listOf(apple, apple.copy(name = "Banana")))

        GetAllProductsUseCase(repo)().test {
            assertEquals(2, awaitItem().size)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `GetAllProductsUseCase returns empty list when no products`() = runTest {
        GetAllProductsUseCase(repo)().test {
            assertTrue(awaitItem().isEmpty())
            cancelAndConsumeRemainingEvents()
        }
    }

    // ── ToggleFavoriteUseCase ────────────────────────────────────────────────

    @Test
    fun `ToggleFavoriteUseCase sets isFavorite to true`() = runTest {
        repo.seed(listOf(apple))
        val id = repo.getProductById(1L)!!.id

        ToggleFavoriteUseCase(repo)(id, true)

        assertTrue(repo.getProductById(id)!!.isFavorite)
    }

    @Test
    fun `ToggleFavoriteUseCase sets isFavorite to false`() = runTest {
        repo.seed(listOf(apple.copy(isFavorite = true)))
        val id = repo.getProductById(1L)!!.id

        ToggleFavoriteUseCase(repo)(id, false)

        assertTrue(!repo.getProductById(id)!!.isFavorite)
    }
}
