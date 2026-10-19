package com.k.shavrin.diethelper.presentation.screen.weight

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.k.shavrin.diethelper.domain.model.WeightEntry
import com.k.shavrin.diethelper.domain.usecase.weight.DeleteWeightEntryUseCase
import com.k.shavrin.diethelper.domain.usecase.weight.GetAllWeightEntriesUseCase
import com.k.shavrin.diethelper.domain.usecase.weight.UpsertWeightEntryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class WeightViewModel @Inject constructor(
    getAllUseCase: GetAllWeightEntriesUseCase,
    private val upsertUseCase: UpsertWeightEntryUseCase,
    private val deleteUseCase: DeleteWeightEntryUseCase
) : ViewModel() {

    private val _input = MutableStateFlow("")
    val input: StateFlow<String> = _input.asStateFlow()
    private val _inputError = MutableStateFlow<String?>(null)
    val inputError: StateFlow<String?> = _inputError.asStateFlow()
    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved.asStateFlow()
    private val _saving = MutableStateFlow(false)
    val saving: StateFlow<Boolean> = _saving.asStateFlow()

    val uiState: StateFlow<WeightUiState> = getAllUseCase()
        .map { entries ->
            val items = entries.mapIndexed { index, entry ->
                val older = entries.getOrNull(index + 1)
                val delta = if (older == null) null else entry.weightKg - older.weightKg
                WeightEntryWithDelta(entry, delta)
            }
            WeightUiState.Success(items) as WeightUiState
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = WeightUiState.Loading
        )

    init {
        viewModelScope.launch {
            val today = LocalDate.now()
            val entries = getAllUseCase().firstOrNull().orEmpty()
            val todayEntry = entries.firstOrNull { it.date == today }
            if (todayEntry != null && _input.value.isEmpty()) {
                _input.value = formatWeightInput(todayEntry.weightKg)
            }
        }
    }

    fun onInputChange(value: String) {
        _input.value = value
        _inputError.value = null
        _saved.value = false
    }

    fun save() {
        if (_saving.value) return
        _saved.value = false
        val value = _input.value.trim().replace(',', '.').toFloatOrNull()
        if (value == null || !value.isFinite() || value <= 0f || value > 500f) {
            _inputError.value = "Введите вес больше 0 и не более 500 кг, например 75,5"
            return
        }
        _inputError.value = null
        _saving.value = true
        viewModelScope.launch {
            try {
                upsertUseCase(LocalDate.now(), value)
                _saved.value = true
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                _inputError.value = "Не удалось сохранить вес. Попробуйте ещё раз."
            } finally {
                _saving.value = false
            }
        }
    }

    fun delete(entry: WeightEntry) {
        viewModelScope.launch { deleteUseCase(entry) }
    }

    private fun formatWeightInput(value: Float): String {
        val rounded = (value * 10f).toInt() / 10f
        return if (rounded % 1f == 0f) rounded.toInt().toString() else rounded.toString()
    }
}
