package com.k.shavrin.diethelper.presentation.screen.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.k.shavrin.diethelper.domain.usecase.foodentry.GetHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    getHistoryUseCase: GetHistoryUseCase
) : ViewModel() {

    val uiState: StateFlow<HistoryUiState> = getHistoryUseCase()
        .map { items -> HistoryUiState.Success(items) as HistoryUiState }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = HistoryUiState.Loading
        )
}
