@file:Suppress("LongMethod", "TooManyFunctions")

package com.k.shavrin.diethelper.presentation.screen.workout

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.k.shavrin.diethelper.domain.model.ExercisePreference
import com.k.shavrin.diethelper.domain.model.PerformedTrainingSet
import com.k.shavrin.diethelper.domain.model.RecoveryStatus
import com.k.shavrin.diethelper.domain.model.TrainingLoadBasis
import com.k.shavrin.diethelper.domain.model.TrainingOutcome
import com.k.shavrin.diethelper.domain.model.TrainingSession
import com.k.shavrin.diethelper.domain.model.TrainingSessionFeedback
import com.k.shavrin.diethelper.domain.model.WorkoutSet

@Composable
internal fun TrainingSetDialog(
    source: WorkoutSet?,
    existing: PerformedTrainingSet?,
    maxima: Map<String, Double>,
    legacyMarked: Boolean,
    saving: Boolean,
    error: String?,
    onSave: (PerformedTrainingSet) -> Unit,
    onUndo: (() -> Unit)?,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = { if (!saving) onDismiss() }) {
        TrainingSetForm(source, existing, maxima, legacyMarked, saving, error, onSave, onUndo, onDismiss)
    }
}

@Composable
internal fun FinishTrainingDialog(
    allComplete: Boolean,
    previous: TrainingSession?,
    saving: Boolean,
    error: String?,
    onSave: (TrainingSessionFeedback) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = { if (!saving) onDismiss() }) {
        FinishTrainingForm(allComplete, previous, saving, error, onSave, onDismiss)
    }
}

@Composable
internal fun TrainingFollowUpDialog(
    session: TrainingSession,
    saving: Boolean,
    error: String?,
    onSave: (RecoveryStatus, String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = { if (!saving) onDismiss() }) {
        TrainingFollowUpForm(session, saving, error, onSave, onDismiss)
    }
}

