package com.k.shavrin.diethelper.presentation.screenshot

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.captureRoboImage
import com.k.shavrin.diethelper.domain.model.DailyGoals
import com.k.shavrin.diethelper.domain.model.DailySummary
import com.k.shavrin.diethelper.domain.model.DayStatus
import com.k.shavrin.diethelper.domain.model.FoodEntry
import com.k.shavrin.diethelper.domain.model.MealType
import com.k.shavrin.diethelper.domain.model.Product
import com.k.shavrin.diethelper.presentation.screen.today.TodayContent
import com.k.shavrin.diethelper.presentation.screen.today.TodayUiState
import com.k.shavrin.diethelper.presentation.theme.DietHelperTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.DayOfWeek
import java.time.LocalDate

@OptIn(ExperimentalRoborazziApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w411dp-h891dp-xxhdpi", application = android.app.Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TodayScreenScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val roborazziOptions = RoborazziOptions(
        recordOptions = RoborazziOptions.RecordOptions(resizeScale = 0.5)
    )

    @Test
    fun `TodayContent populated design reference`() {
        composeTestRule.setContent {
            DietHelperTheme(darkTheme = true) {
                TodayContent(
                    state = referenceState(),
                    onGoToDate = {},
                    onTodayClick = {},
                    onAddTo = {},
                    onUpdateMultiplier = { _, _ -> },
                    onDelete = {},
                    onCopyToDay = { _, _ -> },
                    readOnly = false
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(roborazziOptions = roborazziOptions)
    }

    private fun referenceState(): TodayUiState.Success {
        val date = LocalDate.of(2025, 5, 23)
        val entries = listOf(
            entry(1, "Овсянка на молоке", 250, 290f, 9.5f, 7.2f, 45f, MealType.BREAKFAST, date),
            entry(2, "Греческий йогурт 2%", 150, 99f, 14.5f, 3f, 5.4f, MealType.BREAKFAST, date),
            entry(3, "Кофе с молоком", 200, 60f, 3.1f, 3.3f, 4.6f, MealType.BREAKFAST, date),
            entry(4, "Куриная грудка гриль", 180, 297f, 56f, 6.5f, 0f, MealType.LUNCH, date),
            entry(5, "Гречка отварная", 150, 165f, 6f, 1.5f, 32f, MealType.LUNCH, date),
            entry(6, "Салат огурец/помидор", 200, 40f, 1.6f, 0.4f, 6.4f, MealType.LUNCH, date),
            entry(7, "Лосось запечённый", 150, 312f, 30f, 21f, 0f, MealType.DINNER, date),
            entry(8, "Брокколи на пару", 200, 70f, 5.6f, 0.6f, 13.4f, MealType.DINNER, date),
            entry(9, "Яблоко", 180, 86f, 0.7f, 0.4f, 21f, MealType.SNACK, date)
        )
        val sections = MealType.entries.associateWith { mealType -> entries.filter { it.mealType == mealType } }
        val weekStart = date.with(DayOfWeek.MONDAY)
        val statuses = listOf(
            DayStatus.GREEN,
            DayStatus.GREEN,
            DayStatus.YELLOW,
            DayStatus.GREEN,
            DayStatus.RED,
            DayStatus.GRAY_LOGGED,
            DayStatus.FUTURE
        )
        return TodayUiState.Success(
            date = date,
            sections = sections,
            sectionCalories = sections.mapValues { (_, values) -> values.sumOf { it.calories().toDouble() }.toFloat() },
            sectionProtein = sections.mapValues { (_, values) -> values.sumOf { it.protein().toDouble() }.toFloat() },
            sectionFat = sections.mapValues { (_, values) -> values.sumOf { it.fat().toDouble() }.toFloat() },
            sectionCarbs = sections.mapValues { (_, values) -> values.sumOf { it.carbs().toDouble() }.toFloat() },
            summary = DailySummary(
                totalCalories = entries.sumOf { it.calories().toDouble() }.toFloat(),
                totalProtein = entries.sumOf { it.protein().toDouble() }.toFloat(),
                totalFat = entries.sumOf { it.fat().toDouble() }.toFloat(),
                totalCarbs = entries.sumOf { it.carbs().toDouble() }.toFloat()
            ),
            goals = DailyGoals.DEFAULT,
            weekStatuses = statuses.mapIndexed { index, status -> weekStart.plusDays(index.toLong()) to status },
            streak = 7
        )
    }

    private fun entry(
        id: Long,
        name: String,
        grams: Int,
        calories: Float,
        protein: Float,
        fat: Float,
        carbs: Float,
        mealType: MealType,
        date: LocalDate
    ): FoodEntry {
        val multiplier = grams / 100f
        return FoodEntry(
            id = id,
            productId = id,
            product = Product(
                id = id,
                name = name,
                caloriesPer100g = calories / multiplier,
                proteinPer100g = protein / multiplier,
                fatPer100g = fat / multiplier,
                carbsPer100g = carbs / multiplier,
                isFavorite = false
            ),
            date = date,
            mealType = mealType,
            multiplier = multiplier
        )
    }

    private fun FoodEntry.calories(): Float = product.caloriesPer100g * multiplier
    private fun FoodEntry.protein(): Float = product.proteinPer100g * multiplier
    private fun FoodEntry.fat(): Float = product.fatPer100g * multiplier
    private fun FoodEntry.carbs(): Float = product.carbsPer100g * multiplier
}
