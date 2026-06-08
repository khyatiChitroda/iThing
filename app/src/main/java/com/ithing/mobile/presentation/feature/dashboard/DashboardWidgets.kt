package com.ithing.mobile.presentation.feature.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Clock3
import com.composables.icons.lucide.Settings
import com.composables.icons.lucide.Thermometer
import com.ithing.mobile.domain.model.DashboardWidget
import com.ithing.mobile.domain.model.DashboardWidgetColorValues
import com.ithing.mobile.domain.model.DashboardWidgetSeries
import com.ithing.mobile.presentation.theme.LightGrayBg
import kotlin.math.roundToInt
import kotlin.math.cos
import kotlin.math.sin

private data class WidgetRendererKey(
    val type: String,
    val subType: String
)

private enum class WidgetRenderKind {
    Metric,
    Chart
}

private sealed interface DashboardRenderBlock {
    data class Metric(val widgets: List<DashboardWidget>) : DashboardRenderBlock
    data class Chart(val widget: DashboardWidget) : DashboardRenderBlock
}

private fun rendererKey(widget: DashboardWidget) = WidgetRendererKey(
    type = widget.type.orEmpty().trim().lowercase(),
    subType = widget.subType.orEmpty().trim().lowercase()
)

private fun resolveRenderKind(key: WidgetRendererKey): WidgetRenderKind {
    return if (key.type == "charts") WidgetRenderKind.Chart else WidgetRenderKind.Metric
}

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

private val ComparisonPalette = listOf(
    Color(0xFFE7B0B0),
    Color(0xFFA8C8C4),
    Color(0xFFD5B183),
    Color(0xFF8BB3C6),
    Color(0xFFE7C45A),
    Color(0xFFD48E7A)
)

private data class CombinationBarItem(
    val label: String,
    val value: Double,
    val iconName: String?,
    val unit: String,
    val min: Double,
    val max: Double
) {
    val percent: Double
        get() {
            val range = (max - min).takeIf { it != 0.0 } ?: 1.0
            return ((value - min) / range).coerceIn(0.0, 1.0)
        }
}

private val CombinationBarSegmentColors = listOf(
    Color(0xFF22C55E),
    Color(0xFF65D46E),
    Color(0xFFFACC15),
    Color(0xFFF59E0B),
    Color(0xFFEF4444)
)

private fun DashboardWidget.combinationBarItems(limit: Int = 12): List<CombinationBarItem> {
    return sources.flatMap { source ->
        source.fields.mapIndexed { idx, label ->
            CombinationBarItem(
                label = label,
                value = valuesByField[label] ?: 0.0,
                iconName = source.icons.getOrNull(idx),
                unit = source.units.getOrNull(idx)?.takeIf { it.isNotBlank() } ?: unit.orEmpty(),
                min = source.minValues.getOrNull(idx) ?: source.minValue ?: 0.0,
                max = source.maxValues.getOrNull(idx) ?: source.maxValue ?: 100.0
            )
        }
    }.take(limit)
}

