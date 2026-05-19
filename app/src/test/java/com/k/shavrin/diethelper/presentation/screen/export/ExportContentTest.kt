package com.k.shavrin.diethelper.presentation.screen.export

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.k.shavrin.diethelper.domain.model.ExportMode
import com.k.shavrin.diethelper.presentation.theme.DietHelperTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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
class ExportContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val today: LocalDate = LocalDate.of(2025, 5, 18)

    private fun defaultState(
        mode: ExportMode = ExportMode.DETAILED,
        includeStats: Boolean = true,
        isExporting: Boolean = false,
        errorMessage: String? = null
    ) = ExportUiState(
        from = today.minusDays(6),
        to = today,
        mode = mode,
        includeStats = includeStats,
        isExporting = isExporting,
        errorMessage = errorMessage
    )

    // ── basic rendering ───────────────────────────────────────────────────

    @Test
    fun `renders both date rows`() {
        composeTestRule.setContent {
            DietHelperTheme {
                ExportContent(
                    state = defaultState(),
                    onSetFrom = {},
                    onSetTo = {},
                    onSetMode = {},
                    onSetIncludeStats = {},
                    onExport = {}
                )
            }
        }
        composeTestRule.onNodeWithTag(EXPORT_TAG_FROM_DATE).assertIsDisplayed()
        composeTestRule.onNodeWithTag(EXPORT_TAG_TO_DATE).assertIsDisplayed()
    }

    @Test
    fun `renders both mode rows`() {
        composeTestRule.setContent {
            DietHelperTheme {
                ExportContent(
                    state = defaultState(),
                    onSetFrom = {},
                    onSetTo = {},
                    onSetMode = {},
                    onSetIncludeStats = {},
                    onExport = {}
                )
            }
        }
        composeTestRule.onNodeWithTag(EXPORT_TAG_MODE_DETAILED).assertIsDisplayed()
        composeTestRule.onNodeWithTag(EXPORT_TAG_MODE_SUMMARY).assertIsDisplayed()
    }

    @Test
    fun `renders stats switch`() {
        composeTestRule.setContent {
            DietHelperTheme {
                ExportContent(
                    state = defaultState(),
                    onSetFrom = {},
                    onSetTo = {},
                    onSetMode = {},
                    onSetIncludeStats = {},
                    onExport = {}
                )
            }
        }
        composeTestRule.onNodeWithTag(EXPORT_TAG_STATS_SWITCH).assertIsDisplayed()
    }

    @Test
    fun `renders export button enabled when not exporting`() {
        composeTestRule.setContent {
            DietHelperTheme {
                ExportContent(
                    state = defaultState(),
                    onSetFrom = {},
                    onSetTo = {},
                    onSetMode = {},
                    onSetIncludeStats = {},
                    onExport = {}
                )
            }
        }
        composeTestRule.onNodeWithTag(EXPORT_TAG_BUTTON).performScrollTo()
        composeTestRule.onNodeWithTag(EXPORT_TAG_BUTTON).assertIsDisplayed()
        composeTestRule.onNodeWithTag(EXPORT_TAG_BUTTON).assertIsEnabled()
        composeTestRule.onNodeWithText("Экспортировать").assertIsDisplayed()
    }

    // ── callback wiring ───────────────────────────────────────────────────

    @Test
    fun `clicking from date row does not invoke mode or export callbacks`() {
        // From-date click opens a date picker; we only assert that other callbacks aren't fired.
        var modeCalled = false
        var exportCalled = false
        composeTestRule.setContent {
            DietHelperTheme {
                ExportContent(
                    state = defaultState(),
                    onSetFrom = {},
                    onSetTo = {},
                    onSetMode = { modeCalled = true },
                    onSetIncludeStats = {},
                    onExport = { exportCalled = true }
                )
            }
        }
        composeTestRule.onNodeWithTag(EXPORT_TAG_FROM_DATE).performClick()
        assertTrue("Mode callback should not have fired", !modeCalled)
        assertTrue("Export callback should not have fired", !exportCalled)
    }

    @Test
    fun `clicking detailed mode row invokes onSetMode with DETAILED`() {
        var captured: ExportMode? = null
        composeTestRule.setContent {
            DietHelperTheme {
                // Start in SUMMARY_ONLY so DETAILED click is a real change.
                ExportContent(
                    state = defaultState(mode = ExportMode.SUMMARY_ONLY),
                    onSetFrom = {},
                    onSetTo = {},
                    onSetMode = { captured = it },
                    onSetIncludeStats = {},
                    onExport = {}
                )
            }
        }
        composeTestRule.onNodeWithTag(EXPORT_TAG_MODE_DETAILED).performClick()
        assertEquals(ExportMode.DETAILED, captured)
    }

    @Test
    fun `clicking summary mode row invokes onSetMode with SUMMARY_ONLY`() {
        var captured: ExportMode? = null
        composeTestRule.setContent {
            DietHelperTheme {
                ExportContent(
                    state = defaultState(mode = ExportMode.DETAILED),
                    onSetFrom = {},
                    onSetTo = {},
                    onSetMode = { captured = it },
                    onSetIncludeStats = {},
                    onExport = {}
                )
            }
        }
        composeTestRule.onNodeWithTag(EXPORT_TAG_MODE_SUMMARY).performClick()
        assertEquals(ExportMode.SUMMARY_ONLY, captured)
    }

    @Test
    fun `clicking stats switch invokes onSetIncludeStats with toggled value`() {
        var captured: Boolean? = null
        composeTestRule.setContent {
            DietHelperTheme {
                ExportContent(
                    state = defaultState(includeStats = true),
                    onSetFrom = {},
                    onSetTo = {},
                    onSetMode = {},
                    onSetIncludeStats = { captured = it },
                    onExport = {}
                )
            }
        }
        composeTestRule.onNodeWithTag(EXPORT_TAG_STATS_SWITCH).performClick()
        // Toggling from true should fire with false.
        assertEquals(false, captured)
    }

    @Test
    fun `clicking export button invokes onExport`() {
        var captured = false
        composeTestRule.setContent {
            DietHelperTheme {
                ExportContent(
                    state = defaultState(),
                    onSetFrom = {},
                    onSetTo = {},
                    onSetMode = {},
                    onSetIncludeStats = {},
                    onExport = { captured = true }
                )
            }
        }
        composeTestRule.onNodeWithTag(EXPORT_TAG_BUTTON).performScrollTo()
        composeTestRule.onNodeWithTag(EXPORT_TAG_BUTTON).performClick()
        assertTrue("onExport callback should have been invoked", captured)
    }

    // ── isExporting disables the button and shows progress label ──────────

    @Test
    fun `export button is disabled while exporting`() {
        composeTestRule.setContent {
            DietHelperTheme {
                ExportContent(
                    state = defaultState(isExporting = true),
                    onSetFrom = {},
                    onSetTo = {},
                    onSetMode = {},
                    onSetIncludeStats = {},
                    onExport = {}
                )
            }
        }
        composeTestRule.onNodeWithTag(EXPORT_TAG_BUTTON).performScrollTo()
        composeTestRule.onNodeWithTag(EXPORT_TAG_BUTTON).assertIsNotEnabled()
        composeTestRule.onNodeWithText("Формируем…").assertIsDisplayed()
    }

    @Test
    fun `clicking export button while exporting does not invoke onExport`() {
        var captured = false
        composeTestRule.setContent {
            DietHelperTheme {
                ExportContent(
                    state = defaultState(isExporting = true),
                    onSetFrom = {},
                    onSetTo = {},
                    onSetMode = {},
                    onSetIncludeStats = {},
                    onExport = { captured = true }
                )
            }
        }
        composeTestRule.onNodeWithTag(EXPORT_TAG_BUTTON).performClick()
        assertTrue("onExport must not fire on a disabled button", !captured)
    }

    // ── errorMessage is rendered ──────────────────────────────────────────

    @Test
    fun `shows error message when errorMessage is set`() {
        composeTestRule.setContent {
            DietHelperTheme {
                ExportContent(
                    state = defaultState(errorMessage = "Произошла ошибка"),
                    onSetFrom = {},
                    onSetTo = {},
                    onSetMode = {},
                    onSetIncludeStats = {},
                    onExport = {}
                )
            }
        }
        composeTestRule.onNodeWithText("Произошла ошибка").assertIsDisplayed()
    }

    @Test
    fun `does not show error label when errorMessage is null`() {
        composeTestRule.setContent {
            DietHelperTheme {
                ExportContent(
                    state = defaultState(errorMessage = null),
                    onSetFrom = {},
                    onSetTo = {},
                    onSetMode = {},
                    onSetIncludeStats = {},
                    onExport = {}
                )
            }
        }
        // sanity assertion on state — also documents the test intent
        assertNull(defaultState(errorMessage = null).errorMessage)
        // Verify some arbitrary error text would not be displayed
        composeTestRule.onNodeWithText("Произошла ошибка").assertDoesNotExist()
    }
}
