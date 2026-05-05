package com.k.shavrin.diethelper.presentation.screen.settings

data class SettingsUiState(
    val calories: String = "",
    val proteinMin: String = "",
    val proteinMax: String = "",
    val fatMin: String = "",
    val fatMax: String = "",
    val carbsMin: String = "",
    val carbsMax: String = "",
    val caloriesError: String? = null,
    val proteinMinError: String? = null,
    val proteinMaxError: String? = null,
    val fatMinError: String? = null,
    val fatMaxError: String? = null,
    val carbsMinError: String? = null,
    val carbsMaxError: String? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val justSaved: Boolean = false
)