@Composable
internal fun TrainingSetForm(
    source: WorkoutSet?,
    existing: PerformedTrainingSet?,
    maxima: Map<String, Double>,
    legacyMarked: Boolean,
    saving: Boolean,
    error: String?,
    onSave: (PerformedTrainingSet) -> Unit,
    onUndo: (() -> Unit)?,
    onDismiss: () -> Unit
) {
    val originalName = source?.exercise?.name.orEmpty()
    val plannedWeight = remember { existing?.plannedWeightKg ?: source?.takeIf {
        it.exercise.loadBasis != TrainingLoadBasis.BODYWEIGHT
    }?.let { it.exercise.weight(maxima, it.group.percent) } }
    val initial = remember { initialSetInput(source, existing, legacyMarked, plannedWeight) }
    var name by rememberSaveable { mutableStateOf(initial.name) }
    var weight by rememberSaveable { mutableStateOf(initial.weight) }
    var reps by rememberSaveable { mutableStateOf(initial.reps) }
    var rpe by rememberSaveable { mutableStateOf(initial.rpe) }
    var symptoms by rememberSaveable { mutableStateOf(initial.symptoms) }
    var note by rememberSaveable { mutableStateOf(initial.note) }
    var preference by rememberSaveable { mutableStateOf(initial.preference) }
    var basis by rememberSaveable { mutableStateOf(initial.basis) }
    val valid = validSetInput(name, reps, weight, rpe, basis, symptoms, note)
    TrainingFormLayout(
        title = { Text(if (source == null) "Дополнительный подход" else "Фактический подход") },
        text = {
            Column(Modifier.heightIn(max = 520.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (source != null) Text("План: $originalName · " +
                    (if (source.exercise.loadBasis == TrainingLoadBasis.BODYWEIGHT) "свой вес" else "${plannedWeight?.let(::weightText) ?: "—"} кг") +
                    " × ${source.group.reps}",
                    style = MaterialTheme.typography.bodySmall)
                if (legacyMarked && existing == null) Text("Старая отметка: фактический вес и повторы не были записаны. Уточни их вручную.",
                    style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(name, {
                    if (name == originalName && it != originalName) weight = ""
                    name = it
                }, label = { Text("Упражнение / вариант") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                val alternatives = exerciseAlternatives(originalName, source == null)
                AlternativeExerciseChoices(alternatives, name) { alternative ->
                    name = alternative
                    weight = ""
                    if (source == null) basis = TrainingLoadBasis.BODYWEIGHT
                }
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TrainingLoadBasis.entries.forEach { option ->
                        FilterChip(basis == option, { basis = option }, label = { Text(option.label()) })
                    }
                }
                if (basis != TrainingLoadBasis.BODYWEIGHT) {
                    JournalNumberField(weight, { weight = it }, "Фактический вес, кг")
                }
                JournalNumberField(reps, { reps = it }, "Повторения", integer = true)
                JournalNumberField(rpe, { rpe = it }, "RPE подхода · 1–10")
                Text("RPE 6 ≈ 4+ повтора в запасе, 8 ≈ 2. Ощущения записываются отдельно.",
                    style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(symptoms, { symptoms = it }, label = { Text("Ощущения · где и когда") },
                    modifier = Modifier.fillMaxWidth(), minLines = 2)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ExercisePreference.entries.forEach { option ->
                        FilterChip(preference == option, { preference = option }, label = { Text(option.label()) })
                    }
                }
                OutlinedTextField(note, { note = it }, label = { Text("Заметка / что изменить") }, modifier = Modifier.fillMaxWidth())
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                if (onUndo != null) TextButton(onClick = onUndo, enabled = !saving) { Text("Снять отметку подхода") }
            }
        },
        confirmButton = {
            TextButton(enabled = valid && !saving, onClick = {
                onSave(InitialSetInput(name, weight, reps, rpe, symptoms, note, preference, basis).toPerformedSet(source, existing, plannedWeight))
            }) { Text(if (saving) "Сохранение…" else "Записать подход") }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !saving) { Text("Отмена") } }
    )
}

@Composable
internal fun FinishTrainingForm(
    allComplete: Boolean,
    previous: TrainingSession?,
    saving: Boolean,
    error: String?,
    onSave: (TrainingSessionFeedback) -> Unit,
    onDismiss: () -> Unit
) {
    var outcome by rememberSaveable { mutableStateOf(
        if (allComplete) TrainingOutcome.COMPLETED else TrainingOutcome.MODIFIED
    ) }
    var gym by rememberSaveable { mutableStateOf(previous?.gymMinutes?.toString().orEmpty()) }
    var social by rememberSaveable { mutableStateOf(previous?.socialMinutes?.toString().orEmpty()) }
    var effort by rememberSaveable { mutableStateOf(previous?.sessionRpe?.let(::weightText).orEmpty()) }
    var recovery by rememberSaveable { mutableStateOf(previous?.recovery ?: RecoveryStatus.UNKNOWN) }
    var notes by rememberSaveable { mutableStateOf(previous?.notes.orEmpty()) }
    val total = gym.toIntOrNull()
    val breaks = social.toIntOrNull()
    val rpe = effort.number()
    val valid = validSessionInput(gym, social, effort, outcome, notes)
    TrainingFormLayout(title = { Text("Сохранить занятие") },
        text = {
            Column(Modifier.heightIn(max = 520.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Пропущенные подходы останутся пропущенными. Догонять их не требуется.", style = MaterialTheme.typography.bodySmall)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TrainingOutcome.entries.filter { it != TrainingOutcome.ACTIVE }.forEach { option ->
                        FilterChip(outcome == option, { outcome = option }, enabled = option != TrainingOutcome.COMPLETED || allComplete,
                            label = { Text(option.label()) })
                    }
                }
                JournalNumberField(gym, { gym = it }, "Время в зале, мин", integer = true)
                JournalNumberField(social, { social = it }, "Из них разговоры, мин", integer = true)
                if (total != null && breaks != null && breaks <= total) Text(
                    "Тренировка: ${total - breaks} мин, включая обычный отдых между подходами.",
                    style = MaterialTheme.typography.bodySmall
                ) else Text("Время в зале включает разминку и заминку. Если время неизвестно, оставь пустым. Обычный отдых вычитать не нужно.",
                    style = MaterialTheme.typography.bodySmall)
                JournalNumberField(effort, { effort = it }, "Усилие за всё занятие, 0–10")
                Text("Самочувствие после")
                RecoveryPicker(recovery) { recovery = it }
                OutlinedTextField(notes, { notes = it }, label = { Text("Причина / заметка") },
                    modifier = Modifier.fillMaxWidth(), minLines = 2)
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = { TextButton(enabled = valid && !saving, onClick = {
            onSave(TrainingSessionFeedback(outcome, total, breaks, rpe, recovery, notes))
        }) { Text(if (saving) "Сохранение…" else "Сохранить занятие") } },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !saving) { Text("Вернуться") } }
    )
}

@Composable
internal fun TrainingFollowUpForm(
    session: TrainingSession,
    saving: Boolean,
    error: String?,
    onSave: (RecoveryStatus, String) -> Unit,
    onDismiss: () -> Unit
) {
    var recovery by rememberSaveable(session.id) { mutableStateOf(session.nextDayRecovery) }
    var notes by rememberSaveable(session.id) { mutableStateOf(session.nextDayNotes) }
    TrainingFormLayout(title = { Text("На следующий день") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Как ощущения по сравнению с состоянием до занятия? Где, когда и как долго они возникали?")
                RecoveryPicker(recovery) { recovery = it }
                OutlinedTextField(notes, { notes = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Самочувствие и заметка") }, minLines = 3)
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = { TextButton(enabled = !saving && notes.length <= 4000, onClick = { onSave(recovery, notes) }) { Text("Сохранить") } },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !saving) { Text("Отмена") } }
    )
}

@Composable
private fun JournalNumberField(value: String, onChange: (String) -> Unit, label: String, integer: Boolean = false) {
    OutlinedTextField(value, onChange, label = { Text(label) }, modifier = Modifier.fillMaxWidth(), singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = if (integer) KeyboardType.Number else KeyboardType.Decimal))
}

@Composable
internal fun TrainingFormLayout(
    title: @Composable () -> Unit,
    text: @Composable () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit
) {
    val maximumHeight = LocalConfiguration.current.screenHeightDp.dp * 0.9f
    Surface(shape = MaterialTheme.shapes.extraLarge, color = MaterialTheme.colorScheme.surfaceContainerHigh) {
            Column(Modifier.fillMaxWidth().widthIn(max = 560.dp).heightIn(max = maximumHeight).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ProvideTextStyle(MaterialTheme.typography.headlineSmall) { title() }
                Box(Modifier.weight(1f, fill = false)) { text() }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                    dismissButton()
                    confirmButton()
                }
            }
        }
}

