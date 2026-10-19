package com.k.shavrin.diethelper.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val caloriesPer100g: Float,
    val proteinPer100g: Float,
    val fatPer100g: Float,
    val carbsPer100g: Float,
    val isFavorite: Boolean = false,
    @androidx.room.ColumnInfo(defaultValue = "''") val aliases: String = "",
    @androidx.room.ColumnInfo(defaultValue = "'custom'") val source: String = "custom",
    @androidx.room.ColumnInfo(defaultValue = "''") val sourceUrl: String = "",
    @androidx.room.ColumnInfo(defaultValue = "''") val barcode: String = "",
    @androidx.room.ColumnInfo(defaultValue = "''") val nutritionNote: String = "",
    @androidx.room.ColumnInfo(defaultValue = "''") val portionLabel: String = "",
    @androidx.room.ColumnInfo(defaultValue = "'г'") val unit: String = "г"
)
