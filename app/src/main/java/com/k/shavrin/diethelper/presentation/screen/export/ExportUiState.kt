package com.k.shavrin.diethelper.presentation.screen.export

import android.net.Uri
import com.k.shavrin.diethelper.domain.model.ExportMode
import java.time.LocalDate

data class ExportUiState(
    val from: LocalDate,
    val to: LocalDate,
    val mode: ExportMode = ExportMode.DETAILED,
    val includeStats: Boolean = true,
    val isExporting: Boolean = false,
    val errorMessage: String? = null
)

sealed interface ExportEvent {
    data class Share(val uri: Uri) : ExportEvent
    data class Error(val message: String) : ExportEvent
}
