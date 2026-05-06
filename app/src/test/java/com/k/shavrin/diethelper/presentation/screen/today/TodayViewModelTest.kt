package com.k.shavrin.diethelper.presentation.screen.today

import app.cash.turbine.test
import com.k.shavrin.diethelper.data.FakeFoodEntryRepository
import com.k.shavrin.diethelper.data.FakeGoalsRepository
import com.k.shavrin.diethelper.domain.model.DailyGoals
import com.k.shavrin.diethelper.domain.model.DayStatus
import com.k.shavrin.diethelper.domain.model.FoodEntry
import com.k.shavrin.diethelper.domain.model.MealType
import com.k.shavrin.diethelper.domain.model.Product
import com.k.shavrin.diethelper.domain.usecase.foodentry.CopyFoodEntryToDayUseCase
import com.k.shavrin.diethelper.domain.usecase.foodentry.DeleteFoodEntryUseCase
import com.k.shavrin.diethelper.domain.usecase.foodentry.GetFoodEntriesForDayUseCase
import com.k.shavrin.diethelper.domain.usecase.foodentry.GetStreakUseCase
import com.k.shavrin.diethelper.domain.usecase.foodentry.GetWeekDayStatusesUseCase
import com.k.shavrin.diethelper.domain.usecase.foodentry.UpdateFoodEntryUseCase
import com.k.shavrin.diethelper.domain.usecase.goals.GetDailyGoalsUseCase
import com.k.shavrin.diethelper.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class TodayViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val foodRepo = FakeFoodEntryRepository()
    private val goalsRepo = FakeGoalsRepository(
        DailyGoals(
            calories = 2000f,
            proteinMin = 80f, proteinMax = 120f,
            fatMin = 50f, fatMax = 70f,
            carbsMin = 200f, carbsMax = 300f
        )
    )

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
            copyEntryUseCase = CopyFoodEntryToDayUseCase(foodRepo),
            getWeekDayStatuses = GetWeekDayStatusesUseCase(foodRepo, goalsRepo),
            getStreak = GetStreakUseCase(foodRepo, goalsRepo)
        )
    }

    @Test
    fun `initial state is today`() = runTest {
        viewModel.uiState.test {
            val state = (skipUntilSuccess()) as TodayUiState.Success
            assertEquals(LocalDate.now(), state.date)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `goToPreviousDay then goToNextDay returns to today`() = runTest {
        viewModel.goToPreviousDay()
        assertEquals(LocalDate.now().minusDays(1), viewModel.currentDate.value)

        viewModel.goToNextDay()
        assertEquals(LocalDate.now(), viewModel.currentDate.value)
    }

    @Test
    fun `goToNextDay does not move past today`() = runTest {
        viewModel.goToNextDay()
        assertEquals(LocalDate.now(), viewModel.currentDate.value)
    }

    @Test
    fun `goToDate changes current date`() = runTest {
        val yesterday = LocalDate.now().minusDays(1)
        viewModel.goToDate(yesterday)
        assertEquals(yesterday, viewModel.currentDate.value)
    }

    @Test
    fun `goToDate does not allow future dates`() = runTest {
        val tomorrow = LocalDate.now().plusDays(1)
        viewModel.goToDate(tomorrow)
        assertEquals(LocalDate.now(), viewModel.currentDate.value)
    }

    @Test
    fun `goToToday returns to today`() = runTest {
        viewModel.goToPreviousDay()
        viewModel.goToToday()
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

    @Test
    fun `sectionProtein, sectionFat, sectionCarbs are computed from multiplier`() = runTest {
        // product: protein=4, fat=1, carbs=20 per 100g
        // breakfast: 1.0 multiplier → protein=4, fat=1, carbs=20
        // lunch:     0.5 multiplier → protein=2, fat=0.5, carbs=10
        val today = LocalDate.now()
        foodRepo.seed(
            listOf(
                FoodEntry(productId = 1, product = product, date = today, mealType = MealType.BREAKFAST, multiplier = 1f),
                FoodEntry(productId = 1, product = product, date = today, mealType = MealType.LUNCH, multiplier = 0.5f)
            )
        )

        viewModel.uiState.test {
            val state = (skipUntilSuccess()) as TodayUiState.Success
            assertEquals(4f, state.sectionProtein[MealType.BREAKFAST] ?: 0f, 0.01f)
            assertEquals(1f, state.sectionFat[MealType.BREAKFAST] ?: 0f, 0.01f)
            assertEquals(20f, state.sectionCarbs[MealType.BREAKFAST] ?: 0f, 0.01f)
            assertEquals(2f, state.sectionProtein[MealType.LUNCH] ?: 0f, 0.01f)
            assertEquals(0.5f, state.sectionFat[MealType.LUNCH] ?: 0f, 0.01f)
            assertEquals(10f, state.sectionCarbs[MealType.LUNCH] ?: 0f, 0.01f)
            cancelAndConsumeRemainingEvents()
        }
    }

    // ── weekStatuses ─────────────────────────────────────────────────────────

    @Test
    fun `weekStatuses contains 7 entries for the current week`() = runTest {
        viewModel.uiState.test {
            val state = skipUntilSuccess() as TodayUiState.Success
            assertEquals(7, state.weekStatuses.size)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `weekStatuses week starts on Monday`() = runTest {
        viewModel.uiState.test {
            val state = skipUntilSuccess() as TodayUiState.Success
            assertEquals(DayOfWeek.MONDAY, state.weekStatuses.first().first.dayOfWeek)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `weekStatuses today has GRAY_LOGGED status when no entries`() = runTest {
        viewModel.uiState.test {
            val state = skipUntilSuccess() as TodayUiState.Success
            val todayStatus = state.weekStatuses.first { it.first == LocalDate.now() }.second
            assertEquals(DayStatus.GRAY_LOGGED, todayStatus)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `weekStatuses updates when entries are added for today`() = runTest {
        // product: cal=100, multiplier=10 → 1000 kcal ≥ 600 threshold (30% of 2000)
        // prot=40 not in [80,120], so not GREEN — but calories ≥ threshold so not GRAY_LOGGED
        val today = LocalDate.now()
        foodRepo.seed(
            listOf(
                FoodEntry(productId = 1, product = product, date = today, mealType = MealType.BREAKFAST, multiplier = 10f)
            )
        )
        viewModel.uiState.test {
            val state = skipUntilSuccess() as TodayUiState.Success
            val todayStatus = state.weekStatuses.first { it.first == today }.second
            assertTrue("Expected non-GRAY_LOGGED status after adding entries", todayStatus != DayStatus.GRAY_LOGGED)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `weekStatuses changes when selected date moves to a different week`() = runTest {
        val lastMonday = LocalDate.now().with(DayOfWeek.MONDAY).minusWeeks(1)
        viewModel.goToDate(lastMonday)

        viewModel.uiState.test {
            val state = skipUntilSuccess() as TodayUiState.Success
            assertEquals(7, state.weekStatuses.size)
            assertEquals(DayOfWeek.MONDAY, state.weekStatuses.first().first.dayOfWeek)
            assertEquals(lastMonday, state.weekStatuses.first().first)
            cancelAndConsumeRemainingEvents()
        }
    }

    // ── streak ────────────────────────────────────────────────────────────────

    @Test
    fun `streak is zero when no entries`() = runTest {
        viewModel.uiState.test {
            val state = skipUntilSuccess() as TodayUiState.Success
            assertEquals(0, state.streak)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `streak is 1 when today has enough calories`() = runTest {
        // product: cal=700 per multiplier=1 → 700 ≥ 600 threshold
        val streakProduct = Product(
            id = 2, name = "Серия",
            caloriesPer100g = 700f,
            proteinPer100g = 10f, fatPer100g = 6f, carbsPer100g = 25f
        )
        val today = LocalDate.now()
        foodRepo.seed(
            listOf(
                FoodEntry(productId = 2, product = streakProduct, date = today, mealType = MealType.BREAKFAST, multiplier = 1f)
            )
        )
        viewModel.uiState.test {
            val state = skipUntilSuccess() as TodayUiState.Success
            assertEquals(1, state.streak)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `streak increases with consecutive qualifying days`() = runTest {
        val streakProduct = Product(
            id = 2, name = "Серия",
            caloriesPer100g = 700f,
            proteinPer100g = 10f, fatPer100g = 6f, carbsPer100g = 25f
        )
        val today = LocalDate.now()
        foodRepo.seed(
            listOf(
                FoodEntry(productId = 2, product = streakProduct, date = today, mealType = MealType.BREAKFAST, multiplier = 1f),
                FoodEntry(productId = 2, product = streakProduct, date = today.minusDays(1), mealType = MealType.BREAKFAST, multiplier = 1f),
                FoodEntry(productId = 2, product = streakProduct, date = today.minusDays(2), mealType = MealType.BREAKFAST, multiplier = 1f)
            )
        )
        viewModel.uiState.test {
            val state = skipUntilSuccess() as TodayUiState.Success
            assertEquals(3, state.streak)
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
