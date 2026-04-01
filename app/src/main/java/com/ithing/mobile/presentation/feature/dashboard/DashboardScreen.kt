package com.ithing.mobile.presentation.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ithing.mobile.presentation.theme.AccentBlue
import com.ithing.mobile.presentation.theme.LightGrayBg
import com.ithing.mobile.presentation.theme.MutedText
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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightGrayBg)
    ) {
        if (uiState.isLoading) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F7FC)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Dashboard",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF49586E)
                            )

                            HorizontalDivider(color = Color(0xFFD8E1EC))

                            FilterSection(
                                industries = uiState.industries,
                                oems = uiState.oems,
                                customers = uiState.customers,
                                devices = uiState.devices,
                                selectedIndustry = uiState.selectedIndustry,
                                selectedOem = uiState.selectedOem,
                                selectedCustomer = uiState.selectedCustomer,
                                selectedDevice = uiState.selectedDevice,
                                onIndustrySelected = onIndustrySelected,
                                onOemSelected = onOemSelected,
                                onCustomerSelected = onCustomerSelected,
                                onDeviceSelected = onDeviceSelected
                            )

                            DashboardControlsRow(
                                groups = uiState.availableGroups,
                                selectedGroup = uiState.selectedGroup,
                                selectedCustomer = uiState.selectedCustomer != null,
                                selectedDevice = uiState.selectedDevice != null,
                                isRefreshing = uiState.isRefreshing,
                                onGroupSelected = onGroupSelected,
                                onRefresh = onRefresh
                            )

                            DashboardStatusRow(lastUpdatedAt = uiState.lastUpdatedAt)
                        }
                    }
                }

                uiState.errorMessage?.let { message ->
                    item {
                        ErrorBanner(message = message)
                    }
                }

                if (uiState.isRefreshing) {
                    item {
                        LoadingBanner(message = "Refreshing dashboard...")
                    }
                }

                val filteredWidgets = uiState.widgets.filter {
                    uiState.selectedGroup == "All" || it.dashboardName == uiState.selectedGroup
                }

                if (!uiState.isRefreshing && filteredWidgets.isEmpty()) {
                    item {
                        DashboardEmptyState(
                            hasSelection = uiState.selectedCustomer != null && uiState.selectedDevice != null
                        )
                    }
                } else if (filteredWidgets.isNotEmpty()) {
                    item {
                        DashboardWidgetGrid(
                            widgets = uiState.widgets,
                            selectedGroup = uiState.selectedGroup
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardControlsRow(
    groups: List<String>,
    selectedGroup: String,
    selectedCustomer: Boolean,
    selectedDevice: Boolean,
    isRefreshing: Boolean,
    onGroupSelected: (String) -> Unit,
    onRefresh: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DashboardGroupSelector(
            groups = groups,
            selectedGroup = selectedGroup,
            onGroupSelected = onGroupSelected,
            modifier = Modifier.weight(1f)
        )

        Button(
            onClick = onRefresh,
            enabled = selectedCustomer && selectedDevice && !isRefreshing,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp)
        ) {
            androidx.compose.material3.Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (isRefreshing) "Refreshing" else "Refresh")
        }
    }
}

@Composable
private fun DashboardStatusRow(lastUpdatedAt: Long?) {
    val timestamp = lastUpdatedAt?.let {
        SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(Date(it))
    }

    Text(
        text = timestamp?.let { "Dashboard Last Updated at $it" }
            ?: "Select your filters and refresh to load dashboard widgets.",
        style = MaterialTheme.typography.bodyLarge,
        color = AccentBlue,
        fontWeight = FontWeight.Medium
    )
}

@Composable
private fun DashboardEmptyState(hasSelection: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F7FC)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (hasSelection) {
                    "No dashboard settings have been configured."
                } else {
                    "Choose filters to continue."
                },
                style = MaterialTheme.typography.headlineSmall,
                color = Color(0xFF5A6A7F),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = if (hasSelection) {
                    "Please set up your dashboard to proceed."
                } else {
                    "Select industry, OEM, customer, and device, then refresh the dashboard."
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MutedText,
                textAlign = TextAlign.Center
            )

            if (hasSelection) {
                Text(
                    text = "Click Here to create dashboard",
                    style = MaterialTheme.typography.headlineSmall,
                    color = AccentBlue,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ErrorBanner(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}

@Composable
private fun LoadingBanner(message: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = message, color = MutedText)
    }
}
