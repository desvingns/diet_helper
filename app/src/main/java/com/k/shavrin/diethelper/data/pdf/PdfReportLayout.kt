package com.k.shavrin.diethelper.data.pdf

import kotlin.math.abs

/**
 * Pure-Kotlin layout primitives for PDF rendering.
 * No Android imports — the values consumed by android.graphics primitives in [PdfReportRenderer].
 */
internal object PdfReportLayout {
    // A4 portrait in PDF points (1/72 inch).
    const val PAGE_WIDTH = 595
    const val PAGE_HEIGHT = 842

    const val MARGIN = 36f
    const val FOOTER_HEIGHT = 24f

    const val TITLE_SIZE = 28f
    const val SUBTITLE_SIZE = 12f
    const val RANGE_SIZE = 14f
    const val TIMESTAMP_SIZE = 10f
    const val SECTION_HEADER_SIZE = 14f
    const val BODY_SIZE = 11f
    const val SMALL_BODY_SIZE = 10f
    const val FOOTER_SIZE = 9f

    const val LINE_SPACING = 14f
    const val SECTION_SPACING = 18f
    const val TABLE_ROW_HEIGHT = 18f

    // Palette (ARGB as Int).
    const val COLOR_ACCENT = 0xFFFF7043.toInt()
    const val COLOR_TEXT_PRIMARY = 0xFF222222.toInt()
    const val COLOR_TEXT_SECONDARY = 0xFF666666.toInt()
    const val COLOR_TEXT_MUTED = 0xFF999999.toInt()
    const val COLOR_ZEBRA = 0xFFF5F5F5.toInt()
    const val COLOR_DIVIDER = 0xFFE0E0E0.toInt()
    const val COLOR_GOAL_LINE = 0xFFB0BEC5.toInt()
    const val COLOR_BAR = 0xFFFF7043.toInt()

    // Per-day labels (Russian).
    const val MEAL_LABEL_BREAKFAST = "Завтрак"
    const val MEAL_LABEL_LUNCH = "Обед"
    const val MEAL_LABEL_DINNER = "Ужин"
    const val MEAL_LABEL_SNACK = "Перекус"
}

/**
 * Pure Kotlin: deviation ratio between actual and goal calories.
 * 0 means actual == goal (best); 1 means very far (max deviation, saturates).
 * Returns 0f if [goal] is non-positive (no goal set → no deviation signal).
 */
internal fun calorieDeviationRatio(actual: Float, goal: Float): Float {
    if (goal <= 0f) return 0f
    val ratio = abs(actual - goal) / goal
    return ratio.coerceIn(0f, 1f)
}
