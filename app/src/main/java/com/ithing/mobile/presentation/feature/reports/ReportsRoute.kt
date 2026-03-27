package com.ithing.mobile.presentation.feature.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.ithing.mobile.domain.model.Customer
import com.ithing.mobile.domain.model.Device
import com.ithing.mobile.domain.model.Industry
import com.ithing.mobile.domain.model.Oem
import com.ithing.mobile.domain.model.ReportSchedule
import com.ithing.mobile.presentation.theme.AccentBlue
import com.ithing.mobile.presentation.theme.LightGrayBg
import com.ithing.mobile.presentation.theme.MutedText
import com.ithing.mobile.presentation.theme.NavyBlue
import com.ithing.mobile.presentation.theme.White
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class ReportTypeCardModel(
    val title: String,
    val description: String,
    val illustrationLabel: String,
    val accentColor: Color
)

@Composable
fun ReportsRoute(
    viewModel: ReportsViewModel = hiltViewModel(),
    navController: NavHostController
) {
    val uiState by viewModel.uiState.collectAsState()

    ReportsScreen(
        uiState = uiState,
        onIndustrySelected = viewModel::onIndustrySelected,
        onOemSelected = viewModel::onOemSelected,
        onCustomerSelected = viewModel::onCustomerSelected,
        onDeviceSelected = viewModel::onDeviceSelected,
        onRefresh = viewModel::refreshReports
    )
}

@Composable
private fun ReportsScreen(
    uiState: ReportsUiState,
    onIndustrySelected: (Industry?) -> Unit,
    onOemSelected: (Oem?) -> Unit,
    onCustomerSelected: (Customer?) -> Unit,
    onDeviceSelected: (Device?) -> Unit,
    onRefresh: () -> Unit
) {
    val reportCards = remember {
        listOf(
            ReportTypeCardModel(
                title = "Summary Report",
                description = "Summarized machine metrics enabling informed management decisions.",
                illustrationLabel = "SR",
                accentColor = Color(0xFFFFB020)
            ),
            ReportTypeCardModel(
                title = "Analytic Report",
                description = "Visualize performance trends over time using intuitive charts.",
                illustrationLabel = "AR",
                accentColor = Color(0xFF4C7DFF)
            ),
            ReportTypeCardModel(
                title = "Schedule Report",
                description = "Enable periodic reporting to streamline data monitoring.",
                illustrationLabel = "SC",
                accentColor = Color(0xFF22C55E)
            ),
            ReportTypeCardModel(
                title = "Exception Report",
                description = "Track irregular events to support diagnostic and preventive actions.",
                illustrationLabel = "ER",
                accentColor = Color(0xFFF97316)
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightGrayBg)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ReportsFilterSection(
                    uiState = uiState,
                    onIndustrySelected = onIndustrySelected,
                    onOemSelected = onOemSelected,
                    onCustomerSelected = onCustomerSelected,
                    onDeviceSelected = onDeviceSelected
                )
            }

            item {
                ReportsTypeSection(reportCards = reportCards)
            }

            item {
                ScheduledReportsSection(
                    schedules = uiState.schedules,
                    selectedDevice = uiState.selectedDevice,
                    isLoading = uiState.isLoading,
                    isRefreshing = uiState.isRefreshing,
                    errorMessage = uiState.errorMessage,
                    onRefresh = onRefresh
                )
            }
        }
    }
}

