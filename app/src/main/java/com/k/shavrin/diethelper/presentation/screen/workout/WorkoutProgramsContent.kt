package com.k.shavrin.diethelper.presentation.screen.workout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.k.shavrin.diethelper.domain.model.ReturnToTrainingPlan
import com.k.shavrin.diethelper.domain.model.WorkoutProgram
import com.k.shavrin.diethelper.domain.model.WorkoutProgress

@Composable
internal fun WorkoutProgramPicker(selected: WorkoutProgram, enabled: Boolean, onSelect: (WorkoutProgram) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        WorkoutProgram.entries.forEach { program ->
            Card(onClick = { onSelect(program) }, enabled = enabled, modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = if (selected == program)
                    MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(program.title, style = MaterialTheme.typography.titleSmall)
                    Text(if (program == WorkoutProgram.SRC7) "Табличный цикл" else "План на 6 недель",
                        style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
internal fun ReturnWeekSummary(week: Int, configured: Boolean, onConfigure: () -> Unit) {
    Card {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Неделя $week · возвращение", style = MaterialTheme.typography.titleMedium)
            Text(ReturnToTrainingPlan.weekDescription(week), style = MaterialTheme.typography.bodyMedium)
            Text(if (configured) "Рабочие веса этой недели сохранены. Следующая неделя потребует отдельного решения."
                else "После отчёта выбери рабочие веса этой недели. Автоматической прибавки нет.",
                style = MaterialTheme.typography.bodySmall)
            TextButton(onClick = onConfigure) { Text(if (configured) "Изменить веса недели" else "Задать веса недели") }
        }
    }
}

@Composable
internal fun ReturnWeekLoadsDialog(
    week: Int,
    progress: WorkoutProgress,
    onDismiss: () -> Unit,
    onSave: (Map<String, Double>) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        ReturnWeekLoadsForm(week, progress.returnWeekLoads[week].orEmpty(), onDismiss, onSave)
    }
}

@Composable
internal fun ReturnWeekLoadsForm(
    week: Int,
    initialLoads: Map<String, Double>,
    onDismiss: () -> Unit,
    onSave: (Map<String, Double>) -> Unit
) {
    val values = remember(week) { mutableStateMapOf<String, String>().apply {
        listOf("squat", "bench", "deadlift").forEach { base -> put(base, initialLoads[base]?.let(::weightText).orEmpty()) }
    } }
    val valid = values.values.all { text -> text.replace(',', '.').toDoubleOrNull()?.let { it.isFinite() && it > 0 && it <= 1500 } == true }
    TrainingFormLayout(title = { Text("Неделя $week · рабочие веса") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Это вес основных рабочих подходов, а не максимум. Лёгкие дни рассчитываются от него. " +
                "Для новой недели сначала разбери отчёт; можно оставить те же веса.", style = MaterialTheme.typography.bodySmall)
            listOf("squat" to "Присед · рабочий вес", "bench" to "Жим · рабочий вес", "deadlift" to "Сумо · рабочий вес").forEach { (base, label) ->
                OutlinedTextField(values[base].orEmpty(), { values[base] = it }, label = { Text(label) }, suffix = { Text("кг") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
            }
            Text("Веса СРЦ7 и уже записанные фактические подходы сохраняются отдельно.", style = MaterialTheme.typography.bodySmall)
        }
    }, confirmButton = {
        TextButton(onClick = { onSave(values.mapValues { it.value.replace(',', '.').toDouble() }) }, enabled = valid) { Text("Сохранить веса") }
    }, dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } })
}
