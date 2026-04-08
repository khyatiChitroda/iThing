package com.ithing.mobile.presentation.feature.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ithing.mobile.presentation.theme.White

@Composable
fun AnalyticsReportDialog(
    uiState: ReportsUiState,
    onDismiss: () -> Unit,
    onTimeSpanPresetSelected: (AnalyticsDatePreset) -> Unit,
    onCustomDateRangeSelected: (Long, Long) -> Unit,
    onRowTitleChanged: (String, String) -> Unit,
    onRowChartTypeChanged: (String, AnalyticsChartType?) -> Unit,
    onRowFieldChanged: (String, String?) -> Unit,
    onRowFrequencyChanged: (String, AnalyticsFrequency?) -> Unit,
    onAddMore: () -> Unit,
    onRemoveRow: (String) -> Unit,
    onSaveViewClick: () -> Unit,
    onGeneratePdfClick: () -> Unit
) {
    var showTimeSpanPicker by remember { mutableStateOf(false) }
    var showCustomRangeDialog by remember { mutableStateOf(false) }
    val bodyScrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .heightIn(max = 760.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = White)
        ) {
            BoxWithConstraints {
                val compactLayout = maxWidth < 600.dp
                Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Analytic Report",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF52637E)
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF6B7280)
                        )
                    }
                }

                DividerLine()

                Column(
                    modifier = Modifier
                        .heightIn(max = if (compactLayout) 460.dp else 520.dp)
                        .verticalScroll(bodyScrollState)
                        .padding(horizontal = 24.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    Text(
                        text = "Time Span",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF495C79)
                    )

                    Surface(
                        modifier = Modifier.clickable { showTimeSpanPicker = true },
                        shape = RoundedCornerShape(16.dp),
                        tonalElevation = 0.dp,
                        shadowElevation = 2.dp,
                        color = White
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = Color(0xFF233A69),
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = uiState.analyticsTimeSpanLabel,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF233A69)
                            )
                        }
                    }

                    uiState.analyticsTimeSpanError?.let { error ->
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFFD62828)
                        )
                    }

                    uiState.analyticsChartRows.forEach { row ->
                        AnalyticsChartConfigCard(
                            row = row,
                            compactLayout = compactLayout,
                            availableFields = uiState.availableAnalyticsFields,
                            showRemove = uiState.analyticsChartRows.size > 1,
                            onTitleChanged = { onRowTitleChanged(row.id, it) },
                            onChartTypeChanged = { onRowChartTypeChanged(row.id, it) },
                            onFieldChanged = { onRowFieldChanged(row.id, it) },
                            onFrequencyChanged = { onRowFrequencyChanged(row.id, it) },
                            onRemove = { onRemoveRow(row.id) }
                        )
                    }

                    Button(
                        onClick = onAddMore,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Add More")
                    }
                }

                DividerLine()

                if (compactLayout) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = onSaveViewClick,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Save view")
                        }
                        Button(
                            onClick = onGeneratePdfClick,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Generate PDF")
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 18.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(onClick = onDismiss) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                        Button(onClick = onSaveViewClick) {
                            Text("Save view")
                        }
                        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                        Button(onClick = onGeneratePdfClick) {
                            Text("Generate PDF")
                        }
                    }
                }
            }
        }
        }
    }

    if (showTimeSpanPicker) {
        AnalyticsReportDatePickerDialog(
            selectedPreset = uiState.analyticsSelectedPreset,
            startMillis = uiState.analyticsTimeSpanStart,
            endMillis = uiState.analyticsTimeSpanEnd,
            onPresetSelected = onTimeSpanPresetSelected,
            onCustomClick = {
                showTimeSpanPicker = false
                showCustomRangeDialog = true
            },
            onDismiss = { showTimeSpanPicker = false }
        )
    }

    if (showCustomRangeDialog) {
        AnalyticsCustomDateRangeDialog(
            startMillis = uiState.analyticsTimeSpanStart,
            endMillis = uiState.analyticsTimeSpanEnd,
            onDismiss = { showCustomRangeDialog = false },
            onConfirm = { start, end ->
                showCustomRangeDialog = false
                onCustomDateRangeSelected(start, end)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnalyticsChartConfigCard(
    row: AnalyticsChartConfigUi,
    compactLayout: Boolean,
    availableFields: List<String>,
    showRemove: Boolean,
    onTitleChanged: (String) -> Unit,
    onChartTypeChanged: (AnalyticsChartType?) -> Unit,
    onFieldChanged: (String?) -> Unit,
    onFrequencyChanged: (AnalyticsFrequency?) -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (compactLayout) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    AnalyticsTextFieldBlock(
                        label = "Title",
                        content = {
                            OutlinedTextField(
                                value = row.title,
                                onValueChange = onTitleChanged,
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                    )
                    AnalyticsTextFieldBlock(
                        label = "Type of Chart",
                        error = row.chartTypeError,
                        content = {
                            AnalyticsDropdownField(
                                selectedText = row.chartType?.label ?: "Select Chart Type",
                                options = AnalyticsChartType.entries,
                                optionLabel = { it.label },
                                onSelected = onChartTypeChanged
                            )
                        }
                    )
                    AnalyticsTextFieldBlock(
                        label = "Choose Fields",
                        error = row.fieldError,
                        content = {
                            AnalyticsDropdownField(
                                selectedText = row.selectedField ?: "Select Field",
                                options = availableFields,
                                optionLabel = { it },
                                onSelected = onFieldChanged
                            )
                        }
                    )
                    AnalyticsTextFieldBlock(
                        label = "Chart Data Frequency",
                        error = row.frequencyError,
                        content = {
                            AnalyticsDropdownField(
                                selectedText = row.frequency?.label ?: "Select Frequency",
                                options = AnalyticsFrequency.entries,
                                optionLabel = { it.label },
                                onSelected = onFrequencyChanged
                            )
                        }
                    )
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Title",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color(0xFF52637E)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = row.title,
                            onValueChange = onTitleChanged,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Type of Chart",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color(0xFF52637E)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        AnalyticsDropdownField(
                            selectedText = row.chartType?.label ?: "Select Chart Type",
                            options = AnalyticsChartType.entries,
                            optionLabel = { it.label },
                            onSelected = onChartTypeChanged
                        )
                        row.chartTypeError?.let { error ->
                            ErrorText(error)
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Choose Fields",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color(0xFF52637E)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        AnalyticsDropdownField(
                            selectedText = row.selectedField ?: "Select Field",
                            options = availableFields,
                            optionLabel = { it },
                            onSelected = onFieldChanged
                        )
                        row.fieldError?.let { error ->
                            ErrorText(error)
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Chart Data Frequency",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color(0xFF52637E)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        AnalyticsDropdownField(
                            selectedText = row.frequency?.label ?: "Select Frequency",
                            options = AnalyticsFrequency.entries,
                            optionLabel = { it.label },
                            onSelected = onFrequencyChanged
                        )
                        row.frequencyError?.let { error ->
                            ErrorText(error)
                        }
                    }
                }
            }

            if (showRemove) {
                Row(
                    modifier = Modifier.clickable(onClick = onRemove),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = null,
                        tint = Color(0xFFD62828)
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    Text(
                        text = "Remove",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFFD62828)
                    )
                }
            }
        }
    }
}

@Composable
private fun AnalyticsTextFieldBlock(
    label: String,
    error: String? = null,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.titleLarge,
            color = Color(0xFF52637E)
        )
        Spacer(modifier = Modifier.height(8.dp))
        content()
        error?.let { ErrorText(it) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> AnalyticsDropdownField(
    selectedText: String,
    options: List<T>,
    optionLabel: (T) -> String,
    onSelected: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedText,
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            readOnly = true,
            singleLine = true,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ErrorText(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(top = 6.dp),
        style = MaterialTheme.typography.bodyLarge,
        color = Color(0xFFD62828)
    )
}

@Composable
private fun DividerLine() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color(0xFFDCE3ED))
    )
}