@Composable
fun DashboardWidgetGrid(
    widgets: List<DashboardWidget>,
    selectedGroup: String
) {
    val filteredWidgets = remember(widgets, selectedGroup) {
        widgets
            .filter { selectedGroup == "All" || it.dashboardName == selectedGroup }
            .sortedBy { it.index ?: Int.MAX_VALUE }
    }

    val blocks = remember(filteredWidgets) {
        val out = mutableListOf<DashboardRenderBlock>()
        val metricBuffer = mutableListOf<DashboardWidget>()

        fun flushMetrics() {
            if (metricBuffer.isNotEmpty()) {
                out += DashboardRenderBlock.Metric(metricBuffer.toList())
                metricBuffer.clear()
            }
        }

        filteredWidgets.forEach { widget ->
            if (resolveRenderKind(rendererKey(widget)) == WidgetRenderKind.Chart) {
                flushMetrics()
                out += DashboardRenderBlock.Chart(widget)
            } else {
                metricBuffer += widget
            }
        }
        flushMetrics()
        out
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        blocks.forEach { block ->
            when (block) {
                is DashboardRenderBlock.Metric -> DashboardMetricGrid(widgets = block.widgets)
                is DashboardRenderBlock.Chart -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = LightGrayBg),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(18.dp)
                        ) {
                            DashboardLineChartCard(widget = block.widget)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardMetricGrid(
    widgets: List<DashboardWidget>
) {
    val rows = remember(widgets) {
        fun span(widget: DashboardWidget): Int {
            val type = widget.type.trim().lowercase()
            return if (type == "comparison" || type == "gauge") 2 else 1
        }

        val out = mutableListOf<List<DashboardWidget>>()
        val currentRow = mutableListOf<DashboardWidget>()

        fun flushCurrentRow() {
            if (currentRow.isNotEmpty()) {
                out += currentRow.toList()
                currentRow.clear()
            }
        }

        widgets.forEach { widget ->
            if (span(widget) == 2) {
                flushCurrentRow()
                out += listOf(widget)
            } else {
                currentRow += widget
                if (currentRow.size == 2) flushCurrentRow()
            }
        }
        flushCurrentRow()
        out
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        rows.forEach { rowWidgets ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (rowWidgets.size == 1) {
                    DashboardMetricTile(widget = rowWidgets.first())
                } else {
                    rowWidgets.forEach { widget ->
                        Box(modifier = Modifier.weight(1f)) {
                            DashboardMetricTile(widget = widget)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardMetricTile(
    widget: DashboardWidget
) {
    val key = rendererKey(widget)
    val palette = WidgetSectionPalettes[(widget.index ?: 0).mod(WidgetSectionPalettes.size)]

    when (key.type) {
        "cards" -> DashboardCardTile(widget = widget)
        "comparison" -> {
            when (key.subType) {
                "comparison" -> DashboardComparisonCard(widget = widget)
                "with_4_cards" -> DashboardWith4Cards(widget = widget)
                "table_format" -> DashboardTableFormat(widget = widget)
                "combination_horizontal_bar" -> DashboardCombinationHorizontalBar(widget = widget)
                "combination_vertical_bar" -> DashboardCombinationVerticalBar(widget = widget)
                "dial_status" -> DashboardDialStatus(widget = widget)
                else -> DashboardGenericMetricTile(widget = widget, palette = palette)
            }
        }

        "gauge" -> DashboardGaugeTile(widget = widget)

        else -> DashboardGenericMetricTile(widget = widget, palette = palette)
    }
}

@Composable
private fun DashboardGaugeTile(widget: DashboardWidget) {
    val source = widget.sources.firstOrNull()
    val field = source?.fields?.firstOrNull()
    val value = field?.let { widget.valuesByField[it] } ?: widget.currentValue ?: 0.0
    val minValue = source?.minValue ?: source?.minValues?.firstOrNull() ?: 0.0
    val maxValue = source?.maxValue ?: source?.maxValues?.firstOrNull() ?: (minValue + 1.0)
    val denom = (maxValue - minValue).takeIf { it != 0.0 } ?: 1.0
    val percent = ((value - minValue) / denom).coerceIn(0.0, 1.0).toFloat()

    val subType = widget.subType.orEmpty().trim().lowercase()
    val (colors, arcsLength) = when (subType) {
        "spedo_meter_with_arch_color" -> listOf(
            Color(0xFF1BAAE3),
            Color(0xFFFBE042),
            Color(0xFFE7512C)
        ) to listOf(0.3f, 0.5f, 0.2f)

        "spedo_meter_with_arch" -> listOf(
            Color(0xFF72AAC5),
            Color(0xFF1E3A8A),
            Color(0xFF172554)
        ) to listOf(0.3f, 0.5f, 0.2f)

        "speedometer" -> listOf(
            Color(0xFF72AAC5),
            Color(0xFF1E3A8A)
        ) to listOf(0.5f, 0.5f)

        else -> listOf(
            Color(0xFF72AAC5),
            Color(0xFF1E3A8A)
        ) to listOf(0.5f, 0.5f)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = widget.title,
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF223461),
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            SegmentedSpeedometerGauge(
                percent = percent,
                colors = colors,
                arcsLength = arcsLength,
                valueText = "${value.formatForCard()} ${widget.unit.orEmpty()}".trim(),
                arcPaddingFraction = 0.02f
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = minValue.formatForCard(decimals = 0),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF223461),
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = maxValue.formatForCard(decimals = 0),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF223461),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun SegmentedSpeedometerGauge(
    percent: Float,
    colors: List<Color>,
    arcsLength: List<Float>,
    valueText: String,
    arcPaddingFraction: Float
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 22f
            val radius = (size.minDimension / 2f) - stroke
            val center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height * 0.92f)
            val startAngle = 180f
            val sweepTotal = 180f
            val gapSweep = (sweepTotal * arcPaddingFraction).coerceAtLeast(0f)

            var angleCursor = startAngle
            val segmentFractions = arcsLength
                .take(colors.size)
                .let { list ->
                    val sum = list.sum().takeIf { it > 0f } ?: 1f
                    list.map { it / sum }
                }

            segmentFractions.forEachIndexed { idx, frac ->
                val color = colors.getOrNull(idx) ?: Color(0xFF1E3A8A)
                val segSweepRaw = sweepTotal * frac
                val segSweep = (segSweepRaw - gapSweep).coerceAtLeast(2f)

                drawArc(
                    color = color,
                    startAngle = angleCursor,
                    sweepAngle = segSweep,
                    useCenter = false,
                    topLeft = androidx.compose.ui.geometry.Offset(
                        center.x - radius,
                        center.y - radius
                    ),
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )

                angleCursor += segSweepRaw
            }

            val needleAngle = Math.toRadians(
                (startAngle + (percent.coerceIn(0f, 1f) * sweepTotal)).toDouble()
            )
            val needleLen = radius - 12f
            val needleX = center.x + (cos(needleAngle) * needleLen).toFloat()
            val needleY = center.y + (sin(needleAngle) * needleLen).toFloat()
            drawLine(
                color = Color(0xFF1E3A8A),
                start = center,
                end = androidx.compose.ui.geometry.Offset(needleX, needleY),
                strokeWidth = 7f,
                cap = StrokeCap.Round
            )
            drawCircle(color = Color(0xFF16A34A), radius = 10f, center = center)
        }

        Text(
            text = valueText,
            style = MaterialTheme.typography.titleLarge,
            color = Color(0xFF223461),
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun DashboardComparisonCard(widget: DashboardWidget) {
    val source = widget.sources.firstOrNull()
    val fields = source?.fields.orEmpty()
    val icons = source?.icons.orEmpty()
    val units = source?.units.orEmpty()
    val labelsToRender = remember(fields) { fields.take(6) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LightGrayBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = widget.title,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF223461),
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            val rows = remember(labelsToRender) { labelsToRender.chunked(2) }
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                rows.forEach { rowLabels ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowLabels.forEach { label ->
                            val idx = labelsToRender.indexOf(label)
                            Box(modifier = Modifier.weight(1f)) {
                                ComparisonChildTile(
                                    label = label,
                                    value = widget.valuesByField[label] ?: 0.0,
                                    unit = units.getOrNull(idx)?.takeIf { it.isNotBlank() }
                                        ?: widget.unit.orEmpty(),
                                    iconName = icons.getOrNull(idx),
                                    accent = ComparisonPalette[idx.mod(ComparisonPalette.size)]
                                )
                            }
                        }
                        if (rowLabels.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ComparisonChildTile(
    label: String,
    value: Double,
    unit: String,
    iconName: String?,
    accent: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 108.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
                shape = RoundedCornerShape(12.dp)
            )
            .background(Color.White, RoundedCornerShape(12.dp))
    ) {
        Box(
            modifier = Modifier
                .background(accent, RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                .size(width = 66.dp, height = 108.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = comparisonIcon(iconName),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF556782),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value.formatForCard(),
                style = MaterialTheme.typography.headlineSmall,
                color = Color(0xFF0F172A),
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = unit,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF66768C),
                textAlign = TextAlign.End,
                maxLines = 1
            )
        }

        Box(
            modifier = Modifier
                .background(accent, RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp))
                .size(width = 6.dp, height = 108.dp)
        )
    }
}

@Composable
private fun DashboardWith4Cards(widget: DashboardWidget) {
    val source = widget.sources.firstOrNull()
    val fields = source?.fields.orEmpty()
    val icons = source?.icons.orEmpty()
    val units = source?.units.orEmpty()
    val labelsToRender = remember(fields) { fields.take(6) }
    val palette = listOf(
        Color(0xFF98BAD5),
        Color(0xFFC6D3E3),
        Color(0xFF304674),
        Color(0xFFB2CBDE)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LightGrayBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = widget.title,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF223461),
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                labelsToRender.forEachIndexed { idx, label ->
                    With4RowTile(
                        label = label,
                        value = widget.valuesByField[label] ?: 0.0,
                        unit = units.getOrNull(idx)?.takeIf { it.isNotBlank() }
                            ?: widget.unit.orEmpty(),
                        iconName = icons.getOrNull(idx),
                        accent = palette[idx.mod(palette.size)]
                    )
                }
            }
        }
    }
}

@Composable
private fun With4RowTile(
    label: String,
    value: Double,
    unit: String,
    iconName: String?,
    accent: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 84.dp)
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
                RoundedCornerShape(12.dp)
            )
            .background(Color.White, RoundedCornerShape(12.dp))
    ) {
        Box(
            modifier = Modifier
                .background(accent, RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                .size(width = 76.dp, height = 84.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = comparisonIcon(iconName),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(30.dp)
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF556782),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${value.formatForCard()} ${unit}".trim(),
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFF0F172A),
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Box(
            modifier = Modifier
                .background(accent, RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp))
                .size(width = 6.dp, height = 84.dp)
        )
    }
}

@Composable
private fun DashboardTableFormat(widget: DashboardWidget) {
    val source = widget.sources.firstOrNull()
    val fields = source?.fields.orEmpty()
    val icons = source?.icons.orEmpty()
    val units = source?.units.orEmpty()
    val labelsToRender = remember(fields) { fields.take(6) }
    val palette = listOf(
        Color(0xFFD1E4FF),
        Color(0xFFFFE0C2),
        Color(0xFFE5D9FF),
        Color(0xFFC8F7E2),
        Color(0xFFFFD6E8),
        Color(0xFFFEF9C3)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LightGrayBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = widget.title,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF223461),
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Column {
                labelsToRender.forEachIndexed { idx, label ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        palette[idx.mod(palette.size)],
                                        RoundedCornerShape(20.dp)
                                    )
                                    .border(2.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = comparisonIcon(icons.getOrNull(idx)),
                                    contentDescription = null,
                                    tint = Color(0xFF334155),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(
                                text = label,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 10.dp, end = 10.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF0F172A),
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        val unit = units.getOrNull(idx)?.takeIf { it.isNotBlank() }
                            ?: widget.unit.orEmpty()
                        Text(
                            text = "${(widget.valuesByField[label] ?: 0.0).formatForCard()} ${unit}".trim(),
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF0F172A),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardCombinationHorizontalBar(widget: DashboardWidget) {
    val items = widget.combinationBarItems()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LightGrayBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = widget.title,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF223461),
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items.chunked(2).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowItems.forEach { item ->
                            Box(modifier = Modifier.weight(1f)) {
                                CombinationHorizontalBarTile(
                                    item = item,
                                    segmentColors = CombinationBarSegmentColors
                                )
                            }
                        }
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CombinationHorizontalBarTile(
    item: CombinationBarItem,
    segmentColors: List<Color>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = dashboardLucideIcon(item.iconName, fallback = Lucide.Clock3),
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = item.label,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .weight(1f),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF0F172A),
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = "${item.value.formatForCard()} ${item.unit}".trim(),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF0F172A),
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            ) {
                HorizontalSegmentedBar(
                    percent = item.percent.toFloat(),
                    segmentColors = segmentColors,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .height(64.dp)
                )

                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Min: ${item.min.formatForCard(decimals = 0)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = "Max: ${item.max.formatForCard(decimals = 0)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF64748B)
                    )
                }
            }
        }
    }
}

