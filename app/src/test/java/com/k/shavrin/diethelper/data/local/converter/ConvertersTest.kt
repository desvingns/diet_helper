package com.k.shavrin.diethelper.data.local.converter

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class ConvertersTest {

    private val converters = Converters()

    // ── toEpochDay ───────────────────────────────────────────────────────────

    @Test
    fun `toEpochDay null input returns null`() {
        assertNull(converters.toEpochDay(null))
    }

    @Test
    fun `toEpochDay epoch origin date returns 0`() {
        assertEquals(0L, converters.toEpochDay(LocalDate.ofEpochDay(0)))
    }

    @Test
    fun `toEpochDay known date returns correct epoch day`() {
        val date = LocalDate.of(2025, 1, 1)
        assertEquals(date.toEpochDay(), converters.toEpochDay(date))
    }

    // ── fromEpochDay ─────────────────────────────────────────────────────────

    @Test
    fun `fromEpochDay null input returns null`() {
        assertNull(converters.fromEpochDay(null))
    }

    @Test
    fun `fromEpochDay 0 returns epoch origin`() {
        assertEquals(LocalDate.ofEpochDay(0), converters.fromEpochDay(0L))
    }

    @Test
    fun `fromEpochDay known value returns correct date`() {
        val date = LocalDate.of(2025, 6, 15)
        assertEquals(date, converters.fromEpochDay(date.toEpochDay()))
    }

    // ── round-trip ───────────────────────────────────────────────────────────

    @Test
    fun `toEpochDay then fromEpochDay round-trips`() {
        val original = LocalDate.of(2024, 2, 29) // leap year
        val epochDay = converters.toEpochDay(original)
        val restored = converters.fromEpochDay(epochDay)
        assertEquals(original, restored)
    }

    @Test
    fun `fromEpochDay then toEpochDay round-trips`() {
        val epochDay = 19_000L
        val date = converters.fromEpochDay(epochDay)
        assertEquals(epochDay, converters.toEpochDay(date))
    }
}
