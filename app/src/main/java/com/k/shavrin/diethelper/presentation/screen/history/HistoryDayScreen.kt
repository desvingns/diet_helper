package com.k.shavrin.diethelper.presentation.screen.history

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.k.shavrin.diethelper.presentation.screen.today.ErrorState
import com.k.shavrin.diethelper.presentation.screen.today.LoadingState
import com.k.shavrin.diethelper.presentation.screen.today.TodayContent
import com.k.shavrin.diethelper.presentation.screen.today.TodayUiState
import com.k.shavrin.diethelper.presentation.util.formatDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryDayScreen(
    date: String,
    onNavigateBack: () -> Unit,
    viewModel: HistoryDayViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val titleText = (state as? TodayUiState.Success)?.let { formatDate(it.date) }
                        ?: "История"
                    Text(titleText)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {
            when (val s = state) {
                TodayUiState.Loading -> LoadingState()
                is TodayUiState.Error -> ErrorState(s.message)
                is TodayUiState.Success -> TodayContent(
                    state = s,
                    onGoToDate = {},
                    onTodayClick = {},
                    onAddTo = {},
                    onUpdateMultiplier = { _, _ -> },
                    onDelete = {},
                    onCopyToDay = { _, _ -> },
                    readOnly = true
                )
            }
        }
    }
}
