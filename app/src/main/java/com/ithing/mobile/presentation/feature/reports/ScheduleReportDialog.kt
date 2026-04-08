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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ithing.mobile.presentation.theme.White

@Composable
fun ScheduleReportDialog(
    uiState: ReportsUiState,
    filteredFields: List<String>,
    onDismiss: () -> Unit,
    onEmailChanged: (String) -> Unit,
    onSubjectChanged: (String) -> Unit,
    onBodyChanged: (String) -> Unit,
    onFrequencyChanged: (ScheduleDeliveryFrequency?) -> Unit,
    onSearchChanged: (String) -> Unit,
    onToggleField: (String) -> Unit,
    onToggleSelectAll: () -> Unit,
    onSaveClick: () -> Unit
) {
    val bodyScrollState = rememberScrollState()

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
                        text = "Schedule Report",
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
                            value = uiState.scheduleEmail,
                            onValueChange = onEmailChanged,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true
                        )
                    }

                    SummaryLabeledField("Subject") {
                        OutlinedTextField(
                            value = uiState.scheduleSubject,
                            onValueChange = onSubjectChanged,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    SummaryLabeledField("Message") {
                        OutlinedTextField(
                            value = uiState.scheduleBody,
                            onValueChange = onBodyChanged,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 120.dp),
                            minLines = 4
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Delivery Frequency",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color(0xFF52637E)
                        )

                        ScheduleFrequencyField(
                            selected = uiState.scheduleFrequency,
                            onSelected = onFrequencyChanged
                        )

                        uiState.scheduleFrequencyError?.let {
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
                                        checked = filteredFields.isNotEmpty() && filteredFields.all { it in uiState.scheduleSelectedFields },
                                        onCheckedChange = { onToggleSelectAll() }
                                    )
                                    OutlinedTextField(
                                        value = uiState.scheduleSearchQuery,
                                        onValueChange = onSearchChanged,
                                        modifier = Modifier.fillMaxWidth(),
                                        placeholder = { Text("Search") },
                                        singleLine = true
                                    )
                                }

                                if (uiState.isScheduleFieldsLoading) {
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
                                                    checked = field in uiState.scheduleSelectedFields,
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScheduleFrequencyField(
    selected: ScheduleDeliveryFrequency?,
    onSelected: (ScheduleDeliveryFrequency?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected?.label ?: "",
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
            ScheduleDeliveryFrequency.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
