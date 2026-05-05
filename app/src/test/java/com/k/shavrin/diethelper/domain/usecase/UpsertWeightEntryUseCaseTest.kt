package com.k.shavrin.diethelper.domain.usecase

import com.k.shavrin.diethelper.data.FakeWeightRepository
import com.k.shavrin.diethelper.domain.model.WeightEntry
import com.k.shavrin.diethelper.domain.usecase.weight.UpsertWeightEntryUseCase
import com.k.shavrin.diethelper.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class UpsertWeightEntryUseCaseTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeWeightRepository()
    private val useCase = UpsertWeightEntryUseCase(repository)

    private val today: LocalDate = LocalDate.of(2025, 6, 1)

    @Test
    fun `creates new entry when none exists for the date`() = runTest {
        useCase(today, 75f)

        assertEquals(75f, repository.getEntryByDate(today)!!.weightKg, 0.001f)
    }

    @Test
    fun `updates existing entry for the same date`() = runTest {
        repository.seed(listOf(WeightEntry(date = today, weightKg = 80f)))

        useCase(today, 79.5f)

        assertEquals(79.5f, repository.getEntryByDate(today)!!.weightKg, 0.001f)
        assertEquals(1, countAll())
    }

    @Test
    fun `does not affect entries on other dates`() = runTest {
        val yesterday = today.minusDays(1)
        repository.seed(listOf(WeightEntry(date = yesterday, weightKg = 81f)))

        useCase(today, 80f)

        assertEquals(81f, repository.getEntryByDate(yesterday)!!.weightKg, 0.001f)
        assertEquals(80f, repository.getEntryByDate(today)!!.weightKg, 0.001f)
    }

    private suspend fun countAll(): Int = repository.getAllEntries().first().size
}
