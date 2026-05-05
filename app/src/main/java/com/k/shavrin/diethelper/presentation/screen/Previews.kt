package com.k.shavrin.diethelper.presentation.screen

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.k.shavrin.diethelper.domain.model.DailyGoals
import com.k.shavrin.diethelper.domain.model.DailySummary
import com.k.shavrin.diethelper.domain.model.FoodEntry
import com.k.shavrin.diethelper.domain.model.HistoryItem
import com.k.shavrin.diethelper.domain.model.MealType
import com.k.shavrin.diethelper.domain.model.Product
import com.k.shavrin.diethelper.domain.model.WeightEntry
import com.k.shavrin.diethelper.presentation.components.DailySummaryCard
import com.k.shavrin.diethelper.presentation.screen.today.TodayContent
import com.k.shavrin.diethelper.presentation.screen.today.TodayUiState
import com.k.shavrin.diethelper.presentation.theme.DietHelperTheme
import java.time.LocalDate

@Preview(name = "Today Light", showBackground = true)
@Preview(name = "Today Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TodayScreenPreview() {
    val product = Product(
        id = 1, name = "Гречка варёная",
        caloriesPer100g = 110f, proteinPer100g = 4.5f, fatPer100g = 1f, carbsPer100g = 22f,
        isFavorite = true
    )
    val date = LocalDate.now()
    val entries = listOf(
        FoodEntry(id = 1, productId = 1, product = product, date = date, mealType = MealType.BREAKFAST, multiplier = 1.5f),
        FoodEntry(id = 2, productId = 1, product = product, date = date, mealType = MealType.LUNCH, multiplier = 2f)
    )
    val sections = MealType.values().associateWith { type -> entries.filter { it.mealType == type } }
    val sectionCalories = sections.mapValues { (_, list) -> list.sumOf { (it.product.caloriesPer100g * it.multiplier).toDouble() }.toFloat() }
    val sectionProtein = sections.mapValues { (_, list) -> list.sumOf { (it.product.proteinPer100g * it.multiplier).toDouble() }.toFloat() }
    val sectionFat = sections.mapValues { (_, list) -> list.sumOf { (it.product.fatPer100g * it.multiplier).toDouble() }.toFloat() }
    val sectionCarbs = sections.mapValues { (_, list) -> list.sumOf { (it.product.carbsPer100g * it.multiplier).toDouble() }.toFloat() }
    val state = TodayUiState.Success(
        date = date,
        canGoForward = false,
        sections = sections,
        sectionCalories = sectionCalories,
        sectionProtein = sectionProtein,
        sectionFat = sectionFat,
        sectionCarbs = sectionCarbs,
        summary = DailySummary(385f, 15.75f, 3.5f, 77f),
        goals = DailyGoals(
            calories = 2000f,
            proteinMin = 120f, proteinMax = 180f,
            fatMin = 55f, fatMax = 80f,
            carbsMin = 200f, carbsMax = 280f
        )
    )
    DietHelperTheme {
        Surface { TodayContent(
            state = state,
            onPreviousDay = {},
            onNextDay = {},
            onAddTo = {},
            onUpdateMultiplier = { _, _ -> },
            onDelete = {},
            onCopyToDay = { _, _ -> },
            readOnly = false
        ) }
    }
}

@Preview(name = "DailySummary Light", showBackground = true)
@Preview(name = "DailySummary Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DailySummaryCardPreview() {
    DietHelperTheme {
        Surface {
            DailySummaryCard(
                summary = DailySummary(1500f, 100f, 50f, 180f),
                goals = DailyGoals(
                    calories = 2000f,
                    proteinMin = 120f, proteinMax = 180f,
                    fatMin = 55f, fatMax = 80f,
                    carbsMin = 200f, carbsMax = 280f
                )
            )
        }
    }
}

@Preview(name = "EmptyHistory Light", showBackground = true)
@Preview(name = "EmptyHistory Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EmptyStatePreview() {
    DietHelperTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("История пуста")
            }
        }
    }
}

@Preview(name = "Weight Sample Light", showBackground = true)
@Preview(name = "Weight Sample Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun WeightSamplePreview() {
    @Suppress("UNUSED_VARIABLE")
    val sample = listOf(
        WeightEntry(id = 1, date = LocalDate.now(), weightKg = 78.5f),
        WeightEntry(id = 2, date = LocalDate.now().minusDays(1), weightKg = 79.0f)
    )
    DietHelperTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Weight preview")
            }
        }
    }
}

@Preview(name = "History Sample Light", showBackground = true)
@Preview(name = "History Sample Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HistoryItemPreview() {
    @Suppress("UNUSED_VARIABLE")
    val sample = HistoryItem(date = LocalDate.now(), totalCalories = 1742.5f)
    DietHelperTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("History preview")
            }
        }
    }
}
