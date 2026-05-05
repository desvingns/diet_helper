package com.k.shavrin.diethelper.presentation.screen.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
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
class SettingsScreenContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun defaultState() = SettingsUiState(
        calories = "2000",
        proteinMin = "120",
        proteinMax = "180",
        fatMin = "55",
        fatMax = "80",
        carbsMin = "200",
        carbsMax = "280",
        isLoading = false
    )

    // ── basic rendering ──────────────────────────────────────────────────────

    @Test
    fun `shows daily goals title`() {
        composeTestRule.setContent {
            DietHelperTheme {
                SettingsContent(
                    state = defaultState(),
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
        composeTestRule.onNodeWithText("Дневные цели").assertIsDisplayed()
    }

    @Test
    fun `shows save button`() {
        composeTestRule.setContent {
            DietHelperTheme {
                SettingsContent(
                    state = defaultState(),
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
        composeTestRule.onNodeWithText("Сохранить").assertIsDisplayed()
        composeTestRule.onNodeWithText("Сохранить").assertIsEnabled()
    }

    @Test
    fun `shows calories field with current value`() {
        composeTestRule.setContent {
            DietHelperTheme {
                SettingsContent(
                    state = defaultState(),
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
        composeTestRule.onNodeWithText("2000").assertIsDisplayed()
    }

    // ── validation errors ────────────────────────────────────────────────────

    @Test
    fun `shows calories error when caloriesError is not null`() {
        val state = defaultState().copy(caloriesError = "Некорректное значение")
        composeTestRule.setContent {
            DietHelperTheme {
                SettingsContent(
                    state = state,
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
        composeTestRule.onNodeWithText("Некорректное значение").assertIsDisplayed()
    }

    // ── justSaved banner ─────────────────────────────────────────────────────

    @Test
    fun `shows Сохранено text when justSaved is true`() {
        val state = defaultState().copy(justSaved = true)
        composeTestRule.setContent {
            DietHelperTheme {
                SettingsContent(
                    state = state,
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
        composeTestRule.onNodeWithText("Сохранено").assertIsDisplayed()
    }

    @Test
    fun `does not show Сохранено text when justSaved is false`() {
        composeTestRule.setContent {
            DietHelperTheme {
                SettingsContent(
                    state = defaultState(),
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
        composeTestRule.onNodeWithText("Сохранено").assertDoesNotExist()
    }
}
