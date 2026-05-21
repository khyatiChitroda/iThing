package com.ithing.mobile.presentation.feature.reports.analyticsReport

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.view.View
import java.util.Locale
import kotlin.math.abs
import kotlin.math.ceil

internal class AnalyticsReportSectionView(context: Context) : View(context) {

    private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GRAY
        strokeWidth = 2f
        style = Paint.Style.STROKE
    }

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(19, 52, 96)
        strokeWidth = 3f
        style = Paint.Style.STROKE
    }

    private val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(19, 52, 96)
        style = Paint.Style.FILL
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(40, 19, 52, 96)
        style = Paint.Style.FILL
    }

    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(78, 144, 191)
        style = Paint.Style.FILL
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 12f
    }

    private val metaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.DKGRAY
        textSize = 9f
    }

    private val sectionTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 14f
        isFakeBoldText = true
        textAlign = Paint.Align.LEFT
    }

    private var marginPx: Float = 36f
    private var pageWidthPx: Int = 595

    private var title: String = "Analytic Report"
    private var chartType: AnalyticsChartType? = null
    private var fields: List<String> = emptyList()
    private var data: List<Map<String, String>> = emptyList()

    private fun legendHeightPx(): Float =
        if (chartType == AnalyticsChartType.BAR || chartType == AnalyticsChartType.LINE || chartType == AnalyticsChartType.AREA) 44f else 0f

    private fun tableGapPx(): Float = if (chartType == AnalyticsChartType.BAR) 58f else 18f

    private fun seriesColor(fieldName: String, fallbackIndex: Int): Int {
        val n = fieldName.lowercase(Locale.US)
        return when {
            "import" in n -> Color.rgb(0x13, 0x34, 0x60) // #133460
            "export" in n -> Color.rgb(0x4E, 0x90, 0xBF) // #4E90BF
            "net" in n -> Color.rgb(0x78, 0xC8, 0x5A) // #78C85A
            "total" in n -> Color.rgb(0xFF, 0x8C, 0x3C) // #FF8C3C
            else -> listOf(
                Color.rgb(19, 52, 96),
                Color.rgb(78, 144, 191),
                Color.rgb(30, 144, 255),
                Color.rgb(46, 204, 113),
                Color.rgb(241, 196, 15),
                Color.rgb(231, 76, 60)
            )[fallbackIndex % 6]
        }
    }

    fun bind(
        pageWidthPx: Int,
        marginPx: Float,
        title: String,
        chartType: AnalyticsChartType?,
        fields: List<String>,
        data: List<Map<String, String>>
    ) {
        this.pageWidthPx = pageWidthPx
        this.marginPx = marginPx
        this.title = title.ifBlank { "Analytic Report" }
        this.chartType = chartType
        this.fields = fields
        this.data = data
        requestLayout()
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec).takeIf { it > 0 } ?: pageWidthPx

        val sectionTitleH = 22f
        val legendH = legendHeightPx()
        val chartH = 260f
        val tableGap = tableGapPx()
        val yLabelW = 48f
        val plotWidth = (width.toFloat() - 2 * marginPx - yLabelW).coerceAtLeast(1f)
        val minColW = 52f
        val maxTimeColsPerBlock =
            (((plotWidth / minColW).toInt()).coerceAtLeast(3) - 1).coerceAtLeast(1)
        val blocks = if (chartType == AnalyticsChartType.BAR) {
            ((data.size + maxTimeColsPerBlock - 1) / maxTimeColsPerBlock).coerceAtLeast(1)
        } else {
            1
        }
        val headerH = 16f
        val rowH = 14f
        val footH = 14f
        val blockGap = 10f
        val blockH = headerH + (fields.take(6).size * rowH) + footH
        val tableH = if (chartType == AnalyticsChartType.BAR) {
            blocks * blockH + (blocks - 1) * blockGap
        } else {
            140f
        }
        val sectionH =
            (sectionTitleH + legendH + chartH + tableGap + tableH).toInt().coerceAtLeast(1)

        setMeasuredDimension(width, sectionH)
    }

    override fun onDraw(canvas: Canvas) {
        val sectionTitleH = 22f
        val legendH = legendHeightPx()
        val chartH = 260f
        val tableGap = tableGapPx()
        val tableH = 140f

        val chartLeft = marginPx
        val chartRight = (pageWidthPx - marginPx)
        val yLabelW = 48f
        val plotLeft = chartLeft + yLabelW
        val plotRight = chartRight

        val bigTitlePaint = Paint(sectionTitlePaint).apply { textSize = 16.5f }
        canvas.drawText(title, chartLeft, 18f, bigTitlePaint)

        val chartTop = sectionTitleH + legendH
        val chartBottom = chartTop + chartH
        val tableTop = chartBottom + tableGap

        canvas.drawRect(plotLeft, chartTop, plotRight, chartBottom, axisPaint)

        val labels = data.mapNotNull { it["Time"] ?: it["Date"] }
        val series = fields.map { fieldName ->
            data.mapNotNull { it[fieldName]?.toDoubleOrNull() }
        }

        val usableWidth = plotRight - plotLeft
        val usableHeight = chartBottom - chartTop

        fun xAt(i: Int): Float {
            val denom = (labels.size - 1).coerceAtLeast(1)
            return plotLeft + (i.toFloat() / denom.toFloat()) * usableWidth
        }

        val allValues = series.flatten()
        if (allValues.size >= 2 && chartType != null) {
            val dataMin = allValues.minOrNull() ?: 0.0
            val dataMax = allValues.maxOrNull() ?: 0.0
            val ct = chartType ?: return

            val rawRange = dataMax - dataMin
            val (minVal, maxVal) = if (ct == AnalyticsChartType.BAR || ct == AnalyticsChartType.LINE || ct == AnalyticsChartType.AREA) {
                (-400.0) to 500.0
            } else if (abs(rawRange) < 1e-9) {
                val pad = maxOf(1.0, abs(dataMin) * 0.05)
                (dataMin - pad) to (dataMax + pad)
            } else {
                dataMin to dataMax
            }

            val range = (maxVal - minVal).takeIf { abs(it) >= 1e-9 } ?: 1.0

            fun yAt(v: Double): Float {
                val pct = ((v - minVal) / range).toFloat().coerceIn(0f, 1f)
                return chartBottom - pct * usableHeight
            }

            // Web-like grid + Y labels
            val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(35, 15, 23, 42)
                strokeWidth = 1f
            }
            val yLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.DKGRAY
                textSize = 8.5f
            }
            val ticks = 5
            for (t in 0..ticks) {
                val frac = t.toFloat() / ticks.toFloat()
                val y = chartBottom - frac * usableHeight
                canvas.drawLine(plotLeft, y, plotRight, y, gridPaint)
                val v = minVal + (range * frac)
                canvas.drawText(formatNumber(v), chartLeft + 6f, y - 3f, yLabelPaint)
            }

            listOf(
                Color.rgb(19, 52, 96),
                Color.rgb(78, 144, 191),
                Color.rgb(30, 144, 255),
                Color.rgb(46, 204, 113),
                Color.rgb(241, 196, 15),
                Color.rgb(231, 76, 60)
            )

            fun ellipsize(text: String, paint: Paint, maxWidth: Float): String {
                if (paint.measureText(text) <= maxWidth) return text
                val dots = "…"
                val dotW = paint.measureText(dots)
                var out = text
                while (out.isNotEmpty() && paint.measureText(out) + dotW > maxWidth) {
                    out = out.dropLast(1)
                }
                return if (out.isEmpty()) dots else out + dots
            }

            // Legend for BAR/LINE/AREA (web-style)
            if (legendH > 0f && (ct == AnalyticsChartType.BAR || ct == AnalyticsChartType.LINE || ct == AnalyticsChartType.AREA)) {
                var legendY = sectionTitleH + 18f
                val swatchW = 22f
                var x = plotLeft
                fields.take(4).forEachIndexed { idx, name ->
                    val maxTextW = (plotRight - plotLeft) * 0.45f
                    val label = ellipsize(name, metaPaint, maxTextW)
                    val needed = swatchW + 8f + metaPaint.measureText(label) + 22f
                    if (x + needed > plotRight) {
                        legendY += 14f
                        x = plotLeft
                    }
                    val p = Paint(barPaint).apply { color = seriesColor(name, idx) }
                    canvas.drawRect(x, legendY - 10f, x + swatchW, legendY + 4f, p)
                    canvas.drawText(label, x + swatchW + 8f, legendY + 2f, metaPaint)
                    x += needed
                }
            }

            when (ct) {
                AnalyticsChartType.LINE, AnalyticsChartType.AREA -> {
                    val vGridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = Color.argb(25, 15, 23, 42)
                        strokeWidth = 1f
                    }
                    labels.indices.forEach { i ->
                        val x = xAt(i)
                        canvas.drawLine(x, chartTop, x, chartBottom, vGridPaint)
                    }

                    series.forEachIndexed { sIdx, values ->
                        if (values.size < 2) return@forEachIndexed
                        val p = Paint(linePaint).apply {
                            color = seriesColor(fields.getOrNull(sIdx).orEmpty(), sIdx)
                            strokeWidth = 2.4f
                            strokeCap = Paint.Cap.ROUND
                        }
                        val path = Path()
                        values.forEachIndexed { i, v ->
                            val x = xAt(i)
                            val y = yAt(v)
                            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }
                        canvas.drawPath(path, p)

                        if (ct == AnalyticsChartType.AREA) {
                            val fill = Paint(fillPaint).apply {
                                color = Color.argb(
                                    40,
                                    Color.red(p.color),
                                    Color.green(p.color),
                                    Color.blue(p.color)
                                )
                            }
                            val fillPath = Path(path).apply {
                                lineTo(xAt(values.size - 1), chartBottom)
                                lineTo(xAt(0), chartBottom)
                                close()
                            }
                            canvas.drawPath(fillPath, fill)
                        }

                        values.forEachIndexed { i, v ->
                            val x = xAt(i)
                            val y = yAt(v)
                            canvas.drawCircle(
                                x,
                                y,
                                4.0f,
                                Paint(pointPaint).apply { color = p.color })
                        }
                    }
                }

                AnalyticsChartType.BAR -> {
                    val groupCount = maxOf(1, fields.size)
                    val safeCount = maxOf(1, labels.size)
                    val slotWidth = usableWidth / safeCount.toFloat()
                    val groupWidth = (slotWidth * 0.82f).coerceAtMost(42f)
                    val barWidth = (groupWidth / groupCount.toFloat()).coerceAtLeast(5.5f)

                    val yZero = yAt(0.0)
                    canvas.drawLine(
                        plotLeft,
                        yZero,
                        plotRight,
                        yZero,
                        Paint(axisPaint).apply { strokeWidth = 1.5f })

                    canvas.save()
                    canvas.clipRect(plotLeft, chartTop, plotRight, chartBottom)
                    // Vertical gridlines at each time slot
                    val vGridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = Color.argb(25, 15, 23, 42)
                        strokeWidth = 1f
                    }
                    labels.indices.forEach { i ->
                        val xCenter = plotLeft + (i + 0.5f) * slotWidth
                        canvas.drawLine(xCenter, chartTop, xCenter, chartBottom, vGridPaint)
                        val groupLeft = xCenter - groupWidth / 2f
                        fields.forEachIndexed { fieldIndex, fieldName ->
                            val v = data.getOrNull(i)?.get(fieldName)?.toDoubleOrNull()
                                ?: return@forEachIndexed
                            val x0 = groupLeft + fieldIndex * barWidth
                            val x1 = x0 + barWidth * 0.92f
                            val y = yAt(v)
                            val minBarHeight = 3.5f
                            val topY: Float
                            val bottomY: Float
                            if (v >= 0.0) {
                                topY = minOf(y, yZero - minBarHeight).coerceAtLeast(chartTop)
                                bottomY = yZero.coerceIn(chartTop + minBarHeight, chartBottom)
                            } else {
                                topY = yZero.coerceIn(chartTop, chartBottom - minBarHeight)
                                bottomY = maxOf(y, yZero + minBarHeight).coerceAtMost(chartBottom)
                            }
                            val p =
                                Paint(barPaint).apply { color = seriesColor(fieldName, fieldIndex) }
                            canvas.drawRect(x0, topY, x1, bottomY, p)
                        }
                    }
                    canvas.restore()
                }

                AnalyticsChartType.HEAT_MAP -> {
                    drawHeatMapGrid(
                        canvas = canvas,
                        data = data,
                        fields = fields.take(6),
                        left = plotLeft,
                        top = chartTop,
                        right = plotRight,
                        bottom = chartBottom,
                        labelPaint = metaPaint
                    )
                }
            }

        } else {
            val msg = "No data for selected range/field"
            val textWidth = textPaint.measureText(msg)
            canvas.drawText(
                msg,
                (chartLeft + chartRight) / 2f - textWidth / 2f,
                (chartTop + chartBottom) / 2f,
                textPaint
            )
        }

        if (labels.isNotEmpty() && chartType == AnalyticsChartType.BAR) {
            val safeCount = maxOf(1, labels.size)
            val usableWidth = plotRight - plotLeft
            val slotWidth = usableWidth / safeCount.toFloat()

            val xLabelPaint = Paint(metaPaint).apply {
                textAlign = Paint.Align.CENTER
                // draw all; shrink when dense
                textSize = (slotWidth * 0.32f).coerceIn(5.5f, 8.0f)
            }
            labels.indices.forEach { i ->
                val xCenter = plotLeft + (i + 0.5f) * slotWidth
                val label = labels.getOrNull(i).orEmpty().take(10)
                canvas.drawText(label, xCenter, chartBottom + 16f, xLabelPaint)
            }
        } else if (labels.isNotEmpty() && (chartType == AnalyticsChartType.LINE || chartType == AnalyticsChartType.AREA)) {
            val xLabelPaint = Paint(metaPaint).apply {
                textAlign = Paint.Align.CENTER
                textSize = 7.5f
            }
            val minLabelPx = 44f
            val step = ceil(minLabelPx / (usableWidth / maxOf(1, labels.size).toFloat()))
                .toInt().coerceAtLeast(1)
            labels.indices.forEach { i ->
                if (i % step != 0) return@forEach
                val x = xAt(i)
                val label = labels.getOrNull(i).orEmpty().take(10)
                canvas.drawText(label, x, chartBottom + 16f, xLabelPaint)
            }
        } else if (labels.isNotEmpty()) {
            val sampleCount = 4
            (0 until sampleCount).forEach { s ->
                val idx = ((labels.size - 1) * (s.toFloat() / (sampleCount - 1).toFloat())).toInt()
                val label = labels.getOrNull(idx).orEmpty()
                val x =
                    chartLeft + (s.toFloat() / (sampleCount - 1).toFloat()) * (chartRight - chartLeft)
                canvas.drawText(label.take(12), x - 20f, chartBottom + 44f, metaPaint)
            }
        }

        if (data.isNotEmpty() && fields.isNotEmpty()) {
            drawBottomTable(
                canvas = canvas,
                data = data,
                fields = fields.take(6),
                left = plotLeft,
                right = plotRight,
                top = tableTop,
                metaPaint = metaPaint,
                tableH = tableH
            )
        }
    }

    private fun drawHeatMapGrid(
        canvas: Canvas,
        data: List<Map<String, String>>,
        fields: List<String>,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        labelPaint: Paint
    ) {
        if (data.isEmpty() || fields.isEmpty()) {
            canvas.drawText("No heat map data.", left + 12f, top + 30f, labelPaint)
            return
        }

        val cols = data.size.coerceAtMost(24)
        val sampled = if (data.size <= cols) data else {
            val idxs = (0 until cols).map { i ->
                ((data.size - 1) * (i.toFloat() / (cols - 1).toFloat())).toInt()
            }
            idxs.mapNotNull { data.getOrNull(it) }
        }

        val rows = fields.size
        val labelW = 110f
        val gridLeft = left + labelW
        val gridWidth = right - gridLeft
        val gridHeight = bottom - top
        val cellW = gridWidth / cols.toFloat()
        val cellH = gridHeight / rows.toFloat()

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(90, 0, 0, 0)
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }

        val fieldLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.DKGRAY
            textSize = 10.5f
        }

        val cellPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

        fun discreteColor(v: Double): Int? {
            return when {
                v >= -0.5 && v <= 0.5 -> Color.rgb(170, 170, 170)
                v >= 0.6 && v <= 1.5 -> Color.rgb(0, 0, 255)
                v >= 1.6 && v <= 2.5 -> Color.rgb(0, 255, 0)
                v >= 2.6 && v <= 6.0 -> Color.rgb(255, 0, 0)
                else -> null
            }
        }

        val perFieldValues = fields.associateWith { field ->
            sampled.mapNotNull { it[field]?.toDoubleOrNull() }
        }

        fields.forEachIndexed { r, field ->
            canvas.drawText(field.take(16), left, top + (r + 0.7f) * cellH, fieldLabelPaint)
        }

        fields.forEachIndexed { r, field ->
            val values = perFieldValues[field].orEmpty()
            val fMin = values.minOrNull() ?: 0.0
            val fMax = values.maxOrNull() ?: 0.0
            val fRange = (fMax - fMin).takeIf { abs(it) >= 1e-9 } ?: 1.0

            sampled.forEachIndexed { c, row ->
                val v = row[field]?.toDoubleOrNull()
                val color = v?.let { discreteColor(it) } ?: run {
                    if (v == null) {
                        Color.argb(40, 15, 23, 42)
                    } else {
                        val pct = ((v - fMin) / fRange).toFloat().coerceIn(0f, 1f)
                        Color.argb(
                            255,
                            (40 + 60 * (1 - pct)).toInt(),
                            (90 + 110 * (1 - pct)).toInt(),
                            (160 + 95 * pct).toInt()
                        )
                    }
                }

                cellPaint.color = color
                val x0 = gridLeft + c * cellW
                val y0 = top + r * cellH
                canvas.drawRect(x0, y0, x0 + cellW, y0 + cellH, cellPaint)
            }
        }

        canvas.drawRect(gridLeft, top, right, bottom, borderPaint)

        val colLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.DKGRAY
            textSize = 9.8f
        }
        val labelCount = 4
        (0 until labelCount).forEach { i ->
            val idx = ((sampled.size - 1) * (i.toFloat() / (labelCount - 1).toFloat())).toInt()
            val label = (sampled.getOrNull(idx)?.get("Time") ?: sampled.getOrNull(idx)?.get("Date")
            ?: "").take(10)
            val x = gridLeft + idx * cellW
            canvas.drawText(label, x, bottom + 16f, colLabelPaint)
        }

        val legendItems = listOf(
            "Idle" to Color.rgb(170, 170, 170),
            "Pre Heating" to Color.rgb(0, 0, 255),
            "Cycle On" to Color.rgb(0, 255, 0),
            "Error" to Color.rgb(255, 0, 0)
        )
        val legendTop = bottom + 28f
        legendItems.forEachIndexed { idx, item ->
            val x = gridLeft + idx * 110f
            cellPaint.color = item.second
            canvas.drawRect(x, legendTop, x + 14f, legendTop + 10f, cellPaint)
            canvas.drawText(item.first, x + 18f, legendTop + 10f, colLabelPaint)
        }
    }

    private fun drawBottomTable(
        canvas: Canvas,
        data: List<Map<String, String>>,
        fields: List<String>,
        left: Float,
        right: Float,
        top: Float,
        metaPaint: Paint,
        tableH: Float
    ) {
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(0, 46, 94) }
        val headerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 8.3f
            isFakeBoldText = true
        }
        val cellTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(51, 51, 51)
            textSize = 8.0f
        }
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(80, 0, 0, 0)
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }

        val tableWidth = right - left
        val minColW = 52f
        val maxTimeColsPerBlock =
            (((tableWidth / minColW).toInt()).coerceAtLeast(3) - 1).coerceAtLeast(1)
        val blocks = if (data.isNotEmpty()) data.chunked(maxTimeColsPerBlock) else emptyList()

        val headerH = 16f
        val rowH = 14f
        val footH = 14f
        val blockGap = 10f

        val bottomLimit = top + tableH

        fun ellipsize(text: String, paint: Paint, maxWidth: Float): String {
            if (paint.measureText(text) <= maxWidth) return text
            val dots = "…"
            val dotW = paint.measureText(dots)
            var out = text
            while (out.isNotEmpty() && paint.measureText(out) + dotW > maxWidth) {
                out = out.dropLast(1)
            }
            return if (out.isEmpty()) dots else out + dots
        }

        blocks.forEachIndexed { blockIndex, blockPoints ->
            val blockTop = top + blockIndex * (headerH + fields.size * rowH + footH + blockGap)
            val blockBottom = blockTop + headerH + fields.size * rowH + footH
            if (blockTop >= bottomLimit) return@forEachIndexed
            val clippedBottom = minOf(blockBottom, bottomLimit)

            val columns = 1 + blockPoints.size
            val colWidth = tableWidth / columns.toFloat()

            canvas.drawRect(left, blockTop, right, blockTop + headerH, headerPaint)
            canvas.drawRect(left, blockTop, right, blockTop + headerH, borderPaint)

            val headerTextY = blockTop + 11.8f
            canvas.drawText("value", left + 4f, headerTextY, headerTextPaint)
            blockPoints.forEachIndexed { idx, row ->
                val xCellLeft = left + (idx + 1) * colWidth
                val x = xCellLeft + 4f
                val maxW = colWidth - 8f
                val label =
                    ellipsize((row["Time"] ?: row["Date"] ?: "").take(10), headerTextPaint, maxW)
                canvas.drawText(label, x, headerTextY, headerTextPaint)
            }
            for (c in 0..columns) {
                val x = left + c * colWidth
                canvas.drawLine(x, blockTop, x, blockTop + headerH, borderPaint)
            }

            fields.forEachIndexed { rowIndex, field ->
                val y0 = blockTop + headerH + rowIndex * rowH
                val y1 = y0 + rowH
                if (y0 >= bottomLimit) return@forEachIndexed
                canvas.drawRect(left, y0, right, minOf(y1, clippedBottom), borderPaint)

                val cellTextY = y0 + 10.5f
                val labelMaxW = colWidth - 8f
                canvas.drawText(
                    ellipsize(field, cellTextPaint, labelMaxW),
                    left + 4f,
                    cellTextY,
                    cellTextPaint
                )

                blockPoints.forEachIndexed { idx, row ->
                    val xCellLeft = left + (idx + 1) * colWidth
                    val x = xCellLeft + 4f
                    val maxW = colWidth - 8f
                    val v = row[field]?.toDoubleOrNull()
                    val txt = if (v == null) "-" else formatNumber(v)
                    canvas.drawText(
                        ellipsize(txt, cellTextPaint, maxW),
                        x,
                        cellTextY,
                        cellTextPaint
                    )
                }
                for (c in 0..columns) {
                    val x = left + c * colWidth
                    canvas.drawLine(x, y0, x, minOf(y1, clippedBottom), borderPaint)
                }
            }

            val footY = blockTop + headerH + fields.size * rowH + 12f
            canvas.drawText(
                if (blocks.size > 1) "Table (continued)" else "Table shows all points",
                left,
                minOf(footY, bottomLimit - 2f),
                metaPaint
            )
        }
    }

    private fun formatNumber(value: Double): String {
        return if (abs(value) >= 1000) {
            java.lang.String.format(Locale.US, "%.0f", value)
        } else {
            java.lang.String.format(Locale.US, "%.2f", value)
        }
    }
}
