@file:Suppress("TooManyFunctions", "LongMethod")

package com.k.shavrin.diethelper.presentation.screen.workout

import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.k.shavrin.diethelper.R
import com.k.shavrin.diethelper.domain.model.PerformedTrainingSet
import com.k.shavrin.diethelper.domain.model.TrainingLoadBasis
import com.k.shavrin.diethelper.domain.model.TrainingSession
import com.k.shavrin.diethelper.domain.model.WorkoutDay
import com.k.shavrin.diethelper.domain.model.WorkoutExercise
import com.k.shavrin.diethelper.domain.model.WorkoutProgram
import com.k.shavrin.diethelper.domain.model.WorkoutProgress
import com.k.shavrin.diethelper.domain.model.WorkoutSet
import com.k.shavrin.diethelper.domain.model.completedTrainingPlanKeys
import com.k.shavrin.diethelper.presentation.components.PillTabs
import com.k.shavrin.diethelper.presentation.components.ScreenHeading
import com.k.shavrin.diethelper.presentation.theme.FatSand
import com.k.shavrin.diethelper.presentation.theme.Ink
import com.k.shavrin.diethelper.presentation.theme.Lavender
import com.k.shavrin.diethelper.presentation.theme.Mint
import kotlinx.coroutines.delay
import java.io.File
import java.util.Locale
import kotlin.math.ceil

private val TrainingAccent = FatSand

@Composable
fun WorkoutScreen(viewModel: WorkoutViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    val journalViewModel: TrainingJournalViewModel = hiltViewModel()
    val journal by journalViewModel.state.collectAsState()
    val context = LocalContext.current
    val activeSession = journal.sessions.firstOrNull { it.outcome == com.k.shavrin.diethelper.domain.model.TrainingOutcome.ACTIVE }
    LaunchedEffect(activeSession?.id, state.plan) {
        activeSession?.let { active -> state.plan?.takeIf { it.program == active.program }?.days
            ?.firstOrNull { it.id == active.planDayId }?.let(viewModel::selectDay) }
    }
    LaunchedEffect(journal.completedDayId) {
        if (journal.completedDayId != null) {
            viewModel.selectNext()
            journalViewModel.consumeCompletedDay()
        }
    }
    LaunchedEffect(journal.exportPaths) {
        if (journal.exportPaths.isNotEmpty()) {
            try {
                shareTrainingReport(context, journal.exportPaths)
            } catch (_: Exception) {
                journalViewModel.shareFailed()
            } finally {
                journalViewModel.consumeExport()
            }
        }
    }
    WorkoutContent(state, viewModel::selectDay, viewModel::toggleSet,
        viewModel::saveMaximums, viewModel::timer, viewModel::clearError, journal,
        TrainingJournalActions(journalViewModel::record, journalViewModel::undo, journalViewModel::finish,
            journalViewModel::followUp, journalViewModel::export, journalViewModel::clearError),
        WorkoutProgramActions(viewModel::select, viewModel::saveReturnWeek))
}

