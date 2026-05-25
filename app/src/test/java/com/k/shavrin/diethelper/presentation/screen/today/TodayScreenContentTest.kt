package com.k.shavrin.diethelper.presentation.screen.today

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToKey
import com.k.shavrin.diethelper.domain.model.DailyGoals
import com.k.shavrin.diethelper.domain.model.DailySummary
import com.k.shavrin.diethelper.domain.model.DayStatus
import com.k.shavrin.diethelper.domain.model.FoodEntry
import com.k.shavrin.diethelper.domain.model.MealType
import com.k.shavrin.diethelper.domain.model.Product
import com.k.shavrin.diethelper.presentation.theme.DietHelperTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.DayOfWeek
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w411dp-h891dp-xxhdpi", application = android.app.Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TodayScreenContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val selectedDate: LocalDate = LocalDate.of(2025, 5, 23)
    private val weekStart: LocalDate = selectedDate.with(DayOfWeek.MONDAY)

    private fun defaultWeekStatuses(): List<Pair<LocalDate, DayStatus>> {
        val statuses = listOf(
            DayStatus.GREEN,
            DayStatus.GREEN,
            DayStatus.YELLOW,
            DayStatus.GREEN,
            DayStatus.RED,
            DayStatus.GRAY_LOGGED,
            DayStatus.FUTURE
        )
        return statuses.mapIndexed { index, status -> weekStart.plusDays(index.toLong()) to status }
    }

    private fun defaultState(
        sections: Map<MealType, List<FoodEntry>> = MealType.entries.associateWith { emptyList() },
        streak: Int = 0
    ) = TodayUiState.Success(
        date = selectedDate,
        sections = sections,
        sectionCalories = sections.mapValues { (_, entries) ->
            entries.sumOf { (it.product.caloriesPer100g * it.multiplier).toDouble() }.toFloat()
        },
        sectionProtein = sections.mapValues { (_, entries) ->
            entries.sumOf { (it.product.proteinPer100g * it.multiplier).toDouble() }.toFloat()
        },
        sectionFat = sections.mapValues { (_, entries) ->
            entries.sumOf { (it.product.fatPer100g * it.multiplier).toDouble() }.toFloat()
        },
        sectionCarbs = sections.mapValues { (_, entries) ->
            entries.sumOf { (it.product.carbsPer100g * it.multiplier).toDouble() }.toFloat()
        },
        summary = DailySummary.EMPTY,
        goals = DailyGoals.DEFAULT,
        weekStatuses = defaultWeekStatuses(),
        streak = streak
    )

    @Test
    fun `TodayContent shows designed header and calorie hero`() {
        render(defaultState())

        composeTestRule.onNodeWithText("Сегодня").assertIsDisplayed()
        composeTestRule.onNodeWithText("Пятница, 23 мая").assertIsDisplayed()
        composeTestRule.onNodeWithText("ИЗ 2 000 ККАЛ").assertIsDisplayed()
        composeTestRule.onNodeWithTag("today_feed").performScrollToKey("meal_label")
        composeTestRule.onNodeWithText("ПРИЁМЫ ПИЩИ").assertIsDisplayed()
    }

    @Test
    fun `TodayContent shows streak chip in hero`() {
        render(defaultState(streak = 7))

        composeTestRule.onNodeWithText("7 дней подряд").assertIsDisplayed()
    }

    @Test
    fun `TodayContent shows each empty meal card`() {
        render(defaultState())

        listOf("BREAKFAST", "LUNCH", "DINNER", "SNACK").forEach { meal ->
            composeTestRule.onNodeWithTag("today_feed").performScrollToKey("header_$meal")
        }
        composeTestRule.onAllNodesWithText("Пока пусто").assertCountEquals(4)
        composeTestRule.onAllNodesWithText("Пока пусто")[3].assertIsDisplayed()
    }

    @Test
    fun `TodayContent renders food inside populated card`() {
        val product = Product(
            id = 1,
            name = "Овсянка на молоке",
            caloriesPer100g = 116f,
            proteinPer100g = 3.8f,
            fatPer100g = 2.9f,
            carbsPer100g = 18f,
            isFavorite = false
        )
        val entry = FoodEntry(
            id = 1,
            productId = product.id,
            product = product,
            date = selectedDate,
            mealType = MealType.BREAKFAST,
            multiplier = 2.5f
        )
        val sections = MealType.entries.associateWith { mealType ->
            if (mealType == MealType.BREAKFAST) listOf(entry) else emptyList()
        }

        render(defaultState(sections = sections))

        composeTestRule.onNodeWithTag("today_feed").performScrollToKey("header_BREAKFAST")
        composeTestRule.onNodeWithText("Овсянка на молоке").assertIsDisplayed()
        composeTestRule.onNodeWithText("290 ккал").assertIsDisplayed()
    }

    @Test
    fun `TodayContent renders food with zero carbs without crashing`() {
        val product = Product(
            id = 1,
            name = "Chicken breast",
            caloriesPer100g = 165f,
            proteinPer100g = 31f,
            fatPer100g = 3.6f,
            carbsPer100g = 0f,
            isFavorite = false
        )
        val entry = FoodEntry(
            id = 1,
            productId = product.id,
            product = product,
            date = selectedDate,
            mealType = MealType.LUNCH,
            multiplier = 1f
        )
        val sections = MealType.entries.associateWith { mealType ->
            if (mealType == MealType.LUNCH) listOf(entry) else emptyList()
        }

        render(defaultState(sections = sections))

        composeTestRule.onNodeWithTag("today_feed").performScrollToKey("header_LUNCH")
        composeTestRule.onNodeWithText("Chicken breast").assertIsDisplayed()
    }

    @Test
    fun `read only day presentation does not receive Today redesign header`() {
        render(defaultState(), readOnly = true)

        composeTestRule.onNodeWithText("Сегодня").assertDoesNotExist()
        composeTestRule.onNodeWithTag("today_feed").performScrollToKey("summary")
        composeTestRule.onNodeWithText("Итог за день").assertIsDisplayed()
    }

    private fun render(state: TodayUiState.Success, readOnly: Boolean = false) {
        composeTestRule.setContent {
            DietHelperTheme(darkTheme = true) {
                TodayContent(
                    state = state,
                    onGoToDate = {},
                    onTodayClick = {},
                    onAddTo = {},
                    onUpdateMultiplier = { _, _ -> },
                    onDelete = {},
                    onCopyToDay = { _, _ -> },
                    readOnly = readOnly
                )
            }
        }
    }
}
