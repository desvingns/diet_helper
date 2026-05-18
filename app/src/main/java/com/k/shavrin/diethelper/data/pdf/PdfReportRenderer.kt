@file:Suppress("TooManyFunctions")

package com.k.shavrin.diethelper.data.pdf

import android.content.Context
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.k.shavrin.diethelper.domain.model.DailyGoals
import com.k.shavrin.diethelper.domain.model.MealType
import com.k.shavrin.diethelper.domain.model.ReportData
import com.k.shavrin.diethelper.domain.model.ReportDay
import com.k.shavrin.diethelper.domain.model.ReportEntry
import com.k.shavrin.diethelper.domain.model.ReportStats
import com.k.shavrin.diethelper.domain.repository.ReportRenderer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt

private val ruLocale = Locale("ru")
private val isoDateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
private val fileNameDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
private val humanDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", ruLocale)
private val longDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", ruLocale)
private val shortDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM", ruLocale)
private val timestampFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm", ruLocale)

private const val FILE_PROVIDER_SUFFIX = ".fileprovider"
private const val REPORT_DIR_NAME = "reports"

class PdfReportRenderer @Inject constructor(
    @ApplicationContext private val context: Context
) : ReportRenderer {

    override suspend fun render(data: ReportData): Uri = withContext(Dispatchers.IO) {
        val document = PdfDocument()
        val ctx = PdfPageContext(document)
        try {
            startPage(ctx, data)
            drawTitle(ctx, data)
            drawGoalsCard(ctx, data.goals)
            drawSummaryTable(ctx, data)
            if (data.config.mode == com.k.shavrin.diethelper.domain.model.ExportMode.DETAILED) {
                drawPerDaySections(ctx, data)
            }
            data.stats?.let { drawStatsSection(ctx, it, data) }
            finishPage(ctx)

            val file = writeDocumentToFile(document, data)
            FileProvider.getUriForFile(context, "${context.packageName}$FILE_PROVIDER_SUFFIX", file)
        } finally {
            document.close()
        }
    }

    // ── Page lifecycle ──────────────────────────────────────────────────────

    private fun startPage(ctx: PdfPageContext, data: ReportData) {
        ctx.pageIndex += 1
        val info = PdfDocument.PageInfo
            .Builder(PdfReportLayout.PAGE_WIDTH, PdfReportLayout.PAGE_HEIGHT, ctx.pageIndex)
            .create()
        val page = ctx.document.startPage(info)
        ctx.page = page
        ctx.canvas = page.canvas
        ctx.currentY = PdfReportLayout.MARGIN
        if (ctx.pageIndex > 1) {
            drawPageHeader(ctx, data)
        }
    }

    private fun finishPage(ctx: PdfPageContext) {
        drawPageFooter(ctx)
        ctx.document.finishPage(ctx.requirePage())
        ctx.page = null
        ctx.canvas = null
    }

    private fun ensureSpace(ctx: PdfPageContext, data: ReportData, neededHeight: Float) {
        val bottomLimit = PdfReportLayout.PAGE_HEIGHT - PdfReportLayout.MARGIN - PdfReportLayout.FOOTER_HEIGHT
        if (ctx.currentY + neededHeight > bottomLimit) {
            finishPage(ctx)
            startPage(ctx, data)
        }
    }

    // ── Header / footer ─────────────────────────────────────────────────────

    private fun drawPageHeader(ctx: PdfPageContext, data: ReportData) {
        val canvas = ctx.requireCanvas()
        val paint = paint(PdfReportLayout.SMALL_BODY_SIZE, PdfReportLayout.COLOR_TEXT_SECONDARY)
        val range = formatHeaderRange(data.config.from, data.config.to)
        canvas.drawText("Diet Helper · $range", PdfReportLayout.MARGIN, ctx.currentY + PdfReportLayout.SMALL_BODY_SIZE, paint)
        ctx.currentY += PdfReportLayout.SMALL_BODY_SIZE + 6f
        canvas.drawLine(
            PdfReportLayout.MARGIN,
            ctx.currentY,
            PdfReportLayout.PAGE_WIDTH - PdfReportLayout.MARGIN,
            ctx.currentY,
            paint(1f, PdfReportLayout.COLOR_DIVIDER)
        )
        ctx.currentY += 10f
    }

    private fun drawPageFooter(ctx: PdfPageContext) {
        val canvas = ctx.requireCanvas()
        val y = (PdfReportLayout.PAGE_HEIGHT - PdfReportLayout.MARGIN).toFloat()
        val leftPaint = paint(PdfReportLayout.FOOTER_SIZE, PdfReportLayout.COLOR_TEXT_MUTED)
        canvas.drawText("Diet Helper · стр. ${ctx.pageIndex}", PdfReportLayout.MARGIN, y, leftPaint)
        val rightPaint = paint(PdfReportLayout.FOOTER_SIZE, PdfReportLayout.COLOR_TEXT_MUTED).apply {
            textAlign = Paint.Align.RIGHT
        }
        val ts = LocalDateTime.now().format(timestampFormatter)
        canvas.drawText(ts, PdfReportLayout.PAGE_WIDTH - PdfReportLayout.MARGIN, y, rightPaint)
    }

    // ── Title ───────────────────────────────────────────────────────────────

    private fun drawTitle(ctx: PdfPageContext, data: ReportData) {
        val canvas = ctx.requireCanvas()
        val titlePaint = paint(PdfReportLayout.TITLE_SIZE, PdfReportLayout.COLOR_TEXT_PRIMARY, bold = true)
        ctx.currentY += PdfReportLayout.TITLE_SIZE
        canvas.drawText("DIET HELPER", PdfReportLayout.MARGIN, ctx.currentY, titlePaint)
        ctx.currentY += 6f

        val subPaint = paint(PdfReportLayout.SUBTITLE_SIZE, PdfReportLayout.COLOR_TEXT_SECONDARY)
        ctx.currentY += PdfReportLayout.SUBTITLE_SIZE
        canvas.drawText("Отчёт за период", PdfReportLayout.MARGIN, ctx.currentY, subPaint)
        ctx.currentY += 6f

        val rangePaint = paint(PdfReportLayout.RANGE_SIZE, PdfReportLayout.COLOR_TEXT_PRIMARY, bold = true)
        ctx.currentY += PdfReportLayout.RANGE_SIZE
        canvas.drawText(formatHeaderRange(data.config.from, data.config.to), PdfReportLayout.MARGIN, ctx.currentY, rangePaint)
        ctx.currentY += 4f

        val tsPaint = paint(PdfReportLayout.TIMESTAMP_SIZE, PdfReportLayout.COLOR_TEXT_MUTED)
        ctx.currentY += PdfReportLayout.TIMESTAMP_SIZE
        canvas.drawText(
            "Сформировано: ${LocalDateTime.now().format(timestampFormatter)}",
            PdfReportLayout.MARGIN,
            ctx.currentY,
            tsPaint
        )
        ctx.currentY += PdfReportLayout.SECTION_SPACING
    }

    // ── Goals card ──────────────────────────────────────────────────────────

    private fun drawGoalsCard(ctx: PdfPageContext, goals: DailyGoals) {
        drawSectionHeader(ctx, "Дневные цели")
        val canvas = ctx.requireCanvas()
        val bodyPaint = paint(PdfReportLayout.BODY_SIZE, PdfReportLayout.COLOR_TEXT_PRIMARY)
        val lines = listOf(
            "Калории: ${goals.calories.roundToInt()} ккал",
            "Белки: ${formatRange(goals.proteinMin, goals.proteinMax)} г",
            "Жиры: ${formatRange(goals.fatMin, goals.fatMax)} г",
            "Углеводы: ${formatRange(goals.carbsMin, goals.carbsMax)} г"
        )
        for (line in lines) {
            ctx.currentY += PdfReportLayout.LINE_SPACING
            canvas.drawText(line, PdfReportLayout.MARGIN, ctx.currentY, bodyPaint)
        }
        ctx.currentY += PdfReportLayout.SECTION_SPACING
    }

    // ── Summary table ───────────────────────────────────────────────────────

    private fun drawSummaryTable(ctx: PdfPageContext, data: ReportData) {
        drawSectionHeader(ctx, "Итоги по дням")
        if (data.days.all { it.summary.totalCalories == 0f }) {
            val canvas = ctx.requireCanvas()
            ctx.currentY += PdfReportLayout.LINE_SPACING
            canvas.drawText(
                "Нет записей за период",
                PdfReportLayout.MARGIN,
                ctx.currentY,
                paint(PdfReportLayout.BODY_SIZE, PdfReportLayout.COLOR_TEXT_MUTED)
            )
            ctx.currentY += PdfReportLayout.SECTION_SPACING
            return
        }
        drawSummaryHeaderRow(ctx)
        data.days.forEachIndexed { index, day ->
            ensureSpace(ctx, data, PdfReportLayout.TABLE_ROW_HEIGHT)
            drawSummaryDataRow(ctx, day, data.goals.calories, zebra = index % 2 == 1)
        }
        ctx.currentY += PdfReportLayout.SECTION_SPACING
    }

    private fun drawSummaryHeaderRow(ctx: PdfPageContext) {
        val canvas = ctx.requireCanvas()
        val rowTop = ctx.currentY
        val rowH = PdfReportLayout.TABLE_ROW_HEIGHT
        val rect = RectF(PdfReportLayout.MARGIN, rowTop, PdfReportLayout.PAGE_WIDTH - PdfReportLayout.MARGIN, rowTop + rowH)
        val headerBg = Paint().apply { color = PdfReportLayout.COLOR_DIVIDER }
        canvas.drawRect(rect, headerBg)
        val textPaint = paint(PdfReportLayout.SMALL_BODY_SIZE, PdfReportLayout.COLOR_TEXT_PRIMARY, bold = true)
        val baseline = rowTop + rowH - 5f
        val cols = summaryColumns()
        canvas.drawText("Дата", cols[0], baseline, textPaint)
        canvas.drawText("ккал", cols[1], baseline, textPaint)
        canvas.drawText("Б", cols[2], baseline, textPaint)
        canvas.drawText("Ж", cols[3], baseline, textPaint)
        canvas.drawText("У", cols[4], baseline, textPaint)
        ctx.currentY = rowTop + rowH
    }

    private fun drawSummaryDataRow(ctx: PdfPageContext, day: ReportDay, goalCalories: Float, zebra: Boolean) {
        val canvas = ctx.requireCanvas()
        val rowTop = ctx.currentY
        val rowH = PdfReportLayout.TABLE_ROW_HEIGHT
        if (zebra) {
            val zebraRect = RectF(
                PdfReportLayout.MARGIN,
                rowTop,
                PdfReportLayout.PAGE_WIDTH - PdfReportLayout.MARGIN,
                rowTop + rowH
            )
            canvas.drawRect(zebraRect, Paint().apply { color = PdfReportLayout.COLOR_ZEBRA })
        }
        // Tint the calorie cell only if there's data and a positive goal.
        if (day.summary.totalCalories > 0f && goalCalories > 0f) {
            val cols = summaryColumns()
            val tint = calorieCellColor(day.summary.totalCalories, goalCalories)
            val cellRect = RectF(cols[1] - 4f, rowTop + 1f, cols[2] - 4f, rowTop + rowH - 1f)
            canvas.drawRect(cellRect, Paint().apply { color = tint })
        }
        val textPaint = paint(PdfReportLayout.SMALL_BODY_SIZE, PdfReportLayout.COLOR_TEXT_PRIMARY)
        val baseline = rowTop + rowH - 5f
        val cols = summaryColumns()
        canvas.drawText(day.date.format(humanDateFormatter), cols[0], baseline, textPaint)
        canvas.drawText("${day.summary.totalCalories.roundToInt()}", cols[1], baseline, textPaint)
        canvas.drawText(formatMacro(day.summary.totalProtein), cols[2], baseline, textPaint)
        canvas.drawText(formatMacro(day.summary.totalFat), cols[3], baseline, textPaint)
        canvas.drawText(formatMacro(day.summary.totalCarbs), cols[4], baseline, textPaint)
        ctx.currentY = rowTop + rowH
    }

    private fun summaryColumns(): FloatArray {
        val left = PdfReportLayout.MARGIN + 8f
        val tableWidth = PdfReportLayout.PAGE_WIDTH - 2 * PdfReportLayout.MARGIN - 16f
        return floatArrayOf(
            left,
            left + tableWidth * 0.40f,
            left + tableWidth * 0.55f,
            left + tableWidth * 0.70f,
            left + tableWidth * 0.85f
        )
    }

    private fun calorieCellColor(actual: Float, goal: Float): Int {
        val ratio = calorieDeviationRatio(actual, goal)
        // Interpolate light green → light red on alpha-tinted palette.
        val greenStart = 0xFFC8E6C9.toInt()
        val redEnd = 0xFFFFCDD2.toInt()
        return interpolateColor(greenStart, redEnd, ratio)
    }

    // ── Per-day sections (DETAILED only) ────────────────────────────────────

    private fun drawPerDaySections(ctx: PdfPageContext, data: ReportData) {
        drawSectionHeader(ctx, "Записи по дням")
        for (day in data.days) {
            drawSingleDay(ctx, day, data)
        }
    }

    private fun drawSingleDay(ctx: PdfPageContext, day: ReportDay, data: ReportData) {
        ensureSpace(ctx, data, PdfReportLayout.LINE_SPACING * 3)
        val canvas = ctx.requireCanvas()
        val datePaint = paint(PdfReportLayout.BODY_SIZE + 1f, PdfReportLayout.COLOR_TEXT_PRIMARY, bold = true)
        ctx.currentY += PdfReportLayout.LINE_SPACING
        canvas.drawText(day.date.format(longDateFormatter), PdfReportLayout.MARGIN, ctx.currentY, datePaint)

        if (day.entriesByMeal.isEmpty()) {
            ctx.currentY += PdfReportLayout.LINE_SPACING
            canvas.drawText(
                "Нет записей",
                PdfReportLayout.MARGIN + 12f,
                ctx.currentY,
                paint(PdfReportLayout.BODY_SIZE, PdfReportLayout.COLOR_TEXT_MUTED)
            )
            ctx.currentY += PdfReportLayout.LINE_SPACING
            return
        }
        drawDayMeals(ctx, day, data)
        drawDayTotal(ctx, day)
        ctx.currentY += PdfReportLayout.LINE_SPACING / 2
    }

    private fun drawDayMeals(ctx: PdfPageContext, day: ReportDay, data: ReportData) {
        for (mealType in MealType.values()) {
            val mealEntries = day.entriesByMeal[mealType] ?: continue
            ensureSpace(ctx, data, PdfReportLayout.LINE_SPACING * (mealEntries.size + 1))
            drawMealHeader(ctx, mealType)
            for (entry in mealEntries) {
                ensureSpace(ctx, data, PdfReportLayout.LINE_SPACING)
                drawEntryLine(ctx, entry)
            }
        }
    }

    private fun drawMealHeader(ctx: PdfPageContext, mealType: MealType) {
        val canvas = ctx.requireCanvas()
        ctx.currentY += PdfReportLayout.LINE_SPACING
        val mealPaint = paint(PdfReportLayout.BODY_SIZE, PdfReportLayout.COLOR_ACCENT, bold = true)
        canvas.drawText(mealLabel(mealType), PdfReportLayout.MARGIN + 8f, ctx.currentY, mealPaint)
    }

    private fun drawEntryLine(ctx: PdfPageContext, entry: ReportEntry) {
        val canvas = ctx.requireCanvas()
        ctx.currentY += PdfReportLayout.LINE_SPACING
        val left = PdfReportLayout.MARGIN + 24f
        val textPaint = paint(PdfReportLayout.SMALL_BODY_SIZE, PdfReportLayout.COLOR_TEXT_PRIMARY)
        val rightPaint = paint(PdfReportLayout.SMALL_BODY_SIZE, PdfReportLayout.COLOR_TEXT_SECONDARY).apply {
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("${entry.productName}  ${entry.grams} г", left, ctx.currentY, textPaint)
        val rightText = "${entry.calories.roundToInt()}/" +
            "${entry.protein.roundToInt()}/" +
            "${entry.fat.roundToInt()}/" +
            "${entry.carbs.roundToInt()}"
        canvas.drawText(rightText, PdfReportLayout.PAGE_WIDTH - PdfReportLayout.MARGIN, ctx.currentY, rightPaint)
    }

    private fun drawDayTotal(ctx: PdfPageContext, day: ReportDay) {
        val canvas = ctx.requireCanvas()
        ctx.currentY += PdfReportLayout.LINE_SPACING
        val totalPaint = paint(PdfReportLayout.SMALL_BODY_SIZE, PdfReportLayout.COLOR_TEXT_PRIMARY, bold = true)
        val total = "Итого: ${day.summary.totalCalories.roundToInt()} ккал · " +
            "${formatMacro(day.summary.totalProtein)}/" +
            "${formatMacro(day.summary.totalFat)}/" +
            formatMacro(day.summary.totalCarbs)
        canvas.drawText(total, PdfReportLayout.MARGIN + 8f, ctx.currentY, totalPaint)
    }

    // ── Stats section ───────────────────────────────────────────────────────

    private fun drawStatsSection(ctx: PdfPageContext, stats: ReportStats, data: ReportData) {
        ensureSpace(ctx, data, PdfReportLayout.LINE_SPACING * 5)
        drawSectionHeader(ctx, "Статистика")
        drawStatsSummaryLines(ctx, stats)
        drawCaloriesBarChart(ctx, data)
    }

    private fun drawStatsSummaryLines(ctx: PdfPageContext, stats: ReportStats) {
        val canvas = ctx.requireCanvas()
        val bodyPaint = paint(PdfReportLayout.BODY_SIZE, PdfReportLayout.COLOR_TEXT_PRIMARY)
        val lines = listOf(
            "Период: ${stats.daysCount} дн.",
            "Средние: ${stats.averageCalories.roundToInt()} ккал · " +
                "${formatMacro(stats.averageProtein)}/" +
                "${formatMacro(stats.averageFat)}/" +
                formatMacro(stats.averageCarbs),
            "Попадание в цель: ${stats.daysHitCaloriePercent.roundToInt()}% дней"
        )
        for (line in lines) {
            ctx.currentY += PdfReportLayout.LINE_SPACING
            canvas.drawText(line, PdfReportLayout.MARGIN, ctx.currentY, bodyPaint)
        }
        ctx.currentY += PdfReportLayout.LINE_SPACING / 2
    }

    private fun drawCaloriesBarChart(ctx: PdfPageContext, data: ReportData) {
        val canvas = ctx.requireCanvas()
        ctx.currentY += PdfReportLayout.LINE_SPACING
        val headerPaint = paint(PdfReportLayout.BODY_SIZE, PdfReportLayout.COLOR_TEXT_PRIMARY, bold = true)
        canvas.drawText("Калории по дням", PdfReportLayout.MARGIN, ctx.currentY, headerPaint)
        ctx.currentY += 4f

        val maxCalories = data.days.maxOfOrNull { it.summary.totalCalories } ?: 0f
        val goal = data.goals.calories
        val scaleMax = maxOf(maxCalories, goal).coerceAtLeast(1f)

        val labelW = 48f
        val valueW = 56f
        val chartLeft = PdfReportLayout.MARGIN + labelW
        val chartRight = PdfReportLayout.PAGE_WIDTH - PdfReportLayout.MARGIN - valueW
        val chartWidth = chartRight - chartLeft

        for (day in data.days) {
            ensureSpace(ctx, data, PdfReportLayout.TABLE_ROW_HEIGHT)
            drawBarRow(ctx, day, chartLeft, chartRight, chartWidth, scaleMax, goal)
        }
    }

    @Suppress("LongParameterList")
    private fun drawBarRow(
        ctx: PdfPageContext,
        day: ReportDay,
        chartLeft: Float,
        chartRight: Float,
        chartWidth: Float,
        scaleMax: Float,
        goal: Float
    ) {
        val canvas = ctx.requireCanvas()
        val rowTop = ctx.currentY
        val rowH = PdfReportLayout.TABLE_ROW_HEIGHT
        val labelPaint = paint(PdfReportLayout.SMALL_BODY_SIZE, PdfReportLayout.COLOR_TEXT_SECONDARY)
        val valuePaint = paint(PdfReportLayout.SMALL_BODY_SIZE, PdfReportLayout.COLOR_TEXT_PRIMARY)
        val baseline = rowTop + rowH - 5f
        canvas.drawText(day.date.format(shortDateFormatter), PdfReportLayout.MARGIN, baseline, labelPaint)

        val barH = rowH - 8f
        val barTop = rowTop + 4f
        val barW = if (scaleMax > 0f) chartWidth * (day.summary.totalCalories / scaleMax) else 0f
        val barRect = RectF(chartLeft, barTop, chartLeft + barW, barTop + barH)
        canvas.drawRect(barRect, Paint().apply { color = PdfReportLayout.COLOR_BAR })

        if (goal > 0f && scaleMax > 0f) {
            val goalX = chartLeft + chartWidth * (goal / scaleMax)
            val dashPaint = Paint().apply {
                color = PdfReportLayout.COLOR_GOAL_LINE
                style = Paint.Style.STROKE
                strokeWidth = 1f
                pathEffect = DashPathEffect(floatArrayOf(4f, 3f), 0f)
            }
            canvas.drawLine(goalX, barTop - 1f, goalX, barTop + barH + 1f, dashPaint)
        }

        canvas.drawText("${day.summary.totalCalories.roundToInt()}", chartRight + 4f, baseline, valuePaint)
        ctx.currentY = rowTop + rowH
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private fun drawSectionHeader(ctx: PdfPageContext, title: String) {
        val canvas = ctx.requireCanvas()
        ctx.currentY += PdfReportLayout.SECTION_HEADER_SIZE
        val headerPaint = paint(PdfReportLayout.SECTION_HEADER_SIZE, PdfReportLayout.COLOR_TEXT_PRIMARY, bold = true)
        canvas.drawText(title, PdfReportLayout.MARGIN, ctx.currentY, headerPaint)
        val underlineY = ctx.currentY + 3f
        val underlinePaint = Paint().apply {
            color = PdfReportLayout.COLOR_ACCENT
            strokeWidth = 1f
        }
        canvas.drawLine(
            PdfReportLayout.MARGIN,
            underlineY,
            PdfReportLayout.PAGE_WIDTH - PdfReportLayout.MARGIN,
            underlineY,
            underlinePaint
        )
        ctx.currentY += 8f
    }

    private fun paint(textSize: Float, colorArgb: Int, bold: Boolean = false): Paint = Paint().apply {
        isAntiAlias = true
        color = colorArgb
        this.textSize = textSize
        if (bold) typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private fun mealLabel(mealType: MealType): String = when (mealType) {
        MealType.BREAKFAST -> PdfReportLayout.MEAL_LABEL_BREAKFAST
        MealType.LUNCH -> PdfReportLayout.MEAL_LABEL_LUNCH
        MealType.DINNER -> PdfReportLayout.MEAL_LABEL_DINNER
        MealType.SNACK -> PdfReportLayout.MEAL_LABEL_SNACK
    }

    private fun formatRange(min: Float, max: Float): String =
        "${min.roundToInt()}–${max.roundToInt()}"

    private fun formatMacro(value: Float): String {
        val rounded = (value * 10f).roundToInt() / 10f
        return if (rounded % 1f == 0f) rounded.toInt().toString() else rounded.toString()
    }

    private fun formatHeaderRange(from: LocalDate, to: LocalDate): String =
        "${from.format(humanDateFormatter)} – ${to.format(humanDateFormatter)}"

    private fun writeDocumentToFile(document: PdfDocument, data: ReportData): File {
        val dir = File(context.cacheDir, REPORT_DIR_NAME).apply { mkdirs() }
        val fromName = data.config.from.format(fileNameDateFormatter)
        val toName = data.config.to.format(fileNameDateFormatter)
        val file = File(dir, "diet_helper_${fromName}_$toName.pdf")
        if (file.exists()) file.delete()
        FileOutputStream(file).use { fos -> document.writeTo(fos) }
        return file
    }

    private fun interpolateColor(start: Int, end: Int, t: Float): Int {
        val clamped = t.coerceIn(0f, 1f)
        val sa = Color.alpha(start); val sr = Color.red(start); val sg = Color.green(start); val sb = Color.blue(start)
        val ea = Color.alpha(end); val er = Color.red(end); val eg = Color.green(end); val eb = Color.blue(end)
        return Color.argb(
            (sa + (ea - sa) * clamped).toInt(),
            (sr + (er - sr) * clamped).toInt(),
            (sg + (eg - sg) * clamped).toInt(),
            (sb + (eb - sb) * clamped).toInt()
        )
    }

    @Suppress("unused")
    private fun isoDateForName(date: LocalDate): String = date.format(isoDateFormatter)
}
