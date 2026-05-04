package com.k.shavrin.diethelper.domain.repository

import com.k.shavrin.diethelper.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun getAllProducts(): Flow<List<Product>>
    fun searchProducts(query: String): Flow<List<Product>>
    suspend fun getProductById(id: Long): Product?
    suspend fun addProduct(product: Product): Long
    suspend fun updateProduct(product: Product)
    suspend fun deleteProduct(product: Product)
    suspend fun toggleFavorite(productId: Long, isFavorite: Boolean)
}
