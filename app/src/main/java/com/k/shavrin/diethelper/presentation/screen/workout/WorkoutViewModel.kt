package com.k.shavrin.diethelper.presentation.screen.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.k.shavrin.diethelper.domain.model.WorkoutDay
import com.k.shavrin.diethelper.domain.model.WorkoutPlan
import com.k.shavrin.diethelper.domain.model.WorkoutProgress
import com.k.shavrin.diethelper.domain.usecase.workout.CompleteWorkoutUseCase
import com.k.shavrin.diethelper.domain.usecase.workout.GetWorkoutPlanUseCase
import com.k.shavrin.diethelper.domain.usecase.workout.GetWorkoutProgressUseCase
import com.k.shavrin.diethelper.domain.usecase.workout.RecordWorkoutSetUseCase
import com.k.shavrin.diethelper.domain.usecase.workout.SaveWorkoutMaximaUseCase
import com.k.shavrin.diethelper.domain.usecase.workout.SetRestTimerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WorkoutUiState(
    val plan: WorkoutPlan? = null,
    val progress: WorkoutProgress = WorkoutProgress(),
    val selectedDay: WorkoutDay? = null,
    val loading: Boolean = true,
    val saving: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val getPlan: GetWorkoutPlanUseCase,
    getProgress: GetWorkoutProgressUseCase,
    private val saveMaxima: SaveWorkoutMaximaUseCase,
    private val recordSet: RecordWorkoutSetUseCase,
    private val completeWorkout: CompleteWorkoutUseCase,
    private val setTimer: SetRestTimerUseCase
) : ViewModel() {
    private val plan = MutableStateFlow<WorkoutPlan?>(null)
    private val selectedId = MutableStateFlow<Int?>(null)
    private val _state = MutableStateFlow(WorkoutUiState())
    val state: StateFlow<WorkoutUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                plan.value = getPlan()
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                _state.value = _state.value.copy(loading = false, error = "Не удалось открыть тренировочный план")
            }
        }
        viewModelScope.launch {
            combine(plan, getProgress(), selectedId) { program, progress, selected ->
                val unfinished = program?.days?.firstOrNull { day ->
                    day.id !in progress.completedDays && day.sets().any { it.key in progress.completedSets }
                }
                val next = unfinished ?: program?.days?.firstOrNull { it.id !in progress.completedDays }
                Triple(program, progress, program?.days?.find { it.id == selected } ?: next)
            }.collect { (program, progress, selected) ->
                _state.value = _state.value.copy(plan = program, progress = progress, selectedDay = selected, loading = program == null)
            }
        }
    }

    fun selectDay(day: WorkoutDay) { selectedId.value = day.id }
    fun clearError() { _state.value = _state.value.copy(error = null) }
    fun saveMaximums(maxima: Map<String, Double>) = mutate { saveMaxima(maxima) }

    fun toggleSet(day: WorkoutDay, key: String) = mutate {
        recordSet(day, key, key !in _state.value.progress.completedSets, System.currentTimeMillis())
    }

    fun finish(day: WorkoutDay) = mutate {
        completeWorkout(day)
        selectedId.value = null
    }

    fun timer(seconds: Int, deadline: Long = 0, paused: Int = 0) = mutate { setTimer(seconds, deadline, paused) }

    private fun mutate(action: suspend () -> Unit) {
        if (_state.value.saving) return
        _state.value = _state.value.copy(saving = true, error = null)
        viewModelScope.launch {
            try {
                action()
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                _state.value = _state.value.copy(error = "Не удалось сохранить изменения. Попробуйте ещё раз.")
            } finally {
                _state.value = _state.value.copy(saving = false)
            }
        }
    }
}