@Composable
private fun RecoveryPicker(value: RecoveryStatus, onChange: (RecoveryStatus) -> Unit) {
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        RecoveryStatus.entries.forEach { option ->
            FilterChip(value == option, { onChange(option) }, label = { Text(option.label()) })
        }
    }
}

@Composable
private fun AlternativeExerciseChoices(options: List<String>, selected: String, onChoose: (String) -> Unit) {
    if (options.isEmpty()) return
    Text("Варианты для обсуждения, если движение комфортно. Другой вес укажи вручную; упражнения не равноценны.",
        style = MaterialTheme.typography.bodySmall)
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        options.forEach { alternative ->
            FilterChip(selected == alternative, { onChoose(alternative) }, label = { Text(alternative) })
        }
    }
}

private fun String.number() = replace(',', '.').toDoubleOrNull()

private data class InitialSetInput(
    val name: String,
    val weight: String,
    val reps: String,
    val rpe: String,
    val symptoms: String,
    val note: String,
    val preference: ExercisePreference,
    val basis: TrainingLoadBasis
)

private fun InitialSetInput.toPerformedSet(source: WorkoutSet?, existing: PerformedTrainingSet?, plannedWeight: Double?) = PerformedTrainingSet(
    planKey = source?.key, exerciseName = name.trim(),
    plannedExerciseName = existing?.plannedExerciseName ?: source?.exercise?.name,
    plannedWeightKg = plannedWeight, plannedReps = existing?.plannedReps ?: source?.group?.reps,
    actualWeightKg = if (basis == TrainingLoadBasis.BODYWEIGHT) null else weight.number(),
    actualReps = requireNotNull(reps.toIntOrNull()), loadBasis = basis, rpe = rpe.number(),
    symptomNote = symptoms.trim(), preference = preference, notes = note.trim()
)

