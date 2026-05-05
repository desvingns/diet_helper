package com.k.shavrin.diethelper.presentation.screen.today

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.material3.SelectableDates
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.k.shavrin.diethelper.domain.model.DayStatus
import com.k.shavrin.diethelper.domain.model.FoodEntry
import com.k.shavrin.diethelper.domain.model.MealType
import com.k.shavrin.diethelper.presentation.components.DailySummaryCard
import com.k.shavrin.diethelper.presentation.util.formatCalories
import com.k.shavrin.diethelper.presentation.util.formatGrams
import com.k.shavrin.diethelper.presentation.util.formatIsoDate
import com.k.shavrin.diethelper.presentation.util.formatMacro
import com.k.shavrin.diethelper.presentation.util.formatWeekDateHeader
import com.k.shavrin.diethelper.presentation.util.mealTypeLabel
import java.time.DayOfWeek
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
            onGoToDate = viewModel::goToDate,
            onTodayClick = viewModel::goToToday,
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
    onGoToDate: (LocalDate) -> Unit,
    onTodayClick: () -> Unit,
    onAddTo: (MealType) -> Unit,
    onUpdateMultiplier: (FoodEntry, Float) -> Unit,
    onDelete: (FoodEntry) -> Unit,
    onCopyToDay: (FoodEntry, LocalDate) -> Unit,
    readOnly: Boolean
) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (!readOnly) {
            WeekDateHeader(
                date = state.date,
                weekStatuses = state.weekStatuses,
                streak = state.streak,
                onDateSelected = onGoToDate,
                onTodayClick = onTodayClick
            )
        }

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
                val sectionProtein = state.sectionProtein[mealType] ?: 0f
                val sectionFat = state.sectionFat[mealType] ?: 0f
                val sectionCarbs = state.sectionCarbs[mealType] ?: 0f

                item(key = "header_${mealType.name}") {
                    SectionHeader(
                        mealType = mealType,
                        sectionCalories = sectionCalories,
                        sectionProtein = sectionProtein,
                        sectionFat = sectionFat,
                        sectionCarbs = sectionCarbs,
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

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
internal fun WeekDateHeader(
    date: LocalDate,
    weekStatuses: List<Pair<LocalDate, DayStatus>>,
    streak: Int,
    onDateSelected: (LocalDate) -> Unit,
    onTodayClick: () -> Unit
) {
    val today = LocalDate.now()
    var showDatePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Top row: date title + "Сегодня" shortcut
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatWeekDateHeader(date),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.clickable { showDatePicker = true }
            )
            Text(
                text = "Сегодня",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier.clickable { onTodayClick() }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Week row: 7 circles Mon–Sun
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            weekStatuses.forEach { (day, status) ->
                WeekDayCircle(
                    day = day,
                    status = status,
                    isSelected = day == date,
                    isToday = day == today,
                    onClick = { onDateSelected(day) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Streak row
        val streakLabel = if (streak == 0) {
            "Запишите еду, чтобы начать серию"
        } else {
            "$streak дней подряд"
        }
        Text(
            text = "$streak 🔥 $streakLabel",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    if (showDatePicker) {
        val todayMillis = today.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val selectedMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedMillis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis <= todayMillis
                }
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val ms = datePickerState.selectedDateMillis
                    if (ms != null) {
                        val selected = Instant.ofEpochMilli(ms).atZone(ZoneOffset.UTC).toLocalDate()
                        onDateSelected(selected)
                    }
                    showDatePicker = false
                }) { Text("Выбрать") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Отмена") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

private val weekDayLetters = mapOf(
    DayOfWeek.MONDAY to "П",
    DayOfWeek.TUESDAY to "В",
    DayOfWeek.WEDNESDAY to "С",
    DayOfWeek.THURSDAY to "Ч",
    DayOfWeek.FRIDAY to "Р",
    DayOfWeek.SATURDAY to "С",
    DayOfWeek.SUNDAY to "В"
)

private fun dayStatusCircleColor(status: DayStatus): Color = when (status) {
    DayStatus.GREEN -> Color(0xFF4CAF50)
    DayStatus.YELLOW -> Color(0xFFFFC107)
    DayStatus.RED -> Color(0xFFF44336)
    DayStatus.GRAY_LOGGED -> Color(0xFF9E9E9E)
    DayStatus.FUTURE -> Color.Transparent
}

@Composable
private fun WeekDayCircle(
    day: LocalDate,
    status: DayStatus,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit
) {
    val letter = weekDayLetters[day.dayOfWeek] ?: "?"
    val circleColor = dayStatusCircleColor(status)
    val ringColor = if (isToday) Color(0xFF4CAF50) else Color.White
    val isFuture = status == DayStatus.FUTURE

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .then(
                    if (isFuture) {
                        Modifier
                            .background(Color.Transparent)
                            .border(1.dp, Color(0xFF9E9E9E), CircleShape)
                    } else {
                        Modifier.background(circleColor)
                    }
                )
                .then(
                    if (isSelected) {
                        Modifier.border(2.dp, ringColor, CircleShape)
                    } else {
                        Modifier
                    }
                )
                .clickable(onClick = onClick)
        ) {
            when (status) {
                DayStatus.GREEN -> Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                DayStatus.YELLOW -> Text(
                    text = "!",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                DayStatus.RED -> Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                DayStatus.GRAY_LOGGED -> Unit
                DayStatus.FUTURE -> Unit
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = letter,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SectionHeader(
    mealType: MealType,
    sectionCalories: Float,
    sectionProtein: Float,
    sectionFat: Float,
    sectionCarbs: Float,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatCalories(sectionCalories),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Б ${formatMacro(sectionProtein)} • " +
                            "Ж ${formatMacro(sectionFat)} • " +
                            "У ${formatMacro(sectionCarbs)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (showAdd) {
            IconButton(onClick = onAdd) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Добавить",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(28.dp)
                )
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
