package com.k.shavrin.diethelper.presentation.screen.settings

import com.k.shavrin.diethelper.data.FakeGoalsRepository
import com.k.shavrin.diethelper.domain.model.DailyGoals
import com.k.shavrin.diethelper.domain.usecase.goals.GetDailyGoalsUseCase
import com.k.shavrin.diethelper.domain.usecase.goals.SaveDailyGoalsUseCase
import com.k.shavrin.diethelper.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var goalsRepo: FakeGoalsRepository
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setUp() {
        goalsRepo = FakeGoalsRepository(DailyGoals(2000f, 150f, 67f, 250f))
        viewModel = SettingsViewModel(
            getGoals = GetDailyGoalsUseCase(goalsRepo),
            saveGoals = SaveDailyGoalsUseCase(goalsRepo)
        )
    }

    @Test
    fun `init loads goals from repository`() = runTest {
        val state = viewModel.state.value
        assertEquals("2000", state.calories)
        assertEquals("150", state.protein)
        assertEquals("67", state.fat)
        assertEquals("250", state.carbs)
    }

    @Test
    fun `save with valid values persists to repository`() = runTest {
        viewModel.onCaloriesChange("1800")
        viewModel.onProteinChange("120")
        viewModel.onFatChange("50")
        viewModel.onCarbsChange("220")

        viewModel.save()

        assertEquals(1800f, goalsRepo.current.calories, 0.001f)
        assertEquals(120f, goalsRepo.current.protein, 0.001f)
        assertEquals(50f, goalsRepo.current.fat, 0.001f)
        assertEquals(220f, goalsRepo.current.carbs, 0.001f)
        assertTrue(viewModel.state.value.justSaved)
    }

    @Test
    fun `save with invalid calories sets error`() = runTest {
        viewModel.onCaloriesChange("0")
        viewModel.save()

        assertNotNull(viewModel.state.value.caloriesError)
        assertEquals(2000f, goalsRepo.current.calories, 0.001f)
    }

    @Test
    fun `save with empty field sets error`() = runTest {
        viewModel.onProteinChange("")
        viewModel.save()

        assertNotNull(viewModel.state.value.proteinError)
        assertNull(viewModel.state.value.caloriesError)
    }

    @Test
    fun `comma decimal separator is accepted`() = runTest {
        viewModel.onCaloriesChange("1800,5")
        viewModel.save()

        assertEquals(1800.5f, goalsRepo.current.calories, 0.001f)
    }
}
