package com.k.shavrin.diethelper.presentation.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.k.shavrin.diethelper.presentation.screen.today.LoadingState

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    if (state.isLoading) {
        LoadingState()
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Дневные цели",
            style = MaterialTheme.typography.titleLarge
        )

        OutlinedTextField(
            value = state.calories,
            onValueChange = viewModel::onCaloriesChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Калории, ккал") },
            isError = state.caloriesError != null,
            supportingText = state.caloriesError?.let { msg -> { Text(msg) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )
        OutlinedTextField(
            value = state.protein,
            onValueChange = viewModel::onProteinChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Белки, г") },
            isError = state.proteinError != null,
            supportingText = state.proteinError?.let { msg -> { Text(msg) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )
        OutlinedTextField(
            value = state.fat,
            onValueChange = viewModel::onFatChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Жиры, г") },
            isError = state.fatError != null,
            supportingText = state.fatError?.let { msg -> { Text(msg) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )
        OutlinedTextField(
            value = state.carbs,
            onValueChange = viewModel::onCarbsChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Углеводы, г") },
            isError = state.carbsError != null,
            supportingText = state.carbsError?.let { msg -> { Text(msg) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )

        Button(
            onClick = viewModel::save,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isSaving
        ) { Text("Сохранить") }

        if (state.justSaved) {
            Text(
                text = "Сохранено",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
