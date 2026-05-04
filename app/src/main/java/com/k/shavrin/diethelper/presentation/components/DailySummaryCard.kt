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
import androidx.compose.ui.unit.dp
import com.k.shavrin.diethelper.domain.model.DailyGoals
import com.k.shavrin.diethelper.domain.model.DailySummary
import com.k.shavrin.diethelper.presentation.util.formatCalories
import com.k.shavrin.diethelper.presentation.util.formatMacro

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
                actual = summary.totalCalories,
                goal = goals.calories,
                actualLabel = formatCalories(summary.totalCalories),
                goalLabel = formatCalories(goals.calories)
            )
            NutrientProgress(
                label = "Белки",
                actual = summary.totalProtein,
                goal = goals.protein,
                actualLabel = formatMacro(summary.totalProtein),
                goalLabel = formatMacro(goals.protein)
            )
            NutrientProgress(
                label = "Жиры",
                actual = summary.totalFat,
                goal = goals.fat,
                actualLabel = formatMacro(summary.totalFat),
                goalLabel = formatMacro(goals.fat)
            )
            NutrientProgress(
                label = "Углеводы",
                actual = summary.totalCarbs,
                goal = goals.carbs,
                actualLabel = formatMacro(summary.totalCarbs),
                goalLabel = formatMacro(goals.carbs)
            )
        }
    }
}

@Composable
private fun NutrientProgress(
    label: String,
    actual: Float,
    goal: Float,
    actualLabel: String,
    goalLabel: String
) {
    val progress = if (goal > 0f) (actual / goal).coerceIn(0f, 1f) else 0f
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
                .height(6.dp)
        )
        Spacer(modifier = Modifier.height(0.dp))
    }
}
