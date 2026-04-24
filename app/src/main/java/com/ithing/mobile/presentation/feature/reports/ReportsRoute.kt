package com.ithing.mobile.presentation.feature.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardDoubleArrowLeft
import androidx.compose.material.icons.filled.KeyboardDoubleArrowRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
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

private data class ReportTypeCardModel(
    val kind: ReportTypeCardKind,
    val title: String,
    val description: String,
    val illustrationLabel: String,
    val accentColor: Color
)

private enum class ReportTypeCardKind {
    SUMMARY,
    ANALYTIC,
    SCHEDULE,
    EXCEPTION
}

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
        onSummaryReportClick = viewModel::onSummaryReportClick,
        onDismissSummaryDialog = viewModel::dismissSummaryDialog,
        onDismissSummaryMessage = viewModel::dismissSummaryMessage,
        onSummaryEmailChanged = viewModel::onSummaryEmailChanged,
        onSummarySubjectChanged = viewModel::onSummarySubjectChanged,
        onSummaryBodyChanged = viewModel::onSummaryBodyChanged,
        onSummaryTimeSpanPresetSelected = viewModel::onSummaryTimeSpanPresetSelected,
        onSummaryCustomDateRangeSelected = viewModel::onSummaryCustomDateRangeSelected,
        onSummarySearchChanged = viewModel::onSummarySearchQueryChanged,
        onSummaryFieldToggled = viewModel::onSummaryFieldToggled,
        onSummarySelectAllToggled = viewModel::onSummarySelectAllToggled,
        onSummarySaveClick = viewModel::onSummarySaveClick,
        onScheduleReportClick = viewModel::onScheduleReportClick,
        onDismissScheduleDialog = viewModel::dismissScheduleDialog,
        onDismissScheduleMessage = viewModel::dismissScheduleMessage,
        onScheduleEmailChanged = viewModel::onScheduleEmailChanged,
        onScheduleSubjectChanged = viewModel::onScheduleSubjectChanged,
        onScheduleBodyChanged = viewModel::onScheduleBodyChanged,
        onScheduleFrequencyChanged = viewModel::onScheduleFrequencyChanged,
        onScheduleSearchChanged = viewModel::onScheduleSearchQueryChanged,
        onScheduleFieldToggled = viewModel::onScheduleFieldToggled,
        onScheduleSelectAllToggled = viewModel::onScheduleSelectAllToggled,
        onScheduleSaveClick = viewModel::onScheduleSaveClick,
        onAnalyticReportClick = viewModel::onAnalyticReportClick,
        onDismissAnalyticsDialog = viewModel::dismissAnalyticsDialog,
        onDismissAnalyticsMessage = viewModel::dismissAnalyticsMessage,
        onAnalyticsTimeSpanPresetSelected = viewModel::onAnalyticsTimeSpanPresetSelected,
        onAnalyticsCustomDateRangeSelected = viewModel::onAnalyticsCustomDateRangeSelected,
        onAnalyticsRowTitleChanged = viewModel::onAnalyticsRowTitleChanged,
        onAnalyticsRowChartTypeChanged = viewModel::onAnalyticsRowChartTypeChanged,
        onAnalyticsRowFieldChanged = viewModel::onAnalyticsRowFieldChanged,
        onAnalyticsRowFrequencyChanged = viewModel::onAnalyticsRowFrequencyChanged,
        onAddAnalyticsRow = viewModel::addAnalyticsRow,
        onRemoveAnalyticsRow = viewModel::removeAnalyticsRow,
        onAnalyticsSaveViewClick = viewModel::onAnalyticsSaveViewClick,
        onAnalyticsGeneratePdfClick = viewModel::onAnalyticsGeneratePdfClick,
        onRefresh = viewModel::refreshReports,
        onPageChange = viewModel::goToPage
    )
}

