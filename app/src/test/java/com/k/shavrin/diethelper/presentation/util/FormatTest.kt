package com.k.shavrin.diethelper.presentation.util

import com.k.shavrin.diethelper.domain.model.MealType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class FormatTest {

    // ── formatDate ───────────────────────────────────────────────────────────

    @Test
    fun `formatDate returns dd-MM-yyyy with dots`() {
        assertEquals("05.03.2025", formatDate(LocalDate.of(2025, 3, 5)))
    }

    @Test
    fun `formatDate pads single-digit day and month`() {
        assertEquals("01.01.2024", formatDate(LocalDate.of(2024, 1, 1)))
    }

    // ── formatIsoDate + parseIsoDate ─────────────────────────────────────────

    @Test
    fun `formatIsoDate returns yyyy-MM-dd`() {
        assertEquals("2025-03-05", formatIsoDate(LocalDate.of(2025, 3, 5)))
    }

    @Test
    fun `parseIsoDate round-trips with formatIsoDate`() {
        val date = LocalDate.of(2025, 11, 30)
        assertEquals(date, parseIsoDate(formatIsoDate(date)))
    }

    // ── formatGrams ──────────────────────────────────────────────────────────

    @Test
    fun `formatGrams multiplier 1f returns 100 г`() {
        assertEquals("100 г", formatGrams(1f))
    }

    @Test
    fun `formatGrams multiplier 0_5f returns 50 г`() {
        assertEquals("50 г", formatGrams(0.5f))
    }

    @Test
    fun `formatGrams multiplier 2_5f returns 250 г`() {
        assertEquals("250 г", formatGrams(2.5f))
    }

    @Test
    fun `formatGrams rounds to nearest integer gram`() {
        // 1.456 * 100 = 145.6 → rounds to 146
        assertEquals("146 г", formatGrams(1.456f))
    }

    // ── formatCalories ───────────────────────────────────────────────────────

    @Test
    fun `formatCalories rounds and appends unit`() {
        assertEquals("250 ккал", formatCalories(250f))
    }

    @Test
    fun `formatCalories rounds fractional value`() {
        assertEquals("251 ккал", formatCalories(250.6f))
    }

    @Test
    fun `formatCalories zero`() {
        assertEquals("0 ккал", formatCalories(0f))
    }

    // ── formatMacro ──────────────────────────────────────────────────────────

    @Test
    fun `formatMacro whole number omits decimal`() {
        assertEquals("25 г", formatMacro(25f))
    }

    @Test
    fun `formatMacro one decimal place retained`() {
        assertEquals("25.5 г", formatMacro(25.5f))
    }

    @Test
    fun `formatMacro rounds to one decimal`() {
        // (25.44 * 10).roundToInt() / 10f = 254 / 10f = 25.4
        assertEquals("25.4 г", formatMacro(25.44f))
    }

    @Test
    fun `formatMacro custom unit`() {
        assertEquals("10 мл", formatMacro(10f, "мл"))
    }

    // ── formatWeight ─────────────────────────────────────────────────────────

    @Test
    fun `formatWeight whole number omits decimal`() {
        assertEquals("80 кг", formatWeight(80f))
    }

    @Test
    fun `formatWeight one decimal retained`() {
        assertEquals("79.5 кг", formatWeight(79.5f))
    }

    @Test
    fun `formatWeight rounds to one decimal`() {
        // (79.44 * 10).roundToInt() / 10f = 794 / 10f = 79.4
        assertEquals("79.4 кг", formatWeight(79.44f))
    }

    // ── mealTypeLabel ────────────────────────────────────────────────────────

    @Test
    fun `mealTypeLabel breakfast is Завтрак`() {
        assertEquals("Завтрак", mealTypeLabel(MealType.BREAKFAST))
    }

    @Test
    fun `mealTypeLabel lunch is Обед`() {
        assertEquals("Обед", mealTypeLabel(MealType.LUNCH))
    }

    @Test
    fun `mealTypeLabel dinner is Ужин`() {
        assertEquals("Ужин", mealTypeLabel(MealType.DINNER))
    }

    @Test
    fun `mealTypeLabel snack is Перекус`() {
        assertEquals("Перекус", mealTypeLabel(MealType.SNACK))
    }

    @Test
    fun `all MealType values have labels`() {
        MealType.entries.forEach { type ->
            val label = mealTypeLabel(type)
            assert(label.isNotBlank()) { "Label for $type is blank" }
        }
    }
}
