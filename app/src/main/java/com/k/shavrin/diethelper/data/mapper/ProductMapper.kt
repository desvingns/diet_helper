package com.k.shavrin.diethelper.data.mapper

import com.k.shavrin.diethelper.data.local.entity.ProductEntity
import com.k.shavrin.diethelper.domain.model.Product

fun ProductEntity.toDomain(): Product = Product(
    id = id,
    name = name,
    caloriesPer100g = caloriesPer100g,
    proteinPer100g = proteinPer100g,
    fatPer100g = fatPer100g,
    carbsPer100g = carbsPer100g,
    isFavorite = isFavorite,
    aliases = aliases,
    source = source,
    sourceUrl = sourceUrl,
    barcode = barcode,
    nutritionNote = nutritionNote,
    portionLabel = portionLabel,
    unit = unit
)

fun Product.toEntity(): ProductEntity = ProductEntity(
    id = id,
    name = name,
    caloriesPer100g = caloriesPer100g,
    proteinPer100g = proteinPer100g,
    fatPer100g = fatPer100g,
    carbsPer100g = carbsPer100g,
    isFavorite = isFavorite,
    aliases = aliases,
    source = source,
    sourceUrl = sourceUrl,
    barcode = barcode,
    nutritionNote = nutritionNote,
    portionLabel = portionLabel,
    unit = unit
)

fun List<ProductEntity>.toDomain(): List<Product> = map { it.toDomain() }
