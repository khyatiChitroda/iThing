@file:OptIn(ExperimentalMaterial3Api::class)

package com.ithing.mobile.presentation.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Color
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
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        StyledFilterCard(
            title = "Industry",
            iconLabel = "IN",
            selectedText = selectedIndustry?.name ?: "Select...",
            options = industries,
            optionLabel = { it.name },
            enabled = industries.isNotEmpty(),
            onSelected = onIndustrySelected
        )
        StyledFilterCard(
            title = "OEM",
            iconLabel = "OE",
            selectedText = selectedOem?.name ?: "Select...",
            options = oems,
            optionLabel = { it.name },
            enabled = selectedIndustry != null && oems.isNotEmpty(),
            onSelected = onOemSelected
        )
        StyledFilterCard(
            title = "Customer",
            iconLabel = "CU",
            selectedText = selectedCustomer?.name ?: "Select...",
            options = customers,
            optionLabel = { it.name },
            enabled = selectedOem != null && customers.isNotEmpty(),
            onSelected = onCustomerSelected
        )
        StyledFilterCard(
            title = "Device",
            iconLabel = "DV",
            selectedText = selectedDevice?.name ?: "Select...",
            options = devices,
            optionLabel = { it.name },
            enabled = selectedCustomer != null && devices.isNotEmpty(),
            onSelected = onDeviceSelected
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> StyledFilterCard(
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
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = NavyBlue,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 18.dp
                        )
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
                    color = White.copy(alpha = 0.15f),
                    shape = androidx.compose.foundation.shape.CircleShape
                ) {
                    Text(
                        text = iconLabel,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = White
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
                        .menuAnchor(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(
                        bottomStart = 18.dp,
                        bottomEnd = 18.dp
                    ),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                        disabledTextColor = Color(0xFF9CA3AF),
                        disabledBorderColor = Color(0xFFD8DEE8),
                        disabledTrailingIconColor = Color(0xFFC4CAD5)
                    )
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
