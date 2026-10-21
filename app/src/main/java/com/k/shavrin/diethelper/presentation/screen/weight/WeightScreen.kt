package com.k.shavrin.diethelper.presentation.screen.weight

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.k.shavrin.diethelper.presentation.screen.today.ErrorState
import com.k.shavrin.diethelper.presentation.screen.today.LoadingState
import com.k.shavrin.diethelper.presentation.theme.DeltaNegative
import com.k.shavrin.diethelper.presentation.theme.DeltaPositive
import com.k.shavrin.diethelper.presentation.util.formatDate
import com.k.shavrin.diethelper.presentation.util.formatWeight
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.math.absoluteValue

@Composable
fun WeightScreen(
    viewModel: WeightViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val input by viewModel.input.collectAsState()
    val inputError by viewModel.inputError.collectAsState()
    val saved by viewModel.saved.collectAsState()
    val saving by viewModel.saving.collectAsState()
    val date by viewModel.selectedDate.collectAsState()
    val editing by viewModel.editing.collectAsState()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val keyboard = LocalSoftwareKeyboardController.current
    val onSave = {
        viewModel.save()
        keyboard?.hide()
        Unit
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            WeightEntryForm(input, inputError, saved, saving, viewModel::onInputChange, onSave,
                date, editing != null, viewModel::selectDate, viewModel::cancelEdit)
        }
        when (val s = state) {
            WeightUiState.Loading -> item { LoadingState() }
            is WeightUiState.Error -> item { ErrorState(s.message) }
            is WeightUiState.Success -> {
                if (s.items.isEmpty()) {
                    item {
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Filled.MonitorWeight, null, tint = MaterialTheme.colorScheme.primary)
                                Text("Начните с одного взвешивания", style = MaterialTheme.typography.titleMedium)
                                Text("Запишите вес выше. Здесь появятся история и график вашей динамики.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                } else {
                    item { WeightTrendCard(s.items.map { it.entry }) }
                    item { Text("История взвешиваний", style = MaterialTheme.typography.titleMedium) }
                    items(s.items, key = { it.entry.id }) { weight ->
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(horizontal = 16.dp)) {
                                WeightRow(weight, onDelete = { viewModel.delete(weight.entry) }, onEdit = {
                                    viewModel.edit(weight.entry)
                                    scope.launch { listState.animateScrollToItem(0) }
                                })
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("LongParameterList")
@Composable
private fun WeightEntryForm(
    input: String,
    error: String?,
    saved: Boolean,
    saving: Boolean,
    onInput: (String) -> Unit,
    onSave: () -> Unit,
    date: LocalDate,
    editing: Boolean,
    onDate: (LocalDate) -> Unit,
    onCancel: () -> Unit
) {
    var choosingDate by remember { mutableStateOf(false) }
    Card {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(if (editing) "Редактирование взвешивания" else "Записать взвешивание", style = MaterialTheme.typography.titleMedium)
            Text(if (editing) "Измените вес или дату записи" else "Выберите дату. Повторное сохранение обновит вес за выбранный день.", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedButton(onClick = { choosingDate = true }, enabled = !saving, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.DateRange, null)
                Text("Дата: ${formatDate(date)}", Modifier.padding(start = 8.dp))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = onInput,
                    modifier = Modifier.weight(1f),
                    label = { Text("Вес в кг") },
                    placeholder = { Text("75,5") },
                    isError = error != null,
                    enabled = !saving,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { onSave() })
                )
                Button(onClick = onSave, enabled = !saving) { Text(if (saving) "Сохраняем…" else "Сохранить") }
            }
            if (error != null) Text(error, color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall)
            if (saved) Text("Вес за ${formatDate(date)} сохранён", color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall)
            if (editing) TextButton(onClick = onCancel, enabled = !saving) { Text("Закончить редактирование") }
        }
    }
    if (choosingDate) {
        val picker = rememberDatePickerState(initialSelectedDateMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
        DatePickerDialog(onDismissRequest = { choosingDate = false }, confirmButton = {
            TextButton(enabled = picker.selectedDateMillis != null, onClick = {
                picker.selectedDateMillis?.let { onDate(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()) }
                choosingDate = false
            }) { Text("Выбрать") }
        }, dismissButton = { TextButton(onClick = { choosingDate = false }) { Text("Отмена") } }) {
            DatePicker(state = picker)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WeightRow(
    item: WeightEntryWithDelta,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    var showDelete by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onEdit,
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
        IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, "Редактировать вес и дату") }
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
