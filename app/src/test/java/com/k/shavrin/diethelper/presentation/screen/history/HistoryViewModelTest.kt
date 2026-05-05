package com.k.shavrin.diethelper.presentation.screen.history

import app.cash.turbine.test
import com.k.shavrin.diethelper.data.FakeFoodEntryRepository
import com.k.shavrin.diethelper.domain.model.FoodEntry
import com.k.shavrin.diethelper.domain.model.MealType
import com.k.shavrin.diethelper.domain.model.Product
import com.k.shavrin.diethelper.domain.usecase.foodentry.GetHistoryUseCase
import com.k.shavrin.diethelper.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repo = FakeFoodEntryRepository()
    private val product = Product(
        id = 1, name = "Гречка",
        caloriesPer100g = 110f, proteinPer100g = 4.5f, fatPer100g = 1f, carbsPer100g = 22f
    )

    private fun createViewModel() =
        HistoryViewModel(GetHistoryUseCase(repo))

    @Test
    fun `initial state is Loading`() {
        assertTrue(createViewModel().uiState.value is HistoryUiState.Loading)
    }

    @Test
    fun `emits Success with empty list when no food entries`() = runTest {
        val vm = createViewModel()

        vm.uiState.test {
            val state = skipUntilSuccess()
            assertTrue((state as HistoryUiState.Success).items.isEmpty())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `emits one HistoryItem per distinct day sorted descending`() = runTest {
        val today = LocalDate.of(2025, 5, 10)
        val yesterday = today.minusDays(1)
        repo.seed(
            listOf(
                FoodEntry(productId = 1, product = product, date = today, mealType = MealType.BREAKFAST, multiplier = 1f),
                FoodEntry(productId = 1, product = product, date = yesterday, mealType = MealType.LUNCH, multiplier = 1f)
            )
        )
        val vm = createViewModel()

        vm.uiState.test {
            val state = skipUntilSuccess() as HistoryUiState.Success
            assertEquals(2, state.items.size)
            assertEquals(today, state.items[0].date)
            assertEquals(yesterday, state.items[1].date)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `aggregates calories for all entries on a day`() = runTest {
        val date = LocalDate.of(2025, 5, 10)
        repo.seed(
            listOf(
                FoodEntry(productId = 1, product = product, date = date, mealType = MealType.BREAKFAST, multiplier = 1f),
                FoodEntry(productId = 1, product = product, date = date, mealType = MealType.DINNER, multiplier = 1f)
            )
        )
        val vm = createViewModel()

        vm.uiState.test {
            val state = skipUntilSuccess() as HistoryUiState.Success
            assertEquals(1, state.items.size)
            assertEquals(220f, state.items[0].totalCalories, 0.001f)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `state updates reactively when entry is added`() = runTest {
        val vm = createViewModel()

        vm.uiState.test {
            val empty = skipUntilSuccess() as HistoryUiState.Success
            assertTrue(empty.items.isEmpty())

            val date = LocalDate.of(2025, 5, 10)
            repo.seed(
                listOf(FoodEntry(productId = 1, product = product, date = date, mealType = MealType.SNACK, multiplier = 2f))
            )

            val updated = skipUntilSuccess() as HistoryUiState.Success
            assertEquals(1, updated.items.size)
            cancelAndConsumeRemainingEvents()
        }
    }
}

private suspend fun <T> app.cash.turbine.ReceiveTurbine<T>.skipUntilSuccess(): T {
    while (true) {
        val item = awaitItem()
        if (item !is HistoryUiState.Loading) return item
    }
}
