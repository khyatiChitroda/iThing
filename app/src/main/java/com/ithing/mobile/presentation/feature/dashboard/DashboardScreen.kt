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
    viewModel: DashboardViewModel
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
    onRefresh: () -> Unit
) {
    var dashboardToastMessage by remember { mutableStateOf<String?>(null) }

    val filteredWidgets = uiState.widgets.filter {
        uiState.selectedGroup == "All" || it.dashboardName == uiState.selectedGroup
    }
    val showFullScreenLoader =
        uiState.isLoading &&
                uiState.industries.isEmpty() &&
                uiState.oems.isEmpty() &&
                uiState.customers.isEmpty() &&
                uiState.devices.isEmpty()

    IThingScreenContainer { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LightGrayBg),
                contentPadding = PaddingValues(
                    start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
                    end = paddingValues.calculateEndPadding(LayoutDirection.Ltr),
                    top = 16.dp,
                    bottom = 16.dp
                )
            ) {
                item {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {

                        FilterSection(
                            industries = uiState.industries,
                            oems = uiState.oems,
                            customers = uiState.customers,
                            devices = uiState.devices,
                            isLoading = uiState.isLoading,
                            selectedIndustry = uiState.selectedIndustry,
                            selectedOem = uiState.selectedOem,
                            selectedCustomer = uiState.selectedCustomer,
                            selectedDevice = uiState.selectedDevice,
                            onIndustrySelected = onIndustrySelected,
                            onOemSelected = onOemSelected,
                            onCustomerSelected = onCustomerSelected,
                            onDeviceSelected = onDeviceSelected
                        )

                        DashboardActionsSection(
                            groups = uiState.availableGroups,
                            selectedGroup = uiState.selectedGroup,
                            selectedCustomer = uiState.selectedCustomer != null,
                            selectedDevice = uiState.selectedDevice != null,
                            isRefreshing = uiState.isRefreshing,
                            lastUpdatedAt = uiState.lastUpdatedAt,
                            errorMessage = uiState.errorMessage,
                            onGroupSelected = onGroupSelected,
                            onRefresh = onRefresh
                        )

                        uiState.errorMessage?.let { message ->
                            ErrorBanner(message = message)
                        }

                        if (!uiState.isRefreshing && filteredWidgets.isEmpty()) {
                            DashboardEmptyState(
                                hasSelection = uiState.selectedCustomer != null && uiState.selectedDevice != null,
                                onCreateDashboardClick = {
                                    dashboardToastMessage =
                                        "Please use iThing website to create dashboard."
                                }
                            )
                        } else if (filteredWidgets.isNotEmpty()) {
                            DashboardWidgetGrid(
                                widgets = uiState.widgets,
                                selectedGroup = uiState.selectedGroup
                            )
                        }
                    }
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
        } else if (uiState.isRefreshing) {
            FullScreenDashboardLoader(message = "Refreshing dashboard...")
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
    errorMessage: String?,
    onGroupSelected: (String) -> Unit,
    onRefresh: () -> Unit
) {
    IThingCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp),
        elevation = 2
    ) {
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DashboardStatusSummary(
                    lastUpdatedAt = lastUpdatedAt,
                    isRefreshing = isRefreshing,
                    hasSelection = selectedCustomer && selectedDevice,
                    hasError = errorMessage != null,
                )
                DashboardGroupSelector(
                    groups = groups,
                    selectedGroup = selectedGroup,
                    onGroupSelected = onGroupSelected,
                    modifier = Modifier.weight(1f)
                )

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
                    modifier = Modifier
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
