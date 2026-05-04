package com.k.shavrin.diethelper.data.repository

import app.cash.turbine.test
import com.k.shavrin.diethelper.data.local.dao.FoodEntryDao
import com.k.shavrin.diethelper.data.local.entity.FoodEntryEntity
import com.k.shavrin.diethelper.data.local.entity.FoodEntryWithProduct
import com.k.shavrin.diethelper.data.local.entity.ProductEntity
import com.k.shavrin.diethelper.domain.model.FoodEntry
import com.k.shavrin.diethelper.domain.model.MealType
import com.k.shavrin.diethelper.domain.model.Product
import com.k.shavrin.diethelper.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class FoodEntryRepositoryImplTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var dao: FakeFoodEntryDao
    private lateinit var repository: FoodEntryRepositoryImpl

    private val product = ProductEntity(
        id = 1, name = "Гречка", caloriesPer100g = 100f, proteinPer100g = 4f, fatPer100g = 1f, carbsPer100g = 20f
    )
    private val today: LocalDate = LocalDate.of(2025, 1, 15)
    private val yesterday: LocalDate = today.minusDays(1)

    @Before
    fun setUp() {
        dao = FakeFoodEntryDao(productLookup = mapOf(1L to product))
        repository = FoodEntryRepositoryImpl(dao)
    }

    @Test
    fun `getEntriesForDay returns mapped entries for matching date only`() = runTest {
        dao.seed(
            listOf(
                FoodEntryEntity(id = 1, productId = 1, date = today, mealType = "BREAKFAST", multiplier = 1.5f),
                FoodEntryEntity(id = 2, productId = 1, date = yesterday, mealType = "LUNCH", multiplier = 1f)
            )
        )

        repository.getEntriesForDay(today).test {
            val entries = awaitItem()
            assertEquals(1, entries.size)
            assertEquals(1L, entries[0].id)
            assertEquals(MealType.BREAKFAST, entries[0].mealType)
            assertEquals("Гречка", entries[0].product.name)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `addEntry resets id to 0 before insert`() = runTest {
        val product = Product(id = 1, name = "Гречка", caloriesPer100g = 100f, proteinPer100g = 4f, fatPer100g = 1f, carbsPer100g = 20f)
        val newId = repository.addEntry(
            FoodEntry(id = 999, productId = 1, product = product, date = today, mealType = MealType.BREAKFAST, multiplier = 1f)
        )
        assertEquals(1L, newId)
    }

    @Test
    fun `copyEntryToDay creates new row with target date`() = runTest {
        val product = Product(id = 1, name = "Гречка", caloriesPer100g = 100f, proteinPer100g = 4f, fatPer100g = 1f, carbsPer100g = 20f)
        val source = FoodEntry(id = 5, productId = 1, product = product, date = today, mealType = MealType.LUNCH, multiplier = 2f)

        repository.copyEntryToDay(source, yesterday)

        val all = dao.peekAll()
        assertEquals(1, all.size)
        assertEquals(yesterday, all[0].date)
        assertTrue(all[0].id != 5L)
    }
}

private class FakeFoodEntryDao(
    private val productLookup: Map<Long, ProductEntity>
) : FoodEntryDao {

    private val data = MutableStateFlow<List<FoodEntryEntity>>(emptyList())
    private var nextId = 1L

    fun seed(items: List<FoodEntryEntity>) {
        data.value = items
        nextId = (items.maxOfOrNull { it.id } ?: 0L) + 1
    }

    fun peekAll(): List<FoodEntryEntity> = data.value

    override fun getEntriesForDate(date: LocalDate): Flow<List<FoodEntryWithProduct>> = data.map { list ->
        list.filter { it.date == date }.mapNotNull { entry ->
            productLookup[entry.productId]?.let { FoodEntryWithProduct(entry, it) }
        }
    }

    override fun getEntriesForDates(dates: List<LocalDate>): Flow<List<FoodEntryWithProduct>> = data.map { list ->
        list.filter { it.date in dates }.mapNotNull { entry ->
            productLookup[entry.productId]?.let { FoodEntryWithProduct(entry, it) }
        }
    }

    override fun getDistinctDatesDescending(): Flow<List<LocalDate>> = data.map { list ->
        list.map { it.date }.distinct().sortedDescending()
    }

    override suspend fun insertEntry(entry: FoodEntryEntity): Long {
        val id = if (entry.id == 0L) nextId++ else entry.id
        data.update { it + entry.copy(id = id) }
        return id
    }

    override suspend fun updateEntry(entry: FoodEntryEntity) {
        data.update { list -> list.map { if (it.id == entry.id) entry else it } }
    }

    override suspend fun deleteEntry(entry: FoodEntryEntity) {
        data.update { list -> list.filterNot { it.id == entry.id } }
    }
}
