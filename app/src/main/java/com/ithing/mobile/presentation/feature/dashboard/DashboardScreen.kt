package com.ithing.mobile.presentation.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
    val filteredWidgets = uiState.widgets.filter {
        uiState.selectedGroup == "All" || it.dashboardName == uiState.selectedGroup
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FB))
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(36.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5FB)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        Text(
                            text = "Dashboard",
                            style = MaterialTheme.typography.headlineLarge,
                            color = Color(0xFF4A5E7F),
                            fontWeight = FontWeight.Bold
                        )

                        HorizontalDivider(
                            thickness = 2.dp,
                            color = Color(0xFFD7E0EE)
                        )

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

                        if (uiState.isRefreshing) {
                            LoadingBanner(message = "Refreshing dashboard...")
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
    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        DashboardStatusRow(lastUpdatedAt = lastUpdatedAt)

        DashboardGroupSelector(
            groups = groups,
            selectedGroup = selectedGroup,
            onGroupSelected = onGroupSelected,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedButton(
            onClick = onRefresh,
            enabled = selectedCustomer && selectedDevice && !isRefreshing,
            shape = RoundedCornerShape(18.dp),
            border = ButtonDefaults.outlinedButtonBorder.copy(width = 2.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFF223A84)
            ),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null
            )
            Text(
                text = if (isRefreshing) "Refreshing" else "Refresh",
                modifier = Modifier.padding(start = 10.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
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
            ?: "Select filters to load the dashboard.",
        style = MaterialTheme.typography.bodyLarge,
        color = AccentBlue,
        fontWeight = FontWeight.Medium
    )
}

@Composable
private fun DashboardEmptyState(hasSelection: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (hasSelection) {
                "No dashboard settings have been configured.\nPlease set up your dashboard to proceed."
            } else {
                "Choose filters to continue."
            },
            style = MaterialTheme.typography.headlineSmall,
            color = Color(0xFF566983),
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

@Composable
private fun ErrorBanner(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        shape = RoundedCornerShape(18.dp)
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
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterStart
    ) {
        androidx.compose.foundation.layout.Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(modifier = Modifier.padding(end = 10.dp), strokeWidth = 2.dp)
            Text(text = message, color = MutedText)
        }
    }
}
