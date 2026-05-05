package com.k.shavrin.diethelper.presentation.screen.history

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.k.shavrin.diethelper.data.FakeFoodEntryRepository
import com.k.shavrin.diethelper.data.FakeGoalsRepository
import com.k.shavrin.diethelper.domain.model.DailyGoals
import com.k.shavrin.diethelper.domain.model.FoodEntry
import com.k.shavrin.diethelper.domain.model.MealType
import com.k.shavrin.diethelper.domain.model.Product
import com.k.shavrin.diethelper.domain.usecase.foodentry.GetFoodEntriesForDayUseCase
import com.k.shavrin.diethelper.domain.usecase.goals.GetDailyGoalsUseCase
import com.k.shavrin.diethelper.presentation.navigation.Routes
import com.k.shavrin.diethelper.presentation.screen.today.TodayUiState
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
class HistoryDayViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val foodRepo = FakeFoodEntryRepository()
    private val goalsRepo = FakeGoalsRepository(DailyGoals.DEFAULT)

    private val testDate: LocalDate = LocalDate.of(2025, 4, 20)

    private val product = Product(
        id = 1, name = "Яблоко",
        caloriesPer100g = 52f, proteinPer100g = 0.4f, fatPer100g = 0.4f, carbsPer100g = 14f
    )

    private fun createViewModel(date: LocalDate = testDate): HistoryDayViewModel =
        HistoryDayViewModel(
            savedStateHandle = SavedStateHandle(mapOf(Routes.ARG_DATE to date.toString())),
            getEntriesForDay = GetFoodEntriesForDayUseCase(foodRepo),
            getGoals = GetDailyGoalsUseCase(goalsRepo)
        )

    @Test
    fun `initial state is Loading`() {
        assertTrue(createViewModel().uiState.value is TodayUiState.Loading)
    }

    @Test
    fun `canGoForward is always false`() = runTest {
        val vm = createViewModel()

        vm.uiState.test {
            val state = skipUntilSuccess() as TodayUiState.Success
            assertFalse(state.canGoForward)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `date in state matches SavedStateHandle`() = runTest {
        val vm = createViewModel()

        vm.uiState.test {
            val state = skipUntilSuccess() as TodayUiState.Success
            assertEquals(testDate, state.date)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `sections contain entries grouped by meal type`() = runTest {
        foodRepo.seed(
            listOf(
                FoodEntry(productId = 1, product = product, date = testDate, mealType = MealType.BREAKFAST, multiplier = 1f),
                FoodEntry(productId = 1, product = product, date = testDate, mealType = MealType.BREAKFAST, multiplier = 0.5f),
                FoodEntry(productId = 1, product = product, date = testDate, mealType = MealType.DINNER, multiplier = 2f)
            )
        )
        val vm = createViewModel()

        vm.uiState.test {
            val state = skipUntilSuccess() as TodayUiState.Success
            assertEquals(2, state.sections[MealType.BREAKFAST]?.size)
            assertEquals(1, state.sections[MealType.DINNER]?.size)
            assertEquals(0, state.sections[MealType.LUNCH]?.size)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `summary aggregates all entries for the day`() = runTest {
        foodRepo.seed(
            listOf(
                FoodEntry(productId = 1, product = product, date = testDate, mealType = MealType.BREAKFAST, multiplier = 1f),
                FoodEntry(productId = 1, product = product, date = testDate, mealType = MealType.LUNCH, multiplier = 1f)
            )
        )
        val vm = createViewModel()

        vm.uiState.test {
            val state = skipUntilSuccess() as TodayUiState.Success
            assertEquals(104f, state.summary.totalCalories, 0.001f)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `entries from other dates are not shown`() = runTest {
        val otherDate = testDate.plusDays(1)
        foodRepo.seed(
            listOf(
                FoodEntry(productId = 1, product = product, date = testDate, mealType = MealType.SNACK, multiplier = 1f),
                FoodEntry(productId = 1, product = product, date = otherDate, mealType = MealType.SNACK, multiplier = 1f)
            )
        )
        val vm = createViewModel()

        vm.uiState.test {
            val state = skipUntilSuccess() as TodayUiState.Success
            val totalEntries = state.sections.values.sumOf { it.size }
            assertEquals(1, totalEntries)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `goals from repository appear in state`() = runTest {
        val customGoals = DailyGoals(
            calories = 1500f,
            proteinMin = 100f, proteinMax = 150f,
            fatMin = 40f, fatMax = 60f,
            carbsMin = 150f, carbsMax = 220f
        )
        val goalsRepo = FakeGoalsRepository(customGoals)
        val vm = HistoryDayViewModel(
            savedStateHandle = SavedStateHandle(mapOf(Routes.ARG_DATE to testDate.toString())),
            getEntriesForDay = GetFoodEntriesForDayUseCase(foodRepo),
            getGoals = GetDailyGoalsUseCase(goalsRepo)
        )

        vm.uiState.test {
            val state = skipUntilSuccess() as TodayUiState.Success
            assertEquals(1500f, state.goals.calories, 0.001f)
            cancelAndConsumeRemainingEvents()
        }
    }
}

private suspend fun <T> app.cash.turbine.ReceiveTurbine<T>.skipUntilSuccess(): T {
    while (true) {
        val item = awaitItem()
        if (item !is TodayUiState.Loading) return item
    }
}
