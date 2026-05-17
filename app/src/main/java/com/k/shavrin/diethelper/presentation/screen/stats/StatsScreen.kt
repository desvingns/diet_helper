@file:Suppress("TooManyFunctions")

package com.k.shavrin.diethelper.presentation.screen.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.k.shavrin.diethelper.domain.model.StatsDayItem
import com.k.shavrin.diethelper.presentation.screen.today.ErrorState
import com.k.shavrin.diethelper.presentation.screen.today.LoadingState
import com.k.shavrin.diethelper.presentation.util.formatCalories
import com.k.shavrin.diethelper.presentation.util.formatDate
import com.k.shavrin.diethelper.presentation.util.formatMacro
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val StatsProteinColor = Color(0xFFF07070)
private val StatsFatColor = Color(0xFFF5B731)
private val StatsCarbsColor = Color(0xFF64C8E8)
private val StatsCaloriesColor = Color(0xFFFF7043)
private val shortDateFormatter = DateTimeFormatter.ofPattern("dd.MM")

@Composable
fun StatsScreen(viewModel: StatsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    StatsContent(
        state = state,
        onSetFrom = viewModel::setFrom,
        onSetTo = viewModel::setTo,
        onPreset = viewModel::selectPreset,
        onCurrentMonth = viewModel::selectCurrentMonth
    )
}

@Composable
internal fun StatsContent(
    state: StatsUiState,
    onSetFrom: (LocalDate) -> Unit,
    onSetTo: (LocalDate) -> Unit,
    onPreset: (Int) -> Unit,
    onCurrentMonth: () -> Unit
) {
    when (state) {
        StatsUiState.Loading -> LoadingState()
        is StatsUiState.Error -> ErrorState(state.message)
        is StatsUiState.Success -> StatsSuccessContent(
            state = state,
            onSetFrom = onSetFrom,
            onSetTo = onSetTo,
            onPreset = onPreset,
            onCurrentMonth = onCurrentMonth
        )
    }
}

@Composable
private fun StatsSuccessContent(
    state: StatsUiState.Success,
    onSetFrom: (LocalDate) -> Unit,
    onSetTo: (LocalDate) -> Unit,
    onPreset: (Int) -> Unit,
    onCurrentMonth: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        DateRangeRow(from = state.from, to = state.to, onSetFrom = onSetFrom, onSetTo = onSetTo)
        PresetChipsRow(onPreset = onPreset, onCurrentMonth = onCurrentMonth)
        TotalsCard(state = state)
        if (state.items.isEmpty()) {
            EmptyChart()
        } else {
            ChartCard(items = state.items)
        }
        ChartLegend()
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateRangeRow(
    from: LocalDate,
    to: LocalDate,
    onSetFrom: (LocalDate) -> Unit,
    onSetTo: (LocalDate) -> Unit
) {
    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DateButton(
            label = "С: ${formatDate(from)}",
            modifier = Modifier.weight(1f),
            onClick = { showFromPicker = true }
        )
        Text("—", style = MaterialTheme.typography.bodyMedium)
        DateButton(
            label = "По: ${formatDate(to)}",
            modifier = Modifier.weight(1f),
            onClick = { showToPicker = true }
        )
    }

    if (showFromPicker) {
        val initMs = from.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = initMs,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) = true
            }
        )
        DatePickerDialog(
            onDismissRequest = { showFromPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { ms ->
                        onSetFrom(Instant.ofEpochMilli(ms).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                    showFromPicker = false
                }) { Text("Готово") }
            },
            dismissButton = {
                TextButton(onClick = { showFromPicker = false }) { Text("Отмена") }
            }
        ) { DatePicker(state = pickerState) }
    }

    if (showToPicker) {
        val initMs = to.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = initMs,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) = true
            }
        )
        DatePickerDialog(
            onDismissRequest = { showToPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { ms ->
                        onSetTo(Instant.ofEpochMilli(ms).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                    showToPicker = false
                }) { Text("Готово") }
            },
            dismissButton = {
                TextButton(onClick = { showToPicker = false }) { Text("Отмена") }
            }
        ) { DatePicker(state = pickerState) }
    }
}

@Composable
private fun DateButton(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun PresetChipsRow(onPreset: (Int) -> Unit, onCurrentMonth: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf(7 to "7 дней", 14 to "14 дней", 30 to "30 дней").forEach { (days, label) ->
            FilterChip(
                selected = false,
                onClick = { onPreset(days) },
                label = { Text(label, style = MaterialTheme.typography.labelSmall) }
            )
        }
        FilterChip(
            selected = false,
            onClick = { onCurrentMonth() },
            label = { Text("Месяц", style = MaterialTheme.typography.labelSmall) }
        )
    }
}

