package com.k.shavrin.diethelper.presentation.screen.stats

import app.cash.turbine.test
import com.k.shavrin.diethelper.data.FakeFoodEntryRepository
import com.k.shavrin.diethelper.domain.model.FoodEntry
import com.k.shavrin.diethelper.domain.model.MealType
import com.k.shavrin.diethelper.domain.model.Product
import com.k.shavrin.diethelper.domain.usecase.stats.GetStatsRangeUseCase
import com.k.shavrin.diethelper.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class StatsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val product = Product(
        id = 1L,
        name = "Гречка",
        caloriesPer100g = 343f,
        proteinPer100g = 13f,
        fatPer100g = 3.4f,
        carbsPer100g = 72f
    )

    private fun entry(date: LocalDate, multiplier: Float = 1f) = FoodEntry(
        productId = 1L,
        product = product,
        date = date,
        mealType = MealType.BREAKFAST,
        multiplier = multiplier
    )

    private fun createViewModel(repo: FakeFoodEntryRepository): StatsViewModel =
        StatsViewModel(getStatsRangeUseCase = GetStatsRangeUseCase(repo))

    // ── Initial state ────────────────────────────────────────────────────────

    @Test
    fun `initial range covers the last 7 days ending today`() = runTest {
        val repo = FakeFoodEntryRepository()
        val viewModel = createViewModel(repo)

        viewModel.uiState.test {
            val state = skipLoading()
            val s = state as StatsUiState.Success
            assertEquals(LocalDate.now().minusDays(6), s.from)
            assertEquals(LocalDate.now(), s.to)
            cancelAndConsumeRemainingEvents()
        }
    }

    // ── Aggregation ──────────────────────────────────────────────────────────

    @Test
    fun `Success aggregates totals across days in range`() = runTest {
        val today = LocalDate.now()
        val repo = FakeFoodEntryRepository().apply {
            seed(
                listOf(
                    entry(today, multiplier = 1f),
                    entry(today.minusDays(1), multiplier = 2f)
                )
            )
        }
        val viewModel = createViewModel(repo)

        viewModel.uiState.test {
            val s = skipLoading() as StatsUiState.Success
            assertEquals(2, s.items.size)
            // 1x + 2x of `product` (calories 343 per multiplier=1)
            assertEquals(343f * 3f, s.totalCalories, 0.5f)
        }
    }

    @Test
    fun `Success items are sorted by date ascending`() = runTest {
        val today = LocalDate.now()
        val repo = FakeFoodEntryRepository().apply {
            seed(
                listOf(
                    entry(today.minusDays(2)),
                    entry(today),
                    entry(today.minusDays(1))
                )
            )
        }
        val viewModel = createViewModel(repo)

        viewModel.uiState.test {
            val s = skipLoading() as StatsUiState.Success
            val dates = s.items.map { it.date }
            assertEquals(dates.sorted(), dates)
        }
    }

    @Test
    fun `empty repository yields Success with empty items and zero totals`() = runTest {
        val repo = FakeFoodEntryRepository()
        val viewModel = createViewModel(repo)

        viewModel.uiState.test {
            val s = skipLoading() as StatsUiState.Success
            assertTrue(s.items.isEmpty())
            assertEquals(0f, s.totalCalories, 0f)
            assertEquals(0f, s.totalProtein, 0f)
            assertEquals(0f, s.totalFat, 0f)
            assertEquals(0f, s.totalCarbs, 0f)
        }
    }

    // ── setFrom / setTo ──────────────────────────────────────────────────────

    @Test
    fun `setFrom updates the from date when before to`() = runTest {
        val repo = FakeFoodEntryRepository()
        val viewModel = createViewModel(repo)

        val newFrom = LocalDate.now().minusDays(20)
        viewModel.setFrom(newFrom)

        viewModel.uiState.test {
            val s = awaitItem() as StatsUiState.Success
            assertEquals(newFrom, s.from)
        }
    }

    @Test
    fun `setFrom swaps from and to when new from is after to`() = runTest {
        val repo = FakeFoodEntryRepository()
        val viewModel = createViewModel(repo)

        val futureDate = LocalDate.now().plusDays(5)
        viewModel.setFrom(futureDate)

        viewModel.uiState.test {
            val s = awaitItem() as StatsUiState.Success
            // The new from sits at the previous to (today); the new to is the future date.
            assertEquals(LocalDate.now(), s.from)
            assertEquals(futureDate, s.to)
        }
    }

    @Test
    fun `setTo updates the to date when after from`() = runTest {
        val repo = FakeFoodEntryRepository()
        val viewModel = createViewModel(repo)

        val newTo = LocalDate.now().plusDays(3)
        viewModel.setTo(newTo)

        viewModel.uiState.test {
            val s = awaitItem() as StatsUiState.Success
            assertEquals(newTo, s.to)
        }
    }

    @Test
    fun `setTo swaps to and from when new to is before from`() = runTest {
        val repo = FakeFoodEntryRepository()
        val viewModel = createViewModel(repo)

        val past = LocalDate.now().minusDays(30)
        viewModel.setTo(past)

        viewModel.uiState.test {
            val s = awaitItem() as StatsUiState.Success
            // After swap, the previous `from` (now-6d) becomes the new `to`,
            // and the new `from` is the requested past date.
            assertEquals(past, s.from)
            assertEquals(LocalDate.now().minusDays(6), s.to)
        }
    }

    // ── selectPreset / selectCurrentMonth ────────────────────────────────────

    @Test
    fun `selectPreset 7 covers the last 7 days ending today`() = runTest {
        val repo = FakeFoodEntryRepository()
        val viewModel = createViewModel(repo)

        viewModel.selectPreset(7)

        viewModel.uiState.test {
            val s = awaitItem() as StatsUiState.Success
            assertEquals(LocalDate.now().minusDays(6), s.from)
            assertEquals(LocalDate.now(), s.to)
        }
    }

    @Test
    fun `selectPreset 30 covers the last 30 days ending today`() = runTest {
        val repo = FakeFoodEntryRepository()
        val viewModel = createViewModel(repo)

        viewModel.selectPreset(30)

        viewModel.uiState.test {
            val s = awaitItem() as StatsUiState.Success
            assertEquals(LocalDate.now().minusDays(29), s.from)
            assertEquals(LocalDate.now(), s.to)
        }
    }

    @Test
    fun `selectCurrentMonth covers from day 1 to today`() = runTest {
        val repo = FakeFoodEntryRepository()
        val viewModel = createViewModel(repo)

        viewModel.selectCurrentMonth()

        viewModel.uiState.test {
            val s = awaitItem() as StatsUiState.Success
            assertEquals(LocalDate.now().withDayOfMonth(1), s.from)
            assertEquals(LocalDate.now(), s.to)
        }
    }
}

private suspend fun <T> app.cash.turbine.ReceiveTurbine<T>.skipLoading(): T {
    while (true) {
        val item = awaitItem()
        if (item !is StatsUiState.Loading) return item
    }
}
