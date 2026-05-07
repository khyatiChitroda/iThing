package com.ithing.mobile.presentation.feature.reports

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.pdf.PdfDocument
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

internal object AnalyticsPdfGenerator {

    fun generateAndSave(
        context: Context,
        deviceId: String,
        chartRows: List<AnalyticsChartConfigUi>,
        dataSets: List<List<Map<String, String>>>,
        fromMillis: Long,
        toMillis: Long
    ): File {
        val reportsDir = context.getExternalFilesDir("reports") ?: File(context.filesDir, "reports")
        if (!reportsDir.exists()) {
            reportsDir.mkdirs()
        }

        val now = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(Date())
        val file = File(reportsDir, "${deviceId}_analytic_${now}.pdf")

        val document = PdfDocument()
        try {
            val pageWidth = 595
            val pageHeight = 842

            val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.BLACK
                textSize = 18f
                isFakeBoldText = true
            }

            val metaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.DKGRAY
                textSize = 11f
            }

            val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.GRAY
                strokeWidth = 2f
                style = Paint.Style.STROKE
            }

            val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(19, 52, 96) // web-ish navy
                strokeWidth = 3f
                style = Paint.Style.STROKE
            }

            val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(19, 52, 96)
                style = Paint.Style.FILL
            }

            val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(40, 19, 52, 96)
                style = Paint.Style.FILL
            }

            val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(78, 144, 191) // web-ish blue
                style = Paint.Style.FILL
            }

            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.BLACK
                textSize = 12f
            }

            val margin = 36f
            val chartLeft = margin
            val chartRight = pageWidth - margin
            val chartTop = 170f
            val chartBottom = pageHeight - 260f
            val tableTop = chartBottom + 74f

            val fromLabel = Date(fromMillis).toString()
            val toLabel = Date(toMillis).toString()

            chartRows.forEachIndexed { index, row ->
                val data = dataSets.getOrNull(index).orEmpty()
                val fields = row.selectedFields.takeIf { it.isNotEmpty() }.orEmpty()
                val chartType = row.chartType

                val labels = data.mapNotNull { it["Time"] ?: it["Date"] }
                val series = fields.map { fieldName ->
                    data.mapNotNull { it[fieldName]?.toDoubleOrNull() }
                }

                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, index + 1).create()
                val page = document.startPage(pageInfo)
                val canvas = page.canvas

                canvas.drawText(row.title.ifBlank { "Analytic Report" }, margin, 52f, titlePaint)
                canvas.drawText("Device: $deviceId", margin, 78f, metaPaint)
                canvas.drawText("Range: $fromLabel  →  $toLabel", margin, 96f, metaPaint)
                canvas.drawText("Field: ${fields.joinToString(", ").ifBlank { "-" }}", margin, 114f, metaPaint)
                canvas.drawText("Chart: ${chartType?.label ?: "-"}", margin, 132f, metaPaint)

                // Chart frame
                canvas.drawRect(chartLeft, chartTop, chartRight, chartBottom, axisPaint)

                val allValues = series.flatten()
                if (allValues.size >= 2 && chartType != null) {
                    val dataMin = allValues.minOrNull() ?: 0.0
                    val dataMax = allValues.maxOrNull() ?: 0.0

                    val rawRange = dataMax - dataMin
                    val (minVal, maxVal) = if (abs(rawRange) < 1e-9) {
                        // Match web chart libraries behavior: expand Y range so flat series remains visible.
                        val pad = maxOf(1.0, abs(dataMin) * 0.05)
                        (dataMin - pad) to (dataMax + pad)
                    } else {
                        dataMin to dataMax
                    }

                    val range = (maxVal - minVal).takeIf { abs(it) >= 1e-9 } ?: 1.0

                    val usableWidth = chartRight - chartLeft
                    val usableHeight = chartBottom - chartTop

                    fun xAt(i: Int): Float {
                        val denom = (labels.size - 1).coerceAtLeast(1)
                        return chartLeft + (i.toFloat() / denom.toFloat()) * usableWidth
                    }

                    fun yAt(v: Double): Float {
                        val pct = (v - minVal) / range
                        return (chartBottom - (pct.toFloat() * usableHeight)).coerceIn(chartTop + 2f, chartBottom - 2f)
                    }

                    // Web-like polish: light horizontal gridlines + Y labels based on scaled min/max.
                    val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = Color.argb(35, 15, 23, 42)
                        strokeWidth = 1f
                    }
                    val yLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = Color.DKGRAY
                        textSize = 10f
                    }
                    val ticks = 5
                    for (t in 0..ticks) {
                        val frac = t.toFloat() / ticks.toFloat()
                        val y = chartBottom - frac * usableHeight
                        canvas.drawLine(chartLeft, y, chartRight, y, gridPaint)
                        val yVal = minVal + (range * frac.toDouble())
                        canvas.drawText(formatNumber(yVal), chartLeft, y - 4f, yLabelPaint)
                    }

                    val palette = listOf(
                        Color.rgb(19, 52, 96),
                        Color.rgb(78, 144, 191),
                        Color.rgb(126, 200, 99),
                        Color.rgb(250, 133, 63),
                        Color.rgb(132, 160, 179),
                        Color.rgb(245, 158, 11)
                    )

                    when (chartType) {
                        AnalyticsChartType.LINE, AnalyticsChartType.AREA -> {
                            fields.forEachIndexed { fieldIndex, fieldName ->
                                val fieldValues = data.mapNotNull { it[fieldName]?.toDoubleOrNull() }
                                if (fieldValues.size < 2) return@forEachIndexed

                                val strokePaint = Paint(linePaint).apply { color = palette[fieldIndex % palette.size] }
                                val dotPaint = Paint(pointPaint).apply { color = palette[fieldIndex % palette.size] }

                                val path = Path()
                                fieldValues.forEachIndexed { i, v ->
                                    val x = xAt(i)
                                    val y = yAt(v)
                                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                                }

                                if (chartType == AnalyticsChartType.AREA) {
                                    val fill = Paint(fillPaint).apply { color = Color.argb(35, Color.red(strokePaint.color), Color.green(strokePaint.color), Color.blue(strokePaint.color)) }
                                    val fillPath = Path(path).apply {
                                        lineTo(xAt(fieldValues.size - 1), chartBottom)
                                        lineTo(xAt(0), chartBottom)
                                        close()
                                    }
                                    canvas.drawPath(fillPath, fill)
                                }

                                canvas.drawPath(path, strokePaint)
                                fieldValues.forEachIndexed { i, v ->
                                    val x = xAt(i)
                                    val y = yAt(v)
                                    canvas.drawCircle(x, y, 3.2f, dotPaint)
                                }
                            }
                        }

                        AnalyticsChartType.BAR -> {
                            val groupCount = maxOf(1, fields.size)
                            val slotWidth = usableWidth / labels.size.toFloat()
                            val groupWidth = (slotWidth * 0.82f).coerceAtMost(42f)
                            val barWidth = (groupWidth / groupCount.toFloat()).coerceAtLeast(5.5f)

                            labels.indices.forEach { i ->
                                val xCenter = xAt(i)
                                val groupLeft = xCenter - groupWidth / 2f
                                fields.forEachIndexed { fieldIndex, fieldName ->
                                    val v = data.getOrNull(i)?.get(fieldName)?.toDoubleOrNull() ?: return@forEachIndexed
                                    val x0 = groupLeft + fieldIndex * barWidth
                                    val x1 = x0 + barWidth * 0.92f
                                    val y = yAt(v)
                                    val minBarHeight = 3.5f
                                    val topY = minOf(y, chartBottom - minBarHeight)
                                    val p = Paint(barPaint).apply { color = palette[fieldIndex % palette.size] }
                                    canvas.drawRect(x0, topY, x1, chartBottom, p)
                                }
                            }
                        }

                        AnalyticsChartType.HEAT_MAP -> {
                            drawHeatMapGrid(
                                canvas = canvas,
                                data = data,
                                fields = fields.take(6),
                                left = chartLeft,
                                top = chartTop,
                                right = chartRight,
                                bottom = chartBottom,
                                labelPaint = metaPaint
                            )
                        }
                    }

                    // Min/Max labels
                    canvas.drawText("min: ${formatNumber(dataMin)}", chartLeft, chartBottom + 22f, metaPaint)
                    canvas.drawText("max: ${formatNumber(dataMax)}", chartLeft + 150f, chartBottom + 22f, metaPaint)
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

                // Light label sampling at bottom
                if (labels.isNotEmpty()) {
                    val sampleCount = 4
                    (0 until sampleCount).forEach { s ->
                        val idx = ((labels.size - 1) * (s.toFloat() / (sampleCount - 1).toFloat())).toInt()
                        val label = labels.getOrNull(idx).orEmpty()
                        val x = chartLeft + (s.toFloat() / (sampleCount - 1).toFloat()) * (chartRight - chartLeft)
                        canvas.drawText(label.take(12), x - 20f, chartBottom + 44f, metaPaint)
                    }
                }

                // Bottom table (web PDF includes it). Keep it compact for A4.
                if (data.isNotEmpty() && fields.isNotEmpty()) {
                    drawBottomTable(
                        canvas = canvas,
                        data = data,
                        fields = fields.take(6),
                        left = chartLeft,
                        right = chartRight,
                        top = tableTop,
                        metaPaint = metaPaint
                    )
                }

                document.finishPage(page)
            }

            FileOutputStream(file).use { output ->
                document.writeTo(output)
            }
            return file
        } finally {
            document.close()
        }
    }

    private fun drawHeatMapFallback(
        canvas: Canvas,
        data: List<Map<String, String>>,
        field: String,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        textPaint: Paint
    ) {
        if (data.isEmpty() || field.isBlank()) {
            canvas.drawText("No heat map data.", left + 12f, top + 30f, textPaint)
            return
        }

        val values = data.mapNotNull { it[field]?.toDoubleOrNull() }
        if (values.isEmpty()) {
            canvas.drawText("No numeric values for heat map.", left + 12f, top + 30f, textPaint)
            return
        }

        val minVal = values.minOrNull() ?: 0.0
        val maxVal = values.maxOrNull() ?: 0.0
        val range = (maxVal - minVal).takeIf { it != 0.0 } ?: 1.0

        val cols = 12
        val rows = ((values.size + cols - 1) / cols).coerceAtLeast(1)
        val cellW = (right - left) / cols.toFloat()
        val cellH = (bottom - top) / rows.toFloat()

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        values.forEachIndexed { idx, v ->
            val r = idx / cols
            val c = idx % cols
            val pct = ((v - minVal) / range).toFloat().coerceIn(0f, 1f)
            paint.color = Color.argb(255, (255 * pct).toInt(), 80, (255 * (1f - pct)).toInt())
            val x0 = left + c * cellW
            val y0 = top + r * cellH
            canvas.drawRect(x0, y0, x0 + cellW, y0 + cellH, paint)
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
            val msg = "No heat map data."
            canvas.drawText(msg, left + 12f, top + 30f, labelPaint)
            return
        }

        val cols = data.size.coerceAtMost(24) // keep readable on A4
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

        // Use web-like discrete legend if values are in 0..6, else fallback to per-field normalization.
        fun discreteColor(v: Double): Int? {
            return when {
                v >= -0.5 && v <= 0.5 -> Color.rgb(170, 170, 170) // Idle
                v >= 0.6 && v <= 1.5 -> Color.rgb(0, 0, 255) // Pre Heating
                v >= 1.6 && v <= 2.5 -> Color.rgb(0, 255, 0) // Cycle On
                v >= 2.6 && v <= 6.0 -> Color.rgb(255, 0, 0) // Error
                else -> null
            }
        }

        val perFieldValues = fields.associateWith { field ->
            sampled.mapNotNull { it[field]?.toDoubleOrNull() }
        }

        // Row labels
        fields.forEachIndexed { r, field ->
            canvas.drawText(field.take(16), left, top + (r + 0.7f) * cellH, fieldLabelPaint)
        }

        // Draw cells
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
                        // blue intensity gradient
                        Color.argb(255, (40 + 60 * (1 - pct)).toInt(), (90 + 110 * (1 - pct)).toInt(), (160 + 95 * pct).toInt())
                    }
                }

                cellPaint.color = color
                val x0 = gridLeft + c * cellW
                val y0 = top + r * cellH
                canvas.drawRect(x0, y0, x0 + cellW, y0 + cellH, cellPaint)
            }
        }

        // Border
        canvas.drawRect(gridLeft, top, right, bottom, borderPaint)

        // Column labels (sparse)
        val colLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.DKGRAY
            textSize = 9.8f
        }
        val labelCount = 4
        (0 until labelCount).forEach { i ->
            val idx = ((sampled.size - 1) * (i.toFloat() / (labelCount - 1).toFloat())).toInt()
            val label = (sampled.getOrNull(idx)?.get("Time") ?: sampled.getOrNull(idx)?.get("Date") ?: "").take(10)
            val x = gridLeft + idx * cellW
            canvas.drawText(label, x, bottom + 16f, colLabelPaint)
        }

        // Small legend (web)
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

    private fun formatNumber(value: Double): String {
        return if (kotlin.math.abs(value) >= 1000) {
            String.format(Locale.US, "%.0f", value)
        } else {
            String.format(Locale.US, "%.2f", value)
        }
    }

    private fun drawBottomTable(
        canvas: Canvas,
        data: List<Map<String, String>>,
        fields: List<String>,
        left: Float,
        right: Float,
        top: Float,
        metaPaint: Paint
    ) {
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(0, 46, 94) }
        val headerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 9.5f
            isFakeBoldText = true
        }
        val cellTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(51, 51, 51)
            textSize = 9.2f
        }
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(80, 0, 0, 0)
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }

        val rowsToShow = 6
        val samplePoints = if (data.size <= rowsToShow) data else {
            val idxs = (0 until rowsToShow).map { i ->
                ((data.size - 1) * (i.toFloat() / (rowsToShow - 1).toFloat())).toInt()
            }
            idxs.mapNotNull { data.getOrNull(it) }
        }

        val columns = 1 + samplePoints.size
        val tableWidth = right - left
        val colWidth = tableWidth / columns.toFloat()
        val headerH = 18f
        val rowH = 16f

        // Header background
        canvas.drawRect(left, top, right, top + headerH, headerPaint)
        canvas.drawRect(left, top, right, top + headerH, borderPaint)

        // Header labels
        canvas.drawText("value", left + 4f, top + 12.5f, headerTextPaint)
        samplePoints.forEachIndexed { idx, row ->
            val x = left + (idx + 1) * colWidth + 4f
            val label = (row["Time"] ?: row["Date"] ?: "").take(10)
            canvas.drawText(label, x, top + 12.5f, headerTextPaint)
        }

        // Body rows: one row per field (web-style), columns = sampled timestamps.
        fields.forEachIndexed { rowIndex, field ->
            val y0 = top + headerH + rowIndex * rowH
            val y1 = y0 + rowH
            canvas.drawRect(left, y0, right, y1, borderPaint)

            canvas.drawText(field.take(14), left + 4f, y0 + 11.5f, cellTextPaint)

            samplePoints.forEachIndexed { idx, row ->
                val x = left + (idx + 1) * colWidth + 4f
                val v = row[field]?.toDoubleOrNull()
                val txt = if (v == null) "-" else formatNumber(v)
                canvas.drawText(txt, x, y0 + 11.5f, cellTextPaint)
            }
        }

        // Footnote hint when we sample
        if (data.size > rowsToShow) {
            canvas.drawText("Table shows sampled points", left, top + headerH + fields.size * rowH + 14f, metaPaint)
        }
    }
}
