package com.ithing.mobile.presentation.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.ithing.mobile.domain.model.DashboardWidget
import com.ithing.mobile.domain.model.DashboardWidgetSeries

private data class WidgetSectionPalette(
    val block: Color,
    val accent: Color
)

private val WidgetSectionPalettes = listOf(
    WidgetSectionPalette(block = Color(0xFFE7ABAB), accent = Color(0xFFE6B6BC)),
    WidgetSectionPalette(block = Color(0xFFA9CBC9), accent = Color(0xFFB6D4D2)),
    WidgetSectionPalette(block = Color(0xFFDDB77F), accent = Color(0xFFE1BF8E)),
    WidgetSectionPalette(block = Color(0xFF98BED6), accent = Color(0xFFA7C9DE))
)

private val ChartSeriesColors = listOf(
    Color(0xFF1F3C69),
    Color(0xFF5C97C8),
    Color(0xFF80C759),
    Color(0xFFFF7E2F)
)

@Composable
fun DashboardWidgetGrid(
    widgets: List<DashboardWidget>,
    selectedGroup: String
) {
    val filteredWidgets = remember(widgets, selectedGroup) {
        widgets.filter { selectedGroup == "All" || it.dashboardName == selectedGroup }
    }
    val sections = remember(filteredWidgets) {
        filteredWidgets
            .sortedBy { it.index ?: Int.MAX_VALUE }
            .groupBy { it.dashboardName?.takeIf(String::isNotBlank) ?: it.title }
            .entries
            .sortedBy { entry -> filteredWidgets.indexOfFirst { it.dashboardName == entry.key || it.title == entry.key } }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        sections.forEach { (sectionTitle, sectionWidgets) ->
            DashboardWidgetSection(
                title = sectionTitle,
                widgets = sectionWidgets
            )
        }
    }
}

@Composable
private fun DashboardWidgetSection(
    title: String,
    widgets: List<DashboardWidget>
) {
    val metricWidgets = widgets.filterNot(::isChartWidget)
    val chartWidgets = widgets.filter(::isChartWidget)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text(
                text = title,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.headlineSmall,
                color = Color(0xFF223461),
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            if (metricWidgets.isNotEmpty()) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(metricSectionHeight(metricWidgets.size)),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    userScrollEnabled = false
                ) {
                    items(metricWidgets, key = { it.id }) { widget ->
                        val palette = WidgetSectionPalettes[(widget.index ?: 0).mod(WidgetSectionPalettes.size)]
                        DashboardMetricTile(
                            widget = widget,
                            palette = palette
                        )
                    }
                }
            }

            chartWidgets.forEach { widget ->
                DashboardLineChartCard(widget = widget)
            }
        }
    }
}

@Composable
private fun DashboardMetricTile(
    widget: DashboardWidget,
    palette: WidgetSectionPalette
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(116.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f),
                shape = RoundedCornerShape(12.dp)
            )
            .background(
                color = Color.White,
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = palette.block,
                    shape = RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)
                )
                .size(width = 100.dp, height = 116.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = widgetIcon(widget),
                contentDescription = null,
                modifier = Modifier.size(42.dp),
                tint = Color.White
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = widget.title,
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF556782),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = widgetValueLabel(widget),
                style = MaterialTheme.typography.displaySmall,
                color = Color(0xFF4E5F7A),
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            Text(
                text = widget.unit.orEmpty(),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF5C6E87),
                textAlign = TextAlign.End
            )
        }

        Box(
            modifier = Modifier
                .padding(vertical = 1.dp, horizontal = 1.dp)
                .background(
                    color = palette.accent,
                    shape = RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp)
                )
                .size(width = 10.dp, height = 114.dp)
        )
    }
}

