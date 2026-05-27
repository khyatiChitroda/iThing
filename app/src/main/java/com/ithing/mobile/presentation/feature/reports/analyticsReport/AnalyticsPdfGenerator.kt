package com.ithing.mobile.presentation.feature.reports.analyticsReport

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfDocument
import android.view.View
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal object AnalyticsPdfGenerator {

    fun generateAndSave(
        context: Context,
        deviceId: String,
        chartRows: List<AnalyticsChartConfigUi>,
        dataSets: List<List<Map<String, String>>>,
        fromMillis: Long,
        toMillis: Long,
        customerName: String,
        machineName: String?,
        oemLogoUrl: String?,
        fromLabel: String,
        toLabel: String
    ): File {
        val reportsDir = context.getExternalFilesDir("reports") ?: File(context.filesDir, "reports")
        if (!reportsDir.exists()) reportsDir.mkdirs()

        val now = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(Date())
        val file = File(reportsDir, "${deviceId}_summary_${now}.pdf")

        val pageWidth = 800
        val pageHeight = 1000
        val margin = 25f
        val sectionGap = 8f

        val document = PdfDocument()
        try {
            var pageNumber = 1
            var page = document.startPage(
                PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            )
            var canvas = page.canvas

            fun drawOffscreenViewAtY(view: View, y: Float): Float {
                val wSpec = View.MeasureSpec.makeMeasureSpec(pageWidth, View.MeasureSpec.EXACTLY)
                val hSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                view.measure(wSpec, hSpec)
                view.layout(0, 0, view.measuredWidth, view.measuredHeight)
                canvas.save()
                canvas.translate(0f, y)
                view.draw(canvas)
                canvas.restore()
                return view.measuredHeight.toFloat()
            }

            fun startNewPage() {
                document.finishPage(page)
                pageNumber += 1
                page = document.startPage(
                    PdfDocument.PageInfo.Builder(
                        pageWidth,
                        pageHeight,
                        pageNumber
                    ).create()
                )
                canvas = page.canvas
            }

            val headerView = AnalyticsReportHeaderView(context).apply {
                val oemBitmap = oemLogoUrl?.let { fetchBitmap(it) }
                bind(
                    pageWidthPx = pageWidth.toFloat(),
                    marginPx = margin,
                    customerName = customerName,
                    machineName = machineName,
                    deviceId = deviceId,
                    fromLabel = fromLabel,
                    toLabel = toLabel,
                    ithingLogo = null,
                    oemLogo = oemBitmap
                )
            }

            var currentY = drawOffscreenViewAtY(headerView, 0f) + 2f

            chartRows.forEachIndexed { index, row ->
                val data = dataSets.getOrNull(index).orEmpty()
                val fields = row.selectedFields.takeIf { it.isNotEmpty() }.orEmpty()

                val sectionView = AnalyticsReportSectionView(context).apply {
                    bind(
                        pageWidthPx = pageWidth,
                        marginPx = margin,
                        title = row.title,
                        chartType = row.chartType,
                        fields = fields,
                        data = data
                    )
                }

                val wSpec = View.MeasureSpec.makeMeasureSpec(pageWidth, View.MeasureSpec.EXACTLY)
                val hSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                sectionView.measure(wSpec, hSpec)
                val sectionH = sectionView.measuredHeight.toFloat()

                if (currentY + sectionH > pageHeight - margin) {
                    startNewPage()
                    currentY = margin
                }

                currentY += drawOffscreenViewAtY(sectionView, currentY) + sectionGap
            }

            document.finishPage(page)
            FileOutputStream(file).use { output -> document.writeTo(output) }
            return file
        } finally {
            document.close()
        }
    }

    private fun fetchBitmap(urlString: String): Bitmap? {
        return runCatching {
            val url = URL(urlString)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 3500
                readTimeout = 3500
                instanceFollowRedirects = true
            }
            conn.connect()
            if (conn.responseCode !in 200..299) return@runCatching null
            BufferedInputStream(conn.inputStream).use { stream -> BitmapFactory.decodeStream(stream) }
        }.getOrNull()
    }
}
