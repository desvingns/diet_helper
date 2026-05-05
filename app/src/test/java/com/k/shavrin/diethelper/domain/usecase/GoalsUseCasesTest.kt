package com.k.shavrin.diethelper.domain.usecase

import app.cash.turbine.test
import com.k.shavrin.diethelper.data.FakeGoalsRepository
import com.k.shavrin.diethelper.domain.model.DailyGoals
import com.k.shavrin.diethelper.domain.usecase.goals.GetDailyGoalsUseCase
import com.k.shavrin.diethelper.domain.usecase.goals.SaveDailyGoalsUseCase
import com.k.shavrin.diethelper.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GoalsUseCasesTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repo = FakeGoalsRepository(DailyGoals.DEFAULT)

    // ── GetDailyGoalsUseCase ─────────────────────────────────────────────────

    @Test
    fun `GetDailyGoalsUseCase emits default goals`() = runTest {
        GetDailyGoalsUseCase(repo)().test {
            val goals = awaitItem()
            assertEquals(DailyGoals.DEFAULT.calories, goals.calories, 0.001f)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `GetDailyGoalsUseCase emits updated goals after save`() = runTest {
        GetDailyGoalsUseCase(repo)().test {
            awaitItem() // default

            val newGoals = DailyGoals.DEFAULT.copy(calories = 1800f)
            repo.saveGoals(newGoals)

            assertEquals(1800f, awaitItem().calories, 0.001f)
            cancelAndConsumeRemainingEvents()
        }
    }

    // ── SaveDailyGoalsUseCase ────────────────────────────────────────────────

    @Test
    fun `SaveDailyGoalsUseCase persists all fields`() = runTest {
        val goals = DailyGoals(
            calories = 1600f,
            proteinMin = 90f, proteinMax = 140f,
            fatMin = 45f, fatMax = 65f,
            carbsMin = 160f, carbsMax = 240f
        )
        SaveDailyGoalsUseCase(repo)(goals)

        assertEquals(goals, repo.current)
    }
}
