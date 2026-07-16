package com.ithing.mobile.presentation.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.ithing.mobile.presentation.components.EmptyState
import com.ithing.mobile.presentation.components.IThingButton
import com.ithing.mobile.presentation.components.IThingCard
import com.ithing.mobile.presentation.components.IThingScreenContainer
import com.ithing.mobile.presentation.components.LoadingIndicator
import com.ithing.mobile.presentation.theme.LightGrayBg
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onFilterScreenVisibilityChanged: (Boolean) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val selectedCustomerId = uiState.selectedCustomer?.id
    val selectedDeviceId = uiState.selectedDevice?.id

    DisposableEffect(lifecycleOwner, selectedCustomerId, selectedDeviceId) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    if (selectedCustomerId != null && selectedDeviceId != null) {
                        viewModel.startAutoRefresh()
                    }
                }
                Lifecycle.Event.ON_PAUSE -> viewModel.stopAutoRefresh()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            if (selectedCustomerId != null && selectedDeviceId != null) {
                viewModel.startAutoRefresh()
            } else {
                viewModel.stopAutoRefresh()
            }
        }

        onDispose {
            viewModel.stopAutoRefresh()
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    DashboardContent(
        uiState = uiState,
        onIndustrySelected = viewModel::onIndustrySelected,
        onOemSelected = viewModel::onOemSelected,
        onCustomerSelected = viewModel::onCustomerSelected,
        onDeviceSelected = viewModel::onDeviceSelected,
        onGroupSelected = viewModel::onGroupSelected,
        onFilterEditStarted = viewModel::beginFilterEditing,
        onFilterApplied = viewModel::applyFilterEditing,
        onFilterCancelled = viewModel::cancelFilterEditing,
        onFilterScreenVisibilityChanged = onFilterScreenVisibilityChanged,
        onRefresh = viewModel::refreshDashboard
    )
}

@Composable
private fun DashboardContent(
    uiState: DashboardUiState,
    onIndustrySelected: (com.ithing.mobile.domain.model.Industry?) -> Unit,
    onOemSelected: (com.ithing.mobile.domain.model.Oem?) -> Unit,
    onCustomerSelected: (com.ithing.mobile.domain.model.Customer?) -> Unit,
    onDeviceSelected: (com.ithing.mobile.domain.model.Device?) -> Unit,
    onGroupSelected: (String) -> Unit,
    onFilterEditStarted: () -> Unit,
    onFilterApplied: () -> Unit,
    onFilterCancelled: () -> Unit,
    onFilterScreenVisibilityChanged: (Boolean) -> Unit,
    onRefresh: () -> Unit
) {
    var dashboardToastMessage by remember { mutableStateOf<String?>(null) }
    var showFilterScreen by remember { mutableStateOf(false) }
    var dashboardStateBeforeFilter by remember { mutableStateOf<DashboardUiState?>(null) }

    LaunchedEffect(showFilterScreen) {
        onFilterScreenVisibilityChanged(showFilterScreen)
    }
    DisposableEffect(Unit) {
        onDispose { onFilterScreenVisibilityChanged(false) }
    }

    LaunchedEffect(showFilterScreen, uiState.isRefreshing) {
        if (!showFilterScreen && dashboardStateBeforeFilter != null && !uiState.isRefreshing) {
            dashboardStateBeforeFilter = null
        }
    }

    val contentState = dashboardStateBeforeFilter ?: uiState
    val filteredWidgets = contentState.widgets.filter {
        contentState.selectedGroup == "All" || it.dashboardName == contentState.selectedGroup
    }
    val showFullScreenLoader =
        contentState.isLoading &&
                contentState.industries.isEmpty() &&
                contentState.oems.isEmpty() &&
                contentState.customers.isEmpty() &&
                contentState.devices.isEmpty()

    Box(modifier = Modifier.fillMaxSize()) {
        IThingScreenContainer { paddingValues ->
            Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LightGrayBg),
                contentPadding = PaddingValues(
                    start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
                    end = paddingValues.calculateEndPadding(LayoutDirection.Ltr),
                    top = 12.dp,
                    bottom = 14.dp
                )
            ) {
                item {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        DashboardActionsSection(
                            groups = contentState.availableGroups,
                            selectedGroup = contentState.selectedGroup,
                            selectedCustomer = contentState.selectedCustomer != null,
                            selectedDevice = contentState.selectedDevice != null,
                            isRefreshing = contentState.isRefreshing,
                            lastUpdatedAt = contentState.lastUpdatedAt,
                            selectedIndustryName = contentState.selectedIndustry?.name,
                            selectedCustomerName = contentState.selectedCustomer?.name,
                            selectedDeviceId = contentState.selectedDevice?.id,
                            onGroupSelected = onGroupSelected,
                            onFilterClick = {
                                dashboardStateBeforeFilter = uiState
                                onFilterEditStarted()
                                showFilterScreen = true
                            },
                            onRefresh = onRefresh
                        )

                        contentState.errorMessage?.let { message ->
                            ErrorBanner(message = message)
                        }

                        if (!contentState.isRefreshing && filteredWidgets.isEmpty()) {
                            DashboardEmptyState(
                                hasSelection = contentState.selectedCustomer != null && contentState.selectedDevice != null,
                                onCreateDashboardClick = {
                                    dashboardToastMessage =
                                        "Please use iThing website to create dashboard."
                                }
                            )
                        }
                    }
                }

                if (filteredWidgets.isNotEmpty()) {
                    dashboardWidgetGridItems(
                        widgets = contentState.widgets,
                        selectedGroup = contentState.selectedGroup
                    )
                }
            }

            dashboardToastMessage?.let { message ->
                DashboardTopToast(
                    title = "Dashboard",
                    message = message,
                    onDismiss = { dashboardToastMessage = null }
                )
            }
        }

            if (showFullScreenLoader) {
                FullScreenDashboardLoader()
            } else if (contentState.isRefreshing && contentState.widgets.isEmpty()) {
                FullScreenDashboardLoader(message = "Refreshing dashboard...")
            }
        }

        if (showFilterScreen) {
            DashboardFilterScreen(
                uiState = uiState,
                onIndustrySelected = onIndustrySelected,
                onOemSelected = onOemSelected,
                onCustomerSelected = onCustomerSelected,
                onDeviceSelected = onDeviceSelected,
                onCancel = {
                    onFilterCancelled()
                    dashboardStateBeforeFilter = null
                    showFilterScreen = false
                },
                onApply = {
                    onFilterApplied()
                    dashboardStateBeforeFilter = null
                    showFilterScreen = false
                }
            )
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun DashboardActionsSection(
    groups: List<String>,
    selectedGroup: String,
    selectedCustomer: Boolean,
    selectedDevice: Boolean,
    isRefreshing: Boolean,
    lastUpdatedAt: Long?,
    selectedIndustryName: String?,
    selectedCustomerName: String?,
    selectedDeviceId: String?,
    onGroupSelected: (String) -> Unit,
    onFilterClick: () -> Unit,
    onRefresh: () -> Unit
) {
    val lastUpdatedText = lastUpdatedAt?.let {
        SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(Date(it))
    } ?: "-"
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Dashboard Last Updated at $lastUpdatedText",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF0B3B92),
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = "Industry: ${selectedIndustryName ?: "-"}  •  " +
                "Customer: ${selectedCustomerName ?: "-"}  •  " +
                "Device ID: ${selectedDeviceId ?: "-"}",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF4B5563),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth(),
            color = Color(0xFFE8EEF6),
            shape = RoundedCornerShape(10.dp),
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DashboardGroupSelector(
                    groups = groups,
                    selectedGroup = selectedGroup,
                    onGroupSelected = onGroupSelected,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = onFilterClick) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Open dashboard filters"
                    )
                }

                IThingButton(
                    text = if (isRefreshing) "Refreshing" else "Refresh",
                    onClick = onRefresh,
                    enabled = selectedCustomer && selectedDevice && !isRefreshing,
                    isLoading = isRefreshing,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                        )
                    },
                    modifier = Modifier.widthIn(min = 112.dp)
                )
            }
        }
    }
}

