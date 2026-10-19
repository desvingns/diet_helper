package com.k.shavrin.diethelper.presentation.screen.weight

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.k.shavrin.diethelper.domain.model.WeightEntry
import com.k.shavrin.diethelper.presentation.util.formatDate
import com.k.shavrin.diethelper.presentation.util.formatWeight

@Composable
internal fun WeightTrendCard(entries: List<WeightEntry>) {
    val points = remember(entries) { entries.sortedByDescending { it.date }.take(30).reversed() }
    if (points.isEmpty()) return
    val latest = points.last()
    val color = MaterialTheme.colorScheme.primary
    val grid = MaterialTheme.colorScheme.outlineVariant
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("ТЕКУЩИЙ ВЕС", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(formatWeight(latest.weightKg), style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold, color = color)
            Text(if (points.size == 1) "Ещё одно взвешивание — и появится график" else "Динамика · последние ${points.size} измерений",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (points.size > 1) {
                val low = points.minOf { it.weightKg } - 0.5f
                val high = points.maxOf { it.weightKg } + 0.5f
                val description = points.joinToString("; ") { "${formatDate(it.date)}: ${formatWeight(it.weightKg)}" }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Мин. ${formatWeight(points.minOf { it.weightKg })}", style = MaterialTheme.typography.labelSmall)
                    Text("Макс. ${formatWeight(points.maxOf { it.weightKg })}", style = MaterialTheme.typography.labelSmall)
                }
                Canvas(Modifier.fillMaxWidth().height(120.dp).semantics { contentDescription = "График веса. $description" }) {
                    val inset = 5.dp.toPx()
                    val width = size.width - inset * 2
                    val height = size.height - inset * 2
                    val days = (latest.date.toEpochDay() - points.first().date.toEpochDay()).coerceAtLeast(1)
                    val positions = points.map { entry ->
                        Offset(inset + width * (entry.date.toEpochDay() - points.first().date.toEpochDay()).toFloat() / days,
                            inset + height * (1f - (entry.weightKg - low) / (high - low)))
                    }
                    listOf(0f, 0.5f, 1f).forEach { fraction ->
                        drawLine(grid, Offset(inset, inset + height * fraction), Offset(size.width - inset, inset + height * fraction), 1.dp.toPx())
                    }
                    val line = Path().apply {
                        positions.forEachIndexed { index, point -> if (index == 0) moveTo(point.x, point.y) else lineTo(point.x, point.y) }
                    }
                    val area = Path().apply {
                        addPath(line)
                        lineTo(positions.last().x, size.height)
                        lineTo(positions.first().x, size.height)
                        close()
                    }
                    drawPath(area, Brush.verticalGradient(listOf(color.copy(alpha = 0.2f), Color.Transparent)))
                    drawPath(line, color, style = Stroke(2.5.dp.toPx(), cap = StrokeCap.Round))
                    positions.forEach { drawCircle(color, 3.dp.toPx(), it) }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(formatDate(points.first().date), style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(formatDate(latest.date), style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
