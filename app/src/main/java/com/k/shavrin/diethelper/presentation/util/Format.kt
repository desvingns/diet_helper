package com.k.shavrin.diethelper.presentation.util

import com.k.shavrin.diethelper.domain.model.MealType
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale("ru"))
private val isoDateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
private val ruLocale = Locale("ru")

private val dayOfWeekNames = mapOf(
    java.time.DayOfWeek.MONDAY to "Понедельник",
    java.time.DayOfWeek.TUESDAY to "Вторник",
    java.time.DayOfWeek.WEDNESDAY to "Среда",
    java.time.DayOfWeek.THURSDAY to "Четверг",
    java.time.DayOfWeek.FRIDAY to "Пятница",
    java.time.DayOfWeek.SATURDAY to "Суббота",
    java.time.DayOfWeek.SUNDAY to "Воскресенье"
)

private val monthGenitiveNames = mapOf(
    1 to "января",
    2 to "февраля",
    3 to "марта",
    4 to "апреля",
    5 to "мая",
    6 to "июня",
    7 to "июля",
    8 to "августа",
    9 to "сентября",
    10 to "октября",
    11 to "ноября",
    12 to "декабря"
)

fun formatDate(date: LocalDate): String = date.format(dateFormatter)

fun formatIsoDate(date: LocalDate): String = date.format(isoDateFormatter)

fun parseIsoDate(value: String): LocalDate = LocalDate.parse(value, isoDateFormatter)

fun formatGrams(multiplier: Float): String {
    val grams = (multiplier * 100f).roundToInt()
    return "$grams г"
}

fun formatCalories(value: Float): String = "${value.roundToInt()} ккал"

fun formatMacro(value: Float, unit: String = "г"): String {
    val rounded = (value * 10f).roundToInt() / 10f
    return if (rounded % 1f == 0f) "${rounded.toInt()} $unit" else "$rounded $unit"
}

fun formatWeight(value: Float): String {
    val rounded = (value * 10f).roundToInt() / 10f
    return if (rounded % 1f == 0f) "${rounded.toInt()} кг" else "$rounded кг"
}

fun mealTypeLabel(mealType: MealType): String = when (mealType) {
    MealType.BREAKFAST -> "Завтрак"
    MealType.LUNCH -> "Обед"
    MealType.DINNER -> "Ужин"
    MealType.SNACK -> "Перекус"
}

fun formatWeekDateHeader(date: LocalDate): String {
    val dayName = dayOfWeekNames[date.dayOfWeek] ?: ""
    val monthName = monthGenitiveNames[date.monthValue] ?: ""
    return "$dayName, $monthName ${date.dayOfMonth}"
}
