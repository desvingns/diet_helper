@file:Suppress("TooManyFunctions", "LongParameterList")

package com.k.shavrin.diethelper.presentation.screen.today

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FreeBreakfast
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.k.shavrin.diethelper.domain.model.DailyGoals
import com.k.shavrin.diethelper.domain.model.DailySummary
import com.k.shavrin.diethelper.domain.model.DayStatus
import com.k.shavrin.diethelper.domain.model.FoodEntry
import com.k.shavrin.diethelper.domain.model.MealType
import com.k.shavrin.diethelper.presentation.theme.CarbsBlue
import com.k.shavrin.diethelper.presentation.theme.FatSand
import com.k.shavrin.diethelper.presentation.theme.ForestContainer
import com.k.shavrin.diethelper.presentation.theme.ForestSurface
import com.k.shavrin.diethelper.presentation.theme.Graphite
import com.k.shavrin.diethelper.presentation.theme.Ink
import com.k.shavrin.diethelper.presentation.theme.Mint
import com.k.shavrin.diethelper.presentation.theme.MutedSage
import com.k.shavrin.diethelper.presentation.theme.ProteinRose
import com.k.shavrin.diethelper.presentation.theme.SoftWhite
import com.k.shavrin.diethelper.presentation.util.formatCalories
import com.k.shavrin.diethelper.presentation.util.formatEntryAmount
import com.k.shavrin.diethelper.presentation.util.formatMacro
import com.k.shavrin.diethelper.presentation.util.mealTypeLabel
import java.text.NumberFormat
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

private val TodayBackground = Graphite
private val TodaySurface = ForestSurface
private val TodaySurfaceContainer = ForestContainer
private val TodaySurfaceHigh = Color(0xFF2D3A32)
private val TodayOnSurface = SoftWhite
private val TodayOnSurfaceVariant = MutedSage
private val TodayOutlineVariant = Color(0xFF34463A)
private val TodayPrimary = Mint
private val TodayProtein = ProteinRose
private val TodayFat = FatSand
private val TodayCarbs = CarbsBlue
private val TodayGreen = Color(0xFF81C784)
private val TodayYellow = Color(0xFFFFC107)
private val TodayRed = Color(0xFFEF5350)
private val TodayRingTrack = Color(0xFF3E624D)
private val TodayEmber = Color(0xFFFF8A3D)

private val todayDateFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale("ru"))