@Composable
fun WorkoutContent(
    state: WorkoutUiState,
    onSelect: (WorkoutDay) -> Unit,
    onSet: (WorkoutDay, String) -> Unit,
    onMaxima: (Map<String, Double>) -> Unit,
    onTimer: (Int, Long, Int) -> Unit,
    onClearError: () -> Unit,
    journal: TrainingJournalUiState,
    actions: TrainingJournalActions,
    programs: WorkoutProgramActions
) {
    var tab by rememberSaveable(state.progress.program) { mutableIntStateOf(0) }
    var showMaxima by rememberSaveable(state.progress.program) { mutableStateOf(false) }
    var cycle by rememberSaveable(state.progress.program) { mutableIntStateOf(1) }
    var editingWeek by rememberSaveable(state.progress.program) { mutableIntStateOf(1) }
    val day = state.selectedDay
    val progress = state.progress
    val returning = progress.program == WorkoutProgram.RETURN
    val configure = { editingWeek = configurationWeek(tab, cycle, day); showMaxima = true }
    val activity = LocalContext.current as? Activity
    DisposableEffect(activity, tab, day?.id, day?.let(progress::configuredFor)) {
        if (tab == 0 && day != null && progress.configuredFor(day)) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose { activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
    }
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        WorkoutHeading(returning, !state.saving && !journal.saving, configure)
        WorkoutProgramPicker(progress.program, !state.saving && !journal.saving, programs.select)
        PillTabs(listOf("Тренировка", "Весь план", "Журнал"), tab, {
            if (it == 1) cycle = day?.cycle ?: cycle
            tab = it
        }, Modifier.padding(horizontal = 20.dp, vertical = 4.dp))
        state.error?.let { error ->
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(error, Modifier.weight(1f), color = MaterialTheme.colorScheme.error)
                TextButton(onClick = onClearError) { Text("Закрыть") }
            }
        }
        journal.error?.let { error ->
            Row(Modifier.padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(error, Modifier.weight(1f), color = MaterialTheme.colorScheme.error)
                TextButton(onClick = actions.clearError) { Text("Закрыть") }
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
                key(progress.program, day.id) {
                    SessionContent(day, state, onSet, onTimer, configure,
                        if (journal.program == progress.program) journal else TrainingJournalUiState(program = progress.program), actions)
                }
            }
        } else if (tab == 2) {
            if (journal.program == progress.program) TrainingJournalContent(journal, actions)
        } else {
            WorkoutPlanContent(state, cycle, { cycle = it }, { onSelect(it); tab = 0 }, configure)
        }
    }
    if (showMaxima) {
        if (returning) ReturnWeekLoadsDialog(editingWeek, progress, onDismiss = { showMaxima = false }, onSave = {
            programs.saveWeek(editingWeek, it); showMaxima = false
        }) else MaximaDialog(progress, onDismiss = { showMaxima = false }, onSave = { onMaxima(it); showMaxima = false })
    }
}

@Composable
private fun WorkoutPlanContent(
    state: WorkoutUiState,
    cycle: Int,
    onCycle: (Int) -> Unit,
    onOpen: (WorkoutDay) -> Unit,
    configure: () -> Unit
) {
    val progress = state.progress
    val returning = progress.program == WorkoutProgram.RETURN
    val plan = state.plan
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Text("${plan?.days?.size ?: 0} тренировок · ${plan?.days?.maxOfOrNull { it.cycle } ?: 0} " +
                if (returning) "недель" else "микроциклов", style = MaterialTheme.typography.titleMedium)
            Text(if (returning) "Пять посещений, включая короткий необязательный день. Веса каждой недели выбираются после отчёта."
                else "Волны, коэффициенты упражнений и округление сохранены из таблицы. Рабочие ориентиры задаёшь ты.",
                color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            Row(Modifier.horizontalScroll(rememberScrollState()).padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (plan?.days?.map { it.cycle }?.distinct().orEmpty()).forEach { number ->
                    FilterChip(selected = cycle == number, onClick = { onCycle(number) }, label = { Text(if (returning) "Неделя $number" else "Цикл $number") })
                }
            }
            Text(if (returning) "План можно пересматривать. Старые рекорды не определяют веса этого блока."
                else "В таблице заполнены 57 тренировок. Пустые заготовки циклов 13–25 не добавлены.",
                style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            if (returning) ReturnWeekSummary(cycle, progress.returnWeekLoads[cycle] != null, configure)
            else WorkoutWaveOverview(state.waves, cycle)
        }
        items(plan?.days.orEmpty().filter { it.cycle == cycle }, key = { it.id }) { plannedDay ->
            PlanDayCard(plannedDay, progress, onOpen = { onOpen(plannedDay) })
        }
    }
}

