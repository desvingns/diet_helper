package com.k.shavrin.diethelper.presentation.screen.workout

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import com.k.shavrin.diethelper.domain.model.PerformedTrainingSet
import com.k.shavrin.diethelper.domain.model.TrainingLoadBasis
import com.k.shavrin.diethelper.domain.model.TrainingSessionFeedback
import com.k.shavrin.diethelper.domain.model.WorkoutDay
import com.k.shavrin.diethelper.domain.model.WorkoutExercise
import com.k.shavrin.diethelper.domain.model.WorkoutGroup
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
class TrainingJournalDialogsTest {
    @get:Rule val compose = createComposeRule()
    private val source = WorkoutDay(0, 1, 1, "", listOf(WorkoutExercise(
        4, "Присед на груди", "Тяжелая", "squat", 0.75, 2.5, 1.0, listOf(WorkoutGroup(3, 1, 0.6))
    ))).sets().single()

    private fun settle() {
        compose.waitForIdle()
    }

    @Test
    fun `changing exercise requires a newly entered load rather than retaining the squat derived value`() {
        var saved: PerformedTrainingSet? = null
        compose.setContent {
            DietHelperTheme {
                Box(Modifier.fillMaxSize()) {
                    TrainingSetForm(source, null, mapOf("squat" to 100.0), false, false, null, { saved = it }, null, {})
                }
            }
        }
        settle()
        compose.onNodeWithText("Жим ногами").performClick()
        settle()
        compose.onNodeWithText("Записать подход").assertIsNotEnabled()
        compose.onNodeWithText("Фактический вес, кг").performTextReplacement("30")
        settle()
        compose.onNodeWithText("Записать подход").performClick()
        settle()
        compose.runOnIdle {
            assertEquals("Жим ногами", saved!!.exerciseName)
            assertEquals(30.0, saved!!.actualWeightKg!!, 0.001)
            assertEquals(45.0, saved!!.plannedWeightKg!!, 0.001)
        }
    }

    @Test
    fun `bodyweight extra records unknown external load as null`() {
        var saved: PerformedTrainingSet? = null
        compose.setContent {
            DietHelperTheme { Box(Modifier.fillMaxSize()) {
                TrainingSetForm(null, null, emptyMap(), false, false, null, { saved = it }, null, {})
            } }
        }
        settle()
        compose.onNodeWithText("Подтягивания").performClick()
        settle()
        compose.onNodeWithText("Повторения").performTextReplacement("4")
        settle()
        compose.onNodeWithText("Записать подход").performClick()
        settle()
        compose.runOnIdle {
            assertEquals(TrainingLoadBasis.BODYWEIGHT, saved!!.loadBasis)
            assertNull(saved!!.actualWeightKg)
            assertNull(saved!!.rpe)
        }
    }

    @Test
    fun `partial finish requires a reason and keeps social time distinct`() {
        var saved: TrainingSessionFeedback? = null
        compose.setContent {
            DietHelperTheme { Box(Modifier.fillMaxSize()) { FinishTrainingForm(false, null, false, null, { saved = it }, {}) } }
        }
        settle()
        compose.onNode(hasText("Сохранить занятие") and hasClickAction()).assertIsNotEnabled()
        compose.onNodeWithText("Время в зале, мин").performTextReplacement("90")
        settle()
        compose.onNodeWithText("Из них разговоры, мин").performTextReplacement("10")
        settle()
        compose.onNodeWithText("Причина / заметка").performTextReplacement("Shortened")
        settle()
        compose.onNode(hasText("Сохранить занятие") and hasClickAction()).assertIsEnabled().performClick()
        compose.runOnIdle {
            assertEquals(90, saved!!.gymMinutes)
            assertEquals(10, saved!!.socialMinutes)
        }
    }
}
