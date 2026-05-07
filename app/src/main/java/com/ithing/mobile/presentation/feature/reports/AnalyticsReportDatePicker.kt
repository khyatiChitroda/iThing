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
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

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
    val state = rememberDateRangePickerState(
        initialSelectedStartDateMillis = startMillis,
        initialSelectedEndDateMillis = endMillis
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    val start = state.selectedStartDateMillis
                    val end = state.selectedEndDateMillis
                    if (start != null && end != null) {
                        onConfirm(start, end)
                    }
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
        DateRangePicker(
            state = state,
            showModeToggle = false
        )
    }
}
