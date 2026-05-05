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

    // product: protein=0.4, fat=0.4, carbs=14 per 100g; caloriesPer100g=52
    // multiplier 1.0 → protein=0.4, fat=0.4, carbs=14, calories=52
    // multiplier 2.0 → protein=0.8, fat=0.8, carbs=28, calories=104

    @Test
    fun `sectionProtein is zero for all meal types when no entries`() = runTest {
        val vm = createViewModel()
        vm.uiState.test {
            val state = skipUntilSuccess() as TodayUiState.Success
            MealType.values().forEach { mealType ->
                assertEquals(0f, state.sectionProtein[mealType] ?: 0f, 0.001f)
            }
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `sectionProtein sectionFat sectionCarbs are computed from multiplier`() = runTest {
        // BREAKFAST: multiplier=1.0 → protein=0.4, fat=0.4, carbs=14
        // LUNCH:     multiplier=2.0 → protein=0.8, fat=0.8, carbs=28
        foodRepo.seed(
            listOf(
                FoodEntry(productId = 1, product = product, date = testDate, mealType = MealType.BREAKFAST, multiplier = 1f),
                FoodEntry(productId = 1, product = product, date = testDate, mealType = MealType.LUNCH, multiplier = 2f)
            )
        )
        val vm = createViewModel()
        vm.uiState.test {
            val state = skipUntilSuccess() as TodayUiState.Success
            assertEquals(0.4f, state.sectionProtein[MealType.BREAKFAST] ?: 0f, 0.001f)
            assertEquals(0.4f, state.sectionFat[MealType.BREAKFAST] ?: 0f, 0.001f)
            assertEquals(14f, state.sectionCarbs[MealType.BREAKFAST] ?: 0f, 0.001f)
            assertEquals(0.8f, state.sectionProtein[MealType.LUNCH] ?: 0f, 0.001f)
            assertEquals(0.8f, state.sectionFat[MealType.LUNCH] ?: 0f, 0.001f)
            assertEquals(28f, state.sectionCarbs[MealType.LUNCH] ?: 0f, 0.001f)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `sectionProtein accumulates across multiple entries in the same meal`() = runTest {
        // Two BREAKFAST entries, each multiplier=1.0 → protein=0.4+0.4=0.8
        foodRepo.seed(
            listOf(
                FoodEntry(productId = 1, product = product, date = testDate, mealType = MealType.BREAKFAST, multiplier = 1f),
                FoodEntry(productId = 1, product = product, date = testDate, mealType = MealType.BREAKFAST, multiplier = 1f)
            )
        )
        val vm = createViewModel()
        vm.uiState.test {
            val state = skipUntilSuccess() as TodayUiState.Success
            assertEquals(0.8f, state.sectionProtein[MealType.BREAKFAST] ?: 0f, 0.001f)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `sectionCalories matches calories computed from entries`() = runTest {
        // DINNER: multiplier=2.0, caloriesPer100g=52 → 52*2=104 kcal
        foodRepo.seed(
            listOf(
                FoodEntry(productId = 1, product = product, date = testDate, mealType = MealType.DINNER, multiplier = 2f)
            )
        )
        val vm = createViewModel()
        vm.uiState.test {
            val state = skipUntilSuccess() as TodayUiState.Success
            assertEquals(104f, state.sectionCalories[MealType.DINNER] ?: 0f, 0.001f)
            assertEquals(0f, state.sectionCalories[MealType.BREAKFAST] ?: 0f, 0.001f)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `macros for other dates do not bleed into section maps`() = runTest {
        val otherDate = testDate.plusDays(5)
        foodRepo.seed(
            listOf(
                FoodEntry(productId = 1, product = product, date = testDate, mealType = MealType.SNACK, multiplier = 1f),
                FoodEntry(productId = 1, product = product, date = otherDate, mealType = MealType.SNACK, multiplier = 10f)
            )
        )
        val vm = createViewModel(testDate)
        vm.uiState.test {
            val state = skipUntilSuccess() as TodayUiState.Success
            // Only the testDate entry (multiplier=1.0) should contribute
            assertEquals(0.4f, state.sectionProtein[MealType.SNACK] ?: 0f, 0.001f)
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
