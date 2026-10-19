package com.k.shavrin.diethelper.domain.model

data class Product(
    val id: Long = 0,
    val name: String,
    val caloriesPer100g: Float,
    val proteinPer100g: Float,
    val fatPer100g: Float,
    val carbsPer100g: Float,
    val isFavorite: Boolean = false,
    val aliases: String = "",
    val source: String = "custom",
    val sourceUrl: String = "",
    val barcode: String = "",
    val nutritionNote: String = "",
    val portionLabel: String = "",
    val unit: String = "г"
) {
    val referenceLabel: String get() = if (portionLabel.isBlank()) "100 $unit" else "порция: $portionLabel"
    fun multiplierForAmount(amount: Float): Float = if (portionLabel.isBlank()) amount / 100f else amount
    fun amountForMultiplier(multiplier: Float): Float = if (portionLabel.isBlank()) multiplier * 100f else multiplier
}
