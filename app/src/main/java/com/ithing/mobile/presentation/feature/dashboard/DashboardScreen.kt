package com.ithing.mobile.presentation.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.ithing.mobile.presentation.components.EmptyState
import com.ithing.mobile.presentation.components.IThingButton
import com.ithing.mobile.presentation.components.IThingCard
import com.ithing.mobile.presentation.components.IThingScreenContainer
import com.ithing.mobile.presentation.components.InfoRow
import com.ithing.mobile.presentation.components.LoadingIndicator
import com.ithing.mobile.presentation.theme.LightGrayBg
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

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
                        onGroupSelected = onGroupSelected,
                        onRefresh = onRefresh
                    )

                    uiState.errorMessage?.let { message ->
                        ErrorBanner(message = message)
                    }

                    if (!uiState.isRefreshing && filteredWidgets.isEmpty()) {
                        DashboardEmptyState(
                            hasSelection = uiState.selectedCustomer != null && uiState.selectedDevice != null
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

        if (showFullScreenLoader) {
            FullScreenDashboardLoader()
        } else if (uiState.isRefreshing) {
            FullScreenDashboardLoader(message = "Refreshing dashboard...")
        }
    }
}

@Composable
private fun DashboardActionsSection(
    groups: List<String>,
    selectedGroup: String,
    selectedCustomer: Boolean,
    selectedDevice: Boolean,
    isRefreshing: Boolean,
    lastUpdatedAt: Long?,
    onGroupSelected: (String) -> Unit,
    onRefresh: () -> Unit
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
            DashboardStatusRow(lastUpdatedAt = lastUpdatedAt)

            DashboardGroupSelector(
                groups = groups,
                selectedGroup = selectedGroup,
                onGroupSelected = onGroupSelected,
                modifier = Modifier.fillMaxWidth()
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
                        modifier = Modifier.size(18.dp)
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun DashboardStatusRow(lastUpdatedAt: Long?) {
    val timestamp = lastUpdatedAt?.let {
        SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(Date(it))
    }

    InfoRow(
        label = "Status",
        value = timestamp?.let { "Last updated: $it" } ?: "Select filters to load",
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
private fun DashboardEmptyState(hasSelection: Boolean) {
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
        onAction = if (hasSelection) { { /* TODO: Navigate to dashboard setup */ } } else null,
        modifier = Modifier.padding(vertical = 16.dp)
    )
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
