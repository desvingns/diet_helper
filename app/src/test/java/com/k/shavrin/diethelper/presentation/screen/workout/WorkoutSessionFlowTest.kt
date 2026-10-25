package com.k.shavrin.diethelper.presentation.screen.workout

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.k.shavrin.diethelper.domain.model.PerformedTrainingSet
import com.k.shavrin.diethelper.domain.model.TrainingSession
import com.k.shavrin.diethelper.domain.model.WorkoutDay
import com.k.shavrin.diethelper.domain.model.WorkoutExercise
import com.k.shavrin.diethelper.domain.model.WorkoutGroup
import com.k.shavrin.diethelper.domain.model.WorkoutProgress
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
class WorkoutSessionFlowTest {
    @get:Rule val compose = createComposeRule()
    private val day = WorkoutDay(0, 1, 1, "", listOf(WorkoutExercise(
        4, "Присед", "Тяжелая", "squat", 1.0, 2.5, 1.0, listOf(WorkoutGroup(3, 1, 0.7))
    )))
    private val session = TrainingSession("session", 0, 1, 1, 1000, "UTC", 1,
        warmUpStartedAt = 1000, warmUpFinishedAt = 2000)
    private val actions = TrainingJournalActions({ _, _ -> }, {}, { _, _ -> }, { _, _, _ -> }, {}, {})

    @Test
    fun `record button saves displayed plan without opening a feedback dialog`() {
        var saved: PerformedTrainingSet? = null
        compose.setContent {
            DietHelperTheme {
                WorkoutContent(WorkoutUiState(loading = false, selectedDay = day,
                    progress = WorkoutProgress(maxima = mapOf("squat" to 100.0, "bench" to 100.0, "deadlift" to 100.0))),
                    {}, { _, _ -> }, {}, { _, _, _ -> }, {}, TrainingJournalUiState(sessions = listOf(session), advice = "Old hint"),
                    actions.copy(record = { _, set -> saved = set }), WorkoutProgramActions({}, { _, _ -> }))
            }
        }
        compose.onNodeWithText("Записать подход").performScrollTo().performClick()
        compose.onNodeWithText("Фактический подход").assertDoesNotExist()
        compose.onNodeWithText("Обратная связь").assertDoesNotExist()
        compose.onNodeWithText("Old hint").assertDoesNotExist()
        compose.runOnIdle {
            assertEquals(70.0, saved!!.actualWeightKg!!, 0.001)
            assertEquals(3, saved!!.actualReps)
            assertNull(saved!!.rpe)
        }
    }

    @Test
    fun `feedback opens when persisted cooldown completion arrives after mutation version`() {
        val journal = mutableStateOf(TrainingJournalUiState(sessions = listOf(session.copy(coolDownStartedAt = 3000))))
        compose.setContent { DietHelperTheme {
            Box(Modifier.fillMaxSize()) {
                SessionContent(day, WorkoutUiState(loading = false, selectedDay = day,
                    progress = WorkoutProgress(maxima = mapOf("squat" to 100.0, "bench" to 100.0, "deadlift" to 100.0))),
                    { _, _ -> }, { _, _, _ -> }, {}, journal.value, actions,
                    // Exercise the real session state transitions with a JVM-safe dialog renderer.
                    // Native window sizing and the actual feedback form are checked on the emulator.
                    renderFeedback = { _, _, _, _, _, _ -> Text("Обратная связь после тренировки") })
            }
        } }
        compose.runOnIdle { journal.value = journal.value.copy(changeVersion = 1) }
        compose.waitForIdle()
        compose.runOnIdle {
            journal.value = journal.value.copy(sessions = listOf(session.copy(coolDownStartedAt = 3000, coolDownFinishedAt = 4000)))
        }
        compose.onNodeWithText("Обратная связь после тренировки").assertExists()
    }

    @Test
    fun `warmup completion has its own action`() {
        var finished: WorkoutDay? = null
        compose.setContent { DietHelperTheme {
            TrainingWarmUpCard(day, session.copy(warmUpFinishedAt = null), true,
                actions.copy(finishWarmUp = { finished = it }))
        } }
        compose.onNodeWithText("Завершить разминку").performClick()
        compose.runOnIdle { assertEquals(day, finished) }
    }

    @Test
    fun `cooldown cannot start before warmup ends`() {
        compose.setContent { DietHelperTheme {
            TrainingCoolDownCard(day, session.copy(warmUpFinishedAt = null), true, actions)
        } }
        compose.onNodeWithText("Начать заминку").assertIsNotEnabled()
    }

    @Test
    fun `active screen combines following set and elapsed placeholder without retired cards`() {
        val trainingDay = day.copy(exercises = listOf(day.exercises.single().copy(
            groups = listOf(WorkoutGroup(3, 1, 0.7), WorkoutGroup(2, 1, 0.8))
        )))
        showSession(trainingDay)
        compose.onNodeWithText("Затем: 80 кг × 2").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("С прошлого подхода: —").assertExists()
        compose.onNodeWithText("Разминка завершена", substring = true).assertDoesNotExist()
        compose.onNodeWithText("Твоя стандартная разминка").assertDoesNotExist()
        compose.onNodeWithText("Отдых между подходами").assertDoesNotExist()
        compose.onNodeWithText("Запишется показанный вес", substring = true).assertDoesNotExist()
    }

    @Test
    fun `following preview is absent when the next remaining set belongs to another exercise`() {
        val exercise = day.exercises.single()
        val trainingDay = day.copy(exercises = listOf(exercise, exercise.copy(id = 5, name = "Жим", base = "bench")))
        showSession(trainingDay)
        compose.onNodeWithText("Записать подход").performScrollTo().assertExists()
        compose.onNodeWithText("Затем:", substring = true).assertDoesNotExist()
    }

    @Test
    fun `warmup card disappears as soon as completion arrives`() {
        val warming = mutableStateOf(session.copy(warmUpFinishedAt = null))
        compose.setContent { DietHelperTheme { TrainingWarmUpCard(day, warming.value, true, actions) } }
        compose.onNodeWithText("Твоя стандартная разминка").assertExists()
        compose.runOnIdle { warming.value = warming.value.copy(warmUpFinishedAt = 2000) }
        compose.onNodeWithText("Твоя стандартная разминка").assertDoesNotExist()
        compose.onNodeWithText("Завершить разминку").assertDoesNotExist()
        compose.onNodeWithText("Разминка завершена", substring = true).assertDoesNotExist()
    }

    private fun showSession(trainingDay: WorkoutDay) {
        compose.setContent { DietHelperTheme {
            Box(Modifier.fillMaxSize()) {
                SessionContent(trainingDay, WorkoutUiState(loading = false, selectedDay = trainingDay,
                    progress = WorkoutProgress(maxima = mapOf("squat" to 100.0, "bench" to 100.0, "deadlift" to 100.0))),
                    { _, _ -> }, { _, _, _ -> }, {}, TrainingJournalUiState(sessions = listOf(session)), actions)
            }
        } }
    }
}
