package com.k.shavrin.diethelper.data.mapper

import com.k.shavrin.diethelper.data.local.entity.SavedMealWithItems
import com.k.shavrin.diethelper.domain.model.SavedMeal
import com.k.shavrin.diethelper.domain.model.SavedMealItem

fun SavedMealWithItems.toDomain(): SavedMeal = SavedMeal(
    id = meal.id,
    name = meal.name,
    items = items.map { withProduct ->
        SavedMealItem(
            id = withProduct.item.id,
            savedMealId = withProduct.item.savedMealId,
            productId = withProduct.item.productId,
            product = withProduct.product.toDomain(),
            multiplier = withProduct.item.multiplier
        )
    }
)

fun List<SavedMealWithItems>.toDomain(): List<SavedMeal> = map { it.toDomain() }
