package com.k.shavrin.diethelper.domain.usecase

import app.cash.turbine.test
import com.k.shavrin.diethelper.data.FakeWeightRepository
import com.k.shavrin.diethelper.domain.model.WeightEntry
import com.k.shavrin.diethelper.domain.usecase.weight.DeleteWeightEntryUseCase
import com.k.shavrin.diethelper.domain.usecase.weight.GetAllWeightEntriesUseCase
import com.k.shavrin.diethelper.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class WeightUseCasesTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repo = FakeWeightRepository()
    private val today = LocalDate.of(2025, 5, 1)

    // ── GetAllWeightEntriesUseCase ───────────────────────────────────────────

    @Test
    fun `GetAllWeightEntriesUseCase emits empty list initially`() = runTest {
        GetAllWeightEntriesUseCase(repo)().test {
            assertTrue(awaitItem().isEmpty())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `GetAllWeightEntriesUseCase emits entries sorted descending`() = runTest {
        val yesterday = today.minusDays(1)
        repo.seed(
            listOf(
                WeightEntry(date = yesterday, weightKg = 81f),
                WeightEntry(date = today, weightKg = 80f)
            )
        )

        GetAllWeightEntriesUseCase(repo)().test {
            val list = awaitItem()
            assertEquals(today, list[0].date)
            assertEquals(yesterday, list[1].date)
            cancelAndConsumeRemainingEvents()
        }
    }

    // ── DeleteWeightEntryUseCase ─────────────────────────────────────────────

    @Test
    fun `DeleteWeightEntryUseCase removes entry`() = runTest {
        val entry = WeightEntry(id = 1, date = today, weightKg = 80f)
        repo.seed(listOf(entry))

        DeleteWeightEntryUseCase(repo)(entry)

        assertNull(repo.getEntryByDate(today))
    }
}
