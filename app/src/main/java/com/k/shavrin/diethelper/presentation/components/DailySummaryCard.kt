package com.k.shavrin.diethelper.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.k.shavrin.diethelper.domain.model.DailyGoals
import com.k.shavrin.diethelper.domain.model.DailySummary
import com.k.shavrin.diethelper.presentation.util.caloriesProgressColor
import com.k.shavrin.diethelper.presentation.util.formatCalories
import com.k.shavrin.diethelper.presentation.util.formatMacro
import com.k.shavrin.diethelper.presentation.util.macroProgressColor

@Composable
fun DailySummaryCard(
    summary: DailySummary,
    goals: DailyGoals,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Итог за день",
                style = MaterialTheme.typography.titleMedium
            )
            NutrientProgress(
                label = "Калории",
                progress = if (goals.calories > 0f) (summary.totalCalories / goals.calories).coerceIn(0f, 1f) else 0f,
                color = caloriesProgressColor(summary.totalCalories, goals.calories),
                actualLabel = formatCalories(summary.totalCalories),
                goalLabel = formatCalories(goals.calories)
            )
            NutrientProgress(
                label = "Белки",
                progress = if (goals.proteinMax > 0f) (summary.totalProtein / goals.proteinMax).coerceIn(0f, 1f) else 0f,
                color = macroProgressColor(summary.totalProtein, goals.proteinMin, goals.proteinMax),
                actualLabel = formatMacro(summary.totalProtein),
                goalLabel = "${formatMacro(goals.proteinMin)}–${formatMacro(goals.proteinMax)}"
            )
            NutrientProgress(
                label = "Жиры",
                progress = if (goals.fatMax > 0f) (summary.totalFat / goals.fatMax).coerceIn(0f, 1f) else 0f,
                color = macroProgressColor(summary.totalFat, goals.fatMin, goals.fatMax),
                actualLabel = formatMacro(summary.totalFat),
                goalLabel = "${formatMacro(goals.fatMin)}–${formatMacro(goals.fatMax)}"
            )
            NutrientProgress(
                label = "Углеводы",
                progress = if (goals.carbsMax > 0f) (summary.totalCarbs / goals.carbsMax).coerceIn(0f, 1f) else 0f,
                color = macroProgressColor(summary.totalCarbs, goals.carbsMin, goals.carbsMax),
                actualLabel = formatMacro(summary.totalCarbs),
                goalLabel = "${formatMacro(goals.carbsMin)}–${formatMacro(goals.carbsMax)}"
            )
        }
    }
}

@Composable
private fun NutrientProgress(
    label: String,
    progress: Float,
    color: Color,
    actualLabel: String,
    goalLabel: String
) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "$actualLabel / $goalLabel",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(modifier = Modifier.height(0.dp))
    }
}
