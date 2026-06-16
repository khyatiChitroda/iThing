@file:OptIn(ExperimentalMaterial3Api::class)

package com.ithing.mobile.presentation.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ithing.mobile.domain.model.Customer
import com.ithing.mobile.domain.model.Device
import com.ithing.mobile.domain.model.Industry
import com.ithing.mobile.domain.model.Oem
import com.ithing.mobile.presentation.theme.NavyBlue
import com.ithing.mobile.presentation.theme.White

@Composable
fun FilterSection(
    industries: List<Industry>,
    oems: List<Oem>,
    customers: List<Customer>,
    devices: List<Device>,
    isLoading: Boolean,
    selectedIndustry: Industry?,
    selectedOem: Oem?,
    selectedCustomer: Customer?,
    selectedDevice: Device?,
    onIndustrySelected: (Industry?) -> Unit,
    onOemSelected: (Oem?) -> Unit,
    onCustomerSelected: (Customer?) -> Unit,
    onDeviceSelected: (Device?) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        FilterCard(
            title = "Industry",
            icon = Icons.Filled.Dashboard,
            selectedText = selectedIndustry?.name ?: "Select...",
            options = industries,
            optionLabel = { it.name },
            enabled = industries.isNotEmpty() && !isLoading,
            onSelected = onIndustrySelected
        )
        FilterCard(
            title = "OEM",
            icon = Icons.Filled.Dashboard,
            selectedText = selectedOem?.name ?: "Select...",
            options = oems,
            optionLabel = { it.name },
            enabled = selectedIndustry != null && oems.isNotEmpty() && !isLoading,
            onSelected = onOemSelected
        )
        FilterCard(
            title = "Customer",
            icon = Icons.Outlined.Person,
            selectedText = selectedCustomer?.name ?: "Select...",
            options = customers,
            optionLabel = { it.name },
            enabled = selectedOem != null && customers.isNotEmpty() && !isLoading,
            onSelected = onCustomerSelected
        )
        FilterCard(
            title = "Device",
            icon = Icons.Filled.Dashboard,
            selectedText = selectedDevice?.name ?: "Select...",
            options = devices,
            optionLabel = { it.name },
            enabled = selectedCustomer != null && devices.isNotEmpty() && !isLoading,
            onSelected = onDeviceSelected
        )
    }
}

@Composable
private fun <T> FilterCard(
    title: String,
    icon: ImageVector,
    selectedText: String,
    options: List<T>,
    optionLabel: (T) -> String,
    enabled: Boolean,
    onSelected: (T?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = NavyBlue,
                        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = White,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )

                androidx.compose.material3.Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = White
                )
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
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(
                            type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                            enabled = enabled
                        ),
                    shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("All") },
                        onClick = {
                            onSelected(null)
                            expanded = false
                        }
                    )
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
