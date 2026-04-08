package com.ithing.mobile.presentation.feature.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ithing.mobile.domain.model.DashboardWidget
import com.ithing.mobile.presentation.theme.MutedText
import com.ithing.mobile.presentation.theme.White
import kotlin.math.roundToInt

private data class WidgetVisualState(
    val value: Double,
    val valueText: String,
    val unit: String,
    val progress: Float,
    val minLabel: String,
    val maxLabel: String
)

@Composable
fun DashboardWidgetGrid(
    widgets: List<DashboardWidget>,
    selectedGroup: String
) {
    val filteredWidgets = remember(widgets, selectedGroup) {
        widgets.filter { selectedGroup == "All" || it.dashboardName == selectedGroup }
    }
    val rows = remember(filteredWidgets.size) { ((filteredWidgets.size + 1) / 2).coerceAtLeast(1) }
    val gridHeight = rows * 236 + (rows - 1) * 16

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxWidth()
            .height(gridHeight.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        userScrollEnabled = false
    ) {
        items(filteredWidgets, key = { it.id }) { widget ->
            DashboardWidgetCard(widget = widget)
        }
    }
}

@Composable
fun DashboardWidgetCard(widget: DashboardWidget) {
    when {
        widget.type.equals("cards", ignoreCase = true) -> DashboardMetricCard(widget)
        widget.type.equals("gauge", ignoreCase = true) -> DashboardGaugeCard(widget)
        else -> DashboardFallbackCard(widget)
    }
}

@Composable
fun DashboardMetricCard(widget: DashboardWidget) {
    val display = remember(widget) { deriveWidgetVisualState(widget) }
    val subtype = widget.subType.orEmpty()

    when (subtype) {
        "gradient_card" -> {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                shape = RoundedCornerShape(28.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF3F5E90), Color(0xFF1D2E67))
                            )
                        )
                        .padding(20.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = widget.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = White,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = buildValueText(display),
                        style = MaterialTheme.typography.headlineLarge,
                        color = White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        "label_widget" -> {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = widget.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color(0xFF24386C),
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )

                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFFF8FBFF),
                        shadowElevation = 2.dp
                    ) {
                        Text(
                            text = buildValueText(display),
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color(0xFF24386C),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = widget.subType?.replace('_', ' ') ?: "Label widget",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedText
                    )
                }
            }
        }

        else -> {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = widget.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color(0xFF24386C),
                        fontWeight = FontWeight.SemiBold
                    )

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(88.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFF8FAFD),
                        shadowElevation = 2.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = buildValueText(display),
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color(0xFF24386C),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = widget.subType?.replace('_', ' ') ?: "Value card",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedText
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardGaugeCard(widget: DashboardWidget) {
    val display = remember(widget) { deriveWidgetVisualState(widget) }
    val subtype = widget.subType.orEmpty()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = widget.title,
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFF24386C),
                fontWeight = FontWeight.SemiBold
            )

            when (subtype) {
                "radial" -> DashboardRadialGauge(display)
                else -> DashboardSemicircleGauge(display)
            }
        }
    }
}

@Composable
private fun DashboardRadialGauge(display: WidgetVisualState) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(136.dp)) {
            val stroke = 18.dp.toPx()
            drawArc(
                color = Color(0xFFE5E7EB),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            drawArc(
                color = Color(0xFF27489D),
                startAngle = -90f,
                sweepAngle = 360f * display.progress,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = display.valueText,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF24386C)
            )
            if (display.unit.isNotBlank()) {
                Text(
                    text = display.unit,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedText
                )
            }
        }
    }
}

@Composable
private fun DashboardSemicircleGauge(display: WidgetVisualState) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(92.dp)
            ) {
                val stroke = 16.dp.toPx()
                val arcWidth = size.width * 0.74f
                val arcHeight = size.height * 1.5f
                val topLeft = Offset((size.width - arcWidth) / 2f, 8.dp.toPx())
                val arcSize = Size(arcWidth, arcHeight)

                drawArc(
                    color = Color(0xFFE5E7EB),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
                drawArc(
                    color = Color(0xFF27489D),
                    startAngle = 180f,
                    sweepAngle = 180f * display.progress,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
            }

            Text(
                text = buildValueText(display),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF424242)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = display.minLabel,
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF20316A),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = display.maxLabel,
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF20316A),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun DashboardFallbackCard(widget: DashboardWidget) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = widget.title,
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFF24386C),
                fontWeight = FontWeight.SemiBold
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFF2F4F8)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = widget.type.uppercase().take(2),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFF29408F),
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
            shape = RoundedCornerShape(18.dp),
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
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

private fun deriveWidgetVisualState(widget: DashboardWidget): WidgetVisualState {
    val source = widget.sources.firstOrNull()
    val min = source?.minValue ?: 0.0
    val defaultMax = when {
        widget.title.contains("volt", ignoreCase = true) -> 15.0
        widget.title.contains("amp", ignoreCase = true) -> 300.0
        widget.title.contains("incomer", ignoreCase = true) -> 4.0
        else -> 100.0
    }
    val max = source?.maxValue ?: defaultMax
    val unit = widget.unit ?: when {
        widget.title.contains("volt", ignoreCase = true) -> "kV"
        widget.title.contains("amp", ignoreCase = true) -> "A"
        widget.title.contains("incomer", ignoreCase = true) -> "MW"
        else -> ""
    }
    val value = when {
        widget.title.contains("incomer 1", ignoreCase = true) -> 0.0
        widget.title.contains("incomer 2", ignoreCase = true) -> 3.04
        widget.title.contains("incomer 3", ignoreCase = true) -> 0.75
        widget.title.contains("incomer 4", ignoreCase = true) -> 0.68
        widget.title.contains("volt", ignoreCase = true) -> max * 0.72
        widget.title.contains("amp", ignoreCase = true) -> max * 0.51
        else -> ((widget.index ?: 1) * 7.13) % max
    }.coerceIn(min, max)

    val progress = if (max > min) ((value - min) / (max - min)).toFloat() else 0f

    return WidgetVisualState(
        value = value,
        valueText = if (unit == "A" || unit == "kV" || unit == "MW") {
            "%.2f".format(value)
        } else {
            (value * 10).roundToInt().toString()
        },
        unit = unit,
        progress = progress.coerceIn(0f, 1f),
        minLabel = formatScaleLabel(min),
        maxLabel = formatScaleLabel(max)
    )
}

private fun buildValueText(display: WidgetVisualState): String {
    return if (display.unit.isBlank()) display.valueText else "${display.valueText} ${display.unit}"
}

private fun formatScaleLabel(value: Double): String {
    return if (value % 1.0 == 0.0) {
        value.toInt().toString()
    } else {
        "%.2f".format(value)
    }
}
