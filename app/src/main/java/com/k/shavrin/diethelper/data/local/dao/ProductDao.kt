package com.k.shavrin.diethelper.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.k.shavrin.diethelper.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Query("SELECT * FROM products ORDER BY isFavorite DESC, name COLLATE NOCASE ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query(
        """
        SELECT * FROM products
        WHERE name LIKE '%' || :query || '%' COLLATE NOCASE
        ORDER BY isFavorite DESC, name COLLATE NOCASE ASC
        """
    )
    fun searchProducts(query: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Long): ProductEntity?

    @Insert
    suspend fun insertProduct(product: ProductEntity): Long

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("UPDATE products SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: Long, isFavorite: Boolean)
}