@Composable
private fun ReportsScreen(
    uiState: ReportsUiState,
    onIndustrySelected: (Industry?) -> Unit,
    onOemSelected: (Oem?) -> Unit,
    onCustomerSelected: (Customer?) -> Unit,
    onDeviceSelected: (Device?) -> Unit,
    onSummaryReportClick: () -> Unit,
    onDismissSummaryDialog: () -> Unit,
    onDismissSummaryMessage: () -> Unit,
    onSummaryEmailChanged: (String) -> Unit,
    onSummarySubjectChanged: (String) -> Unit,
    onSummaryBodyChanged: (String) -> Unit,
    onSummaryTimeSpanPresetSelected: (AnalyticsDatePreset) -> Unit,
    onSummaryCustomDateRangeSelected: (Long, Long) -> Unit,
    onSummarySearchChanged: (String) -> Unit,
    onSummaryFieldToggled: (String) -> Unit,
    onSummarySelectAllToggled: () -> Unit,
    onSummarySaveClick: () -> Unit,
    onScheduleReportClick: () -> Unit,
    onDismissScheduleDialog: () -> Unit,
    onDismissScheduleMessage: () -> Unit,
    onScheduleEmailChanged: (String) -> Unit,
    onScheduleSubjectChanged: (String) -> Unit,
    onScheduleBodyChanged: (String) -> Unit,
    onScheduleFrequencyChanged: (ScheduleDeliveryFrequency?) -> Unit,
    onScheduleSearchChanged: (String) -> Unit,
    onScheduleFieldToggled: (String) -> Unit,
    onScheduleSelectAllToggled: () -> Unit,
    onScheduleSaveClick: () -> Unit,
    onAnalyticReportClick: () -> Unit,
    onDismissAnalyticsDialog: () -> Unit,
    onDismissAnalyticsMessage: () -> Unit,
    onAnalyticsTimeSpanPresetSelected: (AnalyticsDatePreset) -> Unit,
    onAnalyticsCustomDateRangeSelected: (Long, Long) -> Unit,
    onAnalyticsRowTitleChanged: (String, String) -> Unit,
    onAnalyticsRowChartTypeChanged: (String, AnalyticsChartType?) -> Unit,
    onAnalyticsRowFieldChanged: (String, String?) -> Unit,
    onAnalyticsRowFrequencyChanged: (String, AnalyticsFrequency?) -> Unit,
    onAddAnalyticsRow: () -> Unit,
    onRemoveAnalyticsRow: (String) -> Unit,
    onAnalyticsSaveViewClick: () -> Unit,
    onAnalyticsGeneratePdfClick: () -> Unit,
    onRefresh: () -> Unit,
    onPageChange: (Int) -> Unit
) {
    var selectedFields by remember { mutableStateOf<List<String>?>(null) }

    val reportCards = remember {
        listOf(
            ReportTypeCardModel(
                kind = ReportTypeCardKind.SUMMARY,
                title = "Summary Report",
                description = "Summarized machine metrics enabling informed management decisions.",
                illustrationLabel = "SR",
                accentColor = Color(0xFFFFB020)
            ),
            ReportTypeCardModel(
                kind = ReportTypeCardKind.ANALYTIC,
                title = "Analytic Report",
                description = "Visualize performance trends over time using intuitive charts.",
                illustrationLabel = "AR",
                accentColor = Color(0xFF4C7DFF)
            ),
            ReportTypeCardModel(
                kind = ReportTypeCardKind.SCHEDULE,
                title = "Schedule Report",
                description = "Enable periodic reporting to streamline data monitoring.",
                illustrationLabel = "SC",
                accentColor = Color(0xFF22C55E)
            ),
            ReportTypeCardModel(
                kind = ReportTypeCardKind.EXCEPTION,
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
                ReportsTypeSection(
                    reportCards = reportCards,
                    onSummaryReportClick = onSummaryReportClick,
                    onScheduleReportClick = onScheduleReportClick,
                    onAnalyticReportClick = onAnalyticReportClick
                )
            }

            item {
                ScheduledReportsSection(
                    schedules = uiState.schedules,
                    currentPage = uiState.currentPage,
                    pageSize = uiState.pageSize,
                    totalCount = uiState.totalCount,
                    totalPages = uiState.totalPages,
                    selectedDevice = uiState.selectedDevice,
                    isLoading = uiState.isLoading,
                    isRefreshing = uiState.isRefreshing,
                    errorMessage = uiState.errorMessage,
                    onRefresh = onRefresh,
                    onViewFields = { selectedFields = it },
                    onPageChange = onPageChange
                )
            }
        }

        selectedFields?.let { fields ->
            ReportFieldsDialog(
                fields = fields,
                onDismiss = { selectedFields = null }
            )
        }

        if (uiState.isAnalyticsDialogVisible) {
            AnalyticsReportDialog(
                uiState = uiState,
                onDismiss = onDismissAnalyticsDialog,
                onTimeSpanPresetSelected = onAnalyticsTimeSpanPresetSelected,
                onCustomDateRangeSelected = onAnalyticsCustomDateRangeSelected,
                onRowTitleChanged = onAnalyticsRowTitleChanged,
                onRowChartTypeChanged = onAnalyticsRowChartTypeChanged,
                onRowFieldChanged = onAnalyticsRowFieldChanged,
                onRowFrequencyChanged = onAnalyticsRowFrequencyChanged,
                onAddMore = onAddAnalyticsRow,
                onRemoveRow = onRemoveAnalyticsRow,
                onSaveViewClick = onAnalyticsSaveViewClick,
                onGeneratePdfClick = onAnalyticsGeneratePdfClick
            )
        }

        if (uiState.isSummaryDialogVisible) {
            SummaryReportDialog(
                uiState = uiState,
                filteredFields = viewModelSummaryFields(uiState),
                onDismiss = onDismissSummaryDialog,
                onEmailChanged = onSummaryEmailChanged,
                onSubjectChanged = onSummarySubjectChanged,
                onBodyChanged = onSummaryBodyChanged,
                onTimeSpanPresetSelected = onSummaryTimeSpanPresetSelected,
                onCustomDateRangeSelected = onSummaryCustomDateRangeSelected,
                onSearchChanged = onSummarySearchChanged,
                onToggleField = onSummaryFieldToggled,
                onToggleSelectAll = onSummarySelectAllToggled,
                onSaveClick = onSummarySaveClick
            )
        }

        uiState.summaryDialogMessage?.let { message ->
            AlertDialog(
                onDismissRequest = onDismissSummaryMessage,
                confirmButton = {
                    Button(onClick = onDismissSummaryMessage) {
                        Text("OK")
                    }
                },
                title = { Text("Summary Report") },
                text = { Text(message) }
            )
        }

        if (uiState.isScheduleDialogVisible) {
            ScheduleReportDialog(
                uiState = uiState,
                filteredFields = viewModelScheduleFields(uiState),
                onDismiss = onDismissScheduleDialog,
                onEmailChanged = onScheduleEmailChanged,
                onSubjectChanged = onScheduleSubjectChanged,
                onBodyChanged = onScheduleBodyChanged,
                onFrequencyChanged = onScheduleFrequencyChanged,
                onSearchChanged = onScheduleSearchChanged,
                onToggleField = onScheduleFieldToggled,
                onToggleSelectAll = onScheduleSelectAllToggled,
                onSaveClick = onScheduleSaveClick
            )
        }

        uiState.scheduleDialogMessage?.let { message ->
            AlertDialog(
                onDismissRequest = onDismissScheduleMessage,
                confirmButton = {
                    Button(onClick = onDismissScheduleMessage) {
                        Text("OK")
                    }
                },
                title = { Text("Schedule Report") },
                text = { Text(message) }
            )
        }

        uiState.analyticsDialogMessage?.let { message ->
            AlertDialog(
                onDismissRequest = onDismissAnalyticsMessage,
                confirmButton = {
                    Button(onClick = onDismissAnalyticsMessage) {
                        Text("OK")
                    }
                },
                title = { Text("Analytics Report") },
                text = { Text(message) }
            )
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
                        .menuAnchor(
                            type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                            enabled = enabled
                        ),
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
private fun ReportsTypeSection(
    reportCards: List<ReportTypeCardModel>,
    onSummaryReportClick: () -> Unit,
    onScheduleReportClick: () -> Unit,
    onAnalyticReportClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        reportCards.forEach { card ->
            ReportTypeCard(
                card = card,
                onClick = {
                    when (card.kind) {
                        ReportTypeCardKind.SUMMARY -> onSummaryReportClick()
                        ReportTypeCardKind.ANALYTIC -> onAnalyticReportClick()
                        ReportTypeCardKind.SCHEDULE -> onScheduleReportClick()
                        else -> Unit
                    }
                }
            )
        }
    }
}

private fun viewModelSummaryFields(uiState: ReportsUiState): List<String> {
    val query = uiState.summarySearchQuery.trim()
    return if (query.isBlank()) {
        uiState.availableSummaryFields
    } else {
        uiState.availableSummaryFields.filter { it.contains(query, ignoreCase = true) }
    }
}

private fun viewModelScheduleFields(uiState: ReportsUiState): List<String> {
    val query = uiState.scheduleSearchQuery.trim()
    return if (query.isBlank()) {
        uiState.availableScheduleFields
    } else {
        uiState.availableScheduleFields.filter { it.contains(query, ignoreCase = true) }
    }
}

@Composable
private fun ReportTypeCard(
    card: ReportTypeCardModel,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
    currentPage: Int,
    pageSize: Int,
    totalCount: Int,
    totalPages: Int,
    selectedDevice: Device?,
    isLoading: Boolean,
    isRefreshing: Boolean,
    errorMessage: String?,
    onRefresh: () -> Unit,
    onViewFields: (List<String>) -> Unit,
    onPageChange: (Int) -> Unit
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
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        ReportsTableContainer {
                            ScheduledReportsTableHeader()
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                schedules.forEach { schedule ->
                                    ScheduledReportsRow(
                                        schedule = schedule,
                                        onViewFields = onViewFields
                                    )
                                }
                            }
                        }

                        ReportsPaginationFooter(
                            currentPage = currentPage,
                            pageSize = pageSize,
                            totalCount = totalCount,
                            totalPages = totalPages,
                            onPageChange = onPageChange
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportsTableContainer(
    content: @Composable ColumnScope.() -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
    ) {
        Column(
            modifier = Modifier.width(840.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}

@Composable
private fun ScheduledReportsTableHeader() {
    Row(
        modifier = Modifier
            .width(840.dp)
            .padding(start = 18.dp, end = 18.dp, top = 6.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ScheduledReportsHeaderCell("DEVICE ID", 230.dp)
        ScheduledReportsHeaderCell("FIELDS", 90.dp)
        ScheduledReportsHeaderCell("EMAIL", 210.dp)
        ScheduledReportsHeaderCell("SCHEDULE", 120.dp)
        ScheduledReportsHeaderCell("ACTION", 130.dp)
    }
}

@Composable
private fun ScheduledReportsHeaderCell(
    text: String,
    width: Dp
) {
    Text(
        text = text,
        modifier = Modifier.width(width),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = Color(0xFF52637E),
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Clip
    )
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
private fun ScheduledReportsRow(
    schedule: ReportSchedule,
    onViewFields: (List<String>) -> Unit,
    onDeleteClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.width(840.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .width(840.dp)
                .padding(horizontal = 18.dp, vertical = 22.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ScheduledReportCell(schedule.deviceId, 230.dp)
            ScheduledReportFieldsCell(
                width = 90.dp,
                onClick = { onViewFields(schedule.fields) }
            )
            ScheduledReportCell(schedule.email, 210.dp)
            ScheduledReportCell(schedule.schedule, 120.dp)
            ScheduledReportDeleteCell(width = 130.dp, onClick = onDeleteClick)
        }
    }
}

@Composable
private fun ScheduledReportCell(
    text: String,
    width: Dp
) {
    Text(
        text = text,
        modifier = Modifier.width(width),
        style = MaterialTheme.typography.bodyLarge,
        color = Color(0xFF53637D),
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun ScheduledReportFieldsCell(
    width: Dp,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(width)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.material3.Icon(
            imageVector = Icons.Outlined.Visibility,
            contentDescription = null,
            tint = Color(0xFF159E9C),
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun ReportFieldsDialog(
    fields: List<String>,
    onDismiss: () -> Unit
) {
    val leftColumn = fields.filterIndexed { index, _ -> index % 2 == 0 }
    val rightColumn = fields.filterIndexed { index, _ -> index % 2 != 0 }
    val rowCount = maxOf(leftColumn.size, rightColumn.size)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = White)
        ) {
            Column(
                modifier = Modifier.padding(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Reports Fields",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF52637E)
                    )

                    IconButton(onClick = onDismiss) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF6B7280)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0xFFDCE3ED))
                )

                Column(
                    modifier = Modifier.padding(horizontal = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Fields",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF52637E)
                        )
                        Text(
                            text = "Fields",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF52637E)
                        )
                    }

                    repeat(rowCount) { index ->
                        val left = leftColumn.getOrNull(index).orEmpty()
                        val right = rightColumn.getOrNull(index).orEmpty()
                        Surface(
                            color = if (index % 2 == 0) Color(0xFFF2F6FB) else White,
                            shape = RoundedCornerShape(0.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = left,
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color(0xFF53637D)
                                )
                                Text(
                                    text = right,
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color(0xFF53637D)
                                )
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp)
                        .height(1.dp)
                        .background(Color(0xFFDCE3ED))
                )
            }
        }
    }
}

@Composable
private fun ReportsPaginationFooter(
    currentPage: Int,
    pageSize: Int,
    totalCount: Int,
    totalPages: Int,
    onPageChange: (Int) -> Unit
) {
    val startEntry = if (totalCount == 0) 0 else ((currentPage - 1) * pageSize) + 1
    val endEntry = if (totalCount == 0) 0 else minOf(currentPage * pageSize, totalCount)

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(18.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PaginationIconButton(
                icon = Icons.Default.KeyboardDoubleArrowLeft,
                enabled = currentPage > 1,
                onClick = { onPageChange(1) }
            )
            PaginationIconButton(
                icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                enabled = currentPage > 1,
                onClick = { onPageChange(currentPage - 1) }
            )
            Text(
                text = currentPage.toString(),
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFF2A3347),
                fontWeight = FontWeight.Medium
            )
            PaginationIconButton(
                icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                enabled = currentPage < totalPages,
                onClick = { onPageChange(currentPage + 1) }
            )
            PaginationIconButton(
                icon = Icons.Default.KeyboardDoubleArrowRight,
                enabled = currentPage < totalPages,
                onClick = { onPageChange(totalPages) }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Showing $startEntry to $endEntry of $totalCount entries",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF4F5D74)
            )

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = White,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = pageSize.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        color = Color(0xFF4F5D74)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "⌄",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color(0xFF4F5D74)
                    )
                }
            }
        }
    }
}

@Composable
private fun PaginationIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean,
    onClick: () -> Unit
) {
    androidx.compose.material3.IconButton(
        onClick = onClick,
        enabled = enabled
    ) {
        androidx.compose.material3.Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) Color(0xFF2A3347) else Color(0xFFB8C0CC)
        )
    }
}

@Composable
private fun ScheduledReportDeleteCell(
    width: Dp,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .width(width)
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            ),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(34.dp)
                .background(Color(0xFFD9E1EC))
        )
        Spacer(modifier = Modifier.width(16.dp))
        androidx.compose.material3.Icon(
            imageVector = Icons.Outlined.DeleteOutline,
            contentDescription = null,
            tint = Color(0xFFD62828),
            modifier = Modifier.size(22.dp)
        )
    }
}
