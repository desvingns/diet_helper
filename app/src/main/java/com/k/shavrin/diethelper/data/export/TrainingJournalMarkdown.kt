package com.k.shavrin.diethelper.data.export

import com.k.shavrin.diethelper.domain.model.TrainingLoadBasis
import com.k.shavrin.diethelper.domain.model.TrainingSession
import java.time.Instant
import java.time.ZoneId

object TrainingJournalMarkdown {
    fun render(sessions: List<TrainingSession>): String = buildString {
        appendLine("# MyDietHelper — training journal")
        appendLine()
        appendLine("Original SRC7 waves are preserved. Actual sets are separate from the plan.")
        appendLine("Unknown values are not inferred. Start time is the first logged set or a manual finish, not gym arrival.")
        sessions.forEach { session ->
            val zone = runCatching { ZoneId.of(session.zoneId) }.getOrDefault(ZoneId.of("UTC"))
            val date = Instant.ofEpochMilli(session.startedAt).atZone(zone).toLocalDate()
            appendLine()
            appendLine("## $date — cycle ${session.cycle}, day ${session.day} — ${session.outcome}")
            appendLine("ID: ${session.id}; timezone: ${session.zoneId}")
            appendLine("Gym: ${session.gymMinutes ?: "unknown"} min; social: ${session.socialMinutes ?: "unknown"} min; " +
                "training including set rests: ${session.trainingMinutes ?: "unknown"} min")
            appendLine("Session RPE: ${session.sessionRpe ?: "unknown"}; after: ${session.recovery}; next day: ${session.nextDayRecovery}")
            appendLine("Legacy marked sets without actual details: ${session.legacySetCount}")
            session.sets.forEach { set ->
                val weight = if (set.loadBasis == TrainingLoadBasis.BODYWEIGHT) "bodyweight"
                else "${set.actualWeightKg ?: "unknown"} kg (${set.loadBasis})"
                appendLine("- ${set.exerciseName}: $weight × ${set.actualReps}; RPE ${set.rpe ?: "unknown"}; " +
                    "preference ${set.preference}; completed=${set.completed}")
                if (set.plannedExerciseName != null) {
                    appendLine("  Planned: ${set.plannedExerciseName}, ${set.plannedWeightKg ?: "unknown"} kg × ${set.plannedReps ?: "unknown"}")
                }
                if (set.symptomNote.isNotBlank()) appendLine("  Reported sensations: ${set.symptomNote}")
                if (set.notes.isNotBlank()) appendLine("  Note: ${set.notes}")
            }
            if (session.notes.isNotBlank()) appendLine("Session note: ${session.notes}")
            if (session.nextDayNotes.isNotBlank()) appendLine("Next-day note: ${session.nextDayNotes}")
        }
    }
}
