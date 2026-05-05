package com.k.shavrin.diethelper.presentation.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.k.shavrin.diethelper.domain.model.DailyGoals
import com.k.shavrin.diethelper.domain.model.DailySummary
import com.k.shavrin.diethelper.presentation.theme.DietHelperTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class DailySummaryCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val defaultGoals = DailyGoals.DEFAULT

    // ── title ────────────────────────────────────────────────────────────────

    @Test
    fun `shows итог за день title`() {
        composeTestRule.setContent {
            DietHelperTheme {
                DailySummaryCard(
                    summary = DailySummary.EMPTY,
                    goals = defaultGoals
                )
            }
        }
        composeTestRule.onNodeWithText("Итог за день").assertIsDisplayed()
    }

    // ── calories ─────────────────────────────────────────────────────────────

    @Test
    fun `shows formatted calories value`() {
        composeTestRule.setContent {
            DietHelperTheme {
                DailySummaryCard(
                    summary = DailySummary(totalCalories = 1500f, totalProtein = 0f, totalFat = 0f, totalCarbs = 0f),
                    goals = defaultGoals
                )
            }
        }
        composeTestRule.onNodeWithText("1500 ккал", substring = true).assertIsDisplayed()
    }

    @Test
    fun `shows zero calories without crash`() {
        composeTestRule.setContent {
            DietHelperTheme {
                DailySummaryCard(
                    summary = DailySummary.EMPTY,
                    goals = defaultGoals
                )
            }
        }
        composeTestRule.onNodeWithText("0 ккал", substring = true).assertIsDisplayed()
    }

    // ── macro labels ─────────────────────────────────────────────────────────

    @Test
    fun `shows macro nutrient labels`() {
        composeTestRule.setContent {
            DietHelperTheme {
                DailySummaryCard(
                    summary = DailySummary.EMPTY,
                    goals = defaultGoals
                )
            }
        }
        composeTestRule.onNodeWithText("Калории").assertIsDisplayed()
        composeTestRule.onNodeWithText("Белки").assertIsDisplayed()
        composeTestRule.onNodeWithText("Жиры").assertIsDisplayed()
        composeTestRule.onNodeWithText("Углеводы").assertIsDisplayed()
    }

    // ── full data ─────────────────────────────────────────────────────────────

    @Test
    fun `shows all formatted macro values within goal`() {
        val summary = DailySummary(
            totalCalories = 1500f,
            totalProtein = 130f,
            totalFat = 60f,
            totalCarbs = 220f
        )
        composeTestRule.setContent {
            DietHelperTheme {
                DailySummaryCard(summary = summary, goals = defaultGoals)
            }
        }
        composeTestRule.onNodeWithText("130 г", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("60 г", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("220 г", substring = true).assertIsDisplayed()
    }

    @Test
    fun `does not crash when calories exceed goal`() {
        composeTestRule.setContent {
            DietHelperTheme {
                DailySummaryCard(
                    summary = DailySummary(totalCalories = 3000f, totalProtein = 0f, totalFat = 0f, totalCarbs = 0f),
                    goals = defaultGoals
                )
            }
        }
        composeTestRule.onNodeWithText("Итог за день").assertIsDisplayed()
    }
}
