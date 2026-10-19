@file:Suppress("TooManyFunctions", "LongMethod")

package com.k.shavrin.diethelper.presentation.screen.workout

import android.app.Activity
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarViewWeek
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.k.shavrin.diethelper.domain.model.WorkoutDay
import com.k.shavrin.diethelper.domain.model.WorkoutExercise
import com.k.shavrin.diethelper.domain.model.WorkoutProgress
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.ceil

private val Mint = Color(0xFFB8F3D4)
private val Lavender = Color(0xFFCEC1FF)

@Composable
fun WorkoutScreen(viewModel: WorkoutViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    WorkoutContent(state, viewModel::selectDay, viewModel::toggleSet, viewModel::finish,
        viewModel::saveMaximums, viewModel::timer, viewModel::clearError)
}

@Composable
fun WorkoutContent(
    state: WorkoutUiState,
    onSelect: (WorkoutDay) -> Unit,
    onSet: (WorkoutDay, String) -> Unit,
    onFinish: (WorkoutDay) -> Unit,
    onMaxima: (Map<String, Double>) -> Unit,
    onTimer: (Int, Long, Int) -> Unit,
    onClearError: () -> Unit
) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var showMaxima by rememberSaveable { mutableStateOf(false) }
    var cycle by rememberSaveable { mutableIntStateOf(1) }
    val day = state.selectedDay
    val progress = state.progress
    val activity = LocalContext.current as? Activity
    DisposableEffect(activity, tab, day?.id, progress.configured) {
        if (tab == 0 && day != null && progress.configured) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose { activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
    }
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("ДВИЖЕНИЕ", color = Mint, style = MaterialTheme.typography.labelMedium, letterSpacing = 3.sp)
                Text("Тренировки", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                Text("СРЦ7 · программа из вашей таблицы", color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall)
            }
            FilledTonalIconButton(onClick = { showMaxima = true }) {
                Icon(Icons.Default.Tune, contentDescription = "Рабочие максимумы")
            }
        }
        TabRow(selectedTabIndex = tab, containerColor = Color.Transparent) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Тренировка") }, icon = {
                Icon(Icons.Default.FitnessCenter, null, Modifier.size(20.dp))
            })
            Tab(selected = tab == 1, onClick = {
                cycle = day?.cycle ?: cycle
                tab = 1
            }, text = { Text("Весь план") }, icon = { Icon(Icons.Default.CalendarViewWeek, null, Modifier.size(20.dp)) })
        }
        state.error?.let { error ->
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(error, Modifier.weight(1f), color = MaterialTheme.colorScheme.error)
                TextButton(onClick = onClearError) { Text("Закрыть") }
            }
        }
        if (state.loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else if (tab == 0) {
            if (day == null) {
                Column(Modifier.padding(28.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Icon(Icons.Default.EmojiEvents, null, Modifier.size(56.dp), tint = Mint)
                    Text("Программа завершена", style = MaterialTheme.typography.headlineMedium)
                    Text("Все тренировки сохранены. Весь пройденный план доступен во второй вкладке.")
                    Button(onClick = { tab = 1 }) { Text("Посмотреть план") }
                }
            } else {
                SessionContent(day, state, onSet, onFinish, onTimer, { showMaxima = true })
            }
        } else {
            val plan = state.plan
            LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                item {
                    Text("${plan?.days?.size ?: 0} тренировок · ${plan?.days?.maxOfOrNull { it.cycle } ?: 0} микроциклов", style = MaterialTheme.typography.titleMedium)
                    Text("Веса пересчитываются от ваших максимумов. Прогрессия и округление сохранены из таблицы.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    Row(Modifier.horizontalScroll(rememberScrollState()).padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        (plan?.days?.map { it.cycle }?.distinct().orEmpty()).forEach { number ->
                            FilterChip(selected = cycle == number, onClick = { cycle = number }, label = { Text("Цикл $number") })
                        }
                    }
                    Text("В таблице заполнены 57 тренировок. Пустые заготовки циклов 13–25 не добавлены.",
                        style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                items(plan?.days.orEmpty().filter { it.cycle == cycle }, key = { it.id }) { plannedDay ->
                    PlanDayCard(plannedDay, progress, onOpen = { onSelect(plannedDay); tab = 0 })
                }
            }
        }
    }
    if (showMaxima) {
        MaximaDialog(progress, onDismiss = { showMaxima = false }, onSave = { onMaxima(it); showMaxima = false })
    }
}

