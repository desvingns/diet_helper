package com.k.shavrin.diethelper.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.k.shavrin.diethelper.data.local.DietHelperDatabase
import com.k.shavrin.diethelper.data.local.entity.ProductEntity
import com.k.shavrin.diethelper.data.local.entity.SavedMealEntity
import com.k.shavrin.diethelper.data.local.entity.SavedMealItemEntity
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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class SavedMealDaoTest {

    private lateinit var db: DietHelperDatabase
    private lateinit var productDao: ProductDao
    private lateinit var dao: SavedMealDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            DietHelperDatabase::class.java
        ).allowMainThreadQueries().build()
        productDao = db.productDao()
        dao = db.savedMealDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private suspend fun insertProduct(name: String = "Apple"): Long =
        productDao.insertProduct(
            ProductEntity(
                name = name,
                caloriesPer100g = 52f,
                proteinPer100g = 0.3f,
                fatPer100g = 0.2f,
                carbsPer100g = 14f
            )
        )

    @Test
    fun `insertMeal and getAllWithItems returns stored meal`() = runTest {
        val productId = insertProduct()
        val mealId = dao.insertMeal(SavedMealEntity(name = "Завтрак"))
        dao.insertItems(
            listOf(SavedMealItemEntity(savedMealId = mealId, productId = productId, multiplier = 1.5f))
        )

        val result = dao.getAllWithItems().first()

        assertEquals(1, result.size)
        assertEquals("Завтрак", result[0].meal.name)
        assertEquals(1, result[0].items.size)
        assertEquals(1.5f, result[0].items[0].item.multiplier, 0.001f)
        assertEquals("Apple", result[0].items[0].product.name)
    }

    @Test
    fun `deleteByName removes meal and cascades to items`() = runTest {
        val productId = insertProduct()
        val mealId = dao.insertMeal(SavedMealEntity(name = "Обед"))
        dao.insertItems(
            listOf(SavedMealItemEntity(savedMealId = mealId, productId = productId, multiplier = 1f))
        )

        dao.deleteByName("Обед")

        val result = dao.getAllWithItems().first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `deleteById removes meal and cascades to items`() = runTest {
        val productId = insertProduct()
        val mealId = dao.insertMeal(SavedMealEntity(name = "Ужин"))
        dao.insertItems(
            listOf(SavedMealItemEntity(savedMealId = mealId, productId = productId, multiplier = 2f))
        )

        dao.deleteById(mealId)

        val result = dao.getAllWithItems().first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `meals are returned sorted alphabetically`() = runTest {
        dao.insertMeal(SavedMealEntity(name = "Ужин"))
        dao.insertMeal(SavedMealEntity(name = "Завтрак"))
        dao.insertMeal(SavedMealEntity(name = "Обед"))

        val result = dao.getAllWithItems().first()

        assertEquals(3, result.size)
        assertEquals("Завтрак", result[0].meal.name)
        assertEquals("Обед", result[1].meal.name)
        assertEquals("Ужин", result[2].meal.name)
    }

    @Test
    fun `overwrite deleteByName then insertMeal replaces meal`() = runTest {
        val productId = insertProduct()
        val mealId1 = dao.insertMeal(SavedMealEntity(name = "Завтрак"))
        dao.insertItems(
            listOf(SavedMealItemEntity(savedMealId = mealId1, productId = productId, multiplier = 1f))
        )

        dao.deleteByName("Завтрак")
        val mealId2 = dao.insertMeal(SavedMealEntity(name = "Завтрак"))
        dao.insertItems(
            listOf(SavedMealItemEntity(savedMealId = mealId2, productId = productId, multiplier = 3f))
        )

        val result = dao.getAllWithItems().first()
        assertEquals(1, result.size)
        assertEquals(3f, result[0].items[0].item.multiplier, 0.001f)
    }
}
