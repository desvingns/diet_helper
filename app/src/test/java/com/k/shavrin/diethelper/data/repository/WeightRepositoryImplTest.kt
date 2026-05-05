package com.k.shavrin.diethelper.data.repository

import app.cash.turbine.test
import com.k.shavrin.diethelper.data.local.dao.WeightEntryDao
import com.k.shavrin.diethelper.data.local.entity.WeightEntryEntity
import com.k.shavrin.diethelper.domain.model.WeightEntry
import com.k.shavrin.diethelper.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class WeightRepositoryImplTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var dao: FakeWeightEntryDao
    private lateinit var repository: WeightRepositoryImpl

    private val today: LocalDate = LocalDate.of(2025, 6, 1)
    private val yesterday: LocalDate = today.minusDays(1)

    @Before
    fun setUp() {
        dao = FakeWeightEntryDao()
        repository = WeightRepositoryImpl(dao)
    }

    @Test
    fun `getAllEntries maps entities to domain sorted descending`() = runTest {
        dao.seed(
            listOf(
                WeightEntryEntity(id = 1, date = yesterday, weightKg = 81f),
                WeightEntryEntity(id = 2, date = today, weightKg = 80f)
            )
        )

        repository.getAllEntries().test {
            val list = awaitItem()
            assertEquals(2, list.size)
            assertEquals(today, list[0].date)
            assertEquals(yesterday, list[1].date)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `getEntryByDate returns null when not found`() = runTest {
        assertNull(repository.getEntryByDate(today))
    }

    @Test
    fun `upsertEntry inserts when no entry for date`() = runTest {
        repository.upsertEntry(today, 78f)

        assertEquals(78f, repository.getEntryByDate(today)!!.weightKg, 0.001f)
    }

    @Test
    fun `upsertEntry updates when entry already exists for date`() = runTest {
        dao.seed(listOf(WeightEntryEntity(id = 1, date = today, weightKg = 80f)))

        repository.upsertEntry(today, 79f)

        assertEquals(79f, repository.getEntryByDate(today)!!.weightKg, 0.001f)
        assertEquals(1, dao.peekAll().size)
    }

    @Test
    fun `upsertEntry keeps existing id when updating`() = runTest {
        dao.seed(listOf(WeightEntryEntity(id = 42, date = today, weightKg = 80f)))

        repository.upsertEntry(today, 77f)

        assertEquals(42L, dao.peekAll().first().id)
    }

    @Test
    fun `deleteEntry removes by entity`() = runTest {
        dao.seed(listOf(WeightEntryEntity(id = 1, date = today, weightKg = 80f)))

        repository.deleteEntry(WeightEntry(id = 1, date = today, weightKg = 80f))

        assertNull(repository.getEntryByDate(today))
    }
}

private class FakeWeightEntryDao : WeightEntryDao {

    private val data = MutableStateFlow<List<WeightEntryEntity>>(emptyList())
    private var nextId = 1L

    fun seed(items: List<WeightEntryEntity>) {
        data.value = items
        nextId = (items.maxOfOrNull { it.id } ?: 0L) + 1
    }

    fun peekAll(): List<WeightEntryEntity> = data.value

    override fun getAllEntries(): Flow<List<WeightEntryEntity>> =
        data.map { list -> list.sortedByDescending { it.date } }

    override suspend fun getEntryByDate(date: LocalDate): WeightEntryEntity? =
        data.value.firstOrNull { it.date == date }

    override suspend fun upsertEntry(entry: WeightEntryEntity): Long {
        val existing = data.value.firstOrNull { it.date == entry.date }
        return if (existing != null) {
            data.update { list -> list.map { if (it.id == existing.id) entry.copy(id = existing.id) else it } }
            existing.id
        } else {
            val id = if (entry.id == 0L) nextId++ else entry.id
            data.update { it + entry.copy(id = id) }
            id
        }
    }

    override suspend fun deleteEntry(entry: WeightEntryEntity) {
        data.update { list -> list.filterNot { it.id == entry.id } }
    }
}
