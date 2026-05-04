package com.k.shavrin.diethelper.presentation.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.k.shavrin.diethelper.domain.model.DailyGoals
import com.k.shavrin.diethelper.domain.usecase.goals.GetDailyGoalsUseCase
import com.k.shavrin.diethelper.domain.usecase.goals.SaveDailyGoalsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getGoals: GetDailyGoalsUseCase,
    private val saveGoals: SaveDailyGoalsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val goals = getGoals().first()
            _state.update {
                it.copy(
                    calories = goals.calories.roundToInt().toString(),
                    protein = goals.protein.roundToInt().toString(),
                    fat = goals.fat.roundToInt().toString(),
                    carbs = goals.carbs.roundToInt().toString(),
                    isLoading = false
                )
            }
        }
    }

    fun onCaloriesChange(value: String) {
        _state.update { it.copy(calories = value, caloriesError = null, justSaved = false) }
    }

    fun onProteinChange(value: String) {
        _state.update { it.copy(protein = value, proteinError = null, justSaved = false) }
    }

    fun onFatChange(value: String) {
        _state.update { it.copy(fat = value, fatError = null, justSaved = false) }
    }

    fun onCarbsChange(value: String) {
        _state.update { it.copy(carbs = value, carbsError = null, justSaved = false) }
    }

    fun save() {
        val current = _state.value
        val cal = current.calories.replace(',', '.').toFloatOrNull()
        val pro = current.protein.replace(',', '.').toFloatOrNull()
        val fat = current.fat.replace(',', '.').toFloatOrNull()
        val car = current.carbs.replace(',', '.').toFloatOrNull()

        val caloriesError = validatePositive(cal)
        val proteinError = validatePositive(pro)
        val fatError = validatePositive(fat)
        val carbsError = validatePositive(car)

        if (
            caloriesError != null ||
            proteinError != null ||
            fatError != null ||
            carbsError != null
        ) {
            _state.update {
                it.copy(
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
            saveGoals(
                DailyGoals(
                    calories = cal!!,
                    protein = pro!!,
                    fat = fat!!,
                    carbs = car!!
                )
            )
            _state.update { it.copy(isSaving = false, justSaved = true) }
        }
    }

    private fun validatePositive(value: Float?): String? = when {
        value == null -> "Введите число"
        value <= 0f -> "Должно быть больше 0"
        else -> null
    }
}
