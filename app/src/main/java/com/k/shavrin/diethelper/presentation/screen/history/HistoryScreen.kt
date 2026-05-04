package com.k.shavrin.diethelper.presentation.screen.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.k.shavrin.diethelper.domain.model.HistoryItem
import com.k.shavrin.diethelper.presentation.screen.today.ErrorState
import com.k.shavrin.diethelper.presentation.screen.today.LoadingState
import com.k.shavrin.diethelper.presentation.util.formatCalories
import com.k.shavrin.diethelper.presentation.util.formatDate
import com.k.shavrin.diethelper.presentation.util.formatIsoDate

@Composable
fun HistoryScreen(
    onNavigateToDay: (date: String) -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    when (val s = state) {
        HistoryUiState.Loading -> LoadingState()
        is HistoryUiState.Error -> ErrorState(s.message)
        is HistoryUiState.Success -> {
            if (s.items.isEmpty()) {
                EmptyHistoryState()
            } else {
                HistoryList(items = s.items, onItemClick = { item ->
                    onNavigateToDay(formatIsoDate(item.date))
                })
            }
        }
    }
}

@Composable
private fun HistoryList(items: List<HistoryItem>, onItemClick: (HistoryItem) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        items(items, key = { it.date.toEpochDay() }) { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onItemClick(item) }
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatDate(item.date),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = formatCalories(item.totalCalories),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            HorizontalDivider()
        }
    }
}

@Composable
private fun EmptyHistoryState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.History,
                contentDescription = null
            )
            Text(text = "История пуста", style = MaterialTheme.typography.titleMedium)
        }
    }
}
