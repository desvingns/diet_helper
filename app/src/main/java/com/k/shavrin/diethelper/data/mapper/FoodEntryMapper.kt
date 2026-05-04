package com.k.shavrin.diethelper.data.mapper

import com.k.shavrin.diethelper.data.local.entity.FoodEntryEntity
import com.k.shavrin.diethelper.data.local.entity.FoodEntryWithProduct
import com.k.shavrin.diethelper.domain.model.FoodEntry
import com.k.shavrin.diethelper.domain.model.MealType

fun FoodEntryWithProduct.toDomain(): FoodEntry = FoodEntry(
    id = entry.id,
    productId = entry.productId,
    product = product.toDomain(),
    date = entry.date,
    mealType = enumValueOf<MealType>(entry.mealType),
    multiplier = entry.multiplier
)

fun FoodEntry.toEntity(): FoodEntryEntity = FoodEntryEntity(
    id = id,
    productId = productId,
    date = date,
    mealType = mealType.name,
    multiplier = multiplier
)

fun List<FoodEntryWithProduct>.toDomain(): List<FoodEntry> = map { it.toDomain() }
