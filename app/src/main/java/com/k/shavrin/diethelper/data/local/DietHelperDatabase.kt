package com.k.shavrin.diethelper.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.k.shavrin.diethelper.data.local.converter.Converters
import com.k.shavrin.diethelper.data.local.dao.FoodEntryDao
import com.k.shavrin.diethelper.data.local.dao.ProductDao
import com.k.shavrin.diethelper.data.local.dao.WeightEntryDao
import com.k.shavrin.diethelper.data.local.entity.FoodEntryEntity
import com.k.shavrin.diethelper.data.local.entity.ProductEntity
import com.k.shavrin.diethelper.data.local.entity.WeightEntryEntity

@Database(
    entities = [
        ProductEntity::class,
        FoodEntryEntity::class,
        WeightEntryEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class DietHelperDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun foodEntryDao(): FoodEntryDao
    abstract fun weightEntryDao(): WeightEntryDao

    companion object {
        const val NAME = "diet_helper.db"
    }
}
