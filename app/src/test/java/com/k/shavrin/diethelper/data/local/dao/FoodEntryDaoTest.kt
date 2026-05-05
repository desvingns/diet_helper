package com.k.shavrin.diethelper.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.k.shavrin.diethelper.data.local.DietHelperDatabase
import com.k.shavrin.diethelper.data.local.entity.FoodEntryEntity
import com.k.shavrin.diethelper.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class FoodEntryDaoTest {

    private lateinit var db: DietHelperDatabase
    private lateinit var productDao: ProductDao
    private lateinit var dao: FoodEntryDao

    private val today: LocalDate = LocalDate.of(2025, 6, 1)
    private val yesterday: LocalDate = today.minusDays(1)

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            DietHelperDatabase::class.java
        ).allowMainThreadQueries().build()
        productDao = db.productDao()
        dao = db.foodEntryDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private suspend fun insertProduct(name: String = "Apple"): Long =
        productDao.insertProduct(ProductEntity(name = name, caloriesPer100g = 52f, proteinPer100g = 0.3f, fatPer100g = 0.2f, carbsPer100g = 14f))

    private suspend fun insertEntry(productId: Long, date: LocalDate = today, mealType: String = "BREAKFAST", multiplier: Float = 1f): Long =
        dao.insertEntry(FoodEntryEntity(productId = productId, date = date, mealType = mealType, multiplier = multiplier))

    // ── getEntriesForDate ────────────────────────────────────────────────────

    @Test
    fun `getEntriesForDate returns only entries for requested date`() = runTest {
        val productId = insertProduct()
        insertEntry(productId, today)
        insertEntry(productId, yesterday)

        val entries = dao.getEntriesForDate(today).first()

        assertEquals(1, entries.size)
        assertEquals(today, entries[0].entry.date)
    }

    @Test
    fun `getEntriesForDate returns empty list when no entries for date`() = runTest {
        val entries = dao.getEntriesForDate(today).first()
        assertTrue(entries.isEmpty())
    }

    @Test
    fun `getEntriesForDate includes product via Relation join`() = runTest {
        val productId = insertProduct("Banana")
        insertEntry(productId, today)

        val entries = dao.getEntriesForDate(today).first()

        assertEquals("Banana", entries[0].product.name)
    }

    // ── getDistinctDatesDescending ───────────────────────────────────────────

    @Test
    fun `getDistinctDatesDescending returns unique dates sorted descending`() = runTest {
        val productId = insertProduct()
        insertEntry(productId, today)
        insertEntry(productId, today)       // duplicate date
        insertEntry(productId, yesterday)

        val dates = dao.getDistinctDatesDescending().first()

        assertEquals(2, dates.size)
        assertEquals(today, dates[0])
        assertEquals(yesterday, dates[1])
    }

    @Test
    fun `getDistinctDatesDescending returns empty when no entries`() = runTest {
        val dates = dao.getDistinctDatesDescending().first()
        assertTrue(dates.isEmpty())
    }

    // ── getEntriesForDates ───────────────────────────────────────────────────

    @Test
    fun `getEntriesForDates returns entries matching any of the requested dates`() = runTest {
        val productId = insertProduct()
        val twoDaysAgo = today.minusDays(2)
        insertEntry(productId, today)
        insertEntry(productId, yesterday)
        insertEntry(productId, twoDaysAgo)

        val entries = dao.getEntriesForDates(listOf(today, twoDaysAgo)).first()

        assertEquals(2, entries.size)
        val dates = entries.map { it.entry.date }.toSet()
        assertTrue(today in dates)
        assertTrue(twoDaysAgo in dates)
    }

    // ── insertEntry / updateEntry ────────────────────────────────────────────

    @Test
    fun `insertEntry with id zero auto-generates id`() = runTest {
        val productId = insertProduct()
        val id = dao.insertEntry(FoodEntryEntity(id = 0, productId = productId, date = today, mealType = "LUNCH", multiplier = 1f))
        assertTrue(id > 0)
    }

    @Test
    fun `updateEntry persists changed multiplier`() = runTest {
        val productId = insertProduct()
        val id = insertEntry(productId, multiplier = 1f)
        val entry = dao.getEntriesForDate(today).first()[0].entry

        dao.updateEntry(entry.copy(multiplier = 2.5f))

        val updated = dao.getEntriesForDate(today).first()[0].entry
        assertEquals(2.5f, updated.multiplier, 0.001f)
    }

    // ── deleteEntry ──────────────────────────────────────────────────────────

    @Test
    fun `deleteEntry removes entry from database`() = runTest {
        val productId = insertProduct()
        insertEntry(productId)
        val entry = dao.getEntriesForDate(today).first()[0].entry

        dao.deleteEntry(entry)

        assertTrue(dao.getEntriesForDate(today).first().isEmpty())
    }

    // ── CASCADE delete ───────────────────────────────────────────────────────

    @Test
    fun `deleting product cascades to its food entries`() = runTest {
        val productId = insertProduct()
        insertEntry(productId)

        val product = productDao.getProductById(productId)!!
        productDao.deleteProduct(product)

        assertTrue(dao.getEntriesForDate(today).first().isEmpty())
    }
}
