package com.ithing.mobile.presentation.feature.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ithing.mobile.presentation.theme.White

@Composable
fun SummaryReportDialog(
    uiState: ReportsUiState,
    filteredFields: List<String>,
    onDismiss: () -> Unit,
    onEmailChanged: (String) -> Unit,
    onSubjectChanged: (String) -> Unit,
    onBodyChanged: (String) -> Unit,
    onTimeSpanPresetSelected: (AnalyticsDatePreset) -> Unit,
    onCustomDateRangeSelected: (Long, Long) -> Unit,
    onSearchChanged: (String) -> Unit,
    onToggleField: (String) -> Unit,
    onToggleSelectAll: () -> Unit,
    onSaveClick: () -> Unit
) {
    val bodyScrollState = rememberScrollState()
    var showPresetPicker by remember { mutableStateOf(false) }
    var showCustomRangeDialog by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .heightIn(max = 780.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = White)
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Summary Report",
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

                SummaryDivider()

                Column(
                    modifier = Modifier
                        .heightIn(max = 560.dp)
                        .verticalScroll(bodyScrollState)
                        .padding(horizontal = 24.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    SummaryLabeledField("Email Address") {
                        OutlinedTextField(
                            value = uiState.summaryEmail,
                            onValueChange = onEmailChanged,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true
                        )
                    }

                    SummaryLabeledField("Subject") {
                        OutlinedTextField(
                            value = uiState.summarySubject,
                            onValueChange = onSubjectChanged,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    SummaryLabeledField("Message") {
                        OutlinedTextField(
                            value = uiState.summaryBody,
                            onValueChange = onBodyChanged,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 120.dp),
                            minLines = 4
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Time Span",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF495C79)
                        )

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showPresetPicker = true },
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
                                    tint = Color(0xFF233A69)
                                )
                                Text(
                                    text = uiState.summaryTimeSpanLabel,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF233A69)
                                )
                            }
                        }

                        uiState.summaryTimeSpanError?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color(0xFFD62828)
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Choose Fields",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color(0xFF52637E)
                        )

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Checkbox(
                                        checked = filteredFields.isNotEmpty() && filteredFields.all { it in uiState.summarySelectedFields },
                                        onCheckedChange = { onToggleSelectAll() }
                                    )
                                    OutlinedTextField(
                                        value = uiState.summarySearchQuery,
                                        onValueChange = onSearchChanged,
                                        modifier = Modifier.fillMaxWidth(),
                                        placeholder = { Text("Search") },
                                        singleLine = true
                                    )
                                }

                                if (uiState.isSummaryFieldsLoading) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 24.dp),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                } else {
                                    LazyVerticalGrid(
                                        columns = GridCells.Adaptive(minSize = 220.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 180.dp, max = 320.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        userScrollEnabled = true
                                    ) {
                                        items(filteredFields) { field ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable { onToggleField(field) },
                                                verticalAlignment = Alignment.Top
                                            ) {
                                                Checkbox(
                                                    checked = field in uiState.summarySelectedFields,
                                                    onCheckedChange = { onToggleField(field) }
                                                )
                                                Text(
                                                    text = field,
                                                    modifier = Modifier.padding(top = 12.dp),
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    color = Color(0xFF53637D)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                SummaryDivider()

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
                    Button(onClick = onSaveClick) {
                        Text("Save")
                    }
                }
            }
        }
    }

    if (showPresetPicker) {
        AnalyticsReportDatePickerDialog(
            selectedPreset = uiState.summarySelectedPreset,
            startMillis = uiState.summaryTimeSpanStart,
            endMillis = uiState.summaryTimeSpanEnd,
            onPresetSelected = onTimeSpanPresetSelected,
            onCustomClick = {
                showPresetPicker = false
                showCustomRangeDialog = true
            },
            onDismiss = { showPresetPicker = false }
        )
    }

    if (showCustomRangeDialog) {
        AnalyticsCustomDateRangeDialog(
            startMillis = uiState.summaryTimeSpanStart,
            endMillis = uiState.summaryTimeSpanEnd,
            onDismiss = { showCustomRangeDialog = false },
            onConfirm = { start, end ->
                showCustomRangeDialog = false
                onCustomDateRangeSelected(start, end)
            }
        )
    }
}

@Composable
fun SummaryLabeledField(
    label: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleLarge,
            color = Color(0xFF52637E)
        )
        content()
    }
}

@Composable
fun SummaryDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color(0xFFDCE3ED))
    )
}
