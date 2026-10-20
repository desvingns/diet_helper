package com.k.shavrin.diethelper.presentation.screen.workout

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import com.k.shavrin.diethelper.domain.model.PerformedTrainingSet
import com.k.shavrin.diethelper.domain.model.ReturnToTrainingPlan
import com.k.shavrin.diethelper.domain.model.TrainingLoadBasis
import com.k.shavrin.diethelper.domain.model.WorkoutProgram
import com.k.shavrin.diethelper.presentation.theme.DietHelperTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w400dp-h900dp-xxhdpi", application = Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class WorkoutProgramsContentTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun `new week requires separately supplied working weights`() {
        var saved: Map<String, Double>? = null
        compose.setContent {
            DietHelperTheme { Box(Modifier.fillMaxSize()) {
                ReturnWeekLoadsForm(2, emptyMap(), {}, { saved = it })
            } }
        }
        compose.onNodeWithText("Сохранить веса").assertIsNotEnabled()
        compose.onNodeWithText("Присед · рабочий вес").performTextReplacement("100")
        compose.onNodeWithText("Жим · рабочий вес").performTextReplacement("80")
        compose.onNodeWithText("Сохранить веса").assertIsNotEnabled()
        compose.onNodeWithText("Сумо · рабочий вес").performTextReplacement("60")
        compose.onNodeWithText("Сохранить веса").performClick()
        compose.runOnIdle { assertEquals(mapOf("squat" to 100.0, "bench" to 80.0, "deadlift" to 60.0), saved) }
    }

    @Test
    fun `planned bodyweight exercise starts without a fabricated zero weight`() {
        val source = ReturnToTrainingPlan.create().days[3].sets().first()
        var saved: PerformedTrainingSet? = null
        compose.setContent {
            DietHelperTheme { Box(Modifier.fillMaxSize()) {
                TrainingSetForm(source, null, emptyMap(), false, false, null, { saved = it }, null, {})
            } }
        }
        compose.onNodeWithText("Записать подход").performClick()
        compose.runOnIdle {
            assertEquals(TrainingLoadBasis.BODYWEIGHT, saved!!.loadBasis)
            assertEquals(4, saved!!.actualReps)
            assertNull(saved!!.plannedWeightKg)
            assertNull(saved!!.actualWeightKg)
        }
    }

    @Test
    fun `both program choices are available without altering workout completion`() {
        var selected = WorkoutProgram.SRC7
        compose.setContent {
            DietHelperTheme { WorkoutProgramPicker(WorkoutProgram.SRC7, true) { selected = it } }
        }
        compose.onNodeWithText("Возвращение").performClick()
        compose.runOnIdle { assertEquals(WorkoutProgram.RETURN, selected) }
        compose.onNodeWithText("СРЦ7").performClick()
        compose.runOnIdle { assertEquals(WorkoutProgram.SRC7, selected) }
    }
}