private fun initialSetInput(source: WorkoutSet?, existing: PerformedTrainingSet?, legacy: Boolean, plannedWeight: Double?): InitialSetInput {
    val unknown = legacy && existing == null
    return InitialSetInput(
        existing?.exerciseName ?: source?.exercise?.name.orEmpty(),
        if (unknown) "" else (existing?.actualWeightKg ?: plannedWeight)?.let(::weightText).orEmpty(),
        if (unknown) "" else (existing?.actualReps ?: source?.group?.reps)?.toString().orEmpty(),
        existing?.rpe?.let(::weightText).orEmpty(), existing?.symptomNote.orEmpty(), existing?.notes.orEmpty(),
        existing?.preference ?: ExercisePreference.NEUTRAL,
        existing?.loadBasis ?: if (source?.exercise?.equipmentMultiplier == 2.0) TrainingLoadBasis.PER_HAND_KG
            else source?.exercise?.loadBasis ?: TrainingLoadBasis.TOTAL_KG
    )
}

private fun validOptionalNumber(text: String, minimum: Double, maximum: Double): Boolean =
    text.isBlank() || text.number()?.let { it.isFinite() && it in minimum..maximum } == true

private fun validSetInput(name: String, reps: String, weight: String, rpe: String, basis: TrainingLoadBasis, symptoms: String, notes: String): Boolean {
    val validWeight = basis == TrainingLoadBasis.BODYWEIGHT || weight.isNotBlank() && validOptionalNumber(weight, 0.0, 1500.0)
    return name.isNotBlank() && name.length <= 120 && reps.toIntOrNull()?.let { it in 1..1000 } == true && validWeight &&
        validOptionalNumber(rpe, 1.0, 10.0) && symptoms.length <= 2000 && notes.length <= 2000
}

private fun validSessionInput(gym: String, social: String, effort: String, outcome: TrainingOutcome, notes: String): Boolean {
    val total = gym.toIntOrNull()
    val breaks = social.toIntOrNull()
    return (gym.isBlank() || total != null && total in 1..1440) &&
        (social.isBlank() || breaks != null && breaks in 0..1440) &&
        (total == null || breaks == null || breaks <= total) && validOptionalNumber(effort, 0.0, 10.0) &&
        (outcome == TrainingOutcome.COMPLETED || notes.isNotBlank()) && notes.length <= 4000
}

private fun exerciseAlternatives(original: String, extra: Boolean): List<String> = when {
    original == "Присед на груди" -> listOf("Жим ногами", "Сплит-присед")
    original == "Наклоны" || original == "Тяга на прямых ногах" -> listOf("Тяга с опорой грудью")
    extra -> listOf("Подтягивания", "Брусья", "Гиперэкстензия")
    else -> emptyList()
}

internal fun RecoveryStatus.label() = when (this) {
    RecoveryStatus.UNKNOWN -> "Не оценивал"
    RecoveryStatus.NORMAL -> "Обычное"
    RecoveryStatus.DISCOMFORT -> "Дискомфорт"
    RecoveryStatus.ILLNESS -> "Болезнь"
}

internal fun TrainingOutcome.label() = when (this) {
    TrainingOutcome.ACTIVE -> "В процессе"
    TrainingOutcome.COMPLETED -> "По плану"
    TrainingOutcome.MODIFIED -> "Изменено"
    TrainingOutcome.STOPPED -> "Остановлено"
}

internal fun ExercisePreference.label() = when (this) {
    ExercisePreference.NEUTRAL -> "Без оценки"
    ExercisePreference.LIKED -> "Нравится"
    ExercisePreference.DISLIKED -> "Не подходит"
}

internal fun TrainingLoadBasis.label() = when (this) {
    TrainingLoadBasis.TOTAL_KG -> "Всего"
    TrainingLoadBasis.PER_HAND_KG -> "На руку"
    TrainingLoadBasis.BODYWEIGHT -> "Свой вес"
}
