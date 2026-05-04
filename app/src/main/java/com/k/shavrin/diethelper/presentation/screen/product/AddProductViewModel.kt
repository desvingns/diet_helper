package com.k.shavrin.diethelper.presentation.screen.product

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.k.shavrin.diethelper.domain.model.Product
import com.k.shavrin.diethelper.domain.usecase.product.AddProductUseCase
import com.k.shavrin.diethelper.presentation.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddProductViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val addProductUseCase: AddProductUseCase
) : ViewModel() {

    private val _state: MutableStateFlow<AddProductUiState> = MutableStateFlow(
        AddProductUiState(name = savedStateHandle.get<String>(Routes.ARG_NAME).orEmpty())
    )
    val state: StateFlow<AddProductUiState> = _state.asStateFlow()

    fun onNameChange(value: String) {
        _state.update { it.copy(name = value, nameError = null) }
    }

    fun onCaloriesChange(value: String) {
        _state.update { it.copy(calories = value, caloriesError = null) }
    }

    fun onProteinChange(value: String) {
        _state.update { it.copy(protein = value, proteinError = null) }
    }

    fun onFatChange(value: String) {
        _state.update { it.copy(fat = value, fatError = null) }
    }

    fun onCarbsChange(value: String) {
        _state.update { it.copy(carbs = value, carbsError = null) }
    }

    fun save(onSuccess: () -> Unit) {
        val current = _state.value
        val nameError = if (current.name.isBlank()) "Введите название" else null
        val caloriesValue = current.calories.replace(',', '.').toFloatOrNull()
        val caloriesError = when {
            caloriesValue == null -> "Введите число"
            caloriesValue <= 0f -> "Должно быть больше 0"
            else -> null
        }
        val proteinValue = current.protein.replace(',', '.').toFloatOrNull()
        val proteinError = when {
            proteinValue == null -> "Введите число"
            proteinValue < 0f -> "Не может быть отрицательным"
            else -> null
        }
        val fatValue = current.fat.replace(',', '.').toFloatOrNull()
        val fatError = when {
            fatValue == null -> "Введите число"
            fatValue < 0f -> "Не может быть отрицательным"
            else -> null
        }
        val carbsValue = current.carbs.replace(',', '.').toFloatOrNull()
        val carbsError = when {
            carbsValue == null -> "Введите число"
            carbsValue < 0f -> "Не может быть отрицательным"
            else -> null
        }

        if (
            nameError != null ||
            caloriesError != null ||
            proteinError != null ||
            fatError != null ||
            carbsError != null
        ) {
            _state.update {
                it.copy(
                    nameError = nameError,
                    caloriesError = caloriesError,
                    proteinError = proteinError,
                    fatError = fatError,
                    carbsError = carbsError
                )
            }
            return
        }

        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            addProductUseCase(
                Product(
                    name = current.name.trim(),
                    caloriesPer100g = caloriesValue!!,
                    proteinPer100g = proteinValue!!,
                    fatPer100g = fatValue!!,
                    carbsPer100g = carbsValue!!
                )
            )
            _state.update { it.copy(isSaving = false, isSaved = true) }
            onSuccess()
        }
    }
}
