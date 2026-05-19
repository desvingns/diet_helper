package com.k.shavrin.diethelper.data.mapper

import com.k.shavrin.diethelper.data.local.entity.ProductEntity
import com.k.shavrin.diethelper.data.local.entity.SavedMealEntity
import com.k.shavrin.diethelper.data.local.entity.SavedMealItemEntity
import com.k.shavrin.diethelper.data.local.entity.SavedMealItemWithProduct
import com.k.shavrin.diethelper.data.local.entity.SavedMealWithItems
import org.junit.Assert.assertEquals
import org.junit.Test

class SavedMealMapperTest {

    private val productA = ProductEntity(
        id = 1L, name = "Гречка",
        caloriesPer100g = 343f, proteinPer100g = 13f,
        fatPer100g = 3.4f, carbsPer100g = 72f
    )
    private val productB = ProductEntity(
        id = 2L, name = "Куриное филе",
        caloriesPer100g = 165f, proteinPer100g = 31f,
        fatPer100g = 3.6f, carbsPer100g = 0f
    )

    private val mealEntity = SavedMealEntity(id = 10L, name = "Обед")

    private val itemA = SavedMealItemWithProduct(
        item = SavedMealItemEntity(id = 100L, savedMealId = 10L, productId = 1L, multiplier = 2.0f),
        product = productA
    )
    private val itemB = SavedMealItemWithProduct(
        item = SavedMealItemEntity(id = 101L, savedMealId = 10L, productId = 2L, multiplier = 1.5f),
        product = productB
    )

    private val withItems = SavedMealWithItems(meal = mealEntity, items = listOf(itemA, itemB))

    // ── toDomain ─────────────────────────────────────────────────────────────

    @Test
    fun `toDomain maps meal id and name`() {
        val result = withItems.toDomain()
        assertEquals(10L, result.id)
        assertEquals("Обед", result.name)
    }

    @Test
    fun `toDomain maps every item`() {
        val result = withItems.toDomain()
        assertEquals(2, result.items.size)
    }

    @Test
    fun `toDomain preserves item fields`() {
        val result = withItems.toDomain()
        val first = result.items[0]
        assertEquals(100L, first.id)
        assertEquals(10L, first.savedMealId)
        assertEquals(1L, first.productId)
        assertEquals(2.0f, first.multiplier, 0f)
    }

    @Test
    fun `toDomain attaches mapped product to each item`() {
        val result = withItems.toDomain()
        assertEquals("Гречка", result.items[0].product.name)
        assertEquals("Куриное филе", result.items[1].product.name)
    }

    @Test
    fun `toDomain on empty items returns empty list`() {
        val result = SavedMealWithItems(meal = mealEntity, items = emptyList()).toDomain()
        assertEquals(0, result.items.size)
    }

    // ── list extension ───────────────────────────────────────────────────────

    @Test
    fun `list toDomain maps every meal`() {
        val list = listOf(withItems, withItems.copy(meal = mealEntity.copy(id = 11L, name = "Ужин")))
        val result = list.toDomain()
        assertEquals(2, result.size)
        assertEquals("Обед", result[0].name)
        assertEquals("Ужин", result[1].name)
    }

    @Test
    fun `empty list toDomain returns empty list`() {
        val result = emptyList<SavedMealWithItems>().toDomain()
        assertEquals(0, result.size)
    }
}
