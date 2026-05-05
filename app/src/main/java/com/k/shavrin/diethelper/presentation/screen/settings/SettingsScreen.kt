package com.k.shavrin.diethelper.presentation.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.k.shavrin.diethelper.presentation.screen.today.LoadingState
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    if (state.isLoading) {
        LoadingState()
        return
    }

    SettingsContent(
        state = state,
        onCaloriesChange = viewModel::onCaloriesChange,
        onProteinMinChange = viewModel::onProteinMinChange,
        onProteinMaxChange = viewModel::onProteinMaxChange,
        onFatMinChange = viewModel::onFatMinChange,
        onFatMaxChange = viewModel::onFatMaxChange,
        onCarbsMinChange = viewModel::onCarbsMinChange,
        onCarbsMaxChange = viewModel::onCarbsMaxChange,
        onSave = viewModel::save
    )
}

@Composable
fun SettingsContent(
    state: SettingsUiState,
    onCaloriesChange: (String) -> Unit,
    onProteinMinChange: (String) -> Unit,
    onProteinMaxChange: (String) -> Unit,
    onFatMinChange: (String) -> Unit,
    onFatMaxChange: (String) -> Unit,
    onCarbsMinChange: (String) -> Unit,
    onCarbsMaxChange: (String) -> Unit,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Дневные цели",
            style = MaterialTheme.typography.titleLarge
        )

        OutlinedTextField(
            value = state.calories,
            onValueChange = onCaloriesChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Калории, ккал") },
            isError = state.caloriesError != null,
            supportingText = state.caloriesError?.let { msg -> { Text(msg) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            trailingIcon = if (state.showMacroCalorieWarningLow) {
                { MacroCalorieWarningIcon("Калорий больше чем нижние границы БЖУ") }
            } else null
        )

        MacroRangeRow(
            label = "Белки, г",
            minValue = state.proteinMin,
            maxValue = state.proteinMax,
            onMinChange = onProteinMinChange,
            onMaxChange = onProteinMaxChange,
            minError = state.proteinMinError,
            maxError = state.proteinMaxError
        )

        MacroRangeRow(
            label = "Жиры, г",
            minValue = state.fatMin,
            maxValue = state.fatMax,
            onMinChange = onFatMinChange,
            onMaxChange = onFatMaxChange,
            minError = state.fatMinError,
            maxError = state.fatMaxError
        )

        MacroRangeRow(
            label = "Углеводы, г",
            minValue = state.carbsMin,
            maxValue = state.carbsMax,
            onMinChange = onCarbsMinChange,
            onMaxChange = onCarbsMaxChange,
            minError = state.carbsMinError,
            maxError = state.carbsMaxError
        )

        Button(
            onClick = onSave,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MacroCalorieWarningIcon(tooltipText: String) {
    val tooltipState = rememberTooltipState()
    val scope = rememberCoroutineScope()
    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = { PlainTooltip { Text(tooltipText) } },
        state = tooltipState
    ) {
        IconButton(onClick = { scope.launch { tooltipState.show() } }) {
            Icon(
                imageVector = Icons.Outlined.Warning,
                contentDescription = tooltipText,
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun MacroRangeRow(
    label: String,
    minValue: String,
    maxValue: String,
    onMinChange: (String) -> Unit,
    onMaxChange: (String) -> Unit,
    minError: String?,
    maxError: String?
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = minValue,
                onValueChange = onMinChange,
                modifier = Modifier.weight(1f),
                label = { Text("Мин") },
                isError = minError != null,
                supportingText = minError?.let { msg -> { Text(msg) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
            OutlinedTextField(
                value = maxValue,
                onValueChange = onMaxChange,
                modifier = Modifier.weight(1f),
                label = { Text("Макс") },
                isError = maxError != null,
                supportingText = maxError?.let { msg -> { Text(msg) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
        }
    }
}
