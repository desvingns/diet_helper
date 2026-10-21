package com.k.shavrin.diethelper.presentation.screen.weight

import app.cash.turbine.test
import com.k.shavrin.diethelper.data.FakeWeightRepository
import com.k.shavrin.diethelper.domain.model.WeightEntry
import com.k.shavrin.diethelper.domain.repository.WeightRepository
import com.k.shavrin.diethelper.domain.usecase.weight.DeleteWeightEntryUseCase
import com.k.shavrin.diethelper.domain.usecase.weight.GetAllWeightEntriesUseCase
import com.k.shavrin.diethelper.domain.usecase.weight.UpdateWeightEntryUseCase
import com.k.shavrin.diethelper.domain.usecase.weight.UpsertWeightEntryUseCase
import com.k.shavrin.diethelper.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class WeightViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun createViewModel(repo: WeightRepository): WeightViewModel = WeightViewModel(
        getAllUseCase = GetAllWeightEntriesUseCase(repo),
        upsertUseCase = UpsertWeightEntryUseCase(repo),
        deleteUseCase = DeleteWeightEntryUseCase(repo),
        updateUseCase = UpdateWeightEntryUseCase(repo)
    )

    @Test
    fun `save uses a selected past date`() = runTest {
        val repo = FakeWeightRepository()
        val vm = createViewModel(repo)
        val date = LocalDate.now().minusDays(7)
        vm.selectDate(date)
        vm.onInputChange("83,4")
        vm.save()
        assertEquals(83.4f, repo.getEntryByDate(date)!!.weightKg, 0.001f)
        assertNull(repo.getEntryByDate(LocalDate.now()))
    }

    @Test
    fun `editing moves one stable record and updates its weight`() = runTest {
        val before = WeightEntry(42, LocalDate.now().minusDays(3), 80f)
        val repo = FakeWeightRepository().apply { seed(listOf(before)) }
        val vm = createViewModel(repo)
        vm.edit(before)
        vm.selectDate(before.date.minusDays(1))
        vm.onInputChange("79,8")
        vm.save()
        assertNull(repo.getEntryByDate(before.date))
        assertEquals(before.copy(date = before.date.minusDays(1), weightKg = 79.8f),
            repo.getEntryByDate(before.date.minusDays(1)))
        assertTrue(vm.saved.value)
    }

    @Test
    fun `conflicting edit preserves both entries and keeps the form open`() = runTest {
        val first = WeightEntry(1, LocalDate.now().minusDays(2), 80f)
        val second = WeightEntry(2, first.date.plusDays(1), 81f)
        val repo = FakeWeightRepository().apply { seed(listOf(first, second)) }
        val vm = createViewModel(repo)
        vm.edit(first)
        vm.selectDate(second.date)
        vm.onInputChange("77")
        vm.save()
        assertNotNull(vm.inputError.value)
        assertFalse(vm.saved.value)
        assertEquals(first, repo.getEntryByDate(first.date))
        assertEquals(second, repo.getEntryByDate(second.date))
        assertEquals(first, vm.editing.value)
    }

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

    @Test
    fun `invalid and non-finite weights show feedback without writing entries`() = runTest {
        val repo = FakeWeightRepository()
        val viewModel = createViewModel(repo)
        listOf("", "abc", "0", "-1", "NaN", "Infinity", "501").forEach { input ->
            viewModel.onInputChange(input)
            viewModel.save()
            assertNotNull("Missing feedback for $input", viewModel.inputError.value)
            assertNull(repo.getEntryByDate(LocalDate.now()))
            assertFalse(viewModel.saving.value)
        }
    }

    @Test
    fun `editing clears error and successful save confirms today's entry`() = runTest {
        val repo = FakeWeightRepository()
        val viewModel = createViewModel(repo)
        viewModel.save()
        assertNotNull(viewModel.inputError.value)
        viewModel.onInputChange(" 75,5 ")
        assertNull(viewModel.inputError.value)
        viewModel.save()
        assertTrue(viewModel.saved.value)
        assertFalse(viewModel.saving.value)
        assertEquals(75.5f, repo.getEntryByDate(LocalDate.now())!!.weightKg, 0.001f)
        viewModel.onInputChange("76")
        assertFalse(viewModel.saved.value)
    }

    @Test
    fun `failed save clears stale confirmation and can be retried`() = runTest {
        val storage = FakeWeightRepository()
        var fail = false
        val repo = object : WeightRepository by storage {
            override suspend fun upsertEntry(date: LocalDate, weightKg: Float) {
                if (fail) throw java.io.IOException("Unavailable storage")
                storage.upsertEntry(date, weightKg)
            }
        }
        val viewModel = createViewModel(repo)
        viewModel.onInputChange("75,5")
        viewModel.save()
        assertTrue(viewModel.saved.value)
        fail = true
        viewModel.save()
        assertNotNull(viewModel.inputError.value)
        assertFalse(viewModel.saved.value)
        assertFalse(viewModel.saving.value)
        assertEquals(75.5f, storage.getEntryByDate(LocalDate.now())!!.weightKg, 0.001f)
        fail = false
        viewModel.save()
        assertTrue(viewModel.saved.value)
        assertNull(viewModel.inputError.value)
    }
}

private suspend fun <T> app.cash.turbine.ReceiveTurbine<T>.skipLoading(): T {
    while (true) {
        val item = awaitItem()
        if (item !is WeightUiState.Loading) return item
    }
}
