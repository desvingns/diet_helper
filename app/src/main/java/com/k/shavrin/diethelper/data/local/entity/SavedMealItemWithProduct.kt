package com.k.shavrin.diethelper.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class SavedMealItemWithProduct(
    @Embedded val item: SavedMealItemEntity,
    @Relation(
        parentColumn = "productId",
        entityColumn = "id"
    )
    val product: ProductEntity
)