@Composable
private fun SessionContent(
    day: WorkoutDay,
    state: WorkoutUiState,
    onSet: (WorkoutDay, String) -> Unit,
    onTimer: (Int, Long, Int) -> Unit,
    onConfigure: () -> Unit,
    journal: TrainingJournalUiState,
    actions: TrainingJournalActions
) {
    val progress = state.progress
    val configured = progress.configuredFor(day)
    val localProgress = progress.copy(maxima = progress.loadsFor(day))
    val sets = remember(day) { day.sets() }
    val session = journal.sessions.firstOrNull { it.planDayId == day.id }
    val recorded = session?.sets.orEmpty().filter { it.planKey != null }.associateBy { requireNotNull(it.planKey) }
    val completedKeys = completedTrainingPlanKeys(progress.completedSets, session?.sets.orEmpty())
    val completed = sets.count { it.key in completedKeys }
    val next = sets.firstOrNull { it.key !in completedKeys }
    val finished = day.id in progress.completedDays
    val editable = state.canEdit(day, journal.saving)
    var editingSet by remember(day.id) { mutableStateOf<WorkoutSet?>(null) }
    var adding by rememberSaveable(day.id) { mutableStateOf(false) }
    var finishing by rememberSaveable(day.id) { mutableStateOf(false) }
    val listState = rememberLazyListState()
    LaunchedEffect(day.id) { listState.scrollToItem(0) }
    LaunchedEffect(journal.changeVersion) { editingSet = null; adding = false; finishing = false }
    LazyColumn(state = listState, contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            TrainingDayHeading(day, completed, sets.size)
        }
        if (!finished) item { TrainingAdviceCard(journal.advice) }
        if (day.program == WorkoutProgram.RETURN) item { ReturnWeekSummary(day.cycle, configured, onConfigure) }
        if (!configured && day.program == WorkoutProgram.SRC7) {
            item {
                Card(shape = RoundedCornerShape(24.dp)) {
                    Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Default.FitnessCenter, null, tint = Mint)
                        Text("Рабочие ориентиры", style = MaterialTheme.typography.headlineSmall)
                        Text("Укажи ориентиры в приседе, жиме и тяге. Это основа расчёта, а не проверка текущей силы.")
                        Button(onClick = onConfigure, modifier = Modifier.fillMaxWidth()) { Text("Настроить ориентиры") }
                    }
                }
            }
        } else if (configured && next != null && !finished) {
            item {
                NextTrainingSetCard(day, next, localProgress, completed, editable) { actions.clearError(); editingSet = next }
            }
        }
        if (configured && !finished) {
            item { RestTimer(progress, onTimer) }
            item {
                OutlinedButton(onClick = { actions.clearError(); adding = true }, enabled = editable, modifier = Modifier.fillMaxWidth()) {
                    Text("Добавить свой подход / упражнение")
                }
                TrainingAdditionalSets(session, editable, actions.undo)
            }
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
            val count = exerciseSets.count { it.key in completedKeys }
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
                            TrainingPlannedSetRow(index, set, recorded[set.key], localProgress,
                                set.key in completedKeys, editable) {
                                actions.clearError(); editingSet = set
                            }
                        }
                    } else {
                        ExerciseGroups(exercise, localProgress)
                    }
                }
            }
        }
        item { TrainingDayNotes(day.notes) }
        item {
            Button(onClick = { actions.clearError(); finishing = true }, enabled = editable,
                modifier = Modifier.fillMaxWidth().height(54.dp)) {
                Text(if (finished) "Тренировка сохранена ✓" else "Завершить тренировку")
            }
            if (!finished && next != null) Text("Можно сохранить неполное занятие и указать, что изменилось.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp))
        }
    }
    if (editingSet != null || adding) {
        SessionSetEditor(day, localProgress, journal, editingSet, recorded, actions,
            onLegacyUndo = { key -> onSet(day, key); editingSet = null },
            onDismiss = { editingSet = null; adding = false })
    }
    if (finishing) FinishTrainingDialog(next == null, session, journal.saving, journal.error,
        onSave = { actions.finish(day, it) }, onDismiss = { finishing = false })
}

private fun configurationWeek(tab: Int, cycle: Int, day: WorkoutDay?) = if (tab == 1) cycle else day?.cycle ?: 1

@Composable
private fun WorkoutHeading(returning: Boolean, enabled: Boolean, configure: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
        ScreenHeading("ДВИЖЕНИЕ И СИЛА", "Тренировки", if (returning) "Возвращение · рабочие веса и отчёты" else "СРЦ7 · волны и личный журнал",
            Modifier.weight(1f), artwork = R.drawable.art_training)
        FilledTonalIconButton(onClick = configure, enabled = enabled) {
            Icon(Icons.Default.Tune, contentDescription = if (returning) "Веса недели" else "Рабочие максимумы")
        }
    }
}

