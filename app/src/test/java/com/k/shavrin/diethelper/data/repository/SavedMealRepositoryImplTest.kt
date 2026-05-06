package com.k.shavrin.diethelper.data.repository

import app.cash.turbine.test
import com.k.shavrin.diethelper.data.local.dao.SavedMealDao
import com.k.shavrin.diethelper.data.local.entity.ProductEntity
import com.k.shavrin.diethelper.data.local.entity.SavedMealEntity
import com.k.shavrin.diethelper.data.local.entity.SavedMealItemEntity
import com.k.shavrin.diethelper.data.local.entity.SavedMealItemWithProduct
import com.k.shavrin.diethelper.data.local.entity.SavedMealWithItems
import com.k.shavrin.diethelper.domain.model.Product
import com.k.shavrin.diethelper.domain.model.SavedMealItem
import com.k.shavrin.diethelper.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SavedMealRepositoryImplTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var dao: FakeSavedMealDao
    private lateinit var repository: SavedMealRepositoryImpl

    @Before
    fun setUp() {
        dao = FakeSavedMealDao()
        repository = SavedMealRepositoryImpl(dao)
    }

    private val productEntity = ProductEntity(
        id = 1,
        name = "Apple",
        caloriesPer100g = 52f,
        proteinPer100g = 0.3f,
        fatPer100g = 0.2f,
        carbsPer100g = 14f
    )

    private val product = Product(
        id = 1,
        name = "Apple",
        caloriesPer100g = 52f,
        proteinPer100g = 0.3f,
        fatPer100g = 0.2f,
        carbsPer100g = 14f
    )

    @Test
    fun `getSavedMeals returns empty list when dao is empty`() = runTest {
        repository.getSavedMeals().test {
            assertTrue(awaitItem().isEmpty())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `getSavedMeals maps DAO entities to domain models`() = runTest {
        val mealEntity = SavedMealEntity(id = 1, name = "Завтрак")
        val itemEntity = SavedMealItemEntity(id = 1, savedMealId = 1, productId = 1, multiplier = 1.5f)
        dao.seed(
            listOf(
                SavedMealWithItems(
                    meal = mealEntity,
                    items = listOf(SavedMealItemWithProduct(item = itemEntity, product = productEntity))
                )
            )
        )

        repository.getSavedMeals().test {
            val list = awaitItem()
            assertEquals(1, list.size)
            assertEquals("Завтрак", list[0].name)
            assertEquals(1, list[0].id)
            assertEquals(1, list[0].items.size)
            assertEquals(1.5f, list[0].items[0].multiplier, 0.001f)
            assertEquals("Apple", list[0].items[0].product.name)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `saveMeal deletes existing meal by name then inserts new one`() = runTest {
        // seed an existing meal
        val existing = SavedMealWithItems(
            meal = SavedMealEntity(id = 1, name = "Обед"),
            items = emptyList()
        )
        dao.seed(listOf(existing))

        val items = listOf(
            SavedMealItem(savedMealId = 0, productId = 1, product = product, multiplier = 2f)
        )
        repository.saveMeal("Обед", items)

        assertEquals(1, dao.deletedByNameCalls.count { it == "Обед" })
        assertTrue(dao.insertedMeals.any { it.name == "Обед" })
        assertEquals(1, dao.insertedItems.size)
        assertEquals(2f, dao.insertedItems[0].multiplier, 0.001f)
    }

    @Test
    fun `saveMeal inserts item entities with correct savedMealId`() = runTest {
        val items = listOf(
            SavedMealItem(savedMealId = 0, productId = 1, product = product, multiplier = 0.5f)
        )

        repository.saveMeal("Ужин", items)

        val insertedMealId = dao.insertedMeals.last().let { dao.lastInsertedId }
        assertEquals(insertedMealId, dao.insertedItems[0].savedMealId)
    }

    @Test
    fun `deleteMeal calls deleteById on DAO`() = runTest {
        repository.deleteMeal(42L)

        assertTrue(dao.deletedByIdCalls.contains(42L))
    }

    @Test
    fun `getSavedMeals emits updated list after saveMeal`() = runTest {
        repository.getSavedMeals().test {
            assertTrue(awaitItem().isEmpty())

            repository.saveMeal("Перекус", emptyList())

            val updated = awaitItem()
            assertEquals(1, updated.size)
            assertEquals("Перекус", updated[0].name)
            cancelAndConsumeRemainingEvents()
        }
    }

    // ── fake DAO ─────────────────────────────────────────────────────────────

    private inner class FakeSavedMealDao : SavedMealDao {

        private val data = MutableStateFlow<List<SavedMealWithItems>>(emptyList())
        private var nextId = 1L

        val deletedByNameCalls = mutableListOf<String>()
        val deletedByIdCalls = mutableListOf<Long>()
        val insertedMeals = mutableListOf<SavedMealEntity>()
        val insertedItems = mutableListOf<SavedMealItemEntity>()
        var lastInsertedId = 0L

        fun seed(items: List<SavedMealWithItems>) {
            data.value = items
            nextId = (items.maxOfOrNull { it.meal.id } ?: 0L) + 1
        }

        override fun getAllWithItems(): Flow<List<SavedMealWithItems>> = data

        override suspend fun getByName(name: String): SavedMealEntity? =
            data.value.firstOrNull { it.meal.name == name }?.meal

        override suspend fun insertMeal(entity: SavedMealEntity): Long {
            val id = nextId++
            lastInsertedId = id
            val inserted = entity.copy(id = id)
            insertedMeals += inserted
            data.update { list -> list + SavedMealWithItems(meal = inserted, items = emptyList()) }
            return id
        }

        override suspend fun insertItems(items: List<SavedMealItemEntity>) {
            insertedItems += items
        }

        override suspend fun deleteByName(name: String) {
            deletedByNameCalls += name
            data.update { list -> list.filterNot { it.meal.name == name } }
        }

        override suspend fun deleteById(id: Long) {
            deletedByIdCalls += id
            data.update { list -> list.filterNot { it.meal.id == id } }
        }
    }
}
