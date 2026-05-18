package com.k.shavrin.diethelper.domain.repository

import com.k.shavrin.diethelper.domain.model.ReportData

interface ReportRenderer {
    suspend fun render(data: ReportData): String
}