@Composable
private fun TrainingDayHeading(day: WorkoutDay, completed: Int, total: Int) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text((if (day.program == WorkoutProgram.RETURN) "НЕДЕЛЯ" else "МИКРОЦИКЛ") + " ${day.cycle}  /  ДЕНЬ ${day.day}",
            color = TrainingAccent, style = MaterialTheme.typography.labelMedium)
        Text("$completed / $total подходов", style = MaterialTheme.typography.labelMedium)
    }
    Spacer(Modifier.height(10.dp))
    if (day.title.isNotBlank()) Text(day.title, style = MaterialTheme.typography.titleLarge)
    LinearProgressIndicator(progress = { completed.toFloat() / total.coerceAtLeast(1) },
        modifier = Modifier.fillMaxWidth().height(5.dp), color = Lavender)
}

@Composable
private fun TrainingDayNotes(notes: String) {
    if (notes.isNotBlank()) {
        Text("Дополнительно", style = MaterialTheme.typography.titleSmall, color = TrainingAccent)
        Text(notes, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun NextTrainingSetCard(
    day: WorkoutDay,
    next: WorkoutSet,
    progress: WorkoutProgress,
    completed: Int,
    editable: Boolean,
    onEdit: () -> Unit
) {
    Column(Modifier.fillMaxWidth().background(
        Brush.linearGradient(listOf(Color(0xFF3B2A53), Color(0xFF20192D))), RoundedCornerShape(28.dp)
    ).padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(if (completed == 0) "ПЕРВЫЙ ПОДХОД" else "СЛЕДУЮЩИЙ ПОДХОД", color = Lavender,
            style = MaterialTheme.typography.labelMedium, letterSpacing = 2.sp)
        Text(next.exercise.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(if (next.exercise.loadBasis == TrainingLoadBasis.BODYWEIGHT) "Свой вес"
                else weightText(next.exercise.weight(progress.maxima, next.group.percent)),
                fontSize = if (next.exercise.loadBasis == TrainingLoadBasis.BODYWEIGHT) 32.sp else 58.sp,
                fontWeight = FontWeight.Bold, color = Lavender)
            Text((if (next.exercise.loadBasis == TrainingLoadBasis.BODYWEIGHT) "× " else "кг × ") + "${next.group.reps}",
                fontSize = 24.sp, modifier = Modifier.padding(bottom = 9.dp))
        }
        Text((if (day.program == WorkoutProgram.RETURN) "Рабочий подход · " else "${percentText(next.group.percent)} по схеме · ") +
            "${next.exercise.load.lowercase()} нагрузка" +
            if (next.exercise.equipmentMultiplier == 2.0) " · вес одной гантели" else "",
            color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        Button(onClick = { onEdit() }, enabled = editable,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Lavender, contentColor = Ink)) {
            Icon(Icons.Default.Check, null)
            Spacer(Modifier.width(8.dp))
            Text("Записать подход", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun TrainingAdditionalSets(
    session: TrainingSession?,
    editable: Boolean,
    onUndo: (PerformedTrainingSet) -> Unit
) {
    session?.completedSets?.filter { it.planKey == null }?.forEach { extra ->
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("${extra.exerciseName} × ${extra.actualReps}", Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
            TextButton(onClick = { onUndo(extra) }, enabled = editable) { Text("Снять") }
        }
    }
}

@Composable
private fun SessionSetEditor(
    day: WorkoutDay,
    progress: WorkoutProgress,
    journal: TrainingJournalUiState,
    source: WorkoutSet?,
    recorded: Map<String, PerformedTrainingSet>,
    actions: TrainingJournalActions,
    onLegacyUndo: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val existing = source?.let { recorded[it.key] }
    val legacy = source?.key in progress.completedSets && existing == null
    val undo: (() -> Unit)? = when {
        existing?.completed == true -> ({ actions.undo(existing) })
        legacy && source != null -> ({ onLegacyUndo(source.key) })
        else -> null
    }
    TrainingSetDialog(source, existing, progress.maxima, legacy, journal.saving, journal.error,
        onSave = { actions.record(day, it) }, onUndo = undo, onDismiss = onDismiss)
}

@Composable
private fun TrainingPlannedSetRow(
    index: Int,
    set: WorkoutSet,
    recorded: PerformedTrainingSet?,
    progress: WorkoutProgress,
    checked: Boolean,
    editable: Boolean,
    onEdit: () -> Unit
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("${index + 1}", Modifier.width(30.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        val actual = recorded?.takeIf { it.completed }
        val actualText = actual?.let {
            val load = if (it.loadBasis == TrainingLoadBasis.BODYWEIGHT) "Свой вес" else "${it.actualWeightKg?.let(::weightText) ?: "—"} кг"
            "${it.exerciseName}: $load × ${it.actualReps}"
        }
        Text(actualText ?: if (set.exercise.loadBasis == TrainingLoadBasis.BODYWEIGHT) "Свой вес × ${set.group.reps}"
            else if (progress.configured) "${weightText(set.exercise.weight(progress.maxima, set.group.percent))} кг × ${set.group.reps}"
            else "— кг × ${set.group.reps}", Modifier.weight(1f), fontWeight = FontWeight.Medium)
        Text(percentText(set.group.percent), style = MaterialTheme.typography.labelMedium, color = TrainingAccent)
        Checkbox(checked = checked, onCheckedChange = { onEdit() }, enabled = editable)
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
    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Timer, null, tint = TrainingAccent)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("Отдых между подходами", style = MaterialTheme.typography.titleSmall)
                    Text(if (remaining > 0) String.format(Locale.ROOT, "%02d:%02d", remaining / 60, remaining % 60)
                        else if (progress.restDeadline != 0L) "Можно продолжать" else "${progress.restSeconds / 60} мин ${progress.restSeconds % 60} сек",
                        style = MaterialTheme.typography.titleLarge, color = TrainingAccent)
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
                else Text("${day.sets().size} подходов", color = TrainingAccent, style = MaterialTheme.typography.labelMedium)
            }
            if (day.title.isNotBlank()) Text(day.title, style = MaterialTheme.typography.titleMedium)
            day.exercises.forEach { exercise ->
                Text(exercise.name, style = MaterialTheme.typography.titleSmall)
                ExerciseGroups(exercise, progress.copy(maxima = progress.loadsFor(day)))
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
                Text(if (exercise.loadBasis == TrainingLoadBasis.BODYWEIGHT) "Свой вес"
                    else if (progress.configured) "${weightText(exercise.weight(progress.maxima, group.percent))} кг"
                    else "— кг", fontWeight = FontWeight.Medium)
                Text("${group.sets} × ${group.reps}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(percentText(group.percent), color = TrainingAccent, style = MaterialTheme.typography.labelMedium)
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
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Рабочие ориентиры") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Задай расчётные значения в килограммах. Исторический рекорд и текущий рабочий ориентир — разные вещи.",
                style = MaterialTheme.typography.bodySmall)
            listOf("squat" to "Присед", "bench" to "Жим лёжа", "deadlift" to "Становая тяга").forEach { (key, label) ->
                OutlinedTextField(value = values[key].orEmpty(), onValueChange = { values[key] = it }, label = { Text(label) },
                    suffix = { Text("кг") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth())
            }
            Text("План пересчитывается сразу. Фактически записанные подходы в журнале сохраняют свой вес.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }, confirmButton = { TextButton(enabled = valid, onClick = {
        onSave(values.mapValues { it.value.replace(',', '.').toDouble() })
    }) { Text("Сохранить") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } })
}

internal fun weightText(value: Double): String = if (value % 1.0 < 0.00001) value.toInt().toString()
    else String.format(Locale.ROOT, "%.1f", value)
private fun percentText(value: Double): String = "${weightText(value * 100)}%"

private fun WorkoutUiState.canEdit(day: WorkoutDay, journalSaving: Boolean): Boolean =
    progress.configuredFor(day) && day.id !in progress.completedDays && !saving && !journalSaving

private fun shareTrainingReport(context: Context, paths: List<String>) {
    val uris = ArrayList(paths.map { FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", File(it)) })
    val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
        type = "application/octet-stream"
        putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
        putExtra(Intent.EXTRA_SUBJECT, "Дневник тренировок MyDietHelper")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        clipData = ClipData.newUri(context.contentResolver, "Журнал тренировок", uris.first()).also { clip ->
            uris.drop(1).forEach { clip.addItem(ClipData.Item(it)) }
        }
    }
    context.startActivity(Intent.createChooser(intent, "Передать журнал для разбора"))
}