@Composable
private fun TotalsCard(state: StatsUiState.Success) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = formatCalories(state.totalCalories),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = StatsCaloriesColor
                )
                Text(
                    text = "за период",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                MacroTotal("Б", state.totalProtein, StatsProteinColor)
                MacroTotal("Ж", state.totalFat, StatsFatColor)
                MacroTotal("У", state.totalCarbs, StatsCarbsColor)
            }
        }
    }
}

@Composable
private fun MacroTotal(label: String, grams: Float, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = formatMacro(grams),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptyChart() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Нет данных за выбранный период",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ChartCard(items: List<StatsDayItem>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "БЖУ по дням",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "— калории",
                    style = MaterialTheme.typography.labelMedium,
                    color = StatsCaloriesColor
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            MacroBarLineChart(items = items, modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .padding(start = 40.dp, end = 16.dp, bottom = 28.dp))
        }
    }
}

@Composable
private fun MacroBarLineChart(items: List<StatsDayItem>, modifier: Modifier = Modifier) {
    val onSurfaceArgb = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()
    val gridArgb = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f).toArgb()

    Canvas(modifier = modifier) {
        if (items.isEmpty()) return@Canvas

        val chartW = size.width
        val chartH = size.height

        val maxMacro = items.maxOf { it.protein + it.fat + it.carbs }.coerceAtLeast(1f)
        val maxCal = items.maxOf { it.calories }.coerceAtLeast(1f)

        val barCount = items.size
        val barSlot = chartW / barCount
        val barW = barSlot * 0.55f
        val barOffset = barSlot * 0.225f

        val gridLines = 4
        repeat(gridLines + 1) { i ->
            val y = chartH * i / gridLines
            drawLine(
                color = Color(gridArgb),
                start = Offset(0f, y),
                end = Offset(chartW, y),
                strokeWidth = 1f
            )
            val label = (maxMacro * (gridLines - i) / gridLines).toInt().toString()
            drawContext.canvas.nativeCanvas.drawText(
                label,
                -2f,
                y + 12f,
                android.graphics.Paint().apply {
                    color = onSurfaceArgb
                    textSize = 28f
                    textAlign = android.graphics.Paint.Align.RIGHT
                }
            )
        }

        items.forEachIndexed { idx, day ->
            val x = idx * barSlot + barOffset
            val totalMacro = day.protein + day.fat + day.carbs

            val proteinH = if (totalMacro > 0) chartH * (day.protein / maxMacro) else 0f
            val fatH = if (totalMacro > 0) chartH * (day.fat / maxMacro) else 0f
            val carbsH = if (totalMacro > 0) chartH * (day.carbs / maxMacro) else 0f

            var stackTop = chartH

            if (carbsH > 0f) {
                stackTop -= carbsH
                drawRect(
                    color = StatsCarbsColor,
                    topLeft = Offset(x, stackTop),
                    size = Size(barW, carbsH)
                )
            }
            if (fatH > 0f) {
                stackTop -= fatH
                drawRect(
                    color = StatsFatColor,
                    topLeft = Offset(x, stackTop),
                    size = Size(barW, fatH)
                )
            }
            if (proteinH > 0f) {
                stackTop -= proteinH
                drawRect(
                    color = StatsProteinColor,
                    topLeft = Offset(x, stackTop),
                    size = Size(barW, proteinH)
                )
            }

            val label = day.date.format(shortDateFormatter)
            drawContext.canvas.nativeCanvas.drawText(
                label,
                x + barW / 2f,
                chartH + 36f,
                android.graphics.Paint().apply {
                    color = onSurfaceArgb
                    textSize = 26f
                    textAlign = android.graphics.Paint.Align.CENTER
                }
            )
        }

        val calPath = Path()
        items.forEachIndexed { idx, day ->
            val cx = idx * barSlot + barOffset + barW / 2f
            val cy = chartH - (chartH * day.calories / maxCal)
            if (idx == 0) calPath.moveTo(cx, cy) else calPath.lineTo(cx, cy)
        }
        drawPath(
            path = calPath,
            color = StatsCaloriesColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
        items.forEachIndexed { idx, day ->
            val cx = idx * barSlot + barOffset + barW / 2f
            val cy = chartH - (chartH * day.calories / maxCal)
            drawCircle(color = StatsCaloriesColor, radius = 4.dp.toPx(), center = Offset(cx, cy))
            drawCircle(
                color = Color.White.copy(alpha = 0.9f),
                radius = 2.dp.toPx(),
                center = Offset(cx, cy)
            )
        }
    }
}

@Composable
private fun ChartLegend() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(
            "Белки" to StatsProteinColor,
            "Жиры" to StatsFatColor,
            "Углеводы" to StatsCarbsColor
        ).forEach { (label, color) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = label, style = MaterialTheme.typography.labelSmall)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(16.dp)
                    .height(3.dp)
                    .background(StatsCaloriesColor)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "Калории", style = MaterialTheme.typography.labelSmall)
        }
    }
}
