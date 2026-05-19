package com.k.shavrin.diethelper.presentation.screen.stats

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.k.shavrin.diethelper.domain.model.StatsDayItem
import com.k.shavrin.diethelper.presentation.theme.DietHelperTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class StatsScreenContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val from = LocalDate.of(2026, 5, 13)
    private val to = LocalDate.of(2026, 5, 19)

    private fun successState(
        items: List<StatsDayItem> = emptyList(),
        totalCalories: Float = 0f,
        totalProtein: Float = 0f,
        totalFat: Float = 0f,
        totalCarbs: Float = 0f
    ) = StatsUiState.Success(
        from = from,
        to = to,
        items = items,
        totalCalories = totalCalories,
        totalProtein = totalProtein,
        totalFat = totalFat,
        totalCarbs = totalCarbs
    )

    // ── Loading state ────────────────────────────────────────────────────────

    @Test
    fun `Loading state renders without crash`() {
        composeTestRule.setContent {
            DietHelperTheme {
                StatsContent(
                    state = StatsUiState.Loading,
                    onSetFrom = {},
                    onSetTo = {},
                    onPreset = {},
                    onCurrentMonth = {}
                )
            }
        }
    }

    // ── Error state ──────────────────────────────────────────────────────────

    @Test
    fun `Error state shows the error message text`() {
        composeTestRule.setContent {
            DietHelperTheme {
                StatsContent(
                    state = StatsUiState.Error("Не удалось загрузить статистику"),
                    onSetFrom = {},
                    onSetTo = {},
                    onPreset = {},
                    onCurrentMonth = {}
                )
            }
        }
        composeTestRule.onNodeWithText("Не удалось загрузить статистику").assertIsDisplayed()
    }

    // ── Success — empty items ────────────────────────────────────────────────

    @Test
    fun `Success with empty items shows the empty-period message`() {
        composeTestRule.setContent {
            DietHelperTheme {
                StatsContent(
                    state = successState(items = emptyList()),
                    onSetFrom = {},
                    onSetTo = {},
                    onPreset = {},
                    onCurrentMonth = {}
                )
            }
        }
        composeTestRule.onNodeWithText("Нет данных за выбранный период").assertIsDisplayed()
    }

    @Test
    fun `Success renders preset chips`() {
        composeTestRule.setContent {
            DietHelperTheme {
                StatsContent(
                    state = successState(),
                    onSetFrom = {},
                    onSetTo = {},
                    onPreset = {},
                    onCurrentMonth = {}
                )
            }
        }
        composeTestRule.onNodeWithText("7 дней").assertIsDisplayed()
        composeTestRule.onNodeWithText("14 дней").assertIsDisplayed()
        composeTestRule.onNodeWithText("30 дней").assertIsDisplayed()
        composeTestRule.onNodeWithText("Месяц").assertIsDisplayed()
    }

    @Test
    fun `Success renders chart legend with Russian labels`() {
        composeTestRule.setContent {
            DietHelperTheme {
                StatsContent(
                    state = successState(),
                    onSetFrom = {},
                    onSetTo = {},
                    onPreset = {},
                    onCurrentMonth = {}
                )
            }
        }
        // Chart legend renders below the chart canvas inside a verticalScroll —
        // assertExists is sufficient; assertIsDisplayed would require scrolling
        // the column into view first.
        composeTestRule.onNodeWithText("Белки").assertExists()
        composeTestRule.onNodeWithText("Жиры").assertExists()
        composeTestRule.onNodeWithText("Углеводы").assertExists()
        composeTestRule.onNodeWithText("Калории").assertExists()
    }

    // ── Success — with items ─────────────────────────────────────────────────

    @Test
    fun `Success with items renders totals section`() {
        val items = listOf(
            StatsDayItem(date = from, calories = 1800f, protein = 100f, fat = 60f, carbs = 220f),
            StatsDayItem(date = to, calories = 2100f, protein = 120f, fat = 70f, carbs = 260f)
        )
        composeTestRule.setContent {
            DietHelperTheme {
                StatsContent(
                    state = successState(
                        items = items,
                        totalCalories = 3900f,
                        totalProtein = 220f,
                        totalFat = 130f,
                        totalCarbs = 480f
                    ),
                    onSetFrom = {},
                    onSetTo = {},
                    onPreset = {},
                    onCurrentMonth = {}
                )
            }
        }
        composeTestRule.onNodeWithText("за период").assertIsDisplayed()
    }

    @Test
    fun `Success with items does not show empty-period message`() {
        val items = listOf(
            StatsDayItem(date = from, calories = 1800f, protein = 100f, fat = 60f, carbs = 220f)
        )
        composeTestRule.setContent {
            DietHelperTheme {
                StatsContent(
                    state = successState(items = items, totalCalories = 1800f),
                    onSetFrom = {},
                    onSetTo = {},
                    onPreset = {},
                    onCurrentMonth = {}
                )
            }
        }
        composeTestRule.onNodeWithText("Нет данных за выбранный период").assertDoesNotExist()
    }
}
