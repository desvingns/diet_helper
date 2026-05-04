package com.k.shavrin.diethelper.data.repository

import com.k.shavrin.diethelper.data.local.dao.ProductDao
import com.k.shavrin.diethelper.data.mapper.toDomain
import com.k.shavrin.diethelper.data.mapper.toEntity
import com.k.shavrin.diethelper.domain.model.Product
import com.k.shavrin.diethelper.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val productDao: ProductDao
) : ProductRepository {

    override fun getAllProducts(): Flow<List<Product>> =
        productDao.getAllProducts().map { entities -> entities.toDomain() }

    override fun searchProducts(query: String): Flow<List<Product>> =
        productDao.searchProducts(query).map { entities -> entities.toDomain() }

    override suspend fun getProductById(id: Long): Product? =
        productDao.getProductById(id)?.toDomain()

    override suspend fun addProduct(product: Product): Long =
        productDao.insertProduct(product.toEntity())

    override suspend fun updateProduct(product: Product) {
        productDao.updateProduct(product.toEntity())
    }

    override suspend fun deleteProduct(product: Product) {
        productDao.deleteProduct(product.toEntity())
    }

    override suspend fun toggleFavorite(productId: Long, isFavorite: Boolean) {
        productDao.setFavorite(productId, isFavorite)
    }
}
