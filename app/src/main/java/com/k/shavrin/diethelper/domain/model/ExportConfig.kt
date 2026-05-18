package com.k.shavrin.diethelper.domain.model

import java.time.LocalDate

enum class ExportMode { DETAILED, SUMMARY_ONLY }

data class ExportConfig(
    val from: LocalDate,
    val to: LocalDate,
    val mode: ExportMode,
    val includeStats: Boolean
)
