package com.k.shavrin.diethelper.presentation.screen.weight

import app.cash.turbine.test
import com.k.shavrin.diethelper.data.FakeWeightRepository
import com.k.shavrin.diethelper.domain.model.WeightEntry
import com.k.shavrin.diethelper.domain.usecase.weight.DeleteWeightEntryUseCase
import com.k.shavrin.diethelper.domain.usecase.weight.GetAllWeightEntriesUseCase
import com.k.shavrin.diethelper.domain.usecase.weight.UpsertWeightEntryUseCase
import com.k.shavrin.diethelper.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class WeightViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun createViewModel(repo: FakeWeightRepository): WeightViewModel = WeightViewModel(
        getAllUseCase = GetAllWeightEntriesUseCase(repo),
        upsertUseCase = UpsertWeightEntryUseCase(repo),
        deleteUseCase = DeleteWeightEntryUseCase(repo)
    )

    @Test
    fun `delta is null for the only entry`() = runTest {
        val today = LocalDate.now()
        val repo = FakeWeightRepository().apply {
            seed(listOf(WeightEntry(date = today, weightKg = 80f)))
        }
        val viewModel = createViewModel(repo)

        viewModel.uiState.test {
            val s = skipLoading()
            val items = (s as WeightUiState.Success).items
            assertEquals(1, items.size)
            assertNull(items[0].deltaKg)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `delta is positive when newer record is heavier`() = runTest {
        val today = LocalDate.now()
        val repo = FakeWeightRepository().apply {
            seed(
                listOf(
                    WeightEntry(date = today, weightKg = 81f),
                    WeightEntry(date = today.minusDays(1), weightKg = 80f)
                )
            )
        }
        val viewModel = createViewModel(repo)

        viewModel.uiState.test {
            val s = skipLoading()
            val items = (s as WeightUiState.Success).items
            assertEquals(2, items.size)
            assertEquals(1f, items[0].deltaKg!!, 0.001f)
            assertNull(items[1].deltaKg)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `save inserts today entry when input is valid`() = runTest {
        val repo = FakeWeightRepository()
        val viewModel = createViewModel(repo)

        viewModel.onInputChange("75.5")
        viewModel.save()

        assertEquals(75.5f, repo.getEntryByDate(LocalDate.now())!!.weightKg, 0.001f)
    }

    @Test
    fun `save with non-positive value is ignored`() = runTest {
        val repo = FakeWeightRepository()
        val viewModel = createViewModel(repo)

        viewModel.onInputChange("0")
        viewModel.save()

        assertNull(repo.getEntryByDate(LocalDate.now()))
    }

    @Test
    fun `save accepts comma decimal separator`() = runTest {
        val repo = FakeWeightRepository()
        val viewModel = createViewModel(repo)

        viewModel.onInputChange("76,2")
        viewModel.save()

        assertEquals(76.2f, repo.getEntryByDate(LocalDate.now())!!.weightKg, 0.001f)
    }

    @Test
    fun `delete removes entry`() = runTest {
        val date = LocalDate.now()
        val entry = WeightEntry(id = 1, date = date, weightKg = 80f)
        val repo = FakeWeightRepository().apply { seed(listOf(entry)) }
        val viewModel = createViewModel(repo)

        viewModel.delete(entry)

        assertNull(repo.getEntryByDate(date))
    }
}

private suspend fun <T> app.cash.turbine.ReceiveTurbine<T>.skipLoading(): T {
    while (true) {
        val item = awaitItem()
        if (item !is WeightUiState.Loading) return item
    }
}