@Composable
private fun SessionContent(
    day: WorkoutDay,
    state: WorkoutUiState,
    onSet: (WorkoutDay, String) -> Unit,
    onFinish: (WorkoutDay) -> Unit,
    onTimer: (Int, Long, Int) -> Unit,
    onConfigure: () -> Unit
) {
    val progress = state.progress
    val sets = remember(day) { day.sets() }
    val completed = sets.count { it.key in progress.completedSets }
    val next = sets.firstOrNull { it.key !in progress.completedSets }
    val finished = day.id in progress.completedDays
    val editable = state.canEdit(day)
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("МИКРОЦИКЛ ${day.cycle}  /  ДЕНЬ ${day.day}", color = Lavender, style = MaterialTheme.typography.labelMedium)
                Text("$completed / ${sets.size} подходов", style = MaterialTheme.typography.labelMedium)
            }
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(progress = { completed.toFloat() / sets.size.coerceAtLeast(1) },
                modifier = Modifier.fillMaxWidth().height(5.dp), color = Mint)
        }
        if (!progress.configured) {
            item {
                Card(shape = RoundedCornerShape(24.dp)) {
                    Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Default.FitnessCenter, null, tint = Mint)
                        Text("Ваши максимумы — ваш план", style = MaterialTheme.typography.headlineSmall)
                        Text("Укажите актуальные максимумы в приседе, жиме и тяге. Рассчитаем каждый подход по формулам вашей программы.")
                        Button(onClick = onConfigure, modifier = Modifier.fillMaxWidth()) { Text("Указать максимумы") }
                    }
                }
            }
        } else if (next != null && !finished) {
            item {
                Column(Modifier.fillMaxWidth().background(
                    Brush.linearGradient(listOf(Color(0xFF35463F), Color(0xFF222A35))), RoundedCornerShape(28.dp)
                ).padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(if (completed == 0) "ПЕРВЫЙ ПОДХОД" else "СЛЕДУЮЩИЙ ПОДХОД", color = Mint,
                        style = MaterialTheme.typography.labelMedium, letterSpacing = 2.sp)
                    Text(next.exercise.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(weightText(next.exercise.weight(progress.maxima, next.group.percent)),
                            fontSize = 58.sp, fontWeight = FontWeight.Bold, color = Mint)
                        Text("кг × ${next.group.reps}", fontSize = 24.sp, modifier = Modifier.padding(bottom = 9.dp))
                    }
                    Text("${percentText(next.group.percent)} от ПМ · ${next.exercise.load.lowercase()} нагрузка" +
                        if (next.exercise.equipmentMultiplier == 2.0) " · вес одной гантели" else "",
                        color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    Button(onClick = { onSet(day, next.key) }, enabled = !state.saving,
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Mint, contentColor = Color(0xFF17251D))) {
                        Icon(Icons.Default.Check, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Подход выполнен", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        if (progress.configured && !finished) {
            item { RestTimer(progress, onTimer) }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Упражнения", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("${day.exercises.size} в плане", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        items(day.exercises, key = { it.id }) { exercise ->
            var expanded by rememberSaveable(day.id, exercise.id) { mutableStateOf(false) }
            val exerciseSets = sets.filter { it.exercise.id == exercise.id }
            val count = exerciseSets.count { it.key in progress.completedSets }
            Card(shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { expanded = !expanded }, contentPadding = PaddingValues(0.dp)) {
                        Column(Modifier.weight(1f)) {
                            Text(exercise.name, color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.titleMedium)
                            Text("$count / ${exerciseSets.size} · ${exercise.load}", color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall)
                        }
                        Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, "Раскрыть подходы")
                    }
                    if (expanded || next?.exercise?.id == exercise.id) {
                        exerciseSets.forEachIndexed { index, set ->
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text("${index + 1}", Modifier.width(30.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(if (progress.configured) "${weightText(exercise.weight(progress.maxima, set.group.percent))} кг × ${set.group.reps}"
                                    else "— кг × ${set.group.reps}", Modifier.weight(1f), fontWeight = FontWeight.Medium)
                                Text(percentText(set.group.percent), style = MaterialTheme.typography.labelMedium, color = Lavender)
                                Checkbox(checked = set.key in progress.completedSets,
                                    onCheckedChange = { onSet(day, set.key) }, enabled = editable)
                            }
                        }
                    } else {
                        ExerciseGroups(exercise, progress)
                    }
                }
            }
        }
        if (day.notes.isNotBlank()) {
            item {
                Text("Дополнительно", style = MaterialTheme.typography.titleSmall, color = Lavender)
                Text(day.notes, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            Button(onClick = { onFinish(day) }, enabled = next == null && !finished && !state.saving,
                modifier = Modifier.fillMaxWidth().height(54.dp)) {
                Text(if (finished) "Тренировка сохранена ✓" else "Завершить тренировку")
            }
            if (!finished && next != null) Text("Отметьте подходы, чтобы перейти к следующей тренировке.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
private fun RestTimer(progress: WorkoutProgress, onTimer: (Int, Long, Int) -> Unit) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val haptic = LocalHapticFeedback.current
    LaunchedEffect(progress.restDeadline) {
        now = System.currentTimeMillis()
        val wasRunning = progress.restDeadline > now
        while (progress.restDeadline > now) {
            delay(250)
            now = System.currentTimeMillis()
        }
        if (wasRunning) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }
    val remaining = if (progress.restDeadline > now) ceil((progress.restDeadline - now) / 1000.0).toInt()
        else progress.pausedRestSeconds
    val running = progress.restDeadline > now
    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF272332))) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Timer, null, tint = Lavender)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("Отдых между подходами", style = MaterialTheme.typography.titleSmall)
                    Text(if (remaining > 0) String.format(Locale.ROOT, "%02d:%02d", remaining / 60, remaining % 60)
                        else if (progress.restDeadline != 0L) "Можно продолжать" else "${progress.restSeconds / 60} мин ${progress.restSeconds % 60} сек",
                        style = MaterialTheme.typography.titleLarge, color = Lavender)
                }
                IconButton(onClick = {
                    if (running) onTimer(progress.restSeconds, 0, remaining)
                    else onTimer(progress.restSeconds, System.currentTimeMillis() +
                        (if (remaining > 0) remaining else progress.restSeconds) * 1000L, 0)
                }) { Icon(if (running) Icons.Default.Pause else Icons.Default.PlayArrow, if (running) "Пауза" else "Запустить таймер") }
                IconButton(onClick = { onTimer(progress.restSeconds, 0, 0) }) { Icon(Icons.Default.SkipNext, "Пропустить отдых") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(60, 90, 120, 180).forEach { seconds ->
                    FilterChip(selected = progress.restSeconds == seconds,
                        onClick = { onTimer(seconds, if (running) System.currentTimeMillis() + seconds * 1000L else 0, 0) },
                        label = { Text("${seconds}с") })
                }
            }
        }
    }
}

