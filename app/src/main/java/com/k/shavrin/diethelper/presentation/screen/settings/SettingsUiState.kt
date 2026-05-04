package com.k.shavrin.diethelper.presentation.screen.settings

data class SettingsUiState(
    val calories: String = "",
    val protein: String = "",
    val fat: String = "",
    val carbs: String = "",
    val caloriesError: String? = null,
    val proteinError: String? = null,
    val fatError: String? = null,
    val carbsError: String? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val justSaved: Boolean = false
)