@Composable
private fun HorizontalSegmentedBar(
    percent: Float,
    segmentColors: List<Color>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val pointerHalfWidth = 10.dp.toPx()
        val pointerHeight = 12.dp.toPx()
        val borderWidth = 8.dp.toPx()
        val barTop = 16.dp.toPx()
        val barHeight = 48.dp.toPx()
        val radius = barHeight / 2f

        val pointerX = (size.width * percent.coerceIn(0f, 1f))
            .coerceIn(pointerHalfWidth, size.width - pointerHalfWidth)

        val pointerPath = Path().apply {
            moveTo(pointerX, 0f)
            lineTo(pointerX - pointerHalfWidth, pointerHeight)
            lineTo(pointerX + pointerHalfWidth, pointerHeight)
            close()
        }
        drawPath(path = pointerPath, color = Color(0xFF4B5563))

        drawRoundRect(
            color = Color(0xFFD1D5DB),
            topLeft = androidx.compose.ui.geometry.Offset(0f, barTop),
            size = androidx.compose.ui.geometry.Size(size.width, barHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius)
        )
        drawRoundRect(
            color = Color(0xFFE2E8F0),
            topLeft = androidx.compose.ui.geometry.Offset(borderWidth, barTop + borderWidth),
            size = androidx.compose.ui.geometry.Size(
                width = (size.width - borderWidth * 2).coerceAtLeast(0f),
                height = (barHeight - borderWidth * 2).coerceAtLeast(0f)
            ),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius)
        )

        val innerLeft = borderWidth
        val innerTop = barTop + borderWidth
        val innerWidth = (size.width - borderWidth * 2).coerceAtLeast(0f)
        val innerHeight = (barHeight - borderWidth * 2).coerceAtLeast(0f)
        if (innerWidth > 0f && innerHeight > 0f && segmentColors.isNotEmpty()) {
            val clip = Path().apply {
                addRoundRect(
                    androidx.compose.ui.geometry.RoundRect(
                        left = innerLeft,
                        top = innerTop,
                        right = innerLeft + innerWidth,
                        bottom = innerTop + innerHeight,
                        radiusX = innerHeight / 2f,
                        radiusY = innerHeight / 2f
                    )
                )
            }
            clipPath(clip) {
                val segmentWidth = innerWidth / segmentColors.size
                segmentColors.forEachIndexed { idx, color ->
                    drawRect(
                        color = color,
                        topLeft = androidx.compose.ui.geometry.Offset(
                            x = innerLeft + segmentWidth * idx,
                            y = innerTop
                        ),
                        size = androidx.compose.ui.geometry.Size(
                            width = segmentWidth + 0.5f,
                            height = innerHeight
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardCombinationVerticalBar(widget: DashboardWidget) {
    val items = widget.combinationBarItems()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LightGrayBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = widget.title,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF223461),
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items.forEach { item ->
                    CombinationVerticalBarTile(
                        item = item,
                        segmentColors = CombinationBarSegmentColors,
                        modifier = Modifier.width(126.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CombinationVerticalBarTile(
    item: CombinationBarItem,
    segmentColors: List<Color>,
    modifier: Modifier = Modifier
) {
    val barHeight = 195.dp
    val reversedPercent = 1.0 - item.percent

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = item.label,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF0F172A),
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(barHeight)
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .width(48.dp)
                        .height(barHeight)
                        .clip(RoundedCornerShape(999.dp))
                        .border(8.dp, Color(0xFFCBD5E1), RoundedCornerShape(999.dp))
                        .background(Color(0xFFE2E8F0), RoundedCornerShape(999.dp))
                ) {
                    segmentColors.asReversed().forEach { c ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .background(c)
                        )
                    }
                }

                Canvas(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .width(72.dp)
                        .height(barHeight)
                ) {
                    val pointerHalfHeight = 12.dp.toPx()
                    val y = (barHeight.toPx() * reversedPercent.toFloat())
                        .coerceIn(pointerHalfHeight, barHeight.toPx() - pointerHalfHeight)
                    val x = 54f
                    val path = Path().apply {
                        moveTo(x, y)
                        lineTo(x + 16f, y - 12f)
                        lineTo(x + 16f, y + 12f)
                        close()
                    }
                    drawPath(path = path, color = Color(0xFF4B5563))
                }

                Text(
                    text = item.max.formatForCard(decimals = 0),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 76.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFEF4444),
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = item.min.formatForCard(decimals = 0),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 76.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF16A34A),
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = "${item.value.formatForCard()} ${item.unit}".trim(),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF0F172A),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun DashboardDialStatus(widget: DashboardWidget) {
    val source = widget.sources.firstOrNull()
    val fields = source?.fields.orEmpty()
    val dialField = fields.getOrNull(0)
    val valueField = fields.getOrNull(1)
    val statusField = fields.getOrNull(2)

    val dialValue = dialField?.let { widget.valuesByField[it] } ?: 0.0
    val value = valueField?.let { widget.valuesByField[it] } ?: 0.0

    val min = source?.minValues?.firstOrNull() ?: source?.minValue ?: 0.0
    val max = source?.maxValues?.firstOrNull() ?: source?.maxValue ?: 100.0
    val percent = ((dialValue - min) / ((max - min).takeIf { it != 0.0 } ?: 1.0)).coerceIn(0.0, 1.0)

    val statusColor = run {
        val numericStatus = statusField?.let { widget.valuesByField[it] }
        val colors = source?.colorValues
        when {
            numericStatus != null && colors?.green != null && numericStatus == colors.green -> Color(
                0xFF22C55E
            )

            numericStatus != null && colors?.red != null && numericStatus == colors.red -> Color(
                0xFFEF4444
            )

            numericStatus != null && colors?.yellow != null && numericStatus == colors.yellow -> Color(
                0xFFEAB308
            )

            else -> Color(0xFFFBBF24)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = widget.title,
                style = MaterialTheme.typography.titleSmall,
                color = Color(0xFFE2E8F0),
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )

            DialGauge(
                percent = percent.toFloat(),
                valueText = dialValue.formatOneDecimal(),
                label = dialField.orEmpty()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(86.dp)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF0EA5E9), Color(0xFF22C55E))
                                ),
                                RoundedCornerShape(14.dp)
                            )
                            .padding(12.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = valueField.orEmpty(),
                            style = MaterialTheme.typography.titleSmall,
                            color = Color(0xFFE2E8F0),
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = value.formatOneDecimal(),
                            style = MaterialTheme.typography.titleLarge,
                            color = Color(0xFFFACC15),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(86.dp)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFFC9BEAB), Color(0xFFCFB4B4))
                                ),
                                RoundedCornerShape(14.dp)
                            )
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Status",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .background(statusColor, RoundedCornerShape(26.dp))
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DialGauge(
    percent: Float,
    valueText: String,
    label: String
) {
    val colors = listOf(
        Color(0xFF22C55E),
        Color(0xFF65D46E),
        Color(0xFFFACC15),
        Color(0xFFF59E0B),
        Color(0xFFEF4444)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(188.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            val levelCount = 20
            val stroke = size.minDimension * 0.09f
            val radius = (size.minDimension / 2f) - stroke
            val center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height * 0.84f)
            val startAngle = 180f
            val sweepTotal = 180f
            val segmentSweep = sweepTotal / levelCount
            val arcGap = sweepTotal * 0.01f

            repeat(levelCount) { idx ->
                val paletteIndex = ((idx.toFloat() / levelCount) * colors.size)
                    .toInt()
                    .coerceIn(0, colors.lastIndex)
                drawArc(
                    color = colors[paletteIndex],
                    startAngle = startAngle + idx * segmentSweep,
                    sweepAngle = (segmentSweep - arcGap).coerceAtLeast(1f),
                    useCenter = false,
                    topLeft = androidx.compose.ui.geometry.Offset(
                        center.x - radius,
                        center.y - radius
                    ),
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
            }

            val angle =
                Math.toRadians((startAngle + (percent.coerceIn(0f, 1f) * sweepTotal)).toDouble())
            val needleLen = radius - 12f
            val needleX = center.x + (cos(angle) * needleLen).toFloat()
            val needleY = center.y + (sin(angle) * needleLen).toFloat()
            drawLine(
                color = Color.White,
                start = center,
                end = androidx.compose.ui.geometry.Offset(needleX, needleY),
                strokeWidth = stroke * 0.32f,
                cap = StrokeCap.Round
            )
            drawCircle(color = Color.White, radius = stroke * 0.42f, center = center)
        }

        Text(
            text = valueText,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = 35.dp),
            style = MaterialTheme.typography.displaySmall,
            color = Color(0xFFE2E8F0),
            fontWeight = FontWeight.Normal
        )
        Text(
            text = label,
            modifier = Modifier
                .align(Alignment.BottomCenter),
            style = MaterialTheme.typography.titleSmall,
            color = Color(0xFFE2E8F0),
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun comparisonIcon(iconName: String?): ImageVector {
    return dashboardLucideIcon(iconName, fallback = Lucide.Settings)
}

@Composable
private fun DashboardGenericMetricTile(
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
private fun DashboardCardTile(widget: DashboardWidget) {
    when (val subType = widget.subType.orEmpty().trim().lowercase()) {
        "value_card" -> DashboardValueCard(widget = widget)
        "gradient_card" -> DashboardGradientCard(widget = widget)
        "label_widget" -> DashboardLabelCard(widget = widget)
        "sensor_value_with_icon" -> DashboardSensorValueWithIconCard(widget = widget)
        "status_card" -> DashboardStatusCard(widget = widget)
        "big_icon_stat_card" -> DashboardBigIconStatCard(widget = widget)
        "value_status_card" -> DashboardValueStatusCard(widget = widget)
        else -> DashboardFallbackCard(
            widget = widget,
            subtitle = "Unsupported card: ${widget.subType}"
        )
    }
}

@Composable
private fun DashboardValueCard(widget: DashboardWidget) {
    val bg = widget.sources.firstOrNull()?.bgColor?.toComposeColorOrNull() ?: Color(0xFFFFFFFF)
    DashboardSimpleValueCard(
        title = widget.title,
        value = widget.currentValue?.let { it.formatForCard() } ?: "--",
        unit = widget.unit.orEmpty(),
        background = bg,
        titleColor = Color(0xFF223461),
        valueColor = Color(0xFF223461)
    )
}

@Composable
private fun DashboardLabelCard(widget: DashboardWidget) {
    val bg = widget.sources.firstOrNull()?.bgColor?.toComposeColorOrNull() ?: Color(0xFFFFFFFF)
    DashboardSimpleValueCard(
        title = widget.title,
        value = widget.currentValue?.let { it.formatForCard() } ?: "--",
        unit = widget.unit.orEmpty(),
        background = bg,
        titleColor = Color(0xFF223461),
        valueColor = Color(0xFF223461)
    )
}

@Composable
private fun DashboardGradientCard(widget: DashboardWidget) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 156.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E3A8A)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = widget.title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = widget.currentValue?.let { it.formatForCard() } ?: "--",
                    style = MaterialTheme.typography.displaySmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = widget.unit.orEmpty(),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun DashboardSensorValueWithIconCard(widget: DashboardWidget) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 180.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Color(0xFFE11D48), RoundedCornerShape(32.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = widgetIconFromName(widget.icon) ?: widgetIcon(widget),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(34.dp)
                )
            }
            Text(
                text = widget.title,
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF223461),
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = widget.currentValue?.let { it.formatForCard() } ?: "--",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFF0F172A),
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = widget.unit.orEmpty(),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF0F172A)
                )
            }
        }
    }
}

