package com.k.shavrin.diethelper.presentation.util

import com.k.shavrin.diethelper.domain.model.FoodEntry
import com.k.shavrin.diethelper.domain.model.MealType
import com.k.shavrin.diethelper.domain.model.Product
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class InMemoryMealClipboardTest {

    private val clipboard = InMemoryMealClipboard()

    private val product = Product(
        id = 1,
        name = "Гречка",
        caloriesPer100g = 100f,
        proteinPer100g = 4f,
        fatPer100g = 1f,
        carbsPer100g = 20f
    )

    private fun makeEntry(mealType: MealType = MealType.BREAKFAST) = FoodEntry(
        id = 1,
        productId = 1,
        product = product,
        date = LocalDate.of(2025, 5, 1),
        mealType = mealType,
        multiplier = 1f
    )

    @Test
    fun `initial state is null`() {
        assertNull(clipboard.state.value)
    }

    @Test
    fun `copy stores snapshot in state`() {
        val snapshot = ClipboardSnapshot(
            entries = listOf(makeEntry()),
            sourceMealType = MealType.BREAKFAST,
            sourceDate = LocalDate.of(2025, 5, 1)
        )

        clipboard.copy(snapshot)

        assertEquals(snapshot, clipboard.state.value)
    }

    @Test
    fun `copy overwrites previous snapshot`() {
        val first = ClipboardSnapshot(
            entries = listOf(makeEntry(MealType.BREAKFAST)),
            sourceMealType = MealType.BREAKFAST,
            sourceDate = LocalDate.of(2025, 5, 1)
        )
        val second = ClipboardSnapshot(
            entries = listOf(makeEntry(MealType.LUNCH)),
            sourceMealType = MealType.LUNCH,
            sourceDate = LocalDate.of(2025, 5, 2)
        )

        clipboard.copy(first)
        clipboard.copy(second)

        assertEquals(second, clipboard.state.value)
        assertEquals(MealType.LUNCH, clipboard.state.value?.sourceMealType)
    }

    @Test
    fun `clear sets state to null`() {
        val snapshot = ClipboardSnapshot(
            entries = listOf(makeEntry()),
            sourceMealType = MealType.DINNER,
            sourceDate = LocalDate.of(2025, 5, 1)
        )
        clipboard.copy(snapshot)

        clipboard.clear()

        assertNull(clipboard.state.value)
    }

    @Test
    fun `clear on empty clipboard stays null`() {
        clipboard.clear()

        assertNull(clipboard.state.value)
    }

    @Test
    fun `state snapshot contains all copied entries`() {
        val entries = listOf(
            makeEntry(MealType.BREAKFAST),
            makeEntry(MealType.BREAKFAST).copy(id = 2, multiplier = 2f)
        )
        val snapshot = ClipboardSnapshot(
            entries = entries,
            sourceMealType = MealType.BREAKFAST,
            sourceDate = LocalDate.of(2025, 5, 1)
        )

        clipboard.copy(snapshot)

        assertEquals(2, clipboard.state.value?.entries?.size)
    }
}