@Composable
internal fun TodayDesignedContent(
    state: TodayUiState.Success,
    onGoToDate: (LocalDate) -> Unit,
    onTodayClick: () -> Unit,
    onAddTo: (MealType) -> Unit,
    onUpdateMultiplier: (FoodEntry, Float) -> Unit,
    onDelete: (FoodEntry) -> Unit,
    onCopyToDay: (FoodEntry, LocalDate) -> Unit,
    mealCallbacks: MealCallbacks,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit
) {
    val expandedSections = remember { mutableStateMapOf<MealType, Boolean>() }
    LaunchedEffect(state.sections) {
        MealType.entries.forEach { type ->
            if (expandedSections[type] == null) expandedSections[type] = true
        }
    }

    var saveMealDialogForType by remember { mutableStateOf<MealType?>(null) }
    var pasteMealDialogForType by remember { mutableStateOf<MealType?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TodayBackground)
    ) {
        TodayHeader(
            date = state.date,
            weekStatuses = state.weekStatuses,
            onDateSelected = onGoToDate,
            onTodayClick = onTodayClick,
            onPreviousWeek = onPreviousWeek,
            onNextWeek = onNextWeek
        )
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .testTag("today_feed"),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item(key = "hero") {
                TodayHeroCard(
                    summary = state.summary,
                    goals = state.goals,
                    streak = state.streak
                )
            }
            item(key = "meal_label") {
                Text(
                    text = "ПРИЁМЫ ПИЩИ",
                    modifier = Modifier.padding(start = 4.dp, top = 2.dp),
                    color = TodayOnSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
            }
            MealType.entries.forEach { mealType ->
                val entries = state.sections[mealType].orEmpty()
                item(key = "header_${mealType.name}") {
                    DesignedMealCard(
                        mealType = mealType,
                        entries = entries,
                        macros = DesignedSectionMacros(
                            calories = state.sectionCalories[mealType] ?: 0f,
                            protein = state.sectionProtein[mealType] ?: 0f,
                            fat = state.sectionFat[mealType] ?: 0f,
                            carbs = state.sectionCarbs[mealType] ?: 0f
                        ),
                        isExpanded = expandedSections[mealType] ?: true,
                        hasClipboard = state.clipboard != null,
                        onToggleExpand = {
                            expandedSections[mealType] = !(expandedSections[mealType] ?: true)
                        },
                        onAdd = { onAddTo(mealType) },
                        onPaste = { pasteMealDialogForType = mealType },
                        onUpdateMultiplier = onUpdateMultiplier,
                        onDelete = onDelete,
                        onCopyToDay = onCopyToDay,
                        onCopyMeal = { mealCallbacks.onCopyMeal(mealType) },
                        onSaveMeal = { saveMealDialogForType = mealType }
                    )
                }
            }
        }
    }

    saveMealDialogForType?.let { mealType ->
        SaveMealDialog(
            mealLabel = mealTypeLabel(mealType),
            onDismiss = { saveMealDialogForType = null },
            onConfirm = { name ->
                mealCallbacks.onSaveMeal(name, mealType)
                saveMealDialogForType = null
            }
        )
    }

    pasteMealDialogForType?.let { targetMealType ->
        state.clipboard?.let { snapshot ->
            PasteMealDialog(
                snapshot = snapshot,
                onPaste = {
                    mealCallbacks.onPasteMeal(targetMealType)
                    pasteMealDialogForType = null
                },
                onDismiss = { pasteMealDialogForType = null },
                onClearClipboard = {
                    mealCallbacks.onClearClipboard()
                    pasteMealDialogForType = null
                }
            )
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun TodayHeader(
    date: LocalDate,
    weekStatuses: List<Pair<LocalDate, DayStatus>>,
    onDateSelected: (LocalDate) -> Unit,
    onTodayClick: () -> Unit,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit
) {
    val today = LocalDate.now()
    var showDatePicker by remember { mutableStateOf(false) }
    var dragAccum by remember { mutableFloatStateOf(0f) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, top = 12.dp, end = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { showDatePicker = true }
            ) {
                Text("ПИТАНИЕ И БАЛАНС", color = Mint, fontSize = 10.sp,
                    lineHeight = 14.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 2.sp)
                Text(
                    text = if (date == today) "Сегодня" else "Дневник",
                    color = TodayOnSurface,
                    fontSize = 30.sp,
                    lineHeight = 38.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formatHeaderDate(date),
                    color = TodayOnSurfaceVariant,
                    fontSize = 13.sp,
                    lineHeight = 16.sp
                )
            }
            if (date != today) {
                IconButton(onClick = onTodayClick) {
                    Icon(Icons.Filled.Today, "Вернуться к сегодня", tint = Mint)
                }
            }
            IconButton(onClick = { showDatePicker = true }) {
                Icon(
                    imageVector = Icons.Filled.CalendarMonth,
                    contentDescription = "Выбрать дату",
                    tint = TodayOnSurface
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = { dragAccum = 0f },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            dragAccum += dragAmount
                            val threshold = 80.dp.toPx()
                            when {
                                dragAccum > threshold -> {
                                    onPreviousWeek()
                                    dragAccum = 0f
                                }
                                dragAccum < -threshold -> {
                                    onNextWeek()
                                    dragAccum = 0f
                                }
                            }
                        }
                    )
                },
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            weekStatuses.forEach { (day, status) ->
                DesignedWeekDay(
                    day = day,
                    status = status,
                    selected = day == date,
                    onClick = { onDateSelected(day) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    if (showDatePicker) {
        val todayMillis = today.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val selectedMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedMillis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis <= todayMillis
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        onDateSelected(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                    showDatePicker = false
                }) { Text("Выбрать") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Отмена") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun DesignedWeekDay(
    day: LocalDate,
    status: DayStatus,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) Mint else Color.Transparent)
            .clickable(enabled = status != DayStatus.FUTURE, onClick = onClick)
            .semantics {
                contentDescription = "${formatHeaderDate(day)}, ${dayStatusLabel(status)}"
                this.selected = selected
            }
            .padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = weekDayLetter(day.dayOfWeek),
            color = if (selected) Ink else TodayOnSurfaceVariant,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .size(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(day.dayOfMonth.toString(), color = when {
                selected -> Ink
                status == DayStatus.FUTURE -> TodayOnSurfaceVariant.copy(alpha = 0.45f)
                else -> TodayOnSurface
            }, fontSize = 16.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold)
        }
        Box(Modifier.size(4.dp).clip(CircleShape).background(
            if (selected) Ink else weekDayBackground(status, false)
        ))
    }
}

private fun weekDayLetter(dayOfWeek: DayOfWeek): String = when (dayOfWeek) {
    DayOfWeek.MONDAY -> "ПН"
    DayOfWeek.TUESDAY -> "ВТ"
    DayOfWeek.WEDNESDAY -> "СР"
    DayOfWeek.THURSDAY -> "ЧТ"
    DayOfWeek.FRIDAY -> "ПТ"
    DayOfWeek.SATURDAY -> "СБ"
    DayOfWeek.SUNDAY -> "ВС"
}

private fun dayStatusLabel(status: DayStatus): String = when (status) {
    DayStatus.FUTURE -> "Будущий день"
    DayStatus.GRAY_LOGGED -> "Мало записей"
    DayStatus.GREEN -> "В пределах цели"
    DayStatus.YELLOW -> "Рядом с целью"
    DayStatus.RED -> "За пределами цели"
}

private fun weekDayBackground(status: DayStatus, selected: Boolean): Color = when {
    selected -> TodayPrimary
    status == DayStatus.GREEN -> TodayGreen
    status == DayStatus.YELLOW -> TodayYellow
    status == DayStatus.RED -> TodayRed
    status == DayStatus.GRAY_LOGGED -> Color(0xFF5C5968)
    else -> Color.Transparent
}

@Composable
private fun TodayHeroCard(summary: DailySummary, goals: DailyGoals, streak: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF2C4838), TodaySurface)))
            .border(1.dp, Color(0xFF44604C), RoundedCornerShape(28.dp))
            .padding(18.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("ЭНЕРГИЯ ДНЯ", color = Mint, fontSize = 10.sp, lineHeight = 14.sp,
                fontWeight = FontWeight.Bold, letterSpacing = 1.6.sp, modifier = Modifier.weight(1f))
            StreakChip(streak = streak)
        }
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            CalorieRing(
                consumed = summary.totalCalories,
                goal = goals.calories,
                modifier = Modifier.size(116.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickCalorieStats(summary = summary, goals = goals)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(TodaySurfaceContainer)
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            DesignedMacroBar("Белки", summary.totalProtein, goals.proteinMin, goals.proteinMax, TodayProtein, Modifier.weight(1f))
            DesignedMacroBar("Жиры", summary.totalFat, goals.fatMin, goals.fatMax, TodayFat, Modifier.weight(1f))
            DesignedMacroBar("Углеводы", summary.totalCarbs, goals.carbsMin, goals.carbsMax, TodayCarbs, Modifier.weight(1f))
        }
    }
}

