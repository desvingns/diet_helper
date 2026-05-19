package com.k.shavrin.diethelper.data.mapper

import com.k.shavrin.diethelper.data.local.entity.WeightEntryEntity
import com.k.shavrin.diethelper.domain.model.WeightEntry
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class WeightEntryMapperTest {

    private val entity = WeightEntryEntity(
        id = 5L,
        date = LocalDate.of(2026, 5, 19),
        weightKg = 73.4f
    )

    private val domain = WeightEntry(
        id = 5L,
        date = LocalDate.of(2026, 5, 19),
        weightKg = 73.4f
    )

    // ── toDomain ─────────────────────────────────────────────────────────────

    @Test
    fun `toDomain maps all fields`() {
        val result = entity.toDomain()
        assertEquals(5L, result.id)
        assertEquals(LocalDate.of(2026, 5, 19), result.date)
        assertEquals(73.4f, result.weightKg, 0f)
    }

    // ── toEntity ─────────────────────────────────────────────────────────────

    @Test
    fun `toEntity maps all fields`() {
        val result = domain.toEntity()
        assertEquals(5L, result.id)
        assertEquals(LocalDate.of(2026, 5, 19), result.date)
        assertEquals(73.4f, result.weightKg, 0f)
    }

    // ── round-trip ───────────────────────────────────────────────────────────

    @Test
    fun `entity round-trips through domain`() {
        assertEquals(entity, entity.toDomain().toEntity())
    }

    @Test
    fun `domain round-trips through entity`() {
        assertEquals(domain, domain.toEntity().toDomain())
    }

    // ── list extension ───────────────────────────────────────────────────────

    @Test
    fun `list toDomain maps every element`() {
        val list = listOf(entity, entity.copy(id = 6L, weightKg = 74.0f))
        val result = list.toDomain()
        assertEquals(2, result.size)
        assertEquals(5L, result[0].id)
        assertEquals(74.0f, result[1].weightKg, 0f)
    }

    @Test
    fun `empty list toDomain returns empty list`() {
        val result = emptyList<WeightEntryEntity>().toDomain()
        assertEquals(0, result.size)
    }
}
