package com.k.shavrin.diethelper.data.pdf

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for pure-Kotlin helpers in PdfReportLayout.kt.
 *
 * Lives in the same package as the file under test so that `internal`
 * declarations are accessible without `@VisibleForTesting`.
 */
class PdfReportLayoutTest {

    // ── calorieDeviationRatio ─────────────────────────────────────────────

    @Test
    fun `calorieDeviationRatio returns zero when actual equals goal`() {
        assertEquals(0f, calorieDeviationRatio(actual = 2000f, goal = 2000f), 0.001f)
    }

    @Test
    fun `calorieDeviationRatio returns proportional ratio when actual is below goal`() {
        // |1800 - 2000| / 2000 = 0.1
        assertEquals(0.1f, calorieDeviationRatio(actual = 1800f, goal = 2000f), 0.001f)
    }

    @Test
    fun `calorieDeviationRatio returns proportional ratio when actual is above goal`() {
        // |2200 - 2000| / 2000 = 0.1
        assertEquals(0.1f, calorieDeviationRatio(actual = 2200f, goal = 2000f), 0.001f)
    }

    @Test
    fun `calorieDeviationRatio saturates at one when deviation is huge`() {
        // |6000 - 2000| / 2000 = 2.0 → clamped to 1.0
        assertEquals(1f, calorieDeviationRatio(actual = 6000f, goal = 2000f), 0.001f)
    }

    @Test
    fun `calorieDeviationRatio returns zero when actual is zero and goal positive`() {
        // |0 - 2000| / 2000 = 1.0 (max deviation)
        assertEquals(1f, calorieDeviationRatio(actual = 0f, goal = 2000f), 0.001f)
    }

    @Test
    fun `calorieDeviationRatio returns zero when goal is zero`() {
        assertEquals(0f, calorieDeviationRatio(actual = 2000f, goal = 0f), 0f)
    }

    @Test
    fun `calorieDeviationRatio returns zero when goal is negative`() {
        assertEquals(0f, calorieDeviationRatio(actual = 2000f, goal = -100f), 0f)
    }

    @Test
    fun `calorieDeviationRatio is symmetric across the goal`() {
        // |actual - goal| is symmetric — under and over by the same amount give the same ratio
        val lo = calorieDeviationRatio(actual = 1500f, goal = 2000f)
        val hi = calorieDeviationRatio(actual = 2500f, goal = 2000f)
        assertEquals(lo, hi, 0.001f)
    }

    @Test
    fun `calorieDeviationRatio result always within zero to one inclusive`() {
        val samples = listOf(
            0f to 2000f,
            500f to 2000f,
            2000f to 2000f,
            3500f to 2000f,
            100000f to 2000f
        )
        for ((actual, goal) in samples) {
            val r = calorieDeviationRatio(actual, goal)
            assertTrue("Ratio $r for actual=$actual goal=$goal should be >= 0", r >= 0f)
            assertTrue("Ratio $r for actual=$actual goal=$goal should be <= 1", r <= 1f)
        }
    }

    // ── PdfReportLayout constants: sanity ─────────────────────────────────

    @Test
    fun `A4 page dimensions in points are correct`() {
        // A4 in PDF points = 595 × 842
        assertEquals(595, PdfReportLayout.PAGE_WIDTH)
        assertEquals(842, PdfReportLayout.PAGE_HEIGHT)
    }

    @Test
    fun `meal labels are non-empty Russian strings`() {
        assertTrue(PdfReportLayout.MEAL_LABEL_BREAKFAST.isNotBlank())
        assertTrue(PdfReportLayout.MEAL_LABEL_LUNCH.isNotBlank())
        assertTrue(PdfReportLayout.MEAL_LABEL_DINNER.isNotBlank())
        assertTrue(PdfReportLayout.MEAL_LABEL_SNACK.isNotBlank())
        // sanity — they should differ
        val labels = setOf(
            PdfReportLayout.MEAL_LABEL_BREAKFAST,
            PdfReportLayout.MEAL_LABEL_LUNCH,
            PdfReportLayout.MEAL_LABEL_DINNER,
            PdfReportLayout.MEAL_LABEL_SNACK
        )
        assertEquals(4, labels.size)
    }
}
