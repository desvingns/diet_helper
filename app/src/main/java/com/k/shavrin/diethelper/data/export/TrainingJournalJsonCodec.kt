package com.k.shavrin.diethelper.data.export

import com.k.shavrin.diethelper.domain.model.TrainingSession
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.ZoneId

object TrainingJournalJsonCodec {
    fun encode(sessions: List<TrainingSession>): JSONObject = JSONObject().apply {
        put("schema_version", 1)
        put("source", "MyDietHelper")
        put("source_plan", "SRC7 frozen import; original waves preserved")
        put("units", JSONObject().put("load", "kg").put("duration", "minutes"))
        put("missing_data", "null means not reported; legacy completion flags have no inferred actual weights or dates")
        put("sessions", JSONArray(sessions.map { session ->
            JSONObject().apply {
                put("id", session.id)
                put("plan_day_id", session.planDayId)
                put("cycle", session.cycle)
                put("day", session.day)
                put("started_at", Instant.ofEpochMilli(session.startedAt).toString())
                put("start_basis", "first_logged_set_or_manual_finish")
                put("timezone", session.zoneId)
                val zone = runCatching { ZoneId.of(session.zoneId) }.getOrDefault(ZoneId.of("UTC"))
                put("local_date", Instant.ofEpochMilli(session.startedAt).atZone(zone).toLocalDate().toString())
                put("finished_at", session.finishedAt?.let { Instant.ofEpochMilli(it).toString() } ?: JSONObject.NULL)
                put("outcome", session.outcome.name)
                put("planned_set_count", session.plannedSetCount)
                put("legacy_set_count_without_actual_details", session.legacySetCount)
                put("gym_minutes_including_warmup_and_cooldown", session.gymMinutes ?: JSONObject.NULL)
                put("social_minutes", session.socialMinutes ?: JSONObject.NULL)
                put("training_minutes_including_set_rests", session.trainingMinutes ?: JSONObject.NULL)
                put("session_rpe", session.sessionRpe ?: JSONObject.NULL)
                put("recovery_after", session.recovery.name)
                put("notes", session.notes)
                put("next_day_recovery", session.nextDayRecovery.name)
                put("next_day_notes", session.nextDayNotes)
                put("sets", JSONArray(session.sets.map { set ->
                    JSONObject().apply {
                        put("id", set.id)
                        put("plan_key", set.planKey ?: JSONObject.NULL)
                        put("exercise_name", set.exerciseName)
                        put("planned_exercise_name", set.plannedExerciseName ?: JSONObject.NULL)
                        put("planned_weight_kg", set.plannedWeightKg ?: JSONObject.NULL)
                        put("planned_reps", set.plannedReps ?: JSONObject.NULL)
                        put("actual_weight_kg", set.actualWeightKg ?: JSONObject.NULL)
                        put("actual_reps", set.actualReps)
                        put("load_basis", set.loadBasis.name)
                        put("rpe", set.rpe ?: JSONObject.NULL)
                        put("symptom_note", set.symptomNote)
                        put("preference", set.preference.name)
                        put("notes", set.notes)
                        put("completed", set.completed)
                    }
                }))
            }
        }))
    }
}