@Composable
private fun CalorieRing(consumed: Float, goal: Float, modifier: Modifier = Modifier) {
    val progress = if (goal > 0f) (consumed / goal).coerceIn(0f, 1f) else 0f
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 8.dp.toPx()
            val arcSize = Size(size.width - stroke, size.height - stroke)
            val topLeft = Offset(stroke / 2f, stroke / 2f)
            drawArc(
                color = TodayRingTrack,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            drawArc(
                color = if (consumed > goal && goal > 0f) TodayEmber else Mint,
                startAngle = -90f,
                sweepAngle = progress * 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = formatNumber(consumed),
                color = TodayOnSurface,
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 34.sp
            )
            Text(
                text = "ИЗ ${formatNumber(goal)} ККАЛ",
                color = TodayOnSurfaceVariant,
                fontSize = 10.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.sp
            )
            Text(
                text = "съедено",
                color = Mint,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun StreakChip(streak: Int) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(Mint.copy(alpha = 0.09f))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.LocalFireDepartment,
            contentDescription = null,
            tint = FatSand,
            modifier = Modifier.size(17.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = if (streak == 0) "Начните серию" else "$streak ${daysWord(streak)} подряд",
            color = SoftWhite,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}

@Composable
private fun QuickCalorieStats(summary: DailySummary, goals: DailyGoals) {
    val remaining = (goals.calories - summary.totalCalories).roundToInt()
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(if (remaining >= 0) "Осталось на день" else "Сверх цели", color = MutedSage, fontSize = 12.sp, lineHeight = 16.sp)
        Text(formatNumber(kotlin.math.abs(remaining).toFloat()), color = if (remaining >= 0) SoftWhite else TodayEmber,
            fontSize = 32.sp, lineHeight = 38.sp, fontWeight = FontWeight.Bold)
        Text("ккал", color = MutedSage, fontSize = 12.sp, lineHeight = 16.sp)
        Spacer(Modifier.height(4.dp))
        Text(if (goals.calories > 0f) "${(summary.totalCalories / goals.calories * 100).roundToInt()}% дневной цели" else "Цель не задана",
            color = Mint, fontSize = 11.sp, lineHeight = 14.sp)
    }
}

@Composable
private fun DesignedMacroBar(
    label: String,
    value: Float,
    min: Float,
    max: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    val displayMaximum = (max * 1.25f).coerceAtLeast(1f)
    val progress = (value / displayMaximum).coerceIn(0f, 1f)
    val minProgress = (min / displayMaximum).coerceIn(0f, 1f)
    val maxProgress = (max / displayMaximum).coerceIn(0f, 1f)
    val actualColor = if (value > max) TodayRed else color
    Column(modifier.semantics(mergeDescendants = true) {
        contentDescription = "$label: ${value.roundToInt()} г, цель от ${min.roundToInt()} до ${max.roundToInt()} г"
    }, verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(text = label, color = color, fontSize = 11.sp, lineHeight = 14.sp, fontWeight = FontWeight.Medium)
            Text(
                text = "${value.roundToInt()} г",
                color = TodayOnSurface,
                fontSize = 18.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text("${min.roundToInt()}–${max.roundToInt()} г", color = MutedSage, fontSize = 10.sp, lineHeight = 14.sp)
        Canvas(modifier = Modifier.fillMaxWidth().height(5.dp)) {
            val corner = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            drawRoundRect(color = TodaySurfaceHigh, size = size, cornerRadius = corner)
            drawRect(
                color = TodayGreen.copy(alpha = 0.16f),
                topLeft = Offset(size.width * minProgress, 0f),
                size = Size(size.width * (maxProgress - minProgress), size.height)
            )
            drawLine(
                color = Color.White.copy(alpha = 0.18f),
                start = Offset(size.width * minProgress, 0f),
                end = Offset(size.width * minProgress, size.height)
            )
            drawLine(
                color = Color.White.copy(alpha = 0.18f),
                start = Offset(size.width * maxProgress, 0f),
                end = Offset(size.width * maxProgress, size.height)
            )
            drawRoundRect(
                color = actualColor,
                size = Size(size.width * progress, size.height),
                cornerRadius = corner
            )
        }
    }
}

private data class DesignedSectionMacros(
    val calories: Float,
    val protein: Float,
    val fat: Float,
    val carbs: Float
)

@Composable
private fun DesignedMealCard(
    mealType: MealType,
    entries: List<FoodEntry>,
    macros: DesignedSectionMacros,
    isExpanded: Boolean,
    hasClipboard: Boolean,
    onToggleExpand: () -> Unit,
    onAdd: () -> Unit,
    onPaste: () -> Unit,
    onUpdateMultiplier: (FoodEntry, Float) -> Unit,
    onDelete: (FoodEntry) -> Unit,
    onCopyToDay: (FoodEntry, LocalDate) -> Unit,
    onCopyMeal: () -> Unit,
    onSaveMeal: () -> Unit
) {
    val mealTint = mealColor(mealType)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(TodaySurface)
            .border(1.dp, TodayOutlineVariant, RoundedCornerShape(24.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggleExpand),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(mealTint.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = mealIcon(mealType),
                    contentDescription = null,
                    tint = mealTint,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = mealTypeLabel(mealType),
                        color = TodayOnSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (entries.isNotEmpty()) {
                        Text(
                            text = " · ${entries.size} шт",
                            color = TodayOnSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }
                Text(
                    text = if (entries.isEmpty()) {
                        "Пока пусто"
                    } else {
                        "${formatCalories(macros.calories)} · Б ${macroNumber(macros.protein)} · " +
                            "Ж ${macroNumber(macros.fat)} · У ${macroNumber(macros.carbs)}"
                    },
                    color = TodayOnSurfaceVariant,
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }
            if (hasClipboard) {
                IconButton(onClick = onPaste, modifier = Modifier.size(38.dp)) {
                    Icon(
                        imageVector = Icons.Filled.ContentPaste,
                        contentDescription = "Вставить",
                        tint = TodayOnSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            IconButton(
                onClick = onAdd,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (entries.isEmpty()) TodayPrimary else TodaySurfaceHigh)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Добавить",
                    tint = if (entries.isEmpty()) Ink else Mint
                )
            }
        }

        if (isExpanded && entries.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            entries.forEachIndexed { index, entry ->
                DesignedFoodEntryRow(
                    entry = entry,
                    onUpdateMultiplier = { multiplier -> onUpdateMultiplier(entry, multiplier) },
                    onDelete = { onDelete(entry) },
                    onCopyToDay = { date -> onCopyToDay(entry, date) }
                )
                if (index < entries.lastIndex) {
                    Box(
                        modifier = Modifier
                            .padding(start = 20.dp)
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(TodayOutlineVariant.copy(alpha = 0.35f))
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onCopyMeal) {
                    Text("Копировать", color = TodayOnSurfaceVariant, fontSize = 12.sp)
                }
                TextButton(onClick = onSaveMeal) {
                    Text("Сохранить", color = TodayOnSurfaceVariant, fontSize = 12.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DesignedFoodEntryRow(
    entry: FoodEntry,
    onUpdateMultiplier: (Float) -> Unit,
    onDelete: () -> Unit,
    onCopyToDay: (LocalDate) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showCopyDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { menuExpanded = true },
                onLongClick = { menuExpanded = true }
            )
            .padding(vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            MacroPill(entry)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.product.name,
                    color = TodayOnSurface,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${formatEntryAmount(entry)} · Б ${macroNumber(entry.product.proteinPer100g * entry.multiplier)} · " +
                        "Ж ${macroNumber(entry.product.fatPer100g * entry.multiplier)} · " +
                        "У ${macroNumber(entry.product.carbsPer100g * entry.multiplier)}",
                    color = TodayOnSurfaceVariant,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = formatCalories(entry.product.caloriesPer100g * entry.multiplier),
                color = TodayOnSurface,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
            modifier = Modifier.background(TodaySurfaceHigh)
        ) {
            DropdownMenuItem(
                text = { Text("Изменить граммы", color = TodayOnSurface) },
                onClick = {
                    menuExpanded = false
                    showEditDialog = true
                }
            )
            DropdownMenuItem(
                text = { Text("Удалить", color = TodayOnSurface) },
                onClick = {
                    menuExpanded = false
                    showDeleteDialog = true
                }
            )
            DropdownMenuItem(
                text = { Text("Скопировать на другой день", color = TodayOnSurface) },
                onClick = {
                    menuExpanded = false
                    showCopyDialog = true
                }
            )
        }
    }

    if (showEditDialog) {
        EditGramsDialog(
            initialGrams = entry.product.amountForMultiplier(entry.multiplier).roundToInt(),
            initialAmount = entry.product.amountForMultiplier(entry.multiplier),
            amountLabel = if (entry.product.portionLabel.isNotBlank()) "Количество порций" else if (entry.product.unit == "мл") "Миллилитры" else "Граммы",
            onDismiss = { showEditDialog = false },
            onConfirm = { grams ->
                onUpdateMultiplier(entry.product.multiplierForAmount(grams))
                showEditDialog = false
            }
        )
    }
    if (showDeleteDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удалить запись?") },
            text = { Text("Запись «${entry.product.name}» будет удалена.") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteDialog = false
                }) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Отмена") }
            }
        )
    }
    if (showCopyDialog) {
        CopyToDateDialog(
            initialDate = entry.date,
            onDismiss = { showCopyDialog = false },
            onConfirm = { date ->
                onCopyToDay(date)
                showCopyDialog = false
            }
        )
    }
}

@Composable
private fun MacroPill(entry: FoodEntry) {
    val protein = entry.product.proteinPer100g * entry.multiplier
    val fat = entry.product.fatPer100g * entry.multiplier
    val carbs = entry.product.carbsPer100g * entry.multiplier
    Column(
        modifier = Modifier
            .width(8.dp)
            .height(36.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(TodaySurfaceHigh)
    ) {
        if (protein > 0f) {
            Box(modifier = Modifier.fillMaxWidth().weight(protein).background(TodayProtein))
        }
        if (fat > 0f) {
            Box(modifier = Modifier.fillMaxWidth().weight(fat).background(TodayFat))
        }
        if (carbs > 0f) {
            Box(modifier = Modifier.fillMaxWidth().weight(carbs).background(TodayCarbs))
        }
    }
}

private fun mealIcon(mealType: MealType): ImageVector = when (mealType) {
    MealType.BREAKFAST -> Icons.Filled.FreeBreakfast
    MealType.LUNCH -> Icons.Filled.Restaurant
    MealType.DINNER -> Icons.Filled.DinnerDining
    MealType.SNACK -> Icons.Filled.Fastfood
}

private fun mealColor(mealType: MealType): Color = when (mealType) {
    MealType.BREAKFAST -> Color(0xFFE1A66B)
    MealType.LUNCH -> Color(0xFF9CCC65)
    MealType.DINNER -> Color(0xFF7E9AE0)
    MealType.SNACK -> Color(0xFFCE93D8)
}

private fun macroNumber(value: Float): String = formatMacro(value).removeSuffix(" г")

private fun formatHeaderDate(date: LocalDate): String =
    date.format(todayDateFormatter).replaceFirstChar { first ->
        if (first.isLowerCase()) first.titlecase(Locale("ru")) else first.toString()
    }

private fun formatNumber(value: Float): String =
    NumberFormat.getIntegerInstance(Locale("ru"))
        .format(value.roundToInt())
        .replace('\u00A0', ' ')
        .replace('\u202F', ' ')

private fun daysWord(count: Int): String = when {
    count % 10 == 1 && count % 100 != 11 -> "день"
    count % 10 in 2..4 && count % 100 !in 12..14 -> "дня"
    else -> "дней"
}