@Composable
private fun DashboardLineChartCard(widget: DashboardWidget) {
    val series = widget.chartSeries.filter { it.points.isNotEmpty() }
    val xLabels = series.firstOrNull()?.points?.map { it.label }.orEmpty().takeLast(8)
    val plottedSeries = series.map { source ->
        source.copy(points = source.points.takeLast(8))
    }
    val allValues = plottedSeries.flatMap { it.points }.map { it.value }
    val minValue = allValues.minOrNull() ?: 0.0
    val maxValue = allValues.maxOrNull() ?: 1.0
    val valueRange = (maxValue - minValue).takeIf { it > 0.0 } ?: 1.0

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = widget.unit?.takeIf { it.isNotBlank() } ?: widget.title,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.headlineSmall,
            color = Color(0xFF223461),
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )

        if (plottedSeries.isEmpty()) {
            Text(
                text = "No live series available yet.",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            return
        }

        LegendGrid(series = plottedSeries)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 12.dp, vertical = 16.dp)
        ) {
            val chartSize = remember { mutableStateOf(IntSize.Zero) }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .onSizeChanged { chartSize.value = it }
            ) {
                val width = size.width
                val height = size.height
                val horizontalSteps = (xLabels.size - 1).coerceAtLeast(1)
                val verticalGridLines = 5

                repeat(verticalGridLines + 1) { step ->
                    val y = height * step / verticalGridLines
                    drawLine(
                        color = Color(0xFFD9DFE8),
                        start = androidx.compose.ui.geometry.Offset(0f, y),
                        end = androidx.compose.ui.geometry.Offset(width, y),
                        strokeWidth = 1f
                    )
                }

                repeat(horizontalSteps + 1) { step ->
                    val x = width * step / horizontalSteps
                    drawLine(
                        color = Color(0xFFD9DFE8),
                        start = androidx.compose.ui.geometry.Offset(x, 0f),
                        end = androidx.compose.ui.geometry.Offset(x, height),
                        strokeWidth = 1f
                    )
                }

                plottedSeries.forEachIndexed { index, dashboardWidgetSeries ->
                    val points = dashboardWidgetSeries.points
                    if (points.isEmpty()) return@forEachIndexed

                    val path = Path()
                    points.forEachIndexed { pointIndex, point ->
                        val x = if (points.size == 1) {
                            width / 2f
                        } else {
                            width * pointIndex / (points.size - 1)
                        }
                        val y = height - (((point.value - minValue) / valueRange).toFloat() * height)
                        if (pointIndex == 0) {
                            path.moveTo(x, y)
                        } else {
                            path.lineTo(x, y)
                        }
                    }

                    val color = ChartSeriesColors[index.mod(ChartSeriesColors.size)]
                    drawPath(
                        path = path,
                        color = color,
                        style = Stroke(width = 5f, cap = StrokeCap.Round)
                    )

                    points.forEachIndexed { pointIndex, point ->
                        val x = if (points.size == 1) width / 2f else width * pointIndex / (points.size - 1)
                        val y = height - (((point.value - minValue) / valueRange).toFloat() * height)
                        drawCircle(
                            color = color,
                            radius = 6f,
                            center = androidx.compose.ui.geometry.Offset(x, y)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 3f,
                            center = androidx.compose.ui.geometry.Offset(x, y)
                        )
                    }
                }
            }

            if (chartSize.value.width > 0 && xLabels.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    xLabels.forEach { label ->
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendGrid(series: List<DashboardWidgetSeries>) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxWidth()
            .height((((series.size + 1) / 2) * 28).dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        userScrollEnabled = false
    ) {
        items(series, key = { it.label }) { item ->
            val color = ChartSeriesColors[series.indexOf(item).mod(ChartSeriesColors.size)]
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 48.dp, height = 16.dp)
                        .border(2.dp, color, RoundedCornerShape(2.dp))
                )
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF66768C),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
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
                .menuAnchor(
                    type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                    enabled = true
                ),
            shape = RoundedCornerShape(20.dp),
            textStyle = MaterialTheme.typography.titleMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.outline,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
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

private fun metricSectionHeight(count: Int) = when {
    count <= 2 -> 116.dp
    else -> (((count + 1) / 2) * 116 + ((count + 1) / 2 - 1) * 14).dp
}

private fun isChartWidget(widget: DashboardWidget): Boolean {
    val type = widget.type.lowercase()
    val subType = widget.subType.orEmpty().lowercase()
    return type == "charts" || "chart" in subType || "line" in subType || "area" in subType || "bar" in subType
}

private fun widgetIcon(widget: DashboardWidget): ImageVector {
    val title = widget.title.lowercase()
    val unit = widget.unit.orEmpty().lowercase()
    return when {
        unit.contains("kv") || title.contains("volt") -> Icons.AutoMirrored.Outlined.ShowChart
        unit.contains("mw") || title.contains("power") -> Icons.Outlined.Bolt
        unit.contains("a") || title.contains("current") -> Icons.Outlined.Speed
        else -> Icons.Outlined.Timeline
    }
}

private fun widgetValueLabel(widget: DashboardWidget): String {
    return widget.currentValueLabel
        ?: widget.currentValue?.let { value ->
            if (value % 1.0 == 0.0) value.toLong().toString() else String.format(java.util.Locale.US, "%.2f", value)
        }
        ?: "--"
}
