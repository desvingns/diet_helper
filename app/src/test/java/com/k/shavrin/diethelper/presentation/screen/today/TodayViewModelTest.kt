package com.k.shavrin.diethelper.presentation.screen.today

import app.cash.turbine.test
import com.k.shavrin.diethelper.data.FakeFoodEntryRepository
import com.k.shavrin.diethelper.data.FakeGoalsRepository
import com.k.shavrin.diethelper.domain.model.DailyGoals
import com.k.shavrin.diethelper.domain.model.FoodEntry
import com.k.shavrin.diethelper.domain.model.MealType
import com.k.shavrin.diethelper.domain.model.Product
import com.k.shavrin.diethelper.domain.usecase.foodentry.CopyFoodEntryToDayUseCase
import com.k.shavrin.diethelper.domain.usecase.foodentry.DeleteFoodEntryUseCase
import com.k.shavrin.diethelper.domain.usecase.foodentry.GetFoodEntriesForDayUseCase
import com.k.shavrin.diethelper.domain.usecase.foodentry.UpdateFoodEntryUseCase
import com.k.shavrin.diethelper.domain.usecase.goals.GetDailyGoalsUseCase
import com.k.shavrin.diethelper.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class TodayViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val foodRepo = FakeFoodEntryRepository()
    private val goalsRepo = FakeGoalsRepository(DailyGoals(2000f, 100f, 60f, 250f))

    private val product = Product(
        id = 1, name = "Гречка",
        caloriesPer100g = 100f, proteinPer100g = 4f, fatPer100g = 1f, carbsPer100g = 20f
    )

    private lateinit var viewModel: TodayViewModel

    @Before
    fun setUp() {
        viewModel = TodayViewModel(
            getEntriesForDay = GetFoodEntriesForDayUseCase(foodRepo),
            getGoals = GetDailyGoalsUseCase(goalsRepo),
            updateEntryUseCase = UpdateFoodEntryUseCase(foodRepo),
            deleteEntryUseCase = DeleteFoodEntryUseCase(foodRepo),
            copyEntryUseCase = CopyFoodEntryToDayUseCase(foodRepo)
        )
    }

    @Test
    fun `initial state is today and cannot go forward`() = runTest {
        viewModel.uiState.test {
            val state = (skipUntilSuccess()) as TodayUiState.Success
            assertEquals(LocalDate.now(), state.date)
            assertFalse(state.canGoForward)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `goToPreviousDay then goToNextDay returns to today`() = runTest {
        viewModel.goToPreviousDay()
        assertEquals(LocalDate.now().minusDays(1), viewModel.currentDate.value)
        assertTrue(viewModel.uiState.value is TodayUiState.Loading || viewModel.uiState.value is TodayUiState.Success)

        viewModel.goToNextDay()
        assertEquals(LocalDate.now(), viewModel.currentDate.value)
    }

    @Test
    fun `goToNextDay does not move past today`() = runTest {
        viewModel.goToNextDay()
        assertEquals(LocalDate.now(), viewModel.currentDate.value)
    }

    @Test
    fun `sections contain entries grouped by meal type`() = runTest {
        val today = LocalDate.now()
        foodRepo.seed(
            listOf(
                FoodEntry(productId = 1, product = product, date = today, mealType = MealType.BREAKFAST, multiplier = 1f),
                FoodEntry(productId = 1, product = product, date = today, mealType = MealType.BREAKFAST, multiplier = 0.5f),
                FoodEntry(productId = 1, product = product, date = today, mealType = MealType.DINNER, multiplier = 2f)
            )
        )

        viewModel.uiState.test {
            val state = (skipUntilSuccess()) as TodayUiState.Success
            assertEquals(2, state.sections[MealType.BREAKFAST]?.size)
            assertEquals(1, state.sections[MealType.DINNER]?.size)
            assertEquals(0, state.sections[MealType.LUNCH]?.size)
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
