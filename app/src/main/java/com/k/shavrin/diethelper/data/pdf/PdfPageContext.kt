package com.k.shavrin.diethelper.data.pdf

import android.graphics.Canvas
import android.graphics.pdf.PdfDocument

/**
 * Mutable rendering state shared by all draw* functions in [PdfReportRenderer].
 * Holds the active page, canvas, vertical cursor, and the running page index.
 */
internal class PdfPageContext(
    val document: PdfDocument
) {
    var page: PdfDocument.Page? = null
    var canvas: Canvas? = null
    var currentY: Float = 0f
    var pageIndex: Int = 0

    fun requireCanvas(): Canvas = canvas ?: error("PDF canvas not initialised — call startPage() first")

    fun requirePage(): PdfDocument.Page =
        page ?: error("PDF page not initialised — call startPage() first")
}
