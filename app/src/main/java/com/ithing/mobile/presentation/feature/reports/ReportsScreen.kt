package com.ithing.mobile.presentation.feature.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.ithing.mobile.presentation.components.EmptyState
import com.ithing.mobile.presentation.components.IThingButton
import com.ithing.mobile.presentation.components.IThingCard
import com.ithing.mobile.presentation.components.IThingScreenContainer
import com.ithing.mobile.presentation.components.InfoRow
import com.ithing.mobile.presentation.components.LoadingIndicator
import com.ithing.mobile.presentation.components.SectionHeader
import com.ithing.mobile.presentation.components.StatusBadge
import com.ithing.mobile.presentation.theme.getGridColumns
import com.ithing.mobile.presentation.theme.rememberWindowSizeClass

// Data models for reports
data class ReportItem(
    val id: String,
    val title: String,
    val description: String,
    val status: ReportStatus,
    val createdAt: String,
    val type: ReportType,
    val deviceName: String,
    val customerName: String
)

enum class ReportStatus {
    COMPLETED, IN_PROGRESS, SCHEDULED, FAILED
}

enum class ReportType {
    DAILY, WEEKLY, MONTHLY, CUSTOM
}

// Sample data
fun getSampleReports(): List<ReportItem> {
    return listOf(
        ReportItem(
            id = "1",
            title = "Daily Production Report",
            description = "Machine performance and output metrics for today",
            status = ReportStatus.COMPLETED,
            createdAt = "2024-01-15 09:30 AM",
            type = ReportType.DAILY,
            deviceName = "Production Line A",
            customerName = "ABC Manufacturing"
        ),
        ReportItem(
            id = "2",
            title = "Weekly Maintenance Report",
            description = "Maintenance activities and equipment health summary",
            status = ReportStatus.IN_PROGRESS,
            createdAt = "2024-01-15 08:15 AM",
            type = ReportType.WEEKLY,
            deviceName = "CNC Machine 3",
            customerName = "XYZ Industries"
        ),
        ReportItem(
            id = "3",
            title = "Monthly Quality Analysis",
            description = "Quality metrics and defect analysis for the month",
            status = ReportStatus.SCHEDULED,
            createdAt = "2024-01-14 04:45 PM",
            type = ReportType.MONTHLY,
            deviceName = "Quality Control Station",
            customerName = "Quality Corp"
        ),
        ReportItem(
            id = "4",
            title = "Custom Energy Report",
            description = "Energy consumption and efficiency analysis",
            status = ReportStatus.FAILED,
            createdAt = "2024-01-14 02:20 PM",
            type = ReportType.CUSTOM,
            deviceName = "Power System 1",
            customerName = "Energy Solutions Ltd"
        ),
        ReportItem(
            id = "5",
            title = "Daily Safety Report",
            description = "Safety incidents and compliance metrics",
            status = ReportStatus.COMPLETED,
            createdAt = "2024-01-15 07:00 AM",
            type = ReportType.DAILY,
            deviceName = "All Departments",
            customerName = "Safety First Inc"
        ),
        ReportItem(
            id = "6",
            title = "Weekly Inventory Report",
            description = "Stock levels and inventory turnover analysis",
            status = ReportStatus.COMPLETED,
            createdAt = "2024-01-15 06:30 AM",
            type = ReportType.WEEKLY,
            deviceName = "Warehouse System",
            customerName = "Logistics Plus"
        )
    )
}

@Composable
fun ReportsScreen(
    modifier: Modifier = Modifier,
    reports: List<ReportItem> = getSampleReports(),
    isLoading: Boolean = false,
    onRefresh: () -> Unit = {},
    onReportClick: (ReportItem) -> Unit = {},
    onDownloadReport: (ReportItem) -> Unit = {},
    onFilterClick: () -> Unit = {},
    onDateRangeClick: () -> Unit = {}
) {
    val windowSizeClass = rememberWindowSizeClass()
    val gridColumns = getGridColumns()
    
    IThingScreenContainer { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
                end = paddingValues.calculateEndPadding(LayoutDirection.Ltr),
                top = 0.dp,
                bottom = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Header
                ReportsHeader(
                    onRefresh = onRefresh,
                    onFilterClick = onFilterClick,
                    onDateRangeClick = onDateRangeClick,
                    isLoading = isLoading
                )
            }
            
            item {
                // Reports Grid
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        LoadingIndicator(message = "Loading reports...")
                    }
                } else if (reports.isEmpty()) {
                    EmptyState(
                        title = "No Reports Available",
                        description = "There are no reports to display. Generate your first report to get started.",
                        actionText = "Generate Report",
                        onAction = { /* TODO: Navigate to report generation */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(gridColumns),
                        contentPadding = PaddingValues(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(reports) { report ->
                            ReportCard(
                                report = report,
                                onClick = { onReportClick(report) },
                                onDownload = { onDownloadReport(report) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportsHeader(
    onRefresh: () -> Unit,
    onFilterClick: () -> Unit,
    onDateRangeClick: () -> Unit,
    isLoading: Boolean
) {
    IThingCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = 2
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SectionHeader(
                title = "Reports",
                subtitle = "View and manage your generated reports"
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IThingButton(
                    text = "Filter",
                    onClick = onFilterClick,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier.weight(1f)
                )

                IThingButton(
                    text = "Date Range",
                    onClick = onDateRangeClick,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = onRefresh,
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportCard(
    report: ReportItem,
    onClick: () -> Unit,
    onDownload: () -> Unit
) {
    IThingCard(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = 1
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Title and Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = report.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                
                StatusBadge(
                    text = getStatusText(report.status),
                    isActive = report.status == ReportStatus.COMPLETED
                )
            }
            
            // Description
            Text(
                text = report.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
            
            // Info Rows
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                InfoRow(
                    label = "Device",
                    value = report.deviceName
                )
                InfoRow(
                    label = "Customer",
                    value = report.customerName
                )
                InfoRow(
                    label = "Created",
                    value = report.createdAt
                )
            }
            
            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IThingButton(
                    text = "View",
                    onClick = onClick,
                    modifier = Modifier.weight(1f)
                )
                
                if (report.status == ReportStatus.COMPLETED) {
                    IThingButton(
                        text = "Download",
                        onClick = onDownload,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun getStatusText(status: ReportStatus): String {
    return when (status) {
        ReportStatus.COMPLETED -> "Completed"
        ReportStatus.IN_PROGRESS -> "In Progress"
        ReportStatus.SCHEDULED -> "Scheduled"
        ReportStatus.FAILED -> "Failed"
    }
}

@Composable
private fun getStatusColor(status: ReportStatus) {
    // This can be used to customize status badge colors
    when (status) {
        ReportStatus.COMPLETED -> MaterialTheme.colorScheme.primary
        ReportStatus.IN_PROGRESS -> MaterialTheme.colorScheme.secondary
        ReportStatus.SCHEDULED -> MaterialTheme.colorScheme.tertiary
        ReportStatus.FAILED -> MaterialTheme.colorScheme.error
    }
}
