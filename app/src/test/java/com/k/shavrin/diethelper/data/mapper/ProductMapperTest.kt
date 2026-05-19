package com.k.shavrin.diethelper.data.mapper

import com.k.shavrin.diethelper.data.local.entity.ProductEntity
import com.k.shavrin.diethelper.domain.model.Product
import org.junit.Assert.assertEquals
import org.junit.Test

class ProductMapperTest {

    private val entity = ProductEntity(
        id = 42L,
        name = "Яблоко",
        caloriesPer100g = 52f,
        proteinPer100g = 0.3f,
        fatPer100g = 0.2f,
        carbsPer100g = 14f,
        isFavorite = true
    )

    private val domain = Product(
        id = 42L,
        name = "Яблоко",
        caloriesPer100g = 52f,
        proteinPer100g = 0.3f,
        fatPer100g = 0.2f,
        carbsPer100g = 14f,
        isFavorite = true
    )

    // ── toDomain ─────────────────────────────────────────────────────────────

    @Test
    fun `toDomain maps all fields`() {
        val result = entity.toDomain()
        assertEquals(entity.id, result.id)
        assertEquals(entity.name, result.name)
        assertEquals(entity.caloriesPer100g, result.caloriesPer100g, 0f)
        assertEquals(entity.proteinPer100g, result.proteinPer100g, 0f)
        assertEquals(entity.fatPer100g, result.fatPer100g, 0f)
        assertEquals(entity.carbsPer100g, result.carbsPer100g, 0f)
        assertEquals(entity.isFavorite, result.isFavorite)
    }

    @Test
    fun `toDomain preserves isFavorite false`() {
        val result = entity.copy(isFavorite = false).toDomain()
        assertEquals(false, result.isFavorite)
    }

    // ── toEntity ─────────────────────────────────────────────────────────────

    @Test
    fun `toEntity maps all fields`() {
        val result = domain.toEntity()
        assertEquals(domain.id, result.id)
        assertEquals(domain.name, result.name)
        assertEquals(domain.caloriesPer100g, result.caloriesPer100g, 0f)
        assertEquals(domain.proteinPer100g, result.proteinPer100g, 0f)
        assertEquals(domain.fatPer100g, result.fatPer100g, 0f)
        assertEquals(domain.carbsPer100g, result.carbsPer100g, 0f)
        assertEquals(domain.isFavorite, result.isFavorite)
    }

    // ── round-trip ───────────────────────────────────────────────────────────

    @Test
    fun `entity round-trips through domain`() {
        val result = entity.toDomain().toEntity()
        assertEquals(entity, result)
    }

    @Test
    fun `domain round-trips through entity`() {
        val result = domain.toEntity().toDomain()
        assertEquals(domain, result)
    }

    // ── list extension ───────────────────────────────────────────────────────

    @Test
    fun `list toDomain maps every element`() {
        val list = listOf(entity, entity.copy(id = 2L, name = "Банан"))
        val result = list.toDomain()
        assertEquals(2, result.size)
        assertEquals(42L, result[0].id)
        assertEquals("Банан", result[1].name)
    }

    @Test
    fun `empty list toDomain returns empty list`() {
        val result = emptyList<ProductEntity>().toDomain()
        assertEquals(0, result.size)
    }
}
