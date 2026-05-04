package com.k.shavrin.diethelper.presentation.screen.today

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import com.k.shavrin.diethelper.domain.model.FoodEntry
import com.k.shavrin.diethelper.domain.model.MealType
import com.k.shavrin.diethelper.presentation.components.DailySummaryCard
import com.k.shavrin.diethelper.presentation.util.formatCalories
import com.k.shavrin.diethelper.presentation.util.formatDate
import com.k.shavrin.diethelper.presentation.util.formatGrams
import com.k.shavrin.diethelper.presentation.util.formatIsoDate
import com.k.shavrin.diethelper.presentation.util.formatMacro
import com.k.shavrin.diethelper.presentation.util.mealTypeLabel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.math.roundToInt

@Composable
fun TodayScreen(
    onNavigateToProductSearch: (date: String, mealType: String) -> Unit,
    viewModel: TodayViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    when (val s = state) {
        TodayUiState.Loading -> LoadingState()
        is TodayUiState.Error -> ErrorState(s.message)
        is TodayUiState.Success -> TodayContent(
            state = s,
            onPreviousDay = viewModel::goToPreviousDay,
            onNextDay = viewModel::goToNextDay,
            onAddTo = { mealType ->
                onNavigateToProductSearch(formatIsoDate(s.date), mealType.name)
            },
            onUpdateMultiplier = viewModel::updateMultiplier,
            onDelete = viewModel::deleteEntry,
            onCopyToDay = viewModel::copyEntryToDay,
            readOnly = false
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun TodayContent(
    state: TodayUiState.Success,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onAddTo: (MealType) -> Unit,
    onUpdateMultiplier: (FoodEntry, Float) -> Unit,
    onDelete: (FoodEntry) -> Unit,
    onCopyToDay: (FoodEntry, LocalDate) -> Unit,
    readOnly: Boolean
) {
    Column(modifier = Modifier.fillMaxSize()) {
        DateHeader(
            date = state.date,
            canGoForward = state.canGoForward,
            onPrevious = onPreviousDay,
            onNext = onNextDay,
            visibleNavigation = !readOnly
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = 16.dp,
                vertical = 8.dp
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MealType.values().forEach { mealType ->
                val entries = state.sections[mealType].orEmpty()
                val sectionCalories = state.sectionCalories[mealType] ?: 0f

                item(key = "header_${mealType.name}") {
                    SectionHeader(
                        mealType = mealType,
                        sectionCalories = sectionCalories,
                        showAdd = !readOnly,
                        onAdd = { onAddTo(mealType) }
                    )
                }

                if (entries.isEmpty()) {
                    item(key = "empty_${mealType.name}") {
                        Text(
                            text = "Пока пусто",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp, bottom = 4.dp)
                        )
                    }
                } else {
                    items(entries, key = { "entry_${it.id}" }) { entry ->
                        FoodEntryRow(
                            entry = entry,
                            readOnly = readOnly,
                            onUpdateMultiplier = { newMul -> onUpdateMultiplier(entry, newMul) },
                            onDelete = { onDelete(entry) },
                            onCopyToDay = { date -> onCopyToDay(entry, date) }
                        )
                    }
                }
                item(key = "divider_${mealType.name}") {
                    HorizontalDivider()
                }
            }

            item(key = "summary") {
                Spacer(modifier = Modifier.height(8.dp))
                DailySummaryCard(summary = state.summary, goals = state.goals)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun DateHeader(
    date: LocalDate,
    canGoForward: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    visibleNavigation: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (visibleNavigation) {
            IconButton(onClick = onPrevious) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Предыдущий день")
            }
        } else {
            Spacer(modifier = Modifier.height(48.dp))
        }
        Text(
            text = formatDate(date),
            style = MaterialTheme.typography.titleLarge
        )
        if (visibleNavigation) {
            IconButton(onClick = onNext, enabled = canGoForward) {
                Icon(Icons.Filled.ArrowForward, contentDescription = "Следующий день")
            }
        } else {
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
private fun SectionHeader(
    mealType: MealType,
    sectionCalories: Float,
    showAdd: Boolean,
    onAdd: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = mealTypeLabel(mealType),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = formatCalories(sectionCalories),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (showAdd) {
            IconButton(onClick = onAdd) {
                Icon(Icons.Filled.Add, contentDescription = "Добавить")
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FoodEntryRow(
    entry: FoodEntry,
    readOnly: Boolean,
    onUpdateMultiplier: (Float) -> Unit,
    onDelete: () -> Unit,
    onCopyToDay: (LocalDate) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showCopyDialog by remember { mutableStateOf(false) }

    val rowModifier = if (readOnly) {
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    } else {
        Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { menuExpanded = true },
                onLongClick = { menuExpanded = true }
            )
            .padding(vertical = 4.dp)
    }

    Box(modifier = rowModifier) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = entry.product.name,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = formatGrams(entry.multiplier),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatCalories(entry.product.caloriesPer100g * entry.multiplier),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Б ${formatMacro(entry.product.proteinPer100g * entry.multiplier)} • " +
                            "Ж ${formatMacro(entry.product.fatPer100g * entry.multiplier)} • " +
                            "У ${formatMacro(entry.product.carbsPer100g * entry.multiplier)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Изменить граммы") },
                onClick = {
                    menuExpanded = false
                    showEditDialog = true
                }
            )
            DropdownMenuItem(
                text = { Text("Удалить") },
                onClick = {
                    menuExpanded = false
                    showDeleteDialog = true
                }
            )
            DropdownMenuItem(
                text = { Text("Скопировать на другой день") },
                onClick = {
                    menuExpanded = false
                    showCopyDialog = true
                }
            )
        }
    }

    if (showEditDialog) {
        EditGramsDialog(
            initialGrams = (entry.multiplier * 100f).roundToInt(),
            onDismiss = { showEditDialog = false },
            onConfirm = { grams ->
                onUpdateMultiplier(grams / 100f)
                showEditDialog = false
            }
        )
    }
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удалить запись?") },
            text = { Text("Запись «${entry.product.name}» будет удалена.") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteDialog = false
                }) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Отмена") }
            }
        )
    }
    if (showCopyDialog) {
        CopyToDateDialog(
            initialDate = entry.date,
            onDismiss = { showCopyDialog = false },
            onConfirm = { date ->
                onCopyToDay(date)
                showCopyDialog = false
            }
        )
    }
}

@Composable
private fun EditGramsDialog(
    initialGrams: Int,
    onDismiss: () -> Unit,
    onConfirm: (Float) -> Unit
) {
    var text by remember { mutableStateOf(initialGrams.toString()) }
    val parsed = text.toFloatOrNull()
    val isValid = parsed != null && parsed > 0f

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Изменить граммы") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Граммы") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = !isValid,
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = { parsed?.let(onConfirm) },
                enabled = isValid
            ) { Text("Сохранить") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun CopyToDateDialog(
    initialDate: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit
) {
    val initMillis = initialDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initMillis)

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val ms = datePickerState.selectedDateMillis
                if (ms != null) {
                    val date = Instant.ofEpochMilli(ms).atZone(ZoneOffset.UTC).toLocalDate()
                    onConfirm(date)
                }
            }) { Text("Скопировать") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@Composable
internal fun LoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
internal fun ErrorState(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = message, style = MaterialTheme.typography.bodyLarge)
    }
}