@Composable
private fun PlanDayCard(day: WorkoutDay, progress: WorkoutProgress, onOpen: () -> Unit) {
    Card(shape = RoundedCornerShape(24.dp)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("День ${day.day}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                if (day.id in progress.completedDays) Icon(Icons.Default.CheckCircle, "Выполнена", tint = Mint)
                else Text("${day.sets().size} подходов", color = Lavender, style = MaterialTheme.typography.labelMedium)
            }
            day.exercises.forEach { exercise ->
                Text(exercise.name, style = MaterialTheme.typography.titleSmall)
                ExerciseGroups(exercise, progress)
            }
            OutlinedButton(onClick = onOpen, modifier = Modifier.fillMaxWidth()) { Text("Открыть тренировку") }
        }
    }
}

@Composable
private fun ExerciseGroups(exercise: WorkoutExercise, progress: WorkoutProgress) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        exercise.groups.forEach { group ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(if (progress.configured) "${weightText(exercise.weight(progress.maxima, group.percent))} кг"
                    else "— кг", fontWeight = FontWeight.Medium)
                Text("${group.sets} × ${group.reps}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(percentText(group.percent), color = Lavender, style = MaterialTheme.typography.labelMedium)
            }
        }
        if (exercise.equipmentMultiplier == 2.0) {
            Text("Вес одной гантели", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun MaximaDialog(progress: WorkoutProgress, onDismiss: () -> Unit, onSave: (Map<String, Double>) -> Unit) {
    val values = remember { mutableStateMapOf<String, String>().apply {
        listOf("squat", "bench", "deadlift").forEach { key -> put(key, progress.maxima[key]?.takeIf { it > 0 }?.let(::weightText).orEmpty()) }
    } }
    val valid = values.values.all { text -> text.replace(',', '.').toDoubleOrNull()?.let { it.isFinite() && it > 0 && it <= 1500 } == true }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Ваши максимумы") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Укажите ПМ в килограммах. Вспомогательные упражнения и прогрессия по микроциклам рассчитаются автоматически.",
                style = MaterialTheme.typography.bodySmall)
            listOf("squat" to "Присед", "bench" to "Жим лёжа", "deadlift" to "Становая тяга").forEach { (key, label) ->
                OutlinedTextField(value = values[key].orEmpty(), onValueChange = { values[key] = it }, label = { Text(label) },
                    suffix = { Text("кг") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth())
            }
            Text("Меняйте максимумы между тренировками: веса в плане пересчитываются сразу.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }, confirmButton = { TextButton(enabled = valid, onClick = {
        onSave(values.mapValues { it.value.replace(',', '.').toDouble() })
    }) { Text("Сохранить") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } })
}

internal fun weightText(value: Double): String = if (value % 1.0 < 0.00001) value.toInt().toString()
    else String.format(Locale.ROOT, "%.1f", value)
private fun percentText(value: Double): String = "${weightText(value * 100)}%"

private fun WorkoutUiState.canEdit(day: WorkoutDay): Boolean =
    progress.configured && day.id !in progress.completedDays && !saving
