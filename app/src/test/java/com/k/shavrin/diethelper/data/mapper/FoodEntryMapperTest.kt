package com.k.shavrin.diethelper.data.mapper

import com.k.shavrin.diethelper.data.local.entity.FoodEntryEntity
import com.k.shavrin.diethelper.data.local.entity.FoodEntryWithProduct
import com.k.shavrin.diethelper.data.local.entity.ProductEntity
import com.k.shavrin.diethelper.domain.model.FoodEntry
import com.k.shavrin.diethelper.domain.model.MealType
import com.k.shavrin.diethelper.domain.model.Product
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class FoodEntryMapperTest {

    private val productEntity = ProductEntity(
        id = 7L,
        name = "Гречка",
        caloriesPer100g = 343f,
        proteinPer100g = 13f,
        fatPer100g = 3.4f,
        carbsPer100g = 72f,
        isFavorite = false
    )

    private val product = Product(
        id = 7L,
        name = "Гречка",
        caloriesPer100g = 343f,
        proteinPer100g = 13f,
        fatPer100g = 3.4f,
        carbsPer100g = 72f,
        isFavorite = false
    )

    private val entryEntity = FoodEntryEntity(
        id = 99L,
        productId = 7L,
        date = LocalDate.of(2026, 5, 19),
        mealType = "BREAKFAST",
        multiplier = 1.5f
    )

    private val withProduct = FoodEntryWithProduct(
        entry = entryEntity,
        product = productEntity
    )

    private val domain = FoodEntry(
        id = 99L,
        productId = 7L,
        product = product,
        date = LocalDate.of(2026, 5, 19),
        mealType = MealType.BREAKFAST,
        multiplier = 1.5f
    )

    // ── toDomain (FoodEntryWithProduct → FoodEntry) ──────────────────────────

    @Test
    fun `toDomain maps all entry fields`() {
        val result = withProduct.toDomain()
        assertEquals(99L, result.id)
        assertEquals(7L, result.productId)
        assertEquals(LocalDate.of(2026, 5, 19), result.date)
        assertEquals(MealType.BREAKFAST, result.mealType)
        assertEquals(1.5f, result.multiplier, 0f)
    }

    @Test
    fun `toDomain attaches mapped product`() {
        val result = withProduct.toDomain()
        assertEquals(product, result.product)
    }

    @Test
    fun `toDomain parses every MealType enum value`() {
        for (mealType in MealType.entries) {
            val custom = withProduct.copy(entry = entryEntity.copy(mealType = mealType.name))
            assertEquals(mealType, custom.toDomain().mealType)
        }
    }

    // ── toEntity (FoodEntry → FoodEntryEntity, drops embedded product) ──────

    @Test
    fun `toEntity maps id productId date and multiplier`() {
        val result = domain.toEntity()
        assertEquals(99L, result.id)
        assertEquals(7L, result.productId)
        assertEquals(LocalDate.of(2026, 5, 19), result.date)
        assertEquals(1.5f, result.multiplier, 0f)
    }

    @Test
    fun `toEntity serialises MealType as enum name`() {
        val result = domain.copy(mealType = MealType.DINNER).toEntity()
        assertEquals("DINNER", result.mealType)
    }

    // ── round-trip through embedded shape ────────────────────────────────────

    @Test
    fun `round-trip via FoodEntryWithProduct preserves observable fields`() {
        val toEntity = domain.toEntity()
        val rebuilt = FoodEntryWithProduct(
            entry = toEntity,
            product = productEntity
        ).toDomain()
        assertEquals(domain, rebuilt)
    }

    // ── list extension ───────────────────────────────────────────────────────

    @Test
    fun `list toDomain maps every element`() {
        val list = listOf(withProduct, withProduct.copy(entry = entryEntity.copy(id = 100L)))
        val result = list.toDomain()
        assertEquals(2, result.size)
        assertEquals(99L, result[0].id)
        assertEquals(100L, result[1].id)
    }

    @Test
    fun `empty list toDomain returns empty list`() {
        val result = emptyList<FoodEntryWithProduct>().toDomain()
        assertEquals(0, result.size)
    }
}
