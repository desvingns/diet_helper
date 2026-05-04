package com.k.shavrin.diethelper.presentation.screen.weight

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.k.shavrin.diethelper.domain.model.WeightEntry
import com.k.shavrin.diethelper.domain.usecase.weight.DeleteWeightEntryUseCase
import com.k.shavrin.diethelper.domain.usecase.weight.GetAllWeightEntriesUseCase
import com.k.shavrin.diethelper.domain.usecase.weight.UpsertWeightEntryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
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
    }

    fun save() {
        val value = _input.value.replace(',', '.').toFloatOrNull()
        if (value == null || value <= 0f) return
        viewModelScope.launch {
            upsertUseCase(LocalDate.now(), value)
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
