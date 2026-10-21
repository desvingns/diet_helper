package com.k.shavrin.diethelper.presentation.screen.workout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.k.shavrin.diethelper.domain.model.PerformedTrainingSet
import com.k.shavrin.diethelper.domain.model.TrainingLoadBasis
import com.k.shavrin.diethelper.domain.model.TrainingSession
import com.k.shavrin.diethelper.domain.model.WorkoutDay
import com.k.shavrin.diethelper.domain.model.WorkoutSet
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
internal fun TrainingSessionTimerCard(session: TrainingSession?) {
    val now = trainingClock(session)
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Вся тренировка", style = MaterialTheme.typography.titleMedium)
            Text(trainingDuration(session?.elapsedMillis(now) ?: 0),
                style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)
            Text(if (session == null) "Начнётся вместе с разминкой"
                else if (session.warmUpStartedAt != session.startedAt) "С первого записанного подхода"
                else "Включая разминку, отдых и заминку", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
internal fun TrainingWarmUpCard(
    day: WorkoutDay,
    session: TrainingSession?,
    enabled: Boolean,
    actions: TrainingJournalActions
) {
    val now = trainingClock(session)
    val started = session?.warmUpStartedAt != null
    val done = session?.warmUpFinishedAt != null
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(if (done) "Разминка завершена ✓" else "Разминка", style = MaterialTheme.typography.titleLarge)
            Text("Твоя стандартная разминка", style = MaterialTheme.typography.bodyMedium)
            Text(session?.warmUpMillis(now)?.let(::trainingDuration) ?: "00:00", style = MaterialTheme.typography.headlineMedium)
            if (!done && session?.coolDownStartedAt == null) Button(
                onClick = { if (started) actions.finishWarmUp(day) else actions.startWarmUp(day) },
                enabled = enabled, modifier = Modifier.fillMaxWidth()
            ) { Text(if (started) "Завершить разминку" else "Начать разминку") }
        }
    }
}

@Composable
internal fun TrainingCoolDownCard(
    day: WorkoutDay,
    session: TrainingSession?,
    enabled: Boolean,
    actions: TrainingJournalActions
) {
    val now = trainingClock(session)
    val started = session?.coolDownStartedAt != null
    val done = session?.coolDownFinishedAt != null
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(if (done) "Заминка завершена ✓" else "Заминка", style = MaterialTheme.typography.titleLarge)
            Text(session?.coolDownMillis(now)?.let(::trainingDuration) ?: "00:00", style = MaterialTheme.typography.headlineMedium)
            if (!done) Button(
                onClick = { if (started) actions.finishCoolDown(day) else actions.startCoolDown(day) },
                enabled = enabled && session != null && (session.warmUpStartedAt == null || session.warmUpFinishedAt != null),
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (started) "Завершить заминку" else "Начать заминку") }
            Text("После заминки — обратная связь за всё занятие.", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun trainingClock(session: TrainingSession?): Long {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(session?.id, session?.finishedAt, session?.coolDownFinishedAt) {
        now = System.currentTimeMillis()
        while (session != null && session.finishedAt == null && session.coolDownFinishedAt == null) {
            delay(1000)
            now = System.currentTimeMillis()
        }
    }
    return now
}

internal fun trainingDuration(millis: Long): String {
    val seconds = millis.coerceAtLeast(0) / 1000
    return if (seconds >= 3600) String.format(Locale.ROOT, "%d:%02d:%02d", seconds / 3600, seconds / 60 % 60, seconds % 60)
        else String.format(Locale.ROOT, "%02d:%02d", seconds / 60, seconds % 60)
}

internal fun plannedTrainingSet(set: WorkoutSet, maxima: Map<String, Double>): PerformedTrainingSet {
    val load = if (set.exercise.loadBasis == TrainingLoadBasis.BODYWEIGHT) null
        else set.exercise.weight(maxima, set.group.percent)
    return PerformedTrainingSet(planKey = set.key, exerciseName = set.exercise.name,
        plannedExerciseName = set.exercise.name, plannedWeightKg = load, plannedReps = set.group.reps,
        actualWeightKg = load, actualReps = set.group.reps, loadBasis = set.exercise.loadBasis)
}

internal fun canRecordTrainingSet(session: TrainingSession?, editable: Boolean): Boolean =
    editable && session != null && session.coolDownStartedAt == null &&
        (session.warmUpStartedAt == null || session.warmUpFinishedAt != null)

internal fun shouldShowTrainingFeedback(session: TrainingSession?, finished: Boolean): Boolean =
    !finished && session?.coolDownFinishedAt != null
