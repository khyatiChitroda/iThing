package com.ithing.mobile.presentation.feature.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ReportsRoute(
    viewModel: ReportsViewModel = hiltViewModel(),
    navController: NavHostController
) {
    val uiState by viewModel.uiState.collectAsState()

    ReportsScreen(
        uiState = uiState,
        onDeviceSelected = viewModel::onDeviceSelected,
        onRefresh = viewModel::refresh
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReportsScreen(
    uiState: ReportsUiState,
    onDeviceSelected: (com.ithing.mobile.domain.model.Device?) -> Unit,
    onRefresh: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Reports",
            style = MaterialTheme.typography.headlineSmall
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = uiState.selectedDevice?.name ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Device") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                uiState.devices.forEach { device ->
                    DropdownMenuItem(
                        text = { Text(device.name) },
                        onClick = {
                            expanded = false
                            onDeviceSelected(device)
                        }
                    )
                }
            }
        }

        uiState.deviceOwnerDetails?.let { owner ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Customer: ${owner.customerName}")
                    Text("OEM: ${owner.oemName}")
                    owner.machineName?.takeIf { it.isNotBlank() }?.let {
                        Text("Machine: $it")
                    }
                }
            }
        }

        Button(
            onClick = onRefresh,
            enabled = uiState.selectedDevice != null && !uiState.isRefreshing
        ) {
            Text(if (uiState.isRefreshing) "Refreshing..." else "Refresh")
        }

        when {
            uiState.isLoading -> {
                CircularProgressIndicator()
            }

            uiState.errorMessage != null -> {
                Text(
                    text = uiState.errorMessage,
                    color = MaterialTheme.colorScheme.error
                )
            }

            uiState.selectedDevice == null -> {
                Text("No devices available.")
            }

            uiState.schedules.isEmpty() -> {
                Text("No scheduled reports found for the selected device.")
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.schedules) { schedule ->
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = schedule.subject,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text("Email: ${schedule.email}")
                                Text("Schedule: ${schedule.schedule}")
                                Text("Fields: ${schedule.fields.joinToString()}")
                            }
                        }
                    }
                }
            }
        }
    }
}
