package com.k.shavrin.diethelper.presentation.screenshot

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.captureRoboImage
import com.k.shavrin.diethelper.domain.model.DailyGoals
import com.k.shavrin.diethelper.domain.model.DailySummary
import com.k.shavrin.diethelper.presentation.components.DailySummaryCard
import com.k.shavrin.diethelper.presentation.theme.DietHelperTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@OptIn(ExperimentalRoborazziApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w411dp-h891dp-xxhdpi", application = android.app.Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class DailySummaryCardScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val roborazziOptions = RoborazziOptions(
        recordOptions = RoborazziOptions.RecordOptions(resizeScale = 0.5)
    )

    @Test
    fun `DailySummaryCard within goal`() {
        composeTestRule.setContent {
            DietHelperTheme(darkTheme = false) {
                DailySummaryCard(
                    summary = DailySummary(
                        totalCalories = 1500f,
                        totalProtein = 130f,
                        totalFat = 60f,
                        totalCarbs = 220f
                    ),
                    goals = DailyGoals.DEFAULT
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage(roborazziOptions = roborazziOptions)
    }

    @Test
    fun `DailySummaryCard over goal red state`() {
        composeTestRule.setContent {
            DietHelperTheme(darkTheme = false) {
                DailySummaryCard(
                    summary = DailySummary(
                        totalCalories = 2600f,
                        totalProtein = 200f,
                        totalFat = 95f,
                        totalCarbs = 310f
                    ),
                    goals = DailyGoals.DEFAULT
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage(roborazziOptions = roborazziOptions)
    }

    @Test
    fun `DailySummaryCard dark theme`() {
        composeTestRule.setContent {
            DietHelperTheme(darkTheme = true) {
                DailySummaryCard(
                    summary = DailySummary(
                        totalCalories = 1500f,
                        totalProtein = 130f,
                        totalFat = 60f,
                        totalCarbs = 220f
                    ),
                    goals = DailyGoals.DEFAULT
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage(roborazziOptions = roborazziOptions)
    }

    @Test
    fun `DailySummaryCard empty state`() {
        composeTestRule.setContent {
            DietHelperTheme(darkTheme = false) {
                DailySummaryCard(
                    summary = DailySummary.EMPTY,
                    goals = DailyGoals.DEFAULT
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage(roborazziOptions = roborazziOptions)
    }
}
