package com.k.shavrin.diethelper.data.local

import android.content.Context
import androidx.room.withTransaction
import com.k.shavrin.diethelper.data.mapper.toEntity
import com.k.shavrin.diethelper.data.repository.OpenFoodFactsRepository
import com.k.shavrin.diethelper.domain.model.Product
import com.k.shavrin.diethelper.domain.repository.ProductRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseSeeder @Inject constructor(
    private val productRepository: ProductRepository,
    private val goalsDataSource: GoalsDataSource,
    @ApplicationContext private val context: Context,
    private val database: DietHelperDatabase
) {

    suspend fun seedIfNeeded() {
        if (goalsDataSource.catalogVersion() >= 3) return
        val rows = JSONArray(context.assets.open("diary-products.json").bufferedReader().use { it.readText() })
        val diary = List(rows.length()) { index ->
            val item = rows.getJSONObject(index)
            Product(
                name = item.getString("name"), caloriesPer100g = item.getDouble("calories").toFloat(),
                proteinPer100g = item.getDouble("protein").toFloat(), fatPer100g = item.getDouble("fat").toFloat(),
                carbsPer100g = item.getDouble("carbs").toFloat(), aliases = item.getString("aliases"), source = "diary",
                nutritionNote = item.getString("note"), portionLabel = item.getString("portionLabel"), unit = item.getString("unit")
            )
        }
        val regionalRows = JSONArray(context.assets.open("regional-products.json").bufferedReader().use { it.readText() })
        val regional = List(regionalRows.length()) { index ->
            OpenFoodFactsRepository.parseProduct(regionalRows.getJSONObject(index))
        }.filterNotNull()
        database.withTransaction {
            val products = productRepository.getAllProducts().first()
            val existing = products.map { it.name.lowercase() }.toMutableSet()
            val diaryNames = products.filter { it.source == "diary" }.map { it.name.lowercase() }.toMutableSet()
            diary.forEach { product ->
                if (diaryNames.add(product.name.lowercase())) database.productDao().insertProduct(product.toEntity())
                existing.add(product.name.lowercase())
            }
            DEFAULT_PRODUCTS.forEach { product ->
                if (existing.add(product.name.lowercase())) database.productDao().insertProduct(product.copy(source = "reference").toEntity())
            }
            val barcodes = products.map { it.barcode }.toMutableSet()
            regional.forEach { product ->
                if (barcodes.add(product.barcode)) database.productDao().insertProduct(product.toEntity())
            }
        }
        goalsDataSource.setSeeded()
        goalsDataSource.setCatalogVersion(3)
    }

    companion object {
        val DEFAULT_PRODUCTS: List<Product> = listOf(
            Product(name = "Гречка варёная", caloriesPer100g = 110f, proteinPer100g = 4.5f, fatPer100g = 1.0f, carbsPer100g = 22f),
            Product(name = "Куриная грудка варёная", caloriesPer100g = 165f, proteinPer100g = 31f, fatPer100g = 3.6f, carbsPer100g = 0f),
            Product(name = "Яйцо куриное", caloriesPer100g = 155f, proteinPer100g = 13f, fatPer100g = 11f, carbsPer100g = 1.1f),
            Product(name = "Творог 5%", caloriesPer100g = 121f, proteinPer100g = 17.2f, fatPer100g = 5f, carbsPer100g = 1.8f),
            Product(name = "Молоко 2.5%", caloriesPer100g = 52f, proteinPer100g = 2.9f, fatPer100g = 2.5f, carbsPer100g = 4.7f),
            Product(name = "Хлеб ржаной", caloriesPer100g = 165f, proteinPer100g = 6.6f, fatPer100g = 1.2f, carbsPer100g = 34.2f),
            Product(name = "Овсянка на воде", caloriesPer100g = 88f, proteinPer100g = 3f, fatPer100g = 1.7f, carbsPer100g = 15f),
            Product(name = "Банан", caloriesPer100g = 89f, proteinPer100g = 1.1f, fatPer100g = 0.3f, carbsPer100g = 22.8f),
            Product(name = "Яблоко", caloriesPer100g = 52f, proteinPer100g = 0.4f, fatPer100g = 0.4f, carbsPer100g = 13.8f),
            Product(name = "Помидор", caloriesPer100g = 18f, proteinPer100g = 0.9f, fatPer100g = 0.2f, carbsPer100g = 3.9f),
            Product(name = "Огурец", caloriesPer100g = 16f, proteinPer100g = 0.8f, fatPer100g = 0.1f, carbsPer100g = 3.6f),
            Product(name = "Рис варёный", caloriesPer100g = 130f, proteinPer100g = 2.7f, fatPer100g = 0.3f, carbsPer100g = 28f),
            Product(name = "Картофель варёный", caloriesPer100g = 86f, proteinPer100g = 2f, fatPer100g = 0.1f, carbsPer100g = 19f),
            Product(name = "Сыр российский", caloriesPer100g = 363f, proteinPer100g = 24.1f, fatPer100g = 29.5f, carbsPer100g = 0.3f),
            Product(name = "Лосось запечённый", caloriesPer100g = 208f, proteinPer100g = 22.1f, fatPer100g = 13.4f, carbsPer100g = 0f)
        )
    }
}
