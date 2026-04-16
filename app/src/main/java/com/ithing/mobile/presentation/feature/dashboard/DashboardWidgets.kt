package com.ithing.mobile.presentation.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ithing.mobile.domain.model.DashboardWidget

@Composable
fun DashboardWidgetGrid(
    widgets: List<DashboardWidget>,
    selectedGroup: String
) {
    val filteredWidgets = remember(widgets, selectedGroup) {
        widgets.filter { selectedGroup == "All" || it.dashboardName == selectedGroup }
    }
    val rows = remember(filteredWidgets.size) { ((filteredWidgets.size + 1) / 2).coerceAtLeast(1) }
    val gridHeight = rows * 254 + (rows - 1) * 18

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .height(gridHeight.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        userScrollEnabled = false
    ) {
        items(filteredWidgets, key = { it.id }) { widget ->
            DashboardWidgetCard(widget = widget)
        }
    }
}

@Composable
fun DashboardWidgetCard(widget: DashboardWidget) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.82f),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = widget.title,
                style = MaterialTheme.typography.headlineSmall,
                color = Color(0xFF2B437E),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )

            Box(
                modifier = Modifier
                    .size(164.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF0F3FA)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = widgetBadge(widget),
                    style = MaterialTheme.typography.displaySmall,
                    color = Color(0xFF2F4AA1),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardGroupSelector(
    groups: List<String>,
    selectedGroup: String,
    onGroupSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedGroup,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            shape = RoundedCornerShape(20.dp),
            textStyle = MaterialTheme.typography.headlineSmall.copy(
                color = Color(0xFF5F6B80)
            ),
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = Color(0xFFD9E2F0),
                unfocusedBorderColor = Color(0xFFD9E2F0)
            )
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            groups.forEach { group ->
                DropdownMenuItem(
                    text = { Text(group) },
                    onClick = {
                        onGroupSelected(group)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun widgetBadge(widget: DashboardWidget): String {
    val subtype = widget.subType.orEmpty().lowercase()
    val title = widget.title.lowercase()
    return when {
        subtype.contains("gauge") || widget.type.equals("gauge", ignoreCase = true) -> "GA"
        subtype.contains("card") || widget.type.equals("cards", ignoreCase = true) -> "CA"
        title.contains("volt") -> "GA"
        title.contains("incomer") -> "CA"
        else -> widget.title.split(" ")
            .mapNotNull { it.firstOrNull()?.uppercaseChar()?.toString() }
            .take(2)
            .joinToString("")
            .ifBlank { "WI" }
    }
}
