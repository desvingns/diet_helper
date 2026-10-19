@file:Suppress("LongMethod")

package com.k.shavrin.diethelper.presentation.screen.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.k.shavrin.diethelper.domain.model.TrainingLoadBasis
import com.k.shavrin.diethelper.domain.model.TrainingOutcome
import com.k.shavrin.diethelper.domain.model.TrainingSession
import com.k.shavrin.diethelper.domain.model.WorkoutWaveCycle
import com.k.shavrin.diethelper.presentation.theme.FatSand
import com.k.shavrin.diethelper.presentation.theme.Lavender
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
internal fun WorkoutWaveOverview(waves: List<WorkoutWaveCycle>, selected: Int) {
    if (waves.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Волны нагрузки", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("Столбцы — повторы во всех упражнениях. Это объём по схеме, а не оценка усталости.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        val maximum = waves.maxOf { it.repetitions }.coerceAtLeast(1)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
            waves.forEach { wave ->
                Column(Modifier.width(32.dp).semantics {
                    contentDescription = "Микроцикл ${wave.cycle}: ${wave.repetitions} повторений, ${wave.days} дней"
                }, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Box(Modifier.width(22.dp).height((56f * wave.repetitions / maximum).dp)
                        .background(if (wave.cycle == selected) Lavender else FatSand.copy(alpha = 0.6f), RoundedCornerShape(6.dp)))
                    Text(wave.cycle.toString(), style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        waves.firstOrNull { it.cycle == selected }?.let { wave ->
            Text("Цикл ${wave.cycle}: ${wave.days} дней · ${wave.sets} подходов · ${wave.repetitions} повторов",
                style = MaterialTheme.typography.bodySmall)
            Text("Упражнения: Т ${wave.heavyExercises} / С ${wave.mediumExercises} / Л ${wave.lightExercises}. " +
                "Пик базовых движений — ${weightText(wave.primaryPeak * 100)}% от рабочего ориентира.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text("Лёгкий вес не всегда означает мало работы. Циклы с разным числом заполненных дней сравнивай отдельно.",
            style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
internal fun TrainingJournalContent(state: TrainingJournalUiState, actions: TrainingJournalActions) {
    var followUp by remember { mutableStateOf<TrainingSession?>(null) }
    LaunchedEffect(state.changeVersion) { followUp = null }
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Text("Твой журнал", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Фактические подходы, изменения и самочувствие сохраняются отдельно от исходного плана.",
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(onClick = actions.export, enabled = state.sessions.isNotEmpty() && !state.saving,
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) { Text("Поделиться для разбора · MD + JSON") }
            Text("Отчёт содержит записи и личные заметки. Приложение передаст его через выбранное тобой приложение.",
                style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item { TrainingAdviceCard(state.advice) }
        if (state.sessions.isEmpty()) item {
            Text("Здесь появятся новые занятия после записи подхода. Старые отметки плана сохранены; их фактические веса и даты неизвестны.")
        }
        items(state.sessions, key = { it.id }) { session ->
            var expanded by rememberSaveable(session.id) { mutableStateOf(false) }
            Card(shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(trainingSessionDate(session), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Цикл ${session.cycle} · день ${session.day} · ${session.outcome.label()}")
                    Text("${session.completedSets.size} записанных подходов · усилие ${session.sessionRpe?.let(::weightText) ?: "—"}/10",
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (session.gymMinutes != null) Text(
                        "В зале ${session.gymMinutes} мин · разговоры ${session.socialMinutes?.toString() ?: "—"} · " +
                            "тренировка ${session.trainingMinutes?.toString() ?: "—"} мин",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text("После: ${session.recovery.label()} · на следующий день: ${session.nextDayRecovery.label()}",
                        style = MaterialTheme.typography.bodySmall)
                    if (session.notes.isNotBlank()) Text(session.notes)
                    if (session.nextDayNotes.isNotBlank()) Text("Позже: ${session.nextDayNotes}")
                    if (session.legacySetCount > 0) Text(
                        "Ещё ${session.legacySetCount} старых отметок без фактических деталей.", style = MaterialTheme.typography.bodySmall
                    )
                    if (expanded) TrainingRecordedSets(session)
                    TextButton(onClick = { expanded = !expanded }) { Text(if (expanded) "Свернуть подходы" else "Посмотреть подходы") }
                    if (session.outcome != TrainingOutcome.ACTIVE) OutlinedButton(
                        onClick = { actions.clearError(); followUp = session }, modifier = Modifier.fillMaxWidth()
                    ) { Text("Самочувствие на следующий день") }
                }
            }
        }
    }
    followUp?.let { session ->
        TrainingFollowUpDialog(session, state.saving, state.error,
            onSave = { recovery, notes -> actions.followUp(session.id, recovery, notes) },
            onDismiss = { followUp = null })
    }
}

private fun trainingSessionDate(session: TrainingSession): String {
    val zone = runCatching { ZoneId.of(session.zoneId) }.getOrDefault(ZoneId.of("UTC"))
    return Instant.ofEpochMilli(session.startedAt).atZone(zone).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
}

@Composable
internal fun TrainingAdviceCard(advice: String) {
    if (advice.isBlank()) return
    Card(shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Обратная связь", style = MaterialTheme.typography.titleSmall, color = FatSand)
            Text(advice, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun TrainingRecordedSets(session: TrainingSession) {
    session.sets.forEach { set ->
        val load = if (set.loadBasis == TrainingLoadBasis.BODYWEIGHT) "Свой вес"
            else "${set.actualWeightKg?.let(::weightText) ?: "—"} кг · ${set.loadBasis.label()}"
        Text("${if (set.completed) "✓" else "○"} ${set.exerciseName}: $load × ${set.actualReps}" +
            (set.rpe?.let { " · RPE ${weightText(it)}" } ?: ""), style = MaterialTheme.typography.bodyMedium)
        if (set.plannedExerciseName != null) Text(
            "План: ${set.plannedExerciseName} · ${set.plannedWeightKg?.let(::weightText) ?: "—"} кг × ${set.plannedReps}",
            style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (set.symptomNote.isNotBlank()) Text("Ощущения: ${set.symptomNote}", color = FatSand)
        if (set.preference != com.k.shavrin.diethelper.domain.model.ExercisePreference.NEUTRAL) Text(set.preference.label())
        if (set.notes.isNotBlank()) Text(set.notes)
    }
}
