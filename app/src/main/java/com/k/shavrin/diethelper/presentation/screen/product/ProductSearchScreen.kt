package com.k.shavrin.diethelper.presentation.screen.product

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.k.shavrin.diethelper.domain.model.Product
import com.k.shavrin.diethelper.presentation.screen.today.ErrorState
import com.k.shavrin.diethelper.presentation.screen.today.LoadingState
import com.k.shavrin.diethelper.presentation.util.formatCalories
import com.k.shavrin.diethelper.presentation.util.mealTypeLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductSearchScreen(
    date: String,
    mealType: String,
    onNavigateBack: () -> Unit,
    onNavigateToAddProduct: (name: String) -> Unit,
    viewModel: ProductViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val query by viewModel.searchQuery.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Поиск продукта") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::setQuery,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Название продукта") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) }
            )

            when (val s = state) {
                ProductUiState.Loading -> LoadingState()
                is ProductUiState.Error -> ErrorState(s.message)
                is ProductUiState.Success -> ProductSearchContent(
                    state = s,
                    onProductClick = { product ->
                        // dialog handled via remembered state inside the row
                    },
                    onToggleFavorite = viewModel::toggleFavorite,
                    onAddEntry = { product, grams ->
                        viewModel.addEntry(product, grams, onNavigateBack)
                    },
                    onAddNewProduct = onNavigateToAddProduct
                )
            }
        }
    }
}

@Composable
private fun ProductSearchContent(
    state: ProductUiState.Success,
    onProductClick: (Product) -> Unit,
    onToggleFavorite: (Product) -> Unit,
    onAddEntry: (Product, Float) -> Unit,
    onAddNewProduct: (String) -> Unit
) {
    val isAllEmpty = state.products.isEmpty() && state.query.isBlank()
    if (isAllEmpty) {
        EmptyProductsState(onAdd = { onAddNewProduct("") })
        return
    }

    var selected by remember { mutableStateOf<Product?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
    ) {
        items(state.products, key = { it.id }) { product ->
            ProductRow(
                product = product,
                onClick = { selected = product; onProductClick(product) },
                onToggleFavorite = { onToggleFavorite(product) }
            )
        }

        if (state.query.isNotBlank() && !state.hasExactMatch) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { onAddNewProduct(state.query) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    Text("Добавить «${state.query}» как новый продукт")
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    selected?.let { product ->
        AddEntryDialog(
            product = product,
            mealLabel = mealTypeLabel(state.mealType),
            onDismiss = { selected = null },
            onConfirm = { grams ->
                onAddEntry(product, grams)
                selected = null
            }
        )
    }
}

@Composable
private fun ProductRow(
    product: Product,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = product.name, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = "${formatCalories(product.caloriesPer100g)} / 100 г",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onToggleFavorite) {
            Icon(
                imageVector = if (product.isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                contentDescription = if (product.isFavorite) "Убрать из избранного" else "Добавить в избранное",
                tint = if (product.isFavorite) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AddEntryDialog(
    product: Product,
    mealLabel: String,
    onDismiss: () -> Unit,
    onConfirm: (grams: Float) -> Unit
) {
    var text by remember { mutableStateOf("100") }
    val parsed = text.toFloatOrNull()
    val isValid = parsed != null && parsed > 0f

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Добавить в «$mealLabel»") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = product.name, style = MaterialTheme.typography.bodyLarge)
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Граммы") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = !isValid
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { parsed?.let(onConfirm) },
                enabled = isValid
            ) { Text("Добавить") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}

@Composable
private fun EmptyProductsState(onAdd: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                modifier = Modifier.height(64.dp)
            )
            Text(text = "Нет продуктов", style = MaterialTheme.typography.titleMedium)
            Button(onClick = onAdd) { Text("Добавить") }
        }
    }
}
