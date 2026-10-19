package com.k.shavrin.diethelper.presentation.screen.product

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.k.shavrin.diethelper.domain.model.Product
import com.k.shavrin.diethelper.domain.repository.FoodMarket
import com.k.shavrin.diethelper.presentation.util.formatCalories
import com.k.shavrin.diethelper.presentation.util.formatMacro

@Composable
fun FoodCatalogScreen(onAdded: () -> Unit, viewModel: FoodCatalogViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    FoodCatalogContent(state, viewModel::query, viewModel::market, viewModel::lidl,
        viewModel::search, { product, amount -> viewModel.add(product, amount, onAdded) })
}

@Composable
fun FoodCatalogContent(
    state: FoodCatalogUiState,
    onQuery: (String) -> Unit,
    onMarket: (FoodMarket) -> Unit,
    onLidl: (Boolean) -> Unit,
    onSearch: () -> Unit,
    onAdd: (Product, Float) -> Unit
) {
    var selected by remember { mutableStateOf<Product?>(null) }
    val uriHandler = LocalUriHandler.current
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            OutlinedTextField(value = state.query, onValueChange = onQuery, modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Название или штрихкод") }, singleLine = true,
                shape = RoundedCornerShape(16.dp), leadingIcon = { Icon(Icons.Default.Search, null) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch() }))
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FoodMarket.entries.forEach { market ->
                    FilterChip(selected = state.market == market, onClick = { onMarket(market) }, label = { Text(market.label) })
                }
                FilterChip(selected = state.lidlOnly, onClick = { onLidl(!state.lidlOnly) }, label = { Text("Только Lidl") })
            }
            Button(onClick = onSearch, enabled = state.query.trim().length >= 2 && !state.loading && !state.saving,
                modifier = Modifier.fillMaxWidth().height(48.dp)) { Text("Найти продукты") }
            Text("Поиск по названию на упаковке. Например: mleko, Milbona, Pilos. Штрихкод ищется по всему миру.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 10.dp))
        }
        if (state.loading) item {
            Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        }
        state.error?.let { message -> item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Column(Modifier.padding(16.dp)) {
                    Text(message)
                    TextButton(onClick = onSearch) { Text("Повторить") }
                }
            }
        } }
        if (!state.searched) item {
            Card(shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.TravelExplore, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(40.dp))
                    Text("Привычные продукты, где бы вы ни были", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Товары из Сербии и России, марки Lidl и поиск по штрихкоду. Добавленные продукты сохранятся в вашей базе и будут доступны без интернета.")
                }
            }
        }
        if (state.searched && !state.loading && state.error == null && state.products.isEmpty()) item {
            Text("Продукты с полными данными КБЖУ не найдены. Попробуйте название на упаковке, другой регион или выключите фильтр Lidl.",
                modifier = Modifier.padding(vertical = 20.dp))
        }
        items(state.products, key = { it.barcode }) { product ->
            Card(onClick = { selected = product }, shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(product.name, style = MaterialTheme.typography.titleMedium)
                    Text("${formatCalories(product.caloriesPer100g)} / 100 г", color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold)
                    Text("Б ${formatMacro(product.proteinPer100g)} · Ж ${formatMacro(product.fatPer100g)} · У ${formatMacro(product.carbsPer100g)}",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            TextButton(onClick = { uriHandler.openUri("https://world.openfoodfacts.org/terms-of-use") }) { Text("Open Food Facts · открытые данные ODbL") }
        }
    }
    selected?.let { product ->
        AddEntryDialog(product = product, mealLabel = "дневник", onDismiss = { selected = null },
            onConfirm = { amount -> selected = null; onAdd(product, amount) })
    }
}
