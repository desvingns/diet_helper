package com.k.shavrin.diethelper.presentation.screen.history

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.k.shavrin.diethelper.R
import com.k.shavrin.diethelper.domain.model.HistoryItem
import com.k.shavrin.diethelper.presentation.components.ScreenHeading
import com.k.shavrin.diethelper.presentation.screen.today.ErrorState
import com.k.shavrin.diethelper.presentation.screen.today.LoadingState
import com.k.shavrin.diethelper.presentation.util.formatCalories
import com.k.shavrin.diethelper.presentation.util.formatDate
import com.k.shavrin.diethelper.presentation.util.formatIsoDate

@Composable
fun HistoryScreen(
    onNavigateToDay: (date: String) -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(Modifier.fillMaxSize()) {
        ScreenHeading("ВАШ ДНЕВНИК", "История", "Каждый день — часть вашего пути",
            Modifier.padding(horizontal = 20.dp, vertical = 16.dp), artwork = R.drawable.art_journey)
        when (val s = state) {
            HistoryUiState.Loading -> LoadingState()
            is HistoryUiState.Error -> ErrorState(s.message)
            is HistoryUiState.Success -> {
                if (s.items.isEmpty()) {
                    EmptyHistoryState()
                } else {
                    HistoryList(items = s.items, onItemClick = { item ->
                        onNavigateToDay(formatIsoDate(item.date))
                    })
                }
            }
        }
    }
}

@Composable
private fun HistoryList(items: List<HistoryItem>, onItemClick: (HistoryItem) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(items, key = { it.date.toEpochDay() }) { item ->
            Card(onClick = { onItemClick(item) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.size(46.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center) {
                        Text(item.date.dayOfMonth.toString(), color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = formatDate(item.date),
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = formatCalories(item.totalCalories),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Icon(Icons.Filled.ChevronRight, "Открыть день", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun EmptyHistoryState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.art_journey),
                contentDescription = null,
                modifier = Modifier.size(144.dp)
            )
            Text(text = "История пуста", style = MaterialTheme.typography.titleMedium)
            Text("Добавьте первый продукт в разделе «Сегодня»",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
