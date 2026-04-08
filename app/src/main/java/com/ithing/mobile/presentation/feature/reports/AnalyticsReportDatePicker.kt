package com.ithing.mobile.presentation.feature.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.ithing.mobile.presentation.theme.White

@Composable
fun AnalyticsReportDatePickerDialog(
    selectedPreset: AnalyticsDatePreset,
    startMillis: Long?,
    endMillis: Long?,
    onPresetSelected: (AnalyticsDatePreset) -> Unit,
    onCustomClick: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = White)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Select Time Span",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF52637E)
                )

                AnalyticsDatePreset.values().forEach { preset ->
                    val isSelected = preset == selectedPreset
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (preset == AnalyticsDatePreset.CUSTOM) {
                                    onCustomClick()
                                } else {
                                    onPresetSelected(preset)
                                }
                            },
                        color = if (isSelected) Color(0xFFF1F5FB) else Color.Transparent,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = preset.label,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = Color(0xFF233A69)
                        )
                    }
                }

                Text(
                    text = analyticsDateRangeLabel(startMillis, endMillis),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF5A6880)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0xFFE4EBF4))
                )

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Done")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsCustomDateRangeDialog(
    startMillis: Long?,
    endMillis: Long?,
    onDismiss: () -> Unit,
    onConfirm: (Long, Long) -> Unit
) {
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }
    var draftStart by remember { mutableStateOf(startMillis) }
    var draftEnd by remember { mutableStateOf(endMillis) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = White)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Custom Range",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF52637E)
                )

                OutlinedButton(
                    onClick = { showStartPicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Start: ${analyticsDateRangeLabel(draftStart, draftStart).substringBefore(" to ")}")
                }

                OutlinedButton(
                    onClick = { showEndPicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("End: ${analyticsDateRangeLabel(draftEnd, draftEnd).substringBefore(" to ")}")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            onConfirm(draftStart ?: startMillis ?: System.currentTimeMillis(), draftEnd ?: endMillis ?: System.currentTimeMillis())
                        }
                    ) {
                        Text("Apply")
                    }
                }
            }
        }
    }

    if (showStartPicker) {
        SingleDatePickerDialog(
            initialDate = draftStart,
            onDismiss = { showStartPicker = false },
            onConfirm = {
                draftStart = it
                showStartPicker = false
            }
        )
    }

    if (showEndPicker) {
        SingleDatePickerDialog(
            initialDate = draftEnd,
            onDismiss = { showEndPicker = false },
            onConfirm = {
                draftEnd = it
                showEndPicker = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SingleDatePickerDialog(
    initialDate: Long?,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    val state = androidx.compose.material3.rememberDatePickerState(
        initialSelectedDateMillis = initialDate
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    state.selectedDateMillis?.let(onConfirm)
                }
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = state)
    }
}
