package com.k.shavrin.diethelper.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.k.shavrin.diethelper.data.local.DietHelperDatabase
import com.k.shavrin.diethelper.data.local.entity.ProductEntity
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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class ProductDaoTest {

    private lateinit var db: DietHelperDatabase
    private lateinit var dao: ProductDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            DietHelperDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.productDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    // ── insertProduct ────────────────────────────────────────────────────────

    @Test
    fun `insertProduct returns generated id greater than zero`() = runTest {
        val id = dao.insertProduct(ProductEntity(name = "Apple", caloriesPer100g = 52f, proteinPer100g = 0.3f, fatPer100g = 0.2f, carbsPer100g = 14f))
        assertTrue(id > 0)
    }

    @Test
    fun `insertProduct stores product retrievable by id`() = runTest {
        val id = dao.insertProduct(ProductEntity(name = "Banana", caloriesPer100g = 89f, proteinPer100g = 1.1f, fatPer100g = 0.3f, carbsPer100g = 23f))
        val retrieved = dao.getProductById(id)
        assertEquals("Banana", retrieved?.name)
    }

    // ── getAllProducts ordering ───────────────────────────────────────────────

    @Test
    fun `getAllProducts returns favorites first then by name ascending`() = runTest {
        dao.insertProduct(ProductEntity(name = "Carrot", caloriesPer100g = 41f, proteinPer100g = 0.9f, fatPer100g = 0.2f, carbsPer100g = 10f, isFavorite = false))
        dao.insertProduct(ProductEntity(name = "Apple", caloriesPer100g = 52f, proteinPer100g = 0.3f, fatPer100g = 0.2f, carbsPer100g = 14f, isFavorite = false))
        dao.insertProduct(ProductEntity(name = "Banana", caloriesPer100g = 89f, proteinPer100g = 1.1f, fatPer100g = 0.3f, carbsPer100g = 23f, isFavorite = true))

        val list = dao.getAllProducts().first()

        assertEquals("Banana", list[0].name) // favorite first
        assertEquals("Apple", list[1].name) // then alphabetical
        assertEquals("Carrot", list[2].name)
    }

    // ── searchProducts ───────────────────────────────────────────────────────

    @Test
    fun `searchProducts returns matching products case-insensitively`() = runTest {
        dao.insertProduct(ProductEntity(name = "Apple", caloriesPer100g = 52f, proteinPer100g = 0.3f, fatPer100g = 0.2f, carbsPer100g = 14f))
        dao.insertProduct(ProductEntity(name = "Banana", caloriesPer100g = 89f, proteinPer100g = 1.1f, fatPer100g = 0.3f, carbsPer100g = 23f))

        val result = dao.searchProducts("apple").first()

        assertEquals(1, result.size)
        assertEquals("Apple", result[0].name)
    }

    @Test
    fun `searchProducts with empty string returns all products`() = runTest {
        dao.insertProduct(ProductEntity(name = "Apple", caloriesPer100g = 52f, proteinPer100g = 0.3f, fatPer100g = 0.2f, carbsPer100g = 14f))
        dao.insertProduct(ProductEntity(name = "Banana", caloriesPer100g = 89f, proteinPer100g = 1.1f, fatPer100g = 0.3f, carbsPer100g = 23f))

        val result = dao.searchProducts("").first()

        assertEquals(2, result.size)
    }

    @Test
    fun `searchProducts partial match works`() = runTest {
        dao.insertProduct(ProductEntity(name = "Pineapple", caloriesPer100g = 50f, proteinPer100g = 0.5f, fatPer100g = 0.1f, carbsPer100g = 13f))
        dao.insertProduct(ProductEntity(name = "Apple", caloriesPer100g = 52f, proteinPer100g = 0.3f, fatPer100g = 0.2f, carbsPer100g = 14f))
        dao.insertProduct(ProductEntity(name = "Banana", caloriesPer100g = 89f, proteinPer100g = 1.1f, fatPer100g = 0.3f, carbsPer100g = 23f))

        val result = dao.searchProducts("apple").first()

        assertEquals(2, result.size) // Apple and Pineapple
    }

    // ── setFavorite ──────────────────────────────────────────────────────────

    @Test
    fun `setFavorite marks product as favorite`() = runTest {
        val id = dao.insertProduct(ProductEntity(name = "Apple", caloriesPer100g = 52f, proteinPer100g = 0.3f, fatPer100g = 0.2f, carbsPer100g = 14f))

        dao.setFavorite(id, isFavorite = true)

        assertTrue(dao.getProductById(id)!!.isFavorite)
    }

    @Test
    fun `setFavorite can unmark favorite`() = runTest {
        val id = dao.insertProduct(ProductEntity(name = "Apple", caloriesPer100g = 52f, proteinPer100g = 0.3f, fatPer100g = 0.2f, carbsPer100g = 14f, isFavorite = true))

        dao.setFavorite(id, isFavorite = false)

        assertTrue(!dao.getProductById(id)!!.isFavorite)
    }

    // ── deleteProduct ────────────────────────────────────────────────────────

    @Test
    fun `deleteProduct removes it from database`() = runTest {
        val id = dao.insertProduct(ProductEntity(name = "Apple", caloriesPer100g = 52f, proteinPer100g = 0.3f, fatPer100g = 0.2f, carbsPer100g = 14f))
        val entity = dao.getProductById(id)!!

        dao.deleteProduct(entity)

        assertNull(dao.getProductById(id))
    }

    // ── getProductById ───────────────────────────────────────────────────────

    @Test
    fun `getProductById returns null for non-existent id`() = runTest {
        val result = dao.getProductById(999L)
        assertNull(result)
    }
}