@Composable
private fun DashboardStatusCard(widget: DashboardWidget) {
    val value = widget.currentValue ?: 0.0
    val source = widget.sources.firstOrNull()
    val minValue = source?.minValue ?: 0.0
    val maxValue = source?.maxValue ?: (minValue + 1.0)
    val pct = ((value - minValue) / (maxValue - minValue) * 100.0)
        .coerceIn(0.0, 100.0)
    val displayedPercent = pct.roundToInt()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 156.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "$displayedPercent%",
                    style = MaterialTheme.typography.displaySmall,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
                Text(
                    text = widget.title + widget.unit?.takeIf { it.isNotBlank() }?.let { " ($it)" }
                        .orEmpty(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF9CA3AF),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "($value)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF9CA3AF)
                )
            }

            ThermometerGauge(percentage = displayedPercent)
        }
    }
}

@Composable
private fun ThermometerGauge(percentage: Int) {
    val clamped = percentage.coerceIn(0, 100)
    val filledSegments = ((clamped / 100f) * 8f).roundToInt().coerceIn(0, 8)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .width(22.dp)
                .height(10.dp)
                .background(
                    if (filledSegments >= 8) Color(0xFFE8380D) else Color(0xFF2A2A2A),
                    RoundedCornerShape(topStart = 11.dp, topEnd = 11.dp)
                )
                .border(
                    2.dp,
                    Color(0xFF444444),
                    RoundedCornerShape(topStart = 11.dp, topEnd = 11.dp)
                )
        )
        (0 until 8).forEach { idx ->
            val segmentIndex = 7 - idx
            val isFilled = segmentIndex < filledSegments
            Box(
                modifier = Modifier
                    .width(22.dp)
                    .height(13.dp)
                    .background(
                        if (isFilled) Color(0xFFE8380D) else Color(0xFF2A2A2A),
                        RoundedCornerShape(3.dp)
                    )
                    .border(2.dp, Color(0xFF444444), RoundedCornerShape(3.dp))
            )
        }
        Box(
            modifier = Modifier
                .size(30.dp)
                .background(Color(0xFFE8380D), RoundedCornerShape(15.dp))
                .border(2.dp, Color(0xFF444444), RoundedCornerShape(15.dp))
        )
    }
}

