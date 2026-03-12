package com.ithing.mobile.presentation.feature.dashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            else -> {
                FilterSection(
                    industries = uiState.industries,
                    oems = uiState.oems,
                    customers = uiState.customers,
                    devices = uiState.devices,
                    selectedIndustry = uiState.selectedIndustry,
                    selectedOem = uiState.selectedOem,
                    selectedCustomer = uiState.selectedCustomer,
                    selectedDevice = uiState.selectedDevice,
                    onIndustrySelected = viewModel::onIndustrySelected,
                    onOemSelected = viewModel::onOemSelected,
                    onCustomerSelected = viewModel::onCustomerSelected,
                    onDeviceSelected = viewModel::onDeviceSelected,
                    onRefresh = viewModel::refreshDashboard
                )

                uiState.errorMessage?.let { msg ->
                    Text(
                        text = msg,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                if (uiState.isRefreshing) {
                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                }

                uiState.widgets.forEach { widget ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Text(
                            text = widget.title,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}