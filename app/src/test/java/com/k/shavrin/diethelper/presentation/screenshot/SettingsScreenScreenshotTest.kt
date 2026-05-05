package com.k.shavrin.diethelper.presentation.screenshot

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.captureRoboImage
import com.k.shavrin.diethelper.presentation.screen.settings.SettingsContent
import com.k.shavrin.diethelper.presentation.screen.settings.SettingsUiState
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
class SettingsScreenScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val roborazziOptions = RoborazziOptions(
        recordOptions = RoborazziOptions.RecordOptions(resizeScale = 0.5)
    )

    private val filledState = SettingsUiState(
        calories = "2000",
        proteinMin = "120",
        proteinMax = "180",
        fatMin = "55",
        fatMax = "80",
        carbsMin = "200",
        carbsMax = "280",
        isLoading = false
    )

    @Test
    fun `SettingsContent filled fields light theme`() {
        composeTestRule.setContent {
            DietHelperTheme(darkTheme = false) {
                SettingsContent(
                    state = filledState,
                    onCaloriesChange = {},
                    onProteinMinChange = {},
                    onProteinMaxChange = {},
                    onFatMinChange = {},
                    onFatMaxChange = {},
                    onCarbsMinChange = {},
                    onCarbsMaxChange = {},
                    onSave = {}
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage(roborazziOptions = roborazziOptions)
    }

    @Test
    fun `SettingsContent with validation errors`() {
        val stateWithErrors = filledState.copy(
            calories = "abc",
            caloriesError = "Некорректное значение",
            proteinMinError = "Мин > Макс"
        )
        composeTestRule.setContent {
            DietHelperTheme(darkTheme = false) {
                SettingsContent(
                    state = stateWithErrors,
                    onCaloriesChange = {},
                    onProteinMinChange = {},
                    onProteinMaxChange = {},
                    onFatMinChange = {},
                    onFatMaxChange = {},
                    onCarbsMinChange = {},
                    onCarbsMaxChange = {},
                    onSave = {}
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage(roborazziOptions = roborazziOptions)
    }

    @Test
    fun `SettingsContent after successful save`() {
        val savedState = filledState.copy(justSaved = true)
        composeTestRule.setContent {
            DietHelperTheme(darkTheme = false) {
                SettingsContent(
                    state = savedState,
                    onCaloriesChange = {},
                    onProteinMinChange = {},
                    onProteinMaxChange = {},
                    onFatMinChange = {},
                    onFatMaxChange = {},
                    onCarbsMinChange = {},
                    onCarbsMaxChange = {},
                    onSave = {}
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage(roborazziOptions = roborazziOptions)
    }

    @Test
    fun `SettingsContent dark theme`() {
        composeTestRule.setContent {
            DietHelperTheme(darkTheme = true) {
                SettingsContent(
                    state = filledState,
                    onCaloriesChange = {},
                    onProteinMinChange = {},
                    onProteinMaxChange = {},
                    onFatMinChange = {},
                    onFatMaxChange = {},
                    onCarbsMinChange = {},
                    onCarbsMaxChange = {},
                    onSave = {}
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage(roborazziOptions = roborazziOptions)
    }
}