@Composable
private fun DashboardBigIconStatCard(widget: DashboardWidget) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 156.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = widget.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF223461),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(Color(0xFFF1F5F9), RoundedCornerShape(48.dp))
                    .border(2.dp, Color(0xFFCBD5E1), RoundedCornerShape(48.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = widget.currentValue?.let { it.formatForCard() } ?: "--",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = widget.unit.orEmpty(),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF0F172A)
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardValueStatusCard(widget: DashboardWidget) {
    val source = widget.sources.firstOrNull()
    val mode = source?.valueInputMode ?: "value"
    val status = computeValueStatus(
        value = widget.currentValue,
        mode = mode,
        bitSelection = source?.bitSelection,
        colorValues = source?.colorValues
    )
    val statusColor = when (status) {
        "green" -> Color(0xFF009104)
        "red" -> Color(0xFFEF4444)
        "yellow" -> Color(0xFFEAB308)
        else -> Color(0xFF9CA3AF)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 156.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = widget.title,
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF223461),
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Box(
                modifier = Modifier
                    .size(86.dp)
                    .background(statusColor.copy(alpha = 0.22f), RoundedCornerShape(43.dp))
                    .border(3.dp, statusColor, RoundedCornerShape(43.dp))
            )
        }
    }
}

@Composable
private fun DashboardFallbackCard(widget: DashboardWidget, subtitle: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 116.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = widget.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DashboardSimpleValueCard(
    title: String,
    value: String,
    unit: String,
    background: Color,
    titleColor: Color,
    valueColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 156.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = titleColor,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
                    .background(background, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.titleLarge,
                        color = valueColor,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.titleMedium,
                        color = valueColor
                    )
                }
            }
        }
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
                        val y =
                            height - (((point.value - minValue) / valueRange).toFloat() * height)
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
                        val x =
                            if (points.size == 1) width / 2f else width * pointIndex / (points.size - 1)
                        val y =
                            height - (((point.value - minValue) / valueRange).toFloat() * height)
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

