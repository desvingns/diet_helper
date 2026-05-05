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
            proteinMin = prefs[KEY_PROTEIN_MIN] ?: DailyGoals.DEFAULT.proteinMin,
            proteinMax = prefs[KEY_PROTEIN_MAX] ?: DailyGoals.DEFAULT.proteinMax,
            fatMin = prefs[KEY_FAT_MIN] ?: DailyGoals.DEFAULT.fatMin,
            fatMax = prefs[KEY_FAT_MAX] ?: DailyGoals.DEFAULT.fatMax,
            carbsMin = prefs[KEY_CARBS_MIN] ?: DailyGoals.DEFAULT.carbsMin,
            carbsMax = prefs[KEY_CARBS_MAX] ?: DailyGoals.DEFAULT.carbsMax
        )
    }

    suspend fun saveGoals(goals: DailyGoals) {
        dataStore.edit { prefs ->
            prefs[KEY_CALORIES] = goals.calories
            prefs[KEY_PROTEIN_MIN] = goals.proteinMin
            prefs[KEY_PROTEIN_MAX] = goals.proteinMax
            prefs[KEY_FAT_MIN] = goals.fatMin
            prefs[KEY_FAT_MAX] = goals.fatMax
            prefs[KEY_CARBS_MIN] = goals.carbsMin
            prefs[KEY_CARBS_MAX] = goals.carbsMax
        }
    }

    suspend fun isSeeded(): Boolean = dataStore.data.first()[KEY_IS_SEEDED] == true

    suspend fun setSeeded() {
        dataStore.edit { prefs -> prefs[KEY_IS_SEEDED] = true }
    }

    companion object {
        const val FILE_NAME = "goals"

        private val KEY_CALORIES = floatPreferencesKey("goal_calories")
        private val KEY_PROTEIN_MIN = floatPreferencesKey("goal_protein_min")
        private val KEY_PROTEIN_MAX = floatPreferencesKey("goal_protein_max")
        private val KEY_FAT_MIN = floatPreferencesKey("goal_fat_min")
        private val KEY_FAT_MAX = floatPreferencesKey("goal_fat_max")
        private val KEY_CARBS_MIN = floatPreferencesKey("goal_carbs_min")
        private val KEY_CARBS_MAX = floatPreferencesKey("goal_carbs_max")
        private val KEY_IS_SEEDED = booleanPreferencesKey("is_seeded")
    }
}
