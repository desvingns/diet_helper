package com.k.shavrin.diethelper.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.k.shavrin.diethelper.data.local.converter.Converters
import com.k.shavrin.diethelper.data.local.dao.FoodEntryDao
import com.k.shavrin.diethelper.data.local.dao.ProductDao
import com.k.shavrin.diethelper.data.local.dao.SavedMealDao
import com.k.shavrin.diethelper.data.local.dao.WeightEntryDao
import com.k.shavrin.diethelper.data.local.entity.FoodEntryEntity
import com.k.shavrin.diethelper.data.local.entity.ProductEntity
import com.k.shavrin.diethelper.data.local.entity.SavedMealEntity
import com.k.shavrin.diethelper.data.local.entity.SavedMealItemEntity
import com.k.shavrin.diethelper.data.local.entity.WeightEntryEntity

@Database(
    entities = [
        ProductEntity::class,
        FoodEntryEntity::class,
        WeightEntryEntity::class,
        SavedMealEntity::class,
        SavedMealItemEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class DietHelperDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun foodEntryDao(): FoodEntryDao
    abstract fun weightEntryDao(): WeightEntryDao
    abstract fun savedMealDao(): SavedMealDao

    companion object {
        const val NAME = "diet_helper.db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS saved_meals " +
                        "(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL)"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS saved_meal_items " +
                        "(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "savedMealId INTEGER NOT NULL, " +
                        "productId INTEGER NOT NULL, " +
                        "multiplier REAL NOT NULL, " +
                        "FOREIGN KEY(savedMealId) REFERENCES saved_meals(id) ON DELETE CASCADE, " +
                        "FOREIGN KEY(productId) REFERENCES products(id) ON DELETE CASCADE)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_saved_meal_items_savedMealId " +
                        "ON saved_meal_items(savedMealId)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_saved_meal_items_productId " +
                        "ON saved_meal_items(productId)"
                )
            }
        }
    }
}
