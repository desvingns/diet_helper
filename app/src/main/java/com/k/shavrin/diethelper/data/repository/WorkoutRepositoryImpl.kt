package com.k.shavrin.diethelper.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.k.shavrin.diethelper.domain.model.WorkoutDay
import com.k.shavrin.diethelper.domain.model.WorkoutExercise
import com.k.shavrin.diethelper.domain.model.WorkoutGroup
import com.k.shavrin.diethelper.domain.model.WorkoutPlan
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
        WorkoutProgress(
            maxima = BASES.associateWith { prefs[doublePreferencesKey("workout_max_$it")] ?: 0.0 },
            completedDays = prefs[DAYS].orEmpty().mapNotNull { it.toIntOrNull() }.toSet(),
            completedSets = prefs[SETS].orEmpty(),
            restSeconds = prefs[REST] ?: 120,
            restDeadline = prefs[DEADLINE] ?: 0,
            pausedRestSeconds = prefs[PAUSED] ?: 0
        )
    }

    override suspend fun getPlan(): WorkoutPlan = withContext(Dispatchers.IO) {
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

    override suspend fun saveMaxima(maxima: Map<String, Double>) {
        require(BASES.all { (maxima[it] ?: 0.0).let { value -> value.isFinite() && value > 0 && value <= 1500 } })
        dataStore.edit { prefs ->
            BASES.forEach { prefs[doublePreferencesKey("workout_max_$it")] = maxima.getValue(it) }
        }
    }

    override suspend fun setCompleted(key: String, completed: Boolean, restDeadline: Long) {
        dataStore.edit { prefs ->
            val current = prefs[SETS].orEmpty()
            prefs[SETS] = if (completed) current + key else current - key
            prefs[DEADLINE] = restDeadline
            prefs[PAUSED] = 0
        }
    }

    override suspend fun completeDay(id: Int) {
        dataStore.edit { prefs ->
            prefs[DAYS] = prefs[DAYS].orEmpty() + id.toString()
            prefs[DEADLINE] = 0
            prefs[PAUSED] = 0
        }
    }

    override suspend fun setRest(seconds: Int, deadline: Long, pausedSeconds: Int) {
        require(seconds in 15..900)
        dataStore.edit { prefs ->
            prefs[REST] = seconds
            prefs[DEADLINE] = deadline
            prefs[PAUSED] = pausedSeconds
        }
    }

    companion object {
        private val BASES = listOf("squat", "bench", "deadlift")
        private val DAYS = stringSetPreferencesKey("workout_completed_days")
        private val SETS = stringSetPreferencesKey("workout_completed_sets")
        private val REST = intPreferencesKey("workout_rest_seconds")
        private val DEADLINE = longPreferencesKey("workout_rest_deadline")
        private val PAUSED = intPreferencesKey("workout_rest_paused")
    }
}
