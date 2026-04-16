@file:OptIn(ExperimentalMaterial3Api::class)

package com.ithing.mobile.presentation.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
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
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        if (isLoading) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.material3.CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = NavyBlue
                )
            }
        }

        StyledFilterCard(
            title = "Industry",
            shortLabel = "IN",
            icon = Icons.Outlined.Apartment,
            selectedText = selectedIndustry?.name ?: "Select...",
            options = industries,
            optionLabel = { it.name },
            enabled = industries.isNotEmpty(),
            onSelected = onIndustrySelected
        )
        StyledFilterCard(
            title = "OEM",
            shortLabel = "OE",
            icon = Icons.Outlined.Business,
            selectedText = selectedOem?.name ?: "Select...",
            options = oems,
            optionLabel = { it.name },
            enabled = selectedIndustry != null && oems.isNotEmpty(),
            onSelected = onOemSelected
        )
        StyledFilterCard(
            title = "Customer",
            shortLabel = "CU",
            icon = Icons.Outlined.PersonOutline,
            selectedText = selectedCustomer?.name ?: "Select...",
            options = customers,
            optionLabel = { it.name },
            enabled = selectedOem != null && customers.isNotEmpty(),
            onSelected = onCustomerSelected
        )
        StyledFilterCard(
            title = "Device",
            shortLabel = "DV",
            icon = Icons.Outlined.Memory,
            selectedText = selectedDevice?.name ?: "Select...",
            options = devices,
            optionLabel = { it.name },
            enabled = selectedCustomer != null && devices.isNotEmpty(),
            onSelected = onDeviceSelected
        )
    }
}

@Composable
private fun <T> StyledFilterCard(
    title: String,
    shortLabel: String,
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
        shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
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
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(
                            topStart = 28.dp,
                            topEnd = 28.dp
                        )
                    )
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = White,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    color = Color(0xFF4B63AF),
                    shape = androidx.compose.foundation.shape.CircleShape
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
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
                        bottomStart = 28.dp,
                        bottomEnd = 28.dp
                    ),
                    textStyle = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF1F2937)
                    ),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                        focusedContainerColor = White,
                        unfocusedContainerColor = White,
                        disabledContainerColor = White,
                        focusedBorderColor = Color(0xFF2D469A),
                        unfocusedBorderColor = Color(0xFF7F7D8A),
                        disabledBorderColor = Color(0xFFD8DEE8),
                        focusedTrailingIconColor = Color(0xFF71717A),
                        unfocusedTrailingIconColor = Color(0xFF71717A),
                        disabledTextColor = Color(0xFF9CA3AF),
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
