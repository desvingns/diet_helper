package com.k.shavrin.diethelper.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class SavedMealWithItems(
    @Embedded val meal: SavedMealEntity,
    @Relation(
        entity = SavedMealItemEntity::class,
        parentColumn = "id",
        entityColumn = "savedMealId"
    )
    val items: List<SavedMealItemWithProduct>
)
