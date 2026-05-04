package com.k.shavrin.diethelper.presentation.screen.product

import com.k.shavrin.diethelper.domain.model.MealType
import com.k.shavrin.diethelper.domain.model.Product
import java.time.LocalDate

sealed interface ProductUiState {
    data object Loading : ProductUiState

    data class Success(
        val date: LocalDate,
        val mealType: MealType,
        val query: String,
        val products: List<Product>,
        val hasExactMatch: Boolean
    ) : ProductUiState

    data class Error(val message: String) : ProductUiState
}