private fun widgetIconFromName(iconName: String?): ImageVector {
    return dashboardLucideIcon(iconName, fallback = Lucide.Thermometer)
}

private fun widgetValueLabel(widget: DashboardWidget): String =
    widget.currentValue?.let { it.formatForCard() } ?: "--"

private fun Double.formatForCard(decimals: Int = 2): String =
    if (!this.isFinite()) "--"
    else "%.${decimals}f".format(java.util.Locale.US, this)

private fun Double.formatOneDecimal(): String =
    if (!this.isFinite()) "--"
    else "%.1f".format(java.util.Locale.US, this)

private fun String.toComposeColorOrNull(): Color? {
    val raw = trim()
    if (!raw.startsWith("#")) return null
    val hex = raw.removePrefix("#")
    val normalized = when (hex.length) {
        3 -> hex.map { "$it$it" }.joinToString(separator = "")
        6 -> hex
        8 -> hex
        else -> return null
    }

    return runCatching {
        val colorLong = normalized.toLong(16)
        when (normalized.length) {
            6 -> Color((0xFF000000 or colorLong).toInt())
            8 -> {
                val a = ((colorLong shr 24) and 0xFF).toInt()
                val r = ((colorLong shr 16) and 0xFF).toInt()
                val g = ((colorLong shr 8) and 0xFF).toInt()
                val b = (colorLong and 0xFF).toInt()
                Color(r, g, b, a)
            }

            else -> null
        }
    }.getOrNull()
}

private fun computeValueStatus(
    value: Double?,
    mode: String,
    bitSelection: Int?,
    colorValues: DashboardWidgetColorValues?
): String {
    if (value == null || !value.isFinite()) return "gray"
    return if (mode.trim().lowercase() == "bit") {
        val rawValue = value.toLong()
        val bit = (bitSelection ?: 0).coerceAtLeast(0)
        val current = ((rawValue shr bit) and 1L).toInt()
        if (current == 1) "green" else "red"
    } else {
        val green = colorValues?.green
        val red = colorValues?.red
        val yellow = colorValues?.yellow
        when {
            green != null && value == green -> "green"
            red != null && value == red -> "red"
            yellow != null && value == yellow -> "yellow"
            else -> "gray"
        }
    }
}
