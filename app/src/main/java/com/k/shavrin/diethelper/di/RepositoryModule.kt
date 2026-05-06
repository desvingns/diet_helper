package com.k.shavrin.diethelper.di

import com.k.shavrin.diethelper.data.repository.FoodEntryRepositoryImpl
import com.k.shavrin.diethelper.data.repository.GoalsRepositoryImpl
import com.k.shavrin.diethelper.data.repository.ProductRepositoryImpl
import com.k.shavrin.diethelper.data.repository.SavedMealRepositoryImpl
import com.k.shavrin.diethelper.data.repository.WeightRepositoryImpl
import com.k.shavrin.diethelper.domain.repository.FoodEntryRepository
import com.k.shavrin.diethelper.domain.repository.GoalsRepository
import com.k.shavrin.diethelper.domain.repository.ProductRepository
import com.k.shavrin.diethelper.domain.repository.SavedMealRepository
import com.k.shavrin.diethelper.domain.repository.WeightRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindProductRepository(impl: ProductRepositoryImpl): ProductRepository

    @Binds
    @Singleton
    abstract fun bindFoodEntryRepository(impl: FoodEntryRepositoryImpl): FoodEntryRepository

    @Binds
    @Singleton
    abstract fun bindWeightRepository(impl: WeightRepositoryImpl): WeightRepository

    @Binds
    @Singleton
    abstract fun bindGoalsRepository(impl: GoalsRepositoryImpl): GoalsRepository

    @Binds
    @Singleton
    abstract fun bindSavedMealRepository(impl: SavedMealRepositoryImpl): SavedMealRepository
}
