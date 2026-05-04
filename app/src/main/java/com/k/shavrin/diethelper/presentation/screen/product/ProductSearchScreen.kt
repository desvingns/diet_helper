package com.k.shavrin.diethelper.presentation.screen.product

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun ProductSearchScreen(
    date: String,
    mealType: String,
    onNavigateBack: () -> Unit,
    onNavigateToAddProduct: (name: String) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Поиск продукта (date=$date, meal=$mealType)")
    }
}
