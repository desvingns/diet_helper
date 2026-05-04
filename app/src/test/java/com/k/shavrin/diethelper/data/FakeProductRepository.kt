package com.k.shavrin.diethelper.data

import com.k.shavrin.diethelper.domain.model.Product
import com.k.shavrin.diethelper.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeProductRepository : ProductRepository {

    private val products = MutableStateFlow<List<Product>>(emptyList())
    private var nextId = 1L

    fun seed(initial: List<Product>) {
        products.value = initial.mapIndexed { index, product ->
            if (product.id == 0L) product.copy(id = (index + 1).toLong()) else product
        }
        nextId = (products.value.maxOfOrNull { it.id } ?: 0L) + 1
    }

    override fun getAllProducts(): Flow<List<Product>> = products.map { list ->
        list.sortedWith(
            compareByDescending<Product> { it.isFavorite }.thenBy { it.name.lowercase() }
        )
    }

    override fun searchProducts(query: String): Flow<List<Product>> = products.map { list ->
        list.filter { it.name.contains(query, ignoreCase = true) }
            .sortedWith(
                compareByDescending<Product> { it.isFavorite }.thenBy { it.name.lowercase() }
            )
    }

    override suspend fun getProductById(id: Long): Product? =
        products.value.firstOrNull { it.id == id }

    override suspend fun addProduct(product: Product): Long {
        val id = nextId++
        products.update { it + product.copy(id = id) }
        return id
    }

    override suspend fun updateProduct(product: Product) {
        products.update { list -> list.map { if (it.id == product.id) product else it } }
    }

    override suspend fun deleteProduct(product: Product) {
        products.update { list -> list.filterNot { it.id == product.id } }
    }

    override suspend fun toggleFavorite(productId: Long, isFavorite: Boolean) {
        products.update { list ->
            list.map { if (it.id == productId) it.copy(isFavorite = isFavorite) else it }
        }
    }
}
