package com.k.shavrin.diethelper.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.k.shavrin.diethelper.domain.model.ReturnToTrainingPlan
import com.k.shavrin.diethelper.domain.model.WorkoutDay
import com.k.shavrin.diethelper.domain.model.WorkoutExercise
import com.k.shavrin.diethelper.domain.model.WorkoutGroup
import com.k.shavrin.diethelper.domain.model.WorkoutPlan
import com.k.shavrin.diethelper.domain.model.WorkoutProgram
import com.k.shavrin.diethelper.domain.model.WorkoutProgress
import com.k.shavrin.diethelper.domain.repository.WorkoutRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataStore: DataStore<Preferences>
) : WorkoutRepository {
    override val progress = dataStore.data.map { prefs ->
        val program = WorkoutProgram.fromId(prefs[PROGRAM])
        WorkoutProgress(
            maxima = BASES.associateWith { prefs[doublePreferencesKey("workout_max_$it")] ?: 0.0 },
            completedDays = prefs[stringSetPreferencesKey(key(program, "completed_days"))].orEmpty().mapNotNull { it.toIntOrNull() }.toSet(),
            completedSets = prefs[stringSetPreferencesKey(key(program, "completed_sets"))].orEmpty(),
            restSeconds = prefs[intPreferencesKey(key(program, "rest_seconds"))] ?: 120,
            restDeadline = prefs[longPreferencesKey(key(program, "rest_deadline"))] ?: 0,
            pausedRestSeconds = prefs[intPreferencesKey(key(program, "rest_paused"))] ?: 0,
            program = program,
            returnWeekLoads = (1..6).mapNotNull { week ->
                val loads = BASES.mapNotNull { base -> prefs[doublePreferencesKey("return_week_${week}_$base")]?.let { base to it } }.toMap()
                loads.takeIf { it.size == BASES.size }?.let { week to it }
            }.toMap()
        )
    }

    override suspend fun getPlan(program: WorkoutProgram): WorkoutPlan = withContext(Dispatchers.IO) {
        if (program == WorkoutProgram.RETURN) return@withContext ReturnToTrainingPlan.create()
        val root = JSONObject(context.assets.open("workout-plan.json").bufferedReader().use { it.readText() })
        val days = root.getJSONArray("days")
        WorkoutPlan(root.getString("title"), List(days.length()) { i ->
            val day = days.getJSONObject(i)
            val exercises = day.getJSONArray("exercises")
            WorkoutDay(day.getInt("id"), day.getInt("cycle"), day.getInt("day"), day.getString("notes"),
                List(exercises.length()) { j ->
                    val exercise = exercises.getJSONObject(j)
                    val groups = exercise.getJSONArray("groups")
                    WorkoutExercise(
                        exercise.getInt("id"), exercise.getString("name"), exercise.getString("load"),
                        exercise.getString("base"), exercise.getDouble("factor"), exercise.getDouble("rounding"),
                        exercise.getDouble("equipmentMultiplier"), List(groups.length()) { k ->
                            val group = groups.getJSONObject(k)
                            WorkoutGroup(group.getInt("reps"), group.getInt("sets"), group.getDouble("percent"))
                        }
                    )
                }
            )
        })
    }

    override suspend fun selectProgram(program: WorkoutProgram) {
        dataStore.edit { it[PROGRAM] = program.id }
    }

    override suspend fun saveReturnWeekLoads(week: Int, loads: Map<String, Double>) {
        require(week in 1..6 && BASES.all { base -> loads[base]?.let { it.isFinite() && it > 0 && it <= 1500 } == true })
        dataStore.edit { prefs ->
            BASES.forEach { base -> prefs[doublePreferencesKey("return_week_${week}_$base")] = loads.getValue(base) }
        }
    }

    override suspend fun saveMaxima(maxima: Map<String, Double>) {
        require(BASES.all { (maxima[it] ?: 0.0).let { value -> value.isFinite() && value > 0 && value <= 1500 } })
        dataStore.edit { prefs ->
            BASES.forEach { prefs[doublePreferencesKey("workout_max_$it")] = maxima.getValue(it) }
        }
    }

    override suspend fun setCompleted(key: String, completed: Boolean, restDeadline: Long, program: WorkoutProgram) {
        dataStore.edit { prefs ->
            val sets = stringSetPreferencesKey(key(program, "completed_sets"))
            val current = prefs[sets].orEmpty()
            prefs[sets] = if (completed) current + key else current - key
            prefs[longPreferencesKey(key(program, "rest_deadline"))] = restDeadline
            prefs[intPreferencesKey(key(program, "rest_paused"))] = 0
        }
    }

    override suspend fun completeDay(id: Int, program: WorkoutProgram) {
        dataStore.edit { prefs ->
            val days = stringSetPreferencesKey(key(program, "completed_days"))
            prefs[days] = prefs[days].orEmpty() + id.toString()
            prefs[longPreferencesKey(key(program, "rest_deadline"))] = 0
            prefs[intPreferencesKey(key(program, "rest_paused"))] = 0
        }
    }

    override suspend fun setRest(seconds: Int, deadline: Long, pausedSeconds: Int, program: WorkoutProgram) {
        require(seconds in 15..900)
        dataStore.edit { prefs ->
            prefs[intPreferencesKey(key(program, "rest_seconds"))] = seconds
            prefs[longPreferencesKey(key(program, "rest_deadline"))] = deadline
            prefs[intPreferencesKey(key(program, "rest_paused"))] = pausedSeconds
        }
    }

    companion object {
        private val BASES = listOf("squat", "bench", "deadlift")
        private val PROGRAM = stringPreferencesKey("workout_selected_program")
        private fun key(program: WorkoutProgram, suffix: String) =
            if (program == WorkoutProgram.SRC7) "workout_$suffix" else "return_$suffix"
    }
}
