package com.k.shavrin.diethelper.di

import android.content.Context
import androidx.room.Room
import com.k.shavrin.diethelper.data.local.DietHelperDatabase
import com.k.shavrin.diethelper.data.local.dao.FoodEntryDao
import com.k.shavrin.diethelper.data.local.dao.ProductDao
import com.k.shavrin.diethelper.data.local.dao.SavedMealDao
import com.k.shavrin.diethelper.data.local.dao.WeightEntryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDietHelperDatabase(
        @ApplicationContext context: Context
    ): DietHelperDatabase =
        Room.databaseBuilder(
            context,
            DietHelperDatabase::class.java,
            DietHelperDatabase.NAME
        )
            .addMigrations(DietHelperDatabase.MIGRATION_1_2)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideProductDao(database: DietHelperDatabase): ProductDao =
        database.productDao()

    @Provides
    @Singleton
    fun provideFoodEntryDao(database: DietHelperDatabase): FoodEntryDao =
        database.foodEntryDao()

    @Provides
    @Singleton
    fun provideWeightEntryDao(database: DietHelperDatabase): WeightEntryDao =
        database.weightEntryDao()

    @Provides
    @Singleton
    fun provideSavedMealDao(database: DietHelperDatabase): SavedMealDao =
        database.savedMealDao()
}