@Composable
private fun ReportsFilterSection(
    uiState: ReportsUiState,
    onIndustrySelected: (Industry?) -> Unit,
    onOemSelected: (Oem?) -> Unit,
    onCustomerSelected: (Customer?) -> Unit,
    onDeviceSelected: (Device?) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Reports",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            FilterCard(
                title = "Industry",
                iconLabel = "IN",
                selectedText = uiState.selectedIndustry?.name ?: "Select...",
                options = uiState.industries,
                optionLabel = { it.name },
                enabled = uiState.industries.isNotEmpty() && !uiState.isLoading,
                onSelected = onIndustrySelected
            )

            FilterCard(
                title = "OEM",
                iconLabel = "OE",
                selectedText = uiState.selectedOem?.name ?: "Select...",
                options = uiState.oems,
                optionLabel = { it.name },
                enabled = uiState.selectedIndustry != null && uiState.oems.isNotEmpty() && !uiState.isLoading,
                onSelected = onOemSelected
            )

            FilterCard(
                title = "Customer",
                iconLabel = "CU",
                selectedText = uiState.selectedCustomer?.name ?: "Select...",
                options = uiState.customers,
                optionLabel = { it.name },
                enabled = uiState.selectedOem != null && uiState.customers.isNotEmpty() && !uiState.isLoading,
                onSelected = onCustomerSelected
            )

            FilterCard(
                title = "Device",
                iconLabel = "DV",
                selectedText = uiState.selectedDevice?.name ?: "Select...",
                options = uiState.devices,
                optionLabel = { it.name },
                enabled = uiState.selectedCustomer != null && uiState.devices.isNotEmpty() && !uiState.isLoading,
                onSelected = onDeviceSelected
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> FilterCard(
    title: String,
    iconLabel: String,
    selectedText: String,
    options: List<T>,
    optionLabel: (T) -> String,
    enabled: Boolean,
    onSelected: (T?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = NavyBlue,
                        shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    shape = CircleShape,
                    color = White.copy(alpha = 0.16f)
                ) {
                    Text(
                        text = iconLabel,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = White,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = {
                    if (enabled) {
                        expanded = !expanded
                    }
                }
            ) {
                OutlinedTextField(
                    value = selectedText,
                    onValueChange = {},
                    readOnly = true,
                    enabled = enabled,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(bottomStart = 18.dp, bottomEnd = 18.dp),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
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
    }
}

@Composable
private fun ReportsTypeSection(reportCards: List<ReportTypeCardModel>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        reportCards.forEach { card ->
            ReportTypeCard(card = card)
        }
    }
}

@Composable
private fun ReportTypeCard(card: ReportTypeCardModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF0FA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = card.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF44536D)
            )
            Text(
                text = card.description,
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF5E6F8D)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                Surface(
                    modifier = Modifier.size(96.dp),
                    shape = CircleShape,
                    color = card.accentColor.copy(alpha = 0.16f)
                ) {}
                Surface(
                    modifier = Modifier
                        .padding(start = 24.dp, bottom = 8.dp)
                        .size(88.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = card.accentColor
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = card.illustrationLabel,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduledReportsSection(
    schedules: List<ReportSchedule>,
    selectedDevice: Device?,
    isLoading: Boolean,
    isRefreshing: Boolean,
    errorMessage: String?,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Scheduled Reports",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )

                Button(
                    onClick = onRefresh,
                    enabled = selectedDevice != null && !isRefreshing
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isRefreshing) "Refreshing..." else "Refresh")
                }
            }

            ScheduledReportsHeader()

            when {
                isLoading -> {
                    LoadingReportsState()
                }

                errorMessage != null && selectedDevice != null -> {
                    EmptyReportsState(
                        title = "Unable to load schedules",
                        description = errorMessage
                    )
                }

                selectedDevice == null -> {
                    EmptyReportsState(
                        title = "Select a device to view scheduled reports",
                        description = "Choose industry, OEM, customer, and device to load scheduled reports."
                    )
                }

                schedules.isEmpty() -> {
                    EmptyReportsState(
                        title = "No scheduled reports found for this device",
                        description = "This device does not have any saved summary or schedule reports yet."
                    )
                }

                else -> {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        schedules.forEach { schedule ->
                            ReportScheduleCard(schedule = schedule)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduledReportsHeader() {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ScheduleHeaderChip("DEVICE ID")
        ScheduleHeaderChip("FIELDS")
        ScheduleHeaderChip("EMAIL")
        ScheduleHeaderChip("SUBJECT")
        ScheduleHeaderChip("SCHEDULE")
    }
}

@Composable
private fun ScheduleHeaderChip(label: String) {
    Surface(
        color = Color(0xFFE8EEF7),
        shape = RoundedCornerShape(999.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFF4B5D7A)
        )
    }
}

@Composable
private fun LoadingReportsState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CircularProgressIndicator()
        Text(
            text = "Loading scheduled reports...",
            style = MaterialTheme.typography.bodyMedium,
            color = MutedText
        )
    }
}

@Composable
private fun EmptyReportsState(
    title: String,
    description: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFFF7F9FC)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MutedText
            )
        }
    }
}

@Composable
private fun ReportScheduleCard(schedule: ReportSchedule) {
    val formatter = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }
    val createdAtText = remember(schedule.createdAt) {
        runCatching { formatter.format(Date(schedule.createdAt)) }.getOrElse { "-" }
    }
    val updatedAtText = remember(schedule.updatedAt) {
        runCatching { formatter.format(Date(schedule.updatedAt)) }.getOrElse { "-" }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FBFF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = schedule.subject,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = schedule.deviceId,
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedText
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        text = schedule.schedule,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Text(
                text = schedule.body,
                style = MaterialTheme.typography.bodyMedium,
                color = MutedText
            )

            ReportInfoRow(label = "Email", value = schedule.email)
            ReportInfoRow(
                label = "Fields",
                value = if (schedule.fields.isEmpty()) {
                    "No fields selected"
                } else {
                    "${schedule.fields.size} selected: ${schedule.fields.take(3).joinToString()}" +
                        if (schedule.fields.size > 3) "..." else ""
                }
            )
            ReportInfoRow(label = "Created", value = createdAtText)
            ReportInfoRow(label = "Updated", value = updatedAtText)
        }
    }
}

@Composable
private fun ReportInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            color = MutedText,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
