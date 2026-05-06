package com.k.shavrin.diethelper.presentation.screen.product

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.k.shavrin.diethelper.data.FakeFoodEntryRepository
import com.k.shavrin.diethelper.data.FakeProductRepository
import com.k.shavrin.diethelper.data.FakeSavedMealRepository
import com.k.shavrin.diethelper.domain.model.MealType
import com.k.shavrin.diethelper.domain.model.Product
import com.k.shavrin.diethelper.domain.usecase.foodentry.AddFoodEntryUseCase
import com.k.shavrin.diethelper.domain.usecase.product.SearchProductsUseCase
import com.k.shavrin.diethelper.domain.usecase.product.ToggleFavoriteUseCase
import com.k.shavrin.diethelper.domain.usecase.savedmeal.AddSavedMealEntriesUseCase
import com.k.shavrin.diethelper.domain.usecase.savedmeal.DeleteSavedMealUseCase
import com.k.shavrin.diethelper.domain.usecase.savedmeal.GetSavedMealsUseCase
import com.k.shavrin.diethelper.presentation.navigation.Routes
import com.k.shavrin.diethelper.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class ProductViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val productRepo = FakeProductRepository()
    private val foodRepo = FakeFoodEntryRepository()

    private val testDate: LocalDate = LocalDate.of(2025, 3, 15)
    private val testMealType: MealType = MealType.LUNCH

    private val savedMealRepo = FakeSavedMealRepository()

    private fun createViewModel(): ProductViewModel {
        val addFoodEntryUseCase = AddFoodEntryUseCase(foodRepo)
        return ProductViewModel(
            savedStateHandle = SavedStateHandle(
                mapOf(
                    Routes.ARG_DATE to testDate.toString(),
                    Routes.ARG_MEAL_TYPE to testMealType.name
                )
            ),
            searchProductsUseCase = SearchProductsUseCase(productRepo),
            toggleFavoriteUseCase = ToggleFavoriteUseCase(productRepo),
            addFoodEntryUseCase = addFoodEntryUseCase,
            getSavedMealsUseCase = GetSavedMealsUseCase(savedMealRepo),
            deleteSavedMealUseCase = DeleteSavedMealUseCase(savedMealRepo),
            addSavedMealEntriesUseCase = AddSavedMealEntriesUseCase(addFoodEntryUseCase)
        )
    }

    private val apple = Product(id = 1, name = "Apple", caloriesPer100g = 52f, proteinPer100g = 0.3f, fatPer100g = 0.2f, carbsPer100g = 14f)
    private val banana = Product(id = 2, name = "Banana", caloriesPer100g = 89f, proteinPer100g = 1.1f, fatPer100g = 0.3f, carbsPer100g = 23f)

    // ── initial state ────────────────────────────────────────────────────────

    @Test
    fun `initial state is Loading`() {
        val vm = createViewModel()
        assertTrue(vm.uiState.value is ProductUiState.Loading)
    }

    @Test
    fun `initial query is empty`() {
        assertEquals("", createViewModel().searchQuery.value)
    }

    // ── search results ───────────────────────────────────────────────────────

    @Test
    fun `empty query returns all products`() = runTest {
        productRepo.seed(listOf(apple, banana))
        val vm = createViewModel()

        vm.uiState.test {
            val state = skipUntilSuccess()
            assertEquals(2, (state as ProductUiState.Success).products.size)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `setQuery filters results`() = runTest {
        productRepo.seed(listOf(apple, banana))
        val vm = createViewModel()

        vm.setQuery("apple")

        vm.uiState.test {
            val state = skipUntilSuccess()
            val products = (state as ProductUiState.Success).products
            assertEquals(1, products.size)
            assertEquals("Apple", products[0].name)
            cancelAndConsumeRemainingEvents()
        }
    }

    // ── hasExactMatch ────────────────────────────────────────────────────────

    @Test
    fun `hasExactMatch true when query matches product name case-insensitively`() = runTest {
        productRepo.seed(listOf(apple))
        val vm = createViewModel()

        vm.setQuery("apple")

        vm.uiState.test {
            val state = skipUntilSuccess() as ProductUiState.Success
            assertTrue(state.hasExactMatch)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `hasExactMatch false when query is empty`() = runTest {
        productRepo.seed(listOf(apple))
        val vm = createViewModel()

        vm.uiState.test {
            val state = skipUntilSuccess() as ProductUiState.Success
            assertFalse(state.hasExactMatch)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `hasExactMatch false when query is partial`() = runTest {
        productRepo.seed(listOf(apple))
        val vm = createViewModel()

        vm.setQuery("app")

        vm.uiState.test {
            val state = skipUntilSuccess() as ProductUiState.Success
            assertFalse(state.hasExactMatch)
            cancelAndConsumeRemainingEvents()
        }
    }

    // ── toggleFavorite ───────────────────────────────────────────────────────

    @Test
    fun `toggleFavorite flips isFavorite`() = runTest {
        productRepo.seed(listOf(apple))
        val vm = createViewModel()

        vm.toggleFavorite(apple)

        assertEquals(true, productRepo.getProductById(1L)!!.isFavorite)
    }

    // ── addEntry ─────────────────────────────────────────────────────────────

    @Test
    fun `addEntry creates FoodEntry with correct multiplier`() = runTest {
        productRepo.seed(listOf(apple))
        val vm = createViewModel()

        vm.addEntry(apple, grams = 150f, onComplete = {})

        foodRepo.getEntriesForDay(testDate).test {
            val list = awaitItem()
            assertEquals(1, list.size)
            assertEquals(1.5f, list[0].multiplier, 0.001f)
            assertEquals(MealType.LUNCH, list[0].mealType)
            assertEquals(testDate, list[0].date)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `addEntry calls onComplete after saving`() = runTest {
        productRepo.seed(listOf(apple))
        val vm = createViewModel()

        var completed = false
        vm.addEntry(apple, grams = 100f, onComplete = { completed = true })

        assertTrue(completed)
    }

    // ── uiState carries date and mealType ────────────────────────────────────

    @Test
    fun `uiState Success carries date and mealType from SavedStateHandle`() = runTest {
        val vm = createViewModel()

        vm.uiState.test {
            val state = skipUntilSuccess() as ProductUiState.Success
            assertEquals(testDate, state.date)
            assertEquals(testMealType, state.mealType)
            cancelAndConsumeRemainingEvents()
        }
    }
}

private suspend fun <T> app.cash.turbine.ReceiveTurbine<T>.skipUntilSuccess(): T {
    while (true) {
        val item = awaitItem()
        if (item !is ProductUiState.Loading) return item
    }
}
