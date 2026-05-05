package com.k.shavrin.diethelper.presentation.screen.today

import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.k.shavrin.diethelper.domain.model.DailyGoals
import com.k.shavrin.diethelper.domain.model.DailySummary
import com.k.shavrin.diethelper.domain.model.DayStatus
import com.k.shavrin.diethelper.domain.model.MealType
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
@Config(sdk = [34], application = android.app.Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TodayScreenContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val today = LocalDate.now()

    // Build a Monday-aligned week with 7 statuses
    private val weekStart: LocalDate = today.with(DayOfWeek.MONDAY)
    private val weekDays: List<LocalDate> = (0..6).map { weekStart.plusDays(it.toLong()) }

    private fun defaultWeekStatuses(
        override: Map<LocalDate, DayStatus> = emptyMap()
    ): List<Pair<LocalDate, DayStatus>> = weekDays.map { day ->
        day to (override[day] ?: if (day.isAfter(today)) DayStatus.FUTURE else DayStatus.GRAY_LOGGED)
    }

    private fun defaultState(
        date: LocalDate = today,
        weekStatuses: List<Pair<LocalDate, DayStatus>> = defaultWeekStatuses(),
        streak: Int = 0
    ) = TodayUiState.Success(
        date = date,
        sections = MealType.entries.associateWith { emptyList() },
        sectionCalories = MealType.entries.associateWith { 0f },
        sectionProtein = MealType.entries.associateWith { 0f },
        sectionFat = MealType.entries.associateWith { 0f },
        sectionCarbs = MealType.entries.associateWith { 0f },
        summary = DailySummary.EMPTY,
        goals = DailyGoals.DEFAULT,
        weekStatuses = weekStatuses,
        streak = streak
    )

    // ── WeekDateHeader: "Сегодня" button visibility ───────────────────────────

    @Test
    fun `shows Сегодня button when today is the selected date`() {
        composeTestRule.setContent {
            DietHelperTheme {
                WeekDateHeader(
                    date = today,
                    weekStatuses = defaultWeekStatuses(),
                    streak = 0,
                    onDateSelected = {},
                    onTodayClick = {}
                )
            }
        }
        composeTestRule.onNodeWithText("Сегодня").assertIsDisplayed()
    }

    @Test
    fun `shows Сегодня button when a past date is selected`() {
        val yesterday = today.minusDays(1)
        composeTestRule.setContent {
            DietHelperTheme {
                WeekDateHeader(
                    date = yesterday,
                    weekStatuses = defaultWeekStatuses(),
                    streak = 0,
                    onDateSelected = {},
                    onTodayClick = {}
                )
            }
        }
        // Button is always visible in WeekDateHeader regardless of selected date
        composeTestRule.onNodeWithText("Сегодня").assertIsDisplayed()
    }

    // ── WeekDateHeader: streak label ─────────────────────────────────────────

    @Test
    fun `shows zero streak prompt when streak is zero`() {
        composeTestRule.setContent {
            DietHelperTheme {
                WeekDateHeader(
                    date = today,
                    weekStatuses = defaultWeekStatuses(),
                    streak = 0,
                    onDateSelected = {},
                    onTodayClick = {}
                )
            }
        }
        composeTestRule.onNodeWithText("Запишите еду, чтобы начать серию", substring = true).assertIsDisplayed()
    }

    @Test
    fun `shows streak count when streak is greater than zero`() {
        composeTestRule.setContent {
            DietHelperTheme {
                WeekDateHeader(
                    date = today,
                    weekStatuses = defaultWeekStatuses(),
                    streak = 5,
                    onDateSelected = {},
                    onTodayClick = {}
                )
            }
        }
        composeTestRule.onNodeWithText("5 дней подряд", substring = true).assertIsDisplayed()
    }

    @Test
    fun `shows formatted date header`() {
        composeTestRule.setContent {
            DietHelperTheme {
                WeekDateHeader(
                    date = today,
                    weekStatuses = defaultWeekStatuses(),
                    streak = 0,
                    onDateSelected = {},
                    onTodayClick = {}
                )
            }
        }
        // The header contains the day-of-month number
        composeTestRule.onNodeWithText(today.dayOfMonth.toString(), substring = true).assertIsDisplayed()
    }

    // ── TodayContent: WeekDateHeader is shown in non-readOnly mode ───────────

    @Test
    fun `TodayContent shows week header when readOnly is false`() {
        composeTestRule.setContent {
            DietHelperTheme {
                TodayContent(
                    state = defaultState(),
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
        composeTestRule.onNodeWithText("Сегодня").assertIsDisplayed()
    }

    @Test
    fun `TodayContent hides week header when readOnly is true`() {
        composeTestRule.setContent {
            DietHelperTheme {
                TodayContent(
                    state = defaultState(),
                    onGoToDate = {},
                    onTodayClick = {},
                    onAddTo = {},
                    onUpdateMultiplier = { _, _ -> },
                    onDelete = {},
                    onCopyToDay = { _, _ -> },
                    readOnly = true
                )
            }
        }
        composeTestRule.onNodeWithText("Сегодня").assertDoesNotExist()
    }

    // ── TodayContent: section headers are visible ─────────────────────────────

    @Test
    fun `TodayContent shows all meal type section headers`() {
        composeTestRule.setContent {
            DietHelperTheme {
                TodayContent(
                    state = defaultState(),
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
        composeTestRule.onNodeWithText("Завтрак").assertIsDisplayed()
        composeTestRule.onNodeWithText("Обед").assertIsDisplayed()
        composeTestRule.onNodeWithText("Ужин").assertIsDisplayed()
        composeTestRule.onNodeWithText("Перекус").assertIsDisplayed()
    }

    @Test
    fun `TodayContent shows Пока пусто placeholder for empty sections`() {
        composeTestRule.setContent {
            DietHelperTheme {
                TodayContent(
                    state = defaultState(),
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
        // At least one "Пока пусто" placeholder should be visible since all sections are empty
        composeTestRule.onNodeWithText("Пока пусто").assertIsDisplayed()
    }

    // ── WeekDateHeader: week row contains 7 day circles (day-of-month labels) ─

    @Test
    fun `WeekDateHeader renders circle for each day in the week`() {
        composeTestRule.setContent {
            DietHelperTheme {
                WeekDateHeader(
                    date = today,
                    weekStatuses = defaultWeekStatuses(),
                    streak = 0,
                    onDateSelected = {},
                    onTodayClick = {}
                )
            }
        }
        // Each circle shows the abbreviated day-of-week letter; the week has exactly 7 days
        // The rendered letters are П В С Ч Р С В — check that at least Monday ("П") is present
        composeTestRule.onNodeWithText("П").assertIsDisplayed()
    }
}
