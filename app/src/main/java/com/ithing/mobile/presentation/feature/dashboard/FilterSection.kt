@file:OptIn(ExperimentalMaterial3Api::class)

package com.ithing.mobile.presentation.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ithing.mobile.domain.model.Customer
import com.ithing.mobile.domain.model.Device
import com.ithing.mobile.domain.model.Industry
import com.ithing.mobile.domain.model.Oem
import com.ithing.mobile.presentation.theme.LightGrayBg
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LightGrayBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            FilterCard(
                title = "Industry",
                iconLabel = "IN",
                selectedText = selectedIndustry?.name ?: "Select...",
                options = industries,
                optionLabel = { it.name },
                enabled = industries.isNotEmpty() && !isLoading,
                onSelected = onIndustrySelected
            )
            FilterCard(
                title = "OEM",
                iconLabel = "OE",
                selectedText = selectedOem?.name ?: "Select...",
                options = oems,
                optionLabel = { it.name },
                enabled = selectedIndustry != null && oems.isNotEmpty() && !isLoading,
                onSelected = onOemSelected
            )
            FilterCard(
                title = "Customer",
                iconLabel = "CU",
                selectedText = selectedCustomer?.name ?: "Select...",
                options = customers,
                optionLabel = { it.name },
                enabled = selectedOem != null && customers.isNotEmpty() && !isLoading,
                onSelected = onCustomerSelected
            )
            FilterCard(
                title = "Device",
                iconLabel = "DV",
                selectedText = selectedDevice?.name ?: "Select...",
                options = devices,
                optionLabel = { it.name },
                enabled = selectedCustomer != null && devices.isNotEmpty() && !isLoading,
                onSelected = onDeviceSelected
            )
        }
    }
}

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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LightGrayBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = NavyBlue,
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = White,
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
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(
                            type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                            enabled = enabled
                        ),
                    shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
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
