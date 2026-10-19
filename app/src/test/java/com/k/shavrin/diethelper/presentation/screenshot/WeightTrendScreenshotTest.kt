package com.k.shavrin.diethelper.presentation.screenshot

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.captureRoboImage
import com.k.shavrin.diethelper.domain.model.WeightEntry
import com.k.shavrin.diethelper.presentation.screen.weight.WeightTrendCard
import com.k.shavrin.diethelper.presentation.theme.DietHelperTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.LocalDate

@OptIn(ExperimentalRoborazziApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w360dp-h800dp-xxhdpi", application = android.app.Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class WeightTrendScreenshotTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `weight trend with irregular dates`() {
        render(listOf(0L to 80f, 2L to 79.8f, 5L to 80.1f, 9L to 79.4f, 14L to 79.2f))
    }

    @Test
    fun `constant weight trend stays finite`() {
        render(listOf(0L to 80f, 5L to 80f, 10L to 80f))
    }

    private fun render(values: List<Pair<Long, Float>>) {
        val start = LocalDate.of(2026, 10, 1)
        composeTestRule.setContent {
            DietHelperTheme {
                WeightTrendCard(values.mapIndexed { index, (day, weight) ->
                    WeightEntry(id = index.toLong(), date = start.plusDays(day), weightKg = weight)
                })
            }
        }
        composeTestRule.onRoot().captureRoboImage(roborazziOptions = RoborazziOptions(
            recordOptions = RoborazziOptions.RecordOptions(resizeScale = 0.5)))
    }
}
