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
                    proteinMin = goals.proteinMin.roundToInt().toString(),
                    proteinMax = goals.proteinMax.roundToInt().toString(),
                    fatMin = goals.fatMin.roundToInt().toString(),
                    fatMax = goals.fatMax.roundToInt().toString(),
                    carbsMin = goals.carbsMin.roundToInt().toString(),
                    carbsMax = goals.carbsMax.roundToInt().toString(),
                    isLoading = false
                )
            }
            computeMacroWarnings()
        }
    }

    fun onCaloriesChange(value: String) {
        _state.update { it.copy(calories = value, caloriesError = null, justSaved = false) }
        computeMacroWarnings()
    }

    fun onProteinMinChange(value: String) {
        _state.update { it.copy(proteinMin = value, proteinMinError = null, proteinMaxError = null, justSaved = false) }
        computeMacroWarnings()
    }

    fun onProteinMaxChange(value: String) {
        _state.update { it.copy(proteinMax = value, proteinMinError = null, proteinMaxError = null, justSaved = false) }
        computeMacroWarnings()
    }

    fun onFatMinChange(value: String) {
        _state.update { it.copy(fatMin = value, fatMinError = null, fatMaxError = null, justSaved = false) }
        computeMacroWarnings()
    }

    fun onFatMaxChange(value: String) {
        _state.update { it.copy(fatMax = value, fatMinError = null, fatMaxError = null, justSaved = false) }
        computeMacroWarnings()
    }

    fun onCarbsMinChange(value: String) {
        _state.update { it.copy(carbsMin = value, carbsMinError = null, carbsMaxError = null, justSaved = false) }
        computeMacroWarnings()
    }

    fun onCarbsMaxChange(value: String) {
        _state.update { it.copy(carbsMax = value, carbsMinError = null, carbsMaxError = null, justSaved = false) }
        computeMacroWarnings()
    }

    private fun computeMacroWarnings() {
        val s = _state.value
        val cal = s.calories.replace(',', '.').toFloatOrNull()
        val proMin = s.proteinMin.replace(',', '.').toFloatOrNull()
        val fatMin = s.fatMin.replace(',', '.').toFloatOrNull()
        val carMin = s.carbsMin.replace(',', '.').toFloatOrNull()

        val warningLow = cal != null && proMin != null && fatMin != null && carMin != null &&
                (proMin * 4f + carMin * 4f + fatMin * 9f) < cal

        _state.update { it.copy(showMacroCalorieWarningLow = warningLow) }
    }

    fun save() {
        val current = _state.value
        val cal = current.calories.replace(',', '.').toFloatOrNull()
        val proMin = current.proteinMin.replace(',', '.').toFloatOrNull()
        val proMax = current.proteinMax.replace(',', '.').toFloatOrNull()
        val fatMin = current.fatMin.replace(',', '.').toFloatOrNull()
        val fatMax = current.fatMax.replace(',', '.').toFloatOrNull()
        val carMin = current.carbsMin.replace(',', '.').toFloatOrNull()
        val carMax = current.carbsMax.replace(',', '.').toFloatOrNull()

        val caloriesError = validatePositive(cal)
        val (proteinMinError, proteinMaxError) = validateRange(proMin, proMax)
        val (fatMinError, fatMaxError) = validateRange(fatMin, fatMax)
        val (carbsMinError, carbsMaxError) = validateRange(carMin, carMax)

        if (caloriesError != null || proteinMinError != null || proteinMaxError != null ||
            fatMinError != null || fatMaxError != null || carbsMinError != null || carbsMaxError != null
        ) {
            _state.update {
                it.copy(
                    caloriesError = caloriesError,
                    proteinMinError = proteinMinError,
                    proteinMaxError = proteinMaxError,
                    fatMinError = fatMinError,
                    fatMaxError = fatMaxError,
                    carbsMinError = carbsMinError,
                    carbsMaxError = carbsMaxError
                )
            }
            return
        }

        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            saveGoals(
                DailyGoals(
                    calories = cal!!,
                    proteinMin = proMin!!,
                    proteinMax = proMax!!,
                    fatMin = fatMin!!,
                    fatMax = fatMax!!,
                    carbsMin = carMin!!,
                    carbsMax = carMax!!
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

    private fun validateRange(min: Float?, max: Float?): Pair<String?, String?> {
        val minError = validatePositive(min)
        val maxError = validatePositive(max)
        if (minError != null || maxError != null) return minError to maxError
        if (max!! <= min!!) return null to "Макс должен быть больше мин"
        return null to null
    }
}
