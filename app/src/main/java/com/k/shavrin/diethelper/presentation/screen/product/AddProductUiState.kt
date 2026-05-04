package com.k.shavrin.diethelper.presentation.screen.product

data class AddProductUiState(
    val name: String = "",
    val calories: String = "",
    val protein: String = "",
    val fat: String = "",
    val carbs: String = "",
    val nameError: String? = null,
    val caloriesError: String? = null,
    val proteinError: String? = null,
    val fatError: String? = null,
    val carbsError: String? = null,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false
)
