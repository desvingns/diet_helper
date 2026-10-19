package com.k.shavrin.diethelper.presentation.screen.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.k.shavrin.diethelper.domain.model.PerformedTrainingSet
import com.k.shavrin.diethelper.domain.model.RecoveryStatus
import com.k.shavrin.diethelper.domain.model.TrainingOutcome
import com.k.shavrin.diethelper.domain.model.TrainingSession
import com.k.shavrin.diethelper.domain.model.TrainingSessionFeedback
import com.k.shavrin.diethelper.domain.model.WorkoutDay
import com.k.shavrin.diethelper.domain.usecase.workout.ExportTrainingJournalUseCase
import com.k.shavrin.diethelper.domain.usecase.workout.FinishTrainingSessionUseCase
import com.k.shavrin.diethelper.domain.usecase.workout.GetTrainingJournalUseCase
import com.k.shavrin.diethelper.domain.usecase.workout.RecordTrainingSetUseCase
import com.k.shavrin.diethelper.domain.usecase.workout.SaveTrainingFollowUpUseCase
import com.k.shavrin.diethelper.domain.usecase.workout.SuggestTrainingAdjustmentUseCase
import com.k.shavrin.diethelper.domain.usecase.workout.UndoTrainingSetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TrainingJournalUiState(
    val sessions: List<TrainingSession> = emptyList(),
    val saving: Boolean = false,
    val error: String? = null,
    val changeVersion: Int = 0,
    val exportPaths: List<String> = emptyList(),
    val advice: String = "",
    val completedDayId: Int? = null
)

data class TrainingJournalActions(
    val record: (WorkoutDay, PerformedTrainingSet) -> Unit,
    val undo: (PerformedTrainingSet) -> Unit,
    val finish: (WorkoutDay, TrainingSessionFeedback) -> Unit,
    val followUp: (String, RecoveryStatus, String) -> Unit,
    val export: () -> Unit,
    val clearError: () -> Unit
)

@HiltViewModel
class TrainingJournalViewModel @Inject constructor(
    getJournal: GetTrainingJournalUseCase,
    private val recordSet: RecordTrainingSetUseCase,
    private val undoSet: UndoTrainingSetUseCase,
    private val finishSession: FinishTrainingSessionUseCase,
    private val saveFollowUp: SaveTrainingFollowUpUseCase,
    private val exportJournal: ExportTrainingJournalUseCase,
    private val suggest: SuggestTrainingAdjustmentUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(TrainingJournalUiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getJournal().catch { _state.update { it.copy(error = "Не удалось открыть журнал тренировок") } }
                .collect { sessions ->
                    _state.update {
                        it.copy(sessions = sessions, advice = suggest(sessions.firstOrNull { s -> s.outcome == TrainingOutcome.ACTIVE }
                            ?: sessions.firstOrNull()))
                    }
                }
        }
    }

    fun record(day: WorkoutDay, set: PerformedTrainingSet) = mutate { recordSet(day, set) }
    fun undo(set: PerformedTrainingSet) = mutate { undoSet(set) }
    fun finish(day: WorkoutDay, feedback: TrainingSessionFeedback) = mutate {
        finishSession(day, feedback)
        _state.update { it.copy(completedDayId = day.id) }
    }
    fun followUp(id: String, recovery: RecoveryStatus, notes: String) = mutate { saveFollowUp(id, recovery, notes) }
    fun export() = mutate {
        val paths = exportJournal()
        _state.update { it.copy(exportPaths = paths) }
    }
    fun clearError() { _state.update { it.copy(error = null) } }
    fun consumeExport() { _state.update { it.copy(exportPaths = emptyList()) } }
    fun consumeCompletedDay() { _state.update { it.copy(completedDayId = null) } }
    fun shareFailed() { _state.update { it.copy(error = "Не удалось открыть передачу файлов. Попробуйте ещё раз.") } }

    private fun mutate(action: suspend () -> Unit) {
        if (_state.value.saving) return
        _state.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            try {
                action()
                _state.update { it.copy(changeVersion = it.changeVersion + 1) }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _state.update { it.copy(error = error.message?.takeIf { message -> message.length < 160 }
                    ?: "Не удалось сохранить. Попробуйте ещё раз.") }
            } finally {
                _state.update { it.copy(saving = false) }
            }
        }
    }
}
