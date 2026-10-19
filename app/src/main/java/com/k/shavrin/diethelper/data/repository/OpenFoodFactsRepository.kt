package com.k.shavrin.diethelper.data.repository

import androidx.room.withTransaction
import com.k.shavrin.diethelper.data.local.DietHelperDatabase
import com.k.shavrin.diethelper.data.mapper.toDomain
import com.k.shavrin.diethelper.data.mapper.toEntity
import com.k.shavrin.diethelper.domain.model.FoodVocabulary
import com.k.shavrin.diethelper.domain.model.Product
import com.k.shavrin.diethelper.domain.repository.FoodCatalogRepository
import com.k.shavrin.diethelper.domain.repository.FoodMarket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OpenFoodFactsRepository @Inject constructor(private val database: DietHelperDatabase) : FoodCatalogRepository {
    private val cache = mutableMapOf<String, List<Product>>()

    override suspend fun search(query: String, market: FoodMarket, lidlOnly: Boolean): List<Product> = withContext(Dispatchers.IO) {
        val term = query.trim()
        require(term.length in 2..120)
        val key = "$term:${market.name}:$lidlOnly"
        synchronized(cache) { cache[key] }?.let { return@withContext it }
        val barcode = term.matches(Regex("[0-9]{8,14}"))
        val fields = "code,product_name,product_name_ru,product_name_sr,brands,nutriments,stores,countries_tags,quantity"
        val url = if (barcode) {
            "$HOST/api/v2/product/$term.json?fields=$fields"
        } else {
            val params = linkedMapOf("q" to textSearchQuery(term, market, lidlOnly), "langs" to "ru:sr:en:de:fr", "page_size" to "30", "fields" to fields)
            "https://search.openfoodfacts.org/search?" + params.entries.joinToString("&") { (k, v) -> "$k=${URLEncoder.encode(v, "UTF-8")}" }
        }
        val connection = URL(url).openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = 12_000
            connection.readTimeout = 20_000
            connection.setRequestProperty("User-Agent", "DietHelper/1.1 (Android; personal food diary)")
            connection.setRequestProperty("Accept", "application/json")
            val status = connection.responseCode
            if (status == 404 && barcode) return@withContext emptyList()
            if (status == 429) throw IOException("Каталог временно ограничил запросы. Подождите минуту.")
            if (status !in 200..299) throw IOException("Каталог недоступен (код $status). Попробуйте позже.")
            val json = JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
            ensureActive()
            val products = if (barcode) listOfNotNull(json.optJSONObject("product")?.let(::parseProduct))
                else json.optJSONArray("hits")?.let { array ->
                    List(array.length()) { i -> parseProduct(array.getJSONObject(i)) }.filterNotNull()
                }.orEmpty()
            synchronized(cache) {
                if (cache.size >= 40) cache.clear()
                cache[key] = products
            }
            products
        } finally {
            connection.disconnect()
        }
    }

    override suspend fun importProduct(product: Product): Product = database.withTransaction {
        val existing = database.productDao().getAllProducts().first().find {
            if (product.barcode.isNotBlank()) it.barcode == product.barcode else it.name.equals(product.name, true)
        }
        existing?.toDomain() ?: product.copy(id = database.productDao().insertProduct(product.copy(id = 0).toEntity()))
    }

    companion object {
        const val HOST = "https://world.openfoodfacts.org"

        internal fun textSearchQuery(term: String, market: FoodMarket, lidlOnly: Boolean): String {
            val searchTerm = if (market == FoodMarket.SERBIA) FoodVocabulary.serbianQuery(term) else term
            val words = searchTerm.replace(Regex("[^\\p{L}\\p{N}\\s'-]"), " ").split(Regex("\\s+"))
                .filter { it.isNotBlank() && it.uppercase() !in setOf("AND", "OR", "NOT") }
            require(words.isNotEmpty())
            // Quoted brand terms produce zero hits in Search-a-licious; use its full-text clause.
            val clauses = mutableListOf(words.joinToString(" "))
            if (market.tag.isNotEmpty()) clauses += "countries_tags:\"en:${market.tag}\""
            if (lidlOnly) clauses += "stores:\"lidl\""
            return clauses.joinToString(" AND ")
        }

        internal fun parseProduct(item: JSONObject): Product? {
            val code = item.optString("code")
            val nutrition = item.optJSONObject("nutriments") ?: return null
            val calories = when {
                nutrition.has("energy-kcal_100g") -> nutrition.optDouble("energy-kcal_100g")
                nutrition.has("energy-kj_100g") -> nutrition.optDouble("energy-kj_100g") / 4.184
                else -> Double.NaN
            }
            val protein = nutrition.optDouble("proteins_100g", Double.NaN)
            val fat = nutrition.optDouble("fat_100g", Double.NaN)
            val carbs = nutrition.optDouble("carbohydrates_100g", Double.NaN)
            if (!code.matches(Regex("[0-9]{8,14}")) || !calories.isFinite() || calories !in 0.0..950.0 ||
                listOf(protein, fat, carbs).any { !it.isFinite() || it !in 0.0..100.0 }) return null
            val names = listOf("product_name_ru", "product_name_sr", "product_name").map { item.optString(it).trim() }
            val name = names.firstOrNull { it.isNotBlank() } ?: return null
            val brand = textField(item, "brands")
            val stores = textField(item, "stores")
            val countries = item.optJSONArray("countries_tags")?.toString().orEmpty()
            return Product(
                name = if (brand.isNotBlank() && !name.contains(brand, true)) "$name · $brand" else name,
                caloriesPer100g = calories.toFloat(), proteinPer100g = protein.toFloat(), fatPer100g = fat.toFloat(), carbsPer100g = carbs.toFloat(),
                aliases = (names + brand + stores + countries + FoodVocabulary.russianAliases(name) +
                    if (stores.contains("lidl", true)) "Лидл Lidl" else "").joinToString(" ") +
                    if (countries.contains("en:serbia")) " Сербия Srbija" else "",
                source = "openfoodfacts", sourceUrl = "$HOST/product/$code", barcode = code,
                nutritionNote = "Open Food Facts · ODbL. Сверьте значения с этикеткой своей упаковки."
            )
        }

        private fun textField(item: JSONObject, key: String): String = item.optJSONArray(key)?.let { array ->
            List(array.length()) { array.optString(it) }.joinToString(", ")
        } ?: item.optString(key).trim()
    }
}
