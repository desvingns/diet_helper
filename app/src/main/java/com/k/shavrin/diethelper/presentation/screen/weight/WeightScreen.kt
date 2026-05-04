package com.k.shavrin.diethelper.presentation.screen.weight

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.k.shavrin.diethelper.domain.model.WeightEntry
import com.k.shavrin.diethelper.presentation.screen.today.ErrorState
import com.k.shavrin.diethelper.presentation.screen.today.LoadingState
import com.k.shavrin.diethelper.presentation.theme.DeltaNegative
import com.k.shavrin.diethelper.presentation.theme.DeltaPositive
import com.k.shavrin.diethelper.presentation.util.formatDate
import com.k.shavrin.diethelper.presentation.util.formatWeight
import kotlin.math.absoluteValue

@Composable
fun WeightScreen(
    viewModel: WeightViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val input by viewModel.input.collectAsState()

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = viewModel::onInputChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Вес в кг") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            Button(onClick = viewModel::save) { Text("Сохранить") }
        }

        Box(modifier = Modifier
            .weight(1f)
            .fillMaxWidth()) {
            when (val s = state) {
                WeightUiState.Loading -> LoadingState()
                is WeightUiState.Error -> ErrorState(s.message)
                is WeightUiState.Success -> WeightList(
                    items = s.items,
                    onDelete = viewModel::delete
                )
            }
        }
    }
}

@Composable
private fun WeightList(
    items: List<WeightEntryWithDelta>,
    onDelete: (WeightEntry) -> Unit
) {
    if (items.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(imageVector = Icons.Filled.MonitorWeight, contentDescription = null)
                Text("Записей нет", style = MaterialTheme.typography.titleMedium)
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(items, key = { it.entry.id }) { item ->
            WeightRow(item = item, onDelete = { onDelete(item.entry) })
            HorizontalDivider()
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WeightRow(
    item: WeightEntryWithDelta,
    onDelete: () -> Unit
) {
    var showDelete by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {},
                onLongClick = { showDelete = true }
            )
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = formatDate(item.entry.date),
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = formatWeight(item.entry.weightKg),
            style = MaterialTheme.typography.bodyLarge
        )
        DeltaText(deltaKg = item.deltaKg)
    }

    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text("Удалить запись?") },
            text = { Text("Запись от ${formatDate(item.entry.date)} будет удалена.") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDelete = false
                }) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = { showDelete = false }) { Text("Отмена") }
            }
        )
    }
}

@Composable
private fun DeltaText(deltaKg: Float?) {
    when {
        deltaKg == null -> Text(
            text = "—",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        deltaKg == 0f -> Text(
            text = "0 кг",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        deltaKg > 0f -> Text(
            text = "+${formatWeight(deltaKg)}",
            style = MaterialTheme.typography.bodyMedium,
            color = DeltaPositive
        )
        else -> Text(
            text = "−${formatWeight(deltaKg.absoluteValue)}",
            style = MaterialTheme.typography.bodyMedium,
            color = DeltaNegative
        )
    }
}
