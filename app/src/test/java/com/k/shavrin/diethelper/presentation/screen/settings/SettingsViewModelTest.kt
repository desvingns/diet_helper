package com.k.shavrin.diethelper.presentation.screen.settings

import com.k.shavrin.diethelper.data.FakeGoalsRepository
import com.k.shavrin.diethelper.domain.model.DailyGoals
import com.k.shavrin.diethelper.domain.usecase.goals.GetDailyGoalsUseCase
import com.k.shavrin.diethelper.domain.usecase.goals.SaveDailyGoalsUseCase
import com.k.shavrin.diethelper.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
        goalsRepo = FakeGoalsRepository(
            DailyGoals(
                calories = 2000f,
                proteinMin = 120f, proteinMax = 180f,
                fatMin = 55f, fatMax = 80f,
                carbsMin = 200f, carbsMax = 300f
            )
        )
        viewModel = SettingsViewModel(
            getGoals = GetDailyGoalsUseCase(goalsRepo),
            saveGoals = SaveDailyGoalsUseCase(goalsRepo)
        )
    }

    @Test
    fun `init loads goals from repository`() = runTest {
        val state = viewModel.state.value
        assertEquals("2000", state.calories)
        assertEquals("120", state.proteinMin)
        assertEquals("180", state.proteinMax)
        assertEquals("55", state.fatMin)
        assertEquals("80", state.fatMax)
        assertEquals("200", state.carbsMin)
        assertEquals("300", state.carbsMax)
    }

    @Test
    fun `save with valid values persists to repository`() = runTest {
        viewModel.onCaloriesChange("1800")
        viewModel.onProteinMinChange("100")
        viewModel.onProteinMaxChange("160")
        viewModel.onFatMinChange("50")
        viewModel.onFatMaxChange("75")
        viewModel.onCarbsMinChange("180")
        viewModel.onCarbsMaxChange("280")

        viewModel.save()

        assertEquals(1800f, goalsRepo.current.calories, 0.001f)
        assertEquals(100f, goalsRepo.current.proteinMin, 0.001f)
        assertEquals(160f, goalsRepo.current.proteinMax, 0.001f)
        assertEquals(50f, goalsRepo.current.fatMin, 0.001f)
        assertEquals(75f, goalsRepo.current.fatMax, 0.001f)
        assertEquals(180f, goalsRepo.current.carbsMin, 0.001f)
        assertEquals(280f, goalsRepo.current.carbsMax, 0.001f)
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
    fun `save with empty protein min sets error`() = runTest {
        viewModel.onProteinMinChange("")
        viewModel.save()

        assertNotNull(viewModel.state.value.proteinMinError)
        assertNull(viewModel.state.value.caloriesError)
    }

    @Test
    fun `save when max less than min sets max error`() = runTest {
        viewModel.onProteinMinChange("200")
        viewModel.onProteinMaxChange("100")
        viewModel.save()

        assertNotNull(viewModel.state.value.proteinMaxError)
        assertNull(viewModel.state.value.proteinMinError)
    }

    @Test
    fun `save when max equals min sets max error`() = runTest {
        viewModel.onFatMinChange("80")
        viewModel.onFatMaxChange("80")
        viewModel.save()

        assertNotNull(viewModel.state.value.fatMaxError)
    }

    @Test
    fun `comma decimal separator is accepted`() = runTest {
        viewModel.onCaloriesChange("1800,5")
        viewModel.save()

        assertEquals(1800.5f, goalsRepo.current.calories, 0.001f)
    }

    // setUp defaults: calories=2000, proteinMin=120, fatMin=55, carbsMin=200
    // low sum  = 120*4 + 200*4 + 55*9 = 480 + 800 + 495 = 1775
    // high sum = 180*4 + 300*4 + 80*9 = 720 + 1200 + 720 = 2640

    @Test
    fun `showMacroCalorieWarningLow true when min macros sum exceeds calories`() = runTest {
        // 1775 > 1000
        viewModel.onCaloriesChange("1000")

        assertTrue(viewModel.state.value.showMacroCalorieWarningLow)
    }

    @Test
    fun `showMacroCalorieWarningLow false when min macros sum is below calories`() = runTest {
        // 1775 < 3000
        viewModel.onCaloriesChange("3000")

        assertFalse(viewModel.state.value.showMacroCalorieWarningLow)
    }

    @Test
    fun `showMacroCalorieWarningLow false when calories field is not a valid number`() = runTest {
        viewModel.onCaloriesChange("abc")

        assertFalse(viewModel.state.value.showMacroCalorieWarningLow)
    }

    @Test
    fun `showMacroCalorieWarningLow false when proteinMin is not a valid number`() = runTest {
        viewModel.onCaloriesChange("1000")
        viewModel.onProteinMinChange("abc")

        assertFalse(viewModel.state.value.showMacroCalorieWarningLow)
    }

    @Test
    fun `showMacroCalorieWarningHigh true when max macros sum is below calories`() = runTest {
        // 2640 < 3000
        viewModel.onCaloriesChange("3000")

        assertTrue(viewModel.state.value.showMacroCalorieWarningHigh)
    }

    @Test
    fun `showMacroCalorieWarningHigh false when max macros sum equals or exceeds calories`() = runTest {
        // 2640 >= 2000
        viewModel.onCaloriesChange("2000")

        assertFalse(viewModel.state.value.showMacroCalorieWarningHigh)
    }

    @Test
    fun `showMacroCalorieWarningHigh false when calories field is not a valid number`() = runTest {
        viewModel.onCaloriesChange("abc")

        assertFalse(viewModel.state.value.showMacroCalorieWarningHigh)
    }

    @Test
    fun `showMacroCalorieWarningHigh false when proteinMax is not a valid number`() = runTest {
        viewModel.onCaloriesChange("3000")
        viewModel.onProteinMaxChange("abc")

        assertFalse(viewModel.state.value.showMacroCalorieWarningHigh)
    }
}
