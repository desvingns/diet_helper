package com.k.shavrin.diethelper.data.repository

import app.cash.turbine.test
import com.k.shavrin.diethelper.data.local.dao.ProductDao
import com.k.shavrin.diethelper.data.local.entity.ProductEntity
import com.k.shavrin.diethelper.domain.model.Product
import com.k.shavrin.diethelper.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProductRepositoryImplTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var dao: FakeProductDao
    private lateinit var repository: ProductRepositoryImpl

    @Before
    fun setUp() {
        dao = FakeProductDao()
        repository = ProductRepositoryImpl(dao)
    }

    @Test
    fun `getAllProducts maps DAO entities to domain`() = runTest {
        dao.seed(
            listOf(
                ProductEntity(id = 1, name = "Apple", caloriesPer100g = 52f, proteinPer100g = 0.3f, fatPer100g = 0.2f, carbsPer100g = 14f),
                ProductEntity(id = 2, name = "Banana", caloriesPer100g = 89f, proteinPer100g = 1.1f, fatPer100g = 0.3f, carbsPer100g = 23f, isFavorite = true)
            )
        )

        repository.getAllProducts().test {
            val products = awaitItem()
            assertEquals(2, products.size)
            assertEquals("Banana", products[0].name)
            assertTrue(products[0].isFavorite)
            assertEquals("Apple", products[1].name)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `addProduct delegates to DAO and returns id`() = runTest {
        val id = repository.addProduct(
            Product(name = "Eggs", caloriesPer100g = 155f, proteinPer100g = 13f, fatPer100g = 11f, carbsPer100g = 1.1f)
        )
        assertEquals(1L, id)
        assertEquals("Eggs", dao.peekAll().first().name)
    }

    @Test
    fun `getProductById returns null for missing`() = runTest {
        assertNull(repository.getProductById(42L))
    }

    @Test
    fun `toggleFavorite flips DAO state`() = runTest {
        dao.seed(
            listOf(
                ProductEntity(id = 1, name = "Milk", caloriesPer100g = 50f, proteinPer100g = 3f, fatPer100g = 2.5f, carbsPer100g = 5f, isFavorite = false)
            )
        )

        repository.toggleFavorite(1L, true)
        assertTrue(dao.peekAll().first { it.id == 1L }.isFavorite)
    }
}

private class FakeProductDao : ProductDao {

    private val data = MutableStateFlow<List<ProductEntity>>(emptyList())
    private var nextId = 1L

    fun seed(items: List<ProductEntity>) {
        data.value = items
        nextId = (items.maxOfOrNull { it.id } ?: 0L) + 1
    }

    fun peekAll(): List<ProductEntity> = data.value

    override fun getAllProducts(): Flow<List<ProductEntity>> = data.map { list ->
        list.sortedWith(
            compareByDescending<ProductEntity> { it.isFavorite }.thenBy { it.name.lowercase() }
        )
    }

    override fun searchProducts(query: String): Flow<List<ProductEntity>> = data.map { list ->
        list.filter { it.name.contains(query, ignoreCase = true) }
            .sortedWith(
                compareByDescending<ProductEntity> { it.isFavorite }.thenBy { it.name.lowercase() }
            )
    }

    override suspend fun getProductById(id: Long): ProductEntity? =
        data.value.firstOrNull { it.id == id }

    override suspend fun insertProduct(product: ProductEntity): Long {
        val id = if (product.id == 0L) nextId++ else product.id
        data.update { it + product.copy(id = id) }
        return id
    }

    override suspend fun updateProduct(product: ProductEntity) {
        data.update { list -> list.map { if (it.id == product.id) product else it } }
    }

    override suspend fun deleteProduct(product: ProductEntity) {
        data.update { list -> list.filterNot { it.id == product.id } }
    }

    override suspend fun setFavorite(id: Long, isFavorite: Boolean) {
        data.update { list -> list.map { if (it.id == id) it.copy(isFavorite = isFavorite) else it } }
    }
}
