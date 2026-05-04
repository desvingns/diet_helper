package com.k.shavrin.diethelper.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class FoodEntryWithProduct(
    @Embedded val entry: FoodEntryEntity,
    @Relation(
        parentColumn = "productId",
        entityColumn = "id"
    )
    val product: ProductEntity
)
