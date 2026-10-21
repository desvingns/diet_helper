package com.k.shavrin.diethelper.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.k.shavrin.diethelper.data.local.DietHelperDatabase
import com.k.shavrin.diethelper.data.local.entity.WeightEntryEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class WeightEntryDaoTest {

    private lateinit var db: DietHelperDatabase
    private lateinit var dao: WeightEntryDao

    private val today: LocalDate = LocalDate.of(2025, 6, 1)
    private val yesterday: LocalDate = today.minusDays(1)

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            DietHelperDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.weightEntryDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    // ── getAllEntries ─────────────────────────────────────────────────────────

    @Test
    fun `editing changes weight and date without duplicating the record`() = runTest {
        dao.upsertEntry(WeightEntryEntity(id = 42, date = today, weightKg = 80f))
        dao.updateEntry(WeightEntryEntity(id = 42, date = yesterday, weightKg = 79.5f))
        assertNull(dao.getEntryByDate(today))
        assertEquals(listOf(WeightEntryEntity(42, yesterday, 79.5f)), dao.getAllEntries().first())
    }

    @Test
    fun `moving to an occupied date rolls back and preserves both weigh ins`() = runTest {
        val first = WeightEntryEntity(1, yesterday, 81f)
        val second = WeightEntryEntity(2, today, 80f)
        dao.upsertEntry(first)
        dao.upsertEntry(second)
        var rejected = false
        try {
            dao.updateEntry(first.copy(date = today, weightKg = 77f))
        } catch (_: IllegalArgumentException) {
            rejected = true
        }
        assertTrue(rejected)
        assertEquals(listOf(second, first), dao.getAllEntries().first())
    }

    @Test
    fun `getAllEntries returns entries sorted by date descending`() = runTest {
        dao.upsertEntry(WeightEntryEntity(date = yesterday, weightKg = 81f))
        dao.upsertEntry(WeightEntryEntity(date = today, weightKg = 80f))

        val entries = dao.getAllEntries().first()

        assertEquals(today, entries[0].date)
        assertEquals(yesterday, entries[1].date)
    }

    @Test
    fun `getAllEntries returns empty list when no entries`() = runTest {
        assertTrue(dao.getAllEntries().first().isEmpty())
    }

    // ── getEntryByDate ───────────────────────────────────────────────────────

    @Test
    fun `getEntryByDate returns null when no entry for date`() = runTest {
        assertNull(dao.getEntryByDate(today))
    }

    @Test
    fun `getEntryByDate returns correct entry for date`() = runTest {
        dao.upsertEntry(WeightEntryEntity(date = today, weightKg = 80f))

        val entry = dao.getEntryByDate(today)

        assertEquals(80f, entry!!.weightKg, 0.001f)
        assertEquals(today, entry.date)
    }

    // ── upsertEntry — insert ─────────────────────────────────────────────────

    @Test
    fun `upsertEntry inserts new entry when none exists for date`() = runTest {
        dao.upsertEntry(WeightEntryEntity(date = today, weightKg = 75f))

        assertEquals(75f, dao.getEntryByDate(today)!!.weightKg, 0.001f)
    }

    @Test
    fun `upsertEntry returns generated id greater than zero on insert`() = runTest {
        val id = dao.upsertEntry(WeightEntryEntity(date = today, weightKg = 80f))
        assertTrue(id > 0)
    }

    // ── upsertEntry — update ─────────────────────────────────────────────────

    @Test
    fun `upsertEntry updates existing entry for same date`() = runTest {
        val id = dao.upsertEntry(WeightEntryEntity(date = today, weightKg = 80f))

        // @Upsert resolves conflicts by PRIMARY KEY — preserve the id from insert
        dao.upsertEntry(WeightEntryEntity(id = id, date = today, weightKg = 79.5f))

        assertEquals(79.5f, dao.getEntryByDate(today)!!.weightKg, 0.001f)
        assertEquals(1, dao.getAllEntries().first().size)
    }

    @Test
    fun `upsertEntry does not affect entries on other dates`() = runTest {
        dao.upsertEntry(WeightEntryEntity(date = yesterday, weightKg = 81f))

        dao.upsertEntry(WeightEntryEntity(date = today, weightKg = 80f))

        assertEquals(81f, dao.getEntryByDate(yesterday)!!.weightKg, 0.001f)
        assertEquals(2, dao.getAllEntries().first().size)
    }

    // ── deleteEntry ──────────────────────────────────────────────────────────

    @Test
    fun `deleteEntry removes entry from database`() = runTest {
        dao.upsertEntry(WeightEntryEntity(date = today, weightKg = 80f))
        val entry = dao.getEntryByDate(today)!!

        dao.deleteEntry(entry)

        assertNull(dao.getEntryByDate(today))
    }

    @Test
    fun `deleteEntry does not affect other entries`() = runTest {
        dao.upsertEntry(WeightEntryEntity(date = yesterday, weightKg = 81f))
        dao.upsertEntry(WeightEntryEntity(date = today, weightKg = 80f))
        val todayEntry = dao.getEntryByDate(today)!!

        dao.deleteEntry(todayEntry)

        assertEquals(1, dao.getAllEntries().first().size)
        assertEquals(yesterday, dao.getAllEntries().first()[0].date)
    }
}
