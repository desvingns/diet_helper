package com.k.shavrin.diethelper.presentation.screen.export

import android.content.Context
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.k.shavrin.diethelper.domain.model.ExportConfig
import com.k.shavrin.diethelper.domain.model.ExportMode
import com.k.shavrin.diethelper.domain.usecase.export.ExportReportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import javax.inject.Inject

private const val DEFAULT_RANGE_DAYS_BACK = 6L
private const val FILE_PROVIDER_SUFFIX = ".fileprovider"

@HiltViewModel
class ExportViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val exportReportUseCase: ExportReportUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(initialState())
    val state: StateFlow<ExportUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<ExportEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events: SharedFlow<ExportEvent> = _events.asSharedFlow()

    fun setFrom(date: LocalDate) {
        if (date.isAfter(_state.value.to)) {
            _state.update { it.copy(from = it.to, to = date, errorMessage = null) }
        } else {
            _state.update { it.copy(from = date, errorMessage = null) }
        }
    }

    fun setTo(date: LocalDate) {
        if (date.isBefore(_state.value.from)) {
            _state.update { it.copy(to = it.from, from = date, errorMessage = null) }
        } else {
            _state.update { it.copy(to = date, errorMessage = null) }
        }
    }

    fun setMode(mode: ExportMode) {
        _state.update { it.copy(mode = mode, errorMessage = null) }
    }

    fun setIncludeStats(value: Boolean) {
        _state.update { it.copy(includeStats = value, errorMessage = null) }
    }

    fun exportReport() {
        if (_state.value.isExporting) return
        _state.update { it.copy(isExporting = true, errorMessage = null) }
        viewModelScope.launch {
            val current = _state.value
            try {
                val path = exportReportUseCase(
                    ExportConfig(
                        from = current.from,
                        to = current.to,
                        mode = current.mode,
                        includeStats = current.includeStats
                    )
                )
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}$FILE_PROVIDER_SUFFIX",
                    File(path)
                )
                _state.update { it.copy(isExporting = false) }
                _events.tryEmit(ExportEvent.Share(uri))
            } catch (t: Throwable) {
                val message = t.message ?: "Не удалось сформировать PDF"
                _state.update { it.copy(isExporting = false, errorMessage = message) }
                _events.tryEmit(ExportEvent.Error(message))
            }
        }
    }

    private fun initialState(): ExportUiState {
        val today = LocalDate.now()
        return ExportUiState(
            from = today.minusDays(DEFAULT_RANGE_DAYS_BACK),
            to = today,
            mode = ExportMode.DETAILED,
            includeStats = true
        )
    }
}
