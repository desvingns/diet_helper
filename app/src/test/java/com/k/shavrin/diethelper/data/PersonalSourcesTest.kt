package com.k.shavrin.diethelper.data

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import com.k.shavrin.diethelper.data.local.DatabaseSeeder
import com.k.shavrin.diethelper.data.local.DietHelperDatabase
import com.k.shavrin.diethelper.data.local.GoalsDataSource
import com.k.shavrin.diethelper.data.repository.OpenFoodFactsRepository
import com.k.shavrin.diethelper.data.repository.ProductRepositoryImpl
import com.k.shavrin.diethelper.data.repository.WorkoutRepositoryImpl
import com.k.shavrin.diethelper.domain.model.Product
import com.k.shavrin.diethelper.domain.model.matchesQuery
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.json.JSONArray
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class PersonalSourcesTest {
    private val context get() = ApplicationProvider.getApplicationContext<Application>()

    @Test
    fun `catalog upgrade is repeatable and does not overwrite existing food`() = runTest {
        val database = Room.inMemoryDatabaseBuilder(context, DietHelperDatabase::class.java).build()
        try {
            val repository = ProductRepositoryImpl(database.productDao())
            val custom = Product(name = "My own product", caloriesPer100g = 123f, proteinPer100g = 20f,
                fatPer100g = 2f, carbsPer100g = 5f, isFavorite = true)
            val id = repository.addProduct(custom)
            val seeder = DatabaseSeeder(repository, GoalsDataSource(FakePreferences()), context, database)
            seeder.seedIfNeeded()
            val first = repository.getAllProducts().first()
            seeder.seedIfNeeded()
            val second = repository.getAllProducts().first()
            assertEquals(first.size, second.size)
            assertEquals(132, second.count { it.source == "diary" })
            assertEquals(107, second.count { it.source == "openfoodfacts" })
            assertEquals(custom.copy(id = id), repository.getProductById(id))
        } finally {
            database.close()
        }
    }

    @Test
    fun `regional snapshot contains real Serbian listings and Lidl attribution`() {
        val items = JSONArray(context.assets.open("regional-products.json").bufferedReader().use { it.readText() })
        val products = List(items.length()) { OpenFoodFactsRepository.parseProduct(items.getJSONObject(it))!! }
        assertEquals(107, products.size)
        assertEquals(73, products.count { it.aliases.contains("Лидл") })
        assertTrue(products.all { it.aliases.contains("en:serbia") && it.barcode.isNotBlank() && it.sourceUrl.startsWith("https://world.openfoodfacts.org/product/") })
    }

    @Test
    fun `version two migration preserves existing product ids nutrition and favourites`() {
        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context).callback(object : SupportSQLiteOpenHelper.Callback(2) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.execSQL("CREATE TABLE products (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, " +
                        "caloriesPer100g REAL NOT NULL, proteinPer100g REAL NOT NULL, fatPer100g REAL NOT NULL, " +
                        "carbsPer100g REAL NOT NULL, isFavorite INTEGER NOT NULL)")
                    db.execSQL("INSERT INTO products VALUES (17, 'Legacy product', 110, 4.5, 1, 22, 1)")
                }
                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
            }).build()
        )
        try {
            val database = helper.writableDatabase
            DietHelperDatabase.MIGRATION_2_3.migrate(database)
            database.query("SELECT * FROM products WHERE id = 17").use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals("Legacy product", cursor.getString(cursor.getColumnIndexOrThrow("name")))
                assertEquals(110.0, cursor.getDouble(cursor.getColumnIndexOrThrow("caloriesPer100g")), 0.001)
                assertEquals(1, cursor.getInt(cursor.getColumnIndexOrThrow("isFavorite")))
                assertEquals("", cursor.getString(cursor.getColumnIndexOrThrow("barcode")))
                assertEquals("custom", cursor.getString(cursor.getColumnIndexOrThrow("source")))
                assertEquals("г", cursor.getString(cursor.getColumnIndexOrThrow("unit")))
            }
        } finally {
            helper.close()
        }
    }

    @Test
    fun `every imported workout weight matches the spreadsheet cached result`() = runTest {
        val repository = WorkoutRepositoryImpl(context, FakePreferences())
        val plan = repository.getPlan()
        assertEquals(57, plan.days.size)
        val checks = JSONArray(context.assets.open("workout-reference-checks.json").bufferedReader().use { it.readText() })
        assertEquals(508, checks.length())
        val exercises = plan.days.flatMap { it.exercises }.associateBy { it.id }
        val maxima = mapOf("squat" to 230.0, "bench" to 160.0, "deadlift" to 240.0)
        repeat(checks.length()) { i ->
            val check = checks.getJSONObject(i)
            val exercise = exercises.getValue(check.getInt("row"))
            val group = exercise.groups[check.getInt("group")]
            assertEquals("Spreadsheet row ${exercise.id}", check.getDouble("expectedWeight"), exercise.weight(maxima, group.percent), 0.001)
        }
    }

    @Test
    fun `all diary products preserve nutrition columns including clipped long labels`() {
        val items = JSONArray(context.assets.open("diary-products.json").bufferedReader().use { it.readText() })
        assertEquals(132, items.length())
        val products = List(items.length()) { items.getJSONObject(it) }.associateBy { it.getString("name") }
        val wraps = products.getValue("Grafschafter High Protein Wraps")
        assertEquals(293.333, wraps.getDouble("calories"), 0.01)
        assertEquals(18.0, wraps.getDouble("protein"), 0.01)
        assertEquals(37.0, wraps.getDouble("carbs"), 0.01)
        assertEquals(7.0, wraps.getDouble("fat"), 0.01)
        val smoked = products.getValue("Dimljena Pecenica")
        assertEquals(101.0, smoked.getDouble("calories"), 0.01)
        assertEquals(16.0, smoked.getDouble("protein"), 0.01)
        assertTrue(products.keys.any { it.startsWith("Говяжий Фарш (85%") })
        assertTrue(products.keys.any { it.startsWith("Мираторг Котлеты") })
        val soup = products.getValue("Суп-Пюре из Тыквы")
        assertEquals(44.5, soup.getDouble("calories"), 0.01)
        assertEquals("", soup.getString("portionLabel"))
        val egg = products.getValue("Яичный Белок")
        assertEquals("5 больших", egg.getString("portionLabel"))
        assertEquals(86.0, egg.getDouble("calories"), 0.01)
    }

    @Test
    fun `unicode search handles Russian case and Serbian scripts and accents`() {
        val product = Product(name = "Димлена печеница", caloriesPer100g = 101f, proteinPer100g = 16f,
            fatPer100g = 3.7f, carbsPer100g = 1f, aliases = "Dimljena Pečenica Lidl")
        assertTrue(product.matchesQuery("ДИМЛЕНА"))
        assertTrue(product.matchesQuery("pecenica"))
        assertTrue(product.matchesQuery("печеница"))
        assertTrue(product.matchesQuery("LIDL PEČENICA"))
        assertFalse(product.matchesQuery("молоко"))
    }

    @Test
    fun `portion multipliers never pretend that pieces are grams`() {
        val product = Product(name = "Яичный Белок", caloriesPer100g = 86f, proteinPer100g = 17.98f,
            fatPer100g = 0.28f, carbsPer100g = 1.2f, portionLabel = "5 больших")
        assertEquals(0.5f, product.multiplierForAmount(0.5f), 0.001f)
        assertEquals(43f, product.caloriesPer100g * product.multiplierForAmount(0.5f), 0.001f)
        assertEquals(0.5f, product.amountForMultiplier(0.5f), 0.001f)
    }

    @Test
    fun `persistent workout state round trips maxima set undo and rest pause`() = runTest {
        val preferences = FakePreferences()
        val repository = WorkoutRepositoryImpl(context, preferences)
        repository.saveMaxima(mapOf("squat" to 120.0, "bench" to 80.0, "deadlift" to 140.0))
        repository.setCompleted("0:4:0:0", true, 123_000)
        repository.setRest(90, 0, 30)
        repository.completeDay(0)
        val reopened = WorkoutRepositoryImpl(context, preferences)
        val state = reopened.progress.first()
        assertEquals(120.0, state.maxima["squat"]!!, 0.001)
        assertEquals(setOf("0:4:0:0"), state.completedSets)
        assertEquals(setOf(0), state.completedDays)
        assertEquals(90, state.restSeconds)
        assertEquals(0L, state.restDeadline)
        assertEquals(0, state.pausedRestSeconds)
    }
}

private class FakePreferences : DataStore<Preferences> {
    override val data = MutableStateFlow(emptyPreferences())
    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
        val next = transform(data.value)
        data.value = next
        return next
    }
}
