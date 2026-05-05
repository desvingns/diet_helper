package com.k.shavrin.diethelper.presentation.util

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MacroColorUtilTest {

    // ── macroProgressColor ──────────────────────────────────────────────────

    @Test
    fun `macroProgressColor bright red when actual is zero`() {
        assertEquals(MacroColorBrightRed, macroProgressColor(actual = 0f, min = 100f, max = 200f))
    }

    @Test
    fun `macroProgressColor bright red when actual is strictly below half of min`() {
        assertEquals(MacroColorBrightRed, macroProgressColor(actual = 49.9f, min = 100f, max = 200f))
    }

    @Test
    fun `macroProgressColor bright green when actual equals min`() {
        assertEquals(MacroColorBrightGreen, macroProgressColor(actual = 100f, min = 100f, max = 200f))
    }

    @Test
    fun `macroProgressColor bright green when actual is between min and max`() {
        assertEquals(MacroColorBrightGreen, macroProgressColor(actual = 150f, min = 100f, max = 200f))
    }

    @Test
    fun `macroProgressColor bright green when actual equals max`() {
        assertEquals(MacroColorBrightGreen, macroProgressColor(actual = 200f, min = 100f, max = 200f))
    }

    @Test
    fun `macroProgressColor bright red when actual is strictly above 125 percent of max`() {
        assertEquals(MacroColorBrightRed, macroProgressColor(actual = 250.1f, min = 100f, max = 200f))
    }

    @Test
    fun `macroProgressColor is interpolated in lower ramp zone`() {
        // midpoint of [50..100] → t = 0.5, result must differ from both extremes
        val color = macroProgressColor(actual = 75f, min = 100f, max = 200f)
        assertTrue(color != MacroColorBrightRed && color != MacroColorBrightGreen)
    }

    @Test
    fun `macroProgressColor is interpolated in upper ramp zone`() {
        // midpoint of [200..250] → t = 0.5
        val color = macroProgressColor(actual = 225f, min = 100f, max = 200f)
        assertTrue(color != MacroColorBrightRed && color != MacroColorBrightGreen)
    }

    @Test
    fun `macroProgressColor at half-of-min boundary is still red`() {
        // actual == lowerStart means we enter the lerp branch with t=0 → BrightRed
        val color = macroProgressColor(actual = 50f, min = 100f, max = 200f)
        assertEquals(MacroColorBrightRed, color)
    }

    @Test
    fun `macroProgressColor at exactly 125 percent of max is still green-to-red lerp end`() {
        // actual == upperEnd → last point of lerp → BrightRed
        val color = macroProgressColor(actual = 250f, min = 100f, max = 200f)
        assertEquals(MacroColorBrightRed, color)
    }

    // ── caloriesProgressColor ───────────────────────────────────────────────

    @Test
    fun `caloriesProgressColor bright green when actual is zero`() {
        assertEquals(MacroColorBrightGreen, caloriesProgressColor(actual = 0f, target = 2000f))
    }

    @Test
    fun `caloriesProgressColor bright green at exactly 80 percent of target`() {
        assertEquals(MacroColorBrightGreen, caloriesProgressColor(actual = 1600f, target = 2000f))
    }

    @Test
    fun `caloriesProgressColor interpolated between 80 and 100 percent`() {
        val color = caloriesProgressColor(actual = 1800f, target = 2000f)
        assertTrue(color != MacroColorBrightGreen && color != MacroColorBrightRed)
    }

    @Test
    fun `caloriesProgressColor interpolated between 100 and 120 percent`() {
        val color = caloriesProgressColor(actual = 2100f, target = 2000f)
        assertTrue(color != MacroColorBrightGreen && color != MacroColorBrightRed)
    }

    @Test
    fun `caloriesProgressColor bright red when actual is strictly above 120 percent`() {
        assertEquals(MacroColorBrightRed, caloriesProgressColor(actual = 2401f, target = 2000f))
    }

    @Test
    fun `caloriesProgressColor at exactly target transitions to light green`() {
        // actual == target → lerp(BrightGreen, LightGreen, 1.0) == LightGreen
        assertEquals(MacroColorLightGreen, caloriesProgressColor(actual = 2000f, target = 2000f))
    }

    @Test
    fun `caloriesProgressColor at exactly 120 percent is bright red`() {
        assertEquals(MacroColorBrightRed, caloriesProgressColor(actual = 2400f, target = 2000f))
    }
}
