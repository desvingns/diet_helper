package com.k.shavrin.diethelper.presentation.screen.product

import androidx.lifecycle.SavedStateHandle
import com.k.shavrin.diethelper.data.FakeProductRepository
import com.k.shavrin.diethelper.domain.usecase.product.AddProductUseCase
import com.k.shavrin.diethelper.presentation.navigation.Routes
import com.k.shavrin.diethelper.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddProductViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repo = FakeProductRepository()

    private fun createViewModel(initialName: String = ""): AddProductViewModel =
        AddProductViewModel(
            savedStateHandle = SavedStateHandle(mapOf(Routes.ARG_NAME to initialName)),
            addProductUseCase = AddProductUseCase(repo)
        )

    // ── initial state ────────────────────────────────────────────────────────

    @Test
    fun `state initialised with empty fields`() {
        val vm = createViewModel()
        val s = vm.state.value
        assertEquals("", s.name)
        assertEquals("", s.calories)
        assertNull(s.nameError)
        assertFalse(s.isSaving)
        assertFalse(s.isSaved)
    }

    @Test
    fun `savedStateHandle name is pre-filled`() {
        val vm = createViewModel(initialName = "Гречка")
        assertEquals("Гречка", vm.state.value.name)
    }

    // ── field change clears error ────────────────────────────────────────────

    @Test
    fun `onNameChange clears nameError`() = runTest {
        val vm = createViewModel()
        vm.save {}
        assertNotNull(vm.state.value.nameError)

        vm.onNameChange("Банан")
        assertNull(vm.state.value.nameError)
    }

    @Test
    fun `onCaloriesChange clears caloriesError`() = runTest {
        val vm = createViewModel()
        vm.onNameChange("X")
        vm.save {}
        assertNotNull(vm.state.value.caloriesError)

        vm.onCaloriesChange("100")
        assertNull(vm.state.value.caloriesError)
    }

    // ── validation: empty / zero / negative ─────────────────────────────────

    @Test
    fun `save with blank name sets nameError`() = runTest {
        val vm = createViewModel()
        vm.onCaloriesChange("100")
        vm.onProteinChange("10")
        vm.onFatChange("5")
        vm.onCarbsChange("20")
        vm.save {}

        assertNotNull(vm.state.value.nameError)
        assertFalse(vm.state.value.isSaved)
    }

    @Test
    fun `save with zero calories sets caloriesError`() = runTest {
        val vm = createViewModel()
        vm.onNameChange("Тест")
        vm.onCaloriesChange("0")
        vm.onProteinChange("0")
        vm.onFatChange("0")
        vm.onCarbsChange("0")
        vm.save {}

        assertNotNull(vm.state.value.caloriesError)
    }

    @Test
    fun `save with negative protein sets proteinError`() = runTest {
        val vm = createViewModel()
        vm.onNameChange("Тест")
        vm.onCaloriesChange("100")
        vm.onProteinChange("-1")
        vm.onFatChange("0")
        vm.onCarbsChange("0")
        vm.save {}

        assertNotNull(vm.state.value.proteinError)
    }

    @Test
    fun `save with non-numeric fat sets fatError`() = runTest {
        val vm = createViewModel()
        vm.onNameChange("Тест")
        vm.onCaloriesChange("100")
        vm.onProteinChange("0")
        vm.onFatChange("abc")
        vm.onCarbsChange("0")
        vm.save {}

        assertNotNull(vm.state.value.fatError)
    }

    // ── happy path ───────────────────────────────────────────────────────────

    @Test
    fun `save with valid values calls onSuccess and sets isSaved`() = runTest {
        val vm = createViewModel()
        vm.onNameChange("Банан")
        vm.onCaloriesChange("89")
        vm.onProteinChange("1.1")
        vm.onFatChange("0.3")
        vm.onCarbsChange("22.8")

        var called = false
        vm.save { called = true }

        assertTrue(called)
        assertTrue(vm.state.value.isSaved)
        assertFalse(vm.state.value.isSaving)
    }

    @Test
    fun `save trims name before persisting`() = runTest {
        val vm = createViewModel()
        vm.onNameChange("  Банан  ")
        vm.onCaloriesChange("89")
        vm.onProteinChange("1.1")
        vm.onFatChange("0.3")
        vm.onCarbsChange("22.8")
        vm.save {}

        val saved = repo.getProductById(1L)!!
        assertEquals("Банан", saved.name)
    }

    @Test
    fun `comma decimal separator is accepted for all fields`() = runTest {
        val vm = createViewModel()
        vm.onNameChange("Тест")
        vm.onCaloriesChange("89,5")
        vm.onProteinChange("1,1")
        vm.onFatChange("0,3")
        vm.onCarbsChange("22,8")
        vm.save {}

        val saved = repo.getProductById(1L)!!
        assertEquals(89.5f, saved.caloriesPer100g, 0.001f)
        assertEquals(1.1f, saved.proteinPer100g, 0.001f)
        assertEquals(0.3f, saved.fatPer100g, 0.001f)
        assertEquals(22.8f, saved.carbsPer100g, 0.001f)
    }

    @Test
    fun `zero protein and fat and carbs are valid`() = runTest {
        val vm = createViewModel()
        vm.onNameChange("Вода")
        vm.onCaloriesChange("0.5")
        vm.onProteinChange("0")
        vm.onFatChange("0")
        vm.onCarbsChange("0")
        vm.save {}

        assertTrue(vm.state.value.isSaved)
        assertNull(vm.state.value.proteinError)
        assertNull(vm.state.value.fatError)
        assertNull(vm.state.value.carbsError)
    }
}
