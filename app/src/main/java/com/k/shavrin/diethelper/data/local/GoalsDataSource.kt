package com.k.shavrin.diethelper.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import com.k.shavrin.diethelper.domain.model.DailyGoals
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoalsDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    val goalsFlow: Flow<DailyGoals> = dataStore.data.map { prefs ->
        DailyGoals(
            calories = prefs[KEY_CALORIES] ?: DailyGoals.DEFAULT.calories,
            protein = prefs[KEY_PROTEIN] ?: DailyGoals.DEFAULT.protein,
            fat = prefs[KEY_FAT] ?: DailyGoals.DEFAULT.fat,
            carbs = prefs[KEY_CARBS] ?: DailyGoals.DEFAULT.carbs
        )
    }

    suspend fun saveGoals(goals: DailyGoals) {
        dataStore.edit { prefs ->
            prefs[KEY_CALORIES] = goals.calories
            prefs[KEY_PROTEIN] = goals.protein
            prefs[KEY_FAT] = goals.fat
            prefs[KEY_CARBS] = goals.carbs
        }
    }

    suspend fun isSeeded(): Boolean = dataStore.data.first()[KEY_IS_SEEDED] == true

    suspend fun setSeeded() {
        dataStore.edit { prefs -> prefs[KEY_IS_SEEDED] = true }
    }

    companion object {
        const val FILE_NAME = "goals"

        private val KEY_CALORIES = floatPreferencesKey("goal_calories")
        private val KEY_PROTEIN = floatPreferencesKey("goal_protein")
        private val KEY_FAT = floatPreferencesKey("goal_fat")
        private val KEY_CARBS = floatPreferencesKey("goal_carbs")
        private val KEY_IS_SEEDED = booleanPreferencesKey("is_seeded")
    }
}
