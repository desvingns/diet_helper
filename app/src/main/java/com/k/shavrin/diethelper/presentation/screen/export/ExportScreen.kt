@file:Suppress("TooManyFunctions")

package com.k.shavrin.diethelper.presentation.screen.export

import android.content.Intent
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.k.shavrin.diethelper.domain.model.ExportMode
import com.k.shavrin.diethelper.presentation.util.formatDate
import kotlinx.coroutines.flow.collectLatest
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

const val EXPORT_TAG_FROM_DATE = "export_from_date"
const val EXPORT_TAG_TO_DATE = "export_to_date"
const val EXPORT_TAG_MODE_DETAILED = "export_mode_detailed"
const val EXPORT_TAG_MODE_SUMMARY = "export_mode_summary"
const val EXPORT_TAG_STATS_SWITCH = "export_stats_switch"
const val EXPORT_TAG_BUTTON = "export_button"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(
    onNavigateBack: () -> Unit,
    viewModel: ExportViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            if (event is ExportEvent.Share) {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, event.uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                val chooser = Intent.createChooser(intent, "Поделиться отчётом").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(chooser)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Экспорт в PDF") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {
            ExportContent(
                state = state,
                onSetFrom = viewModel::setFrom,
                onSetTo = viewModel::setTo,
                onSetMode = viewModel::setMode,
                onSetIncludeStats = viewModel::setIncludeStats,
                onExport = viewModel::exportReport
            )
        }
    }
}

@Composable
fun ExportContent(
    state: ExportUiState,
    onSetFrom: (LocalDate) -> Unit,
    onSetTo: (LocalDate) -> Unit,
    onSetMode: (ExportMode) -> Unit,
    onSetIncludeStats: (Boolean) -> Unit,
    onExport: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        DateRangeCard(state = state, onSetFrom = onSetFrom, onSetTo = onSetTo)
        ModeCard(state = state, onSetMode = onSetMode)
        ExtrasCard(state = state, onSetIncludeStats = onSetIncludeStats)
        if (state.errorMessage != null) {
            Text(
                text = state.errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Button(
            onClick = onExport,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(EXPORT_TAG_BUTTON),
            enabled = !state.isExporting
        ) {
            if (state.isExporting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text("Формируем…")
            } else {
                Text("Экспортировать")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateRangeCard(
    state: ExportUiState,
    onSetFrom: (LocalDate) -> Unit,
    onSetTo: (LocalDate) -> Unit
) {
    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Период",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            DateRow(
                label = "С: ${formatDate(state.from)}",
                tag = EXPORT_TAG_FROM_DATE,
                onClick = { showFromPicker = true }
            )
            DateRow(
                label = "По: ${formatDate(state.to)}",
                tag = EXPORT_TAG_TO_DATE,
                onClick = { showToPicker = true }
            )
        }
    }
    if (showFromPicker) {
        ExportDatePickerDialog(
            initial = state.from,
            onConfirm = { onSetFrom(it); showFromPicker = false },
            onDismiss = { showFromPicker = false }
        )
    }
    if (showToPicker) {
        ExportDatePickerDialog(
            initial = state.to,
            onConfirm = { onSetTo(it); showToPicker = false },
            onDismiss = { showToPicker = false }
        )
    }
}

@Composable
private fun DateRow(label: String, tag: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp)
            .testTag(tag)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExportDatePickerDialog(
    initial: LocalDate,
    onConfirm: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val initMs = initial.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = initMs,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = true
        }
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                pickerState.selectedDateMillis?.let { ms ->
                    onConfirm(Instant.ofEpochMilli(ms).atZone(ZoneOffset.UTC).toLocalDate())
                }
            }) { Text("Готово") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    ) { DatePicker(state = pickerState) }
}

@Composable
private fun ModeCard(state: ExportUiState, onSetMode: (ExportMode) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Содержание отчёта",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            ModeRow(
                label = "Подробно: что ел каждый день",
                selected = state.mode == ExportMode.DETAILED,
                tag = EXPORT_TAG_MODE_DETAILED,
                onClick = { onSetMode(ExportMode.DETAILED) }
            )
            ModeRow(
                label = "Только итоги БЖУ за день",
                selected = state.mode == ExportMode.SUMMARY_ONLY,
                tag = EXPORT_TAG_MODE_SUMMARY,
                onClick = { onSetMode(ExportMode.SUMMARY_ONLY) }
            )
        }
    }
}

@Composable
private fun ModeRow(label: String, selected: Boolean, tag: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

@Composable
private fun ExtrasCard(state: ExportUiState, onSetIncludeStats: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Дополнительно",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Включать статистику",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = state.includeStats,
                    onCheckedChange = onSetIncludeStats,
                    modifier = Modifier.testTag(EXPORT_TAG_STATS_SWITCH)
                )
            }
        }
    }
}

@Composable
@Suppress("UnusedPrivateMember")
private fun PreviewBackground(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier.background(MaterialTheme.colorScheme.background)
    ) { content() }
}