@Composable
private fun DashboardStatusSummary(
    lastUpdatedAt: Long?,
    isRefreshing: Boolean,
    hasSelection: Boolean,
    hasError: Boolean,
) {
    val statusText = when {
        isRefreshing -> "Refreshing"
        hasError -> "Error"
        !hasSelection -> "Select filters"
        else -> "Ready"
    }

    val updatedText = lastUpdatedAt?.let {
        "Updated: " + SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(Date(it))
    } ?: "Updated: -"

    Column(
        modifier = Modifier.padding(vertical = 2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = "Status: $statusText",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = updatedText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DashboardEmptyState(
    hasSelection: Boolean,
    onCreateDashboardClick: () -> Unit
) {
    EmptyState(
        title = if (hasSelection) {
            "No dashboard settings configured"
        } else {
            "Choose filters to continue"
        },
        description = if (hasSelection) {
            "Please set up your dashboard to proceed."
        } else {
            "Select industry, OEM, customer, and device to view dashboard."
        },
        actionText = if (hasSelection) "Create Dashboard" else null,
        onAction = if (hasSelection) {
            onCreateDashboardClick
        } else null,
        modifier = Modifier.padding(vertical = 16.dp)
    )
}

@Composable
private fun DashboardTopToast(
    title: String,
    message: String,
    onDismiss: () -> Unit
) {
    LaunchedEffect(message) {
        delay(4500)
        onDismiss()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 10.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Surface(
            color = Color(0xFF0F172A),
            shape = RoundedCornerShape(14.dp),
            shadowElevation = 6.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(Color(0xFF22C55E), CircleShape)
                )
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFE2E8F0),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF94A3B8)
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorBanner(message: String) {
    IThingCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = 0
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(12.dp),
            color = MaterialTheme.colorScheme.onErrorContainer,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun FullScreenDashboardLoader(message: String = "Loading dashboard...") {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.92f)),
        contentAlignment = Alignment.Center
    ) {
        LoadingIndicator(
            message = message,
            modifier = Modifier.padding(16.dp)
        )
    }
}
