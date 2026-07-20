package com.ithing.mobile.presentation.feature.reports.analyticsReport

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.View
import com.ithing.mobile.R

internal class AnalyticsReportHeaderView(context: Context) : View(context) {

    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 18f
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
    }

    private val boldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(31, 41, 55)
        textSize = 9.5f
        isFakeBoldText = true
    }

    private val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(31, 41, 55)
        textSize = 9.5f
    }

    private var marginPx: Float = 36f
    private var pageWidthPx: Float = 595f

    private var customerName: String = "-"
    private var machineName: String = "-"
    private var deviceId: String = "-"
    private var fromLabel: String = "-"
    private var toLabel: String = "-"

    private var ithingBitmap: Bitmap? = null
    private var oemBitmap: Bitmap? = null

    fun bind(
        pageWidthPx: Float,
        marginPx: Float,
        customerName: String,
        machineName: String?,
        deviceId: String,
        fromLabel: String,
        toLabel: String,
        ithingLogo: Bitmap?,
        oemLogo: Bitmap?
    ) {
        this.pageWidthPx = pageWidthPx
        this.marginPx = marginPx
        this.customerName = customerName.ifBlank { "-" }
        this.machineName = machineName ?: "-"
        this.deviceId = deviceId
        this.fromLabel = fromLabel
        this.toLabel = toLabel
        this.ithingBitmap = ithingLogo
        this.oemBitmap = oemLogo
        requestLayout()
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec).takeIf { it > 0 } ?: pageWidthPx.toInt()

        val logoTop = marginPx
        var bottomY = logoTop
        val leftLogoH = 64f // OEM
        val rightLogoH = 48f // iThing

        val leftBitmap = oemBitmap
        leftBitmap?.let { bmp ->
            bottomY = maxOf(bottomY, logoTop + leftLogoH)
        }

        val rightBitmap = ithingBitmap ?: run {
            val b = runCatching {
                BitmapFactory.decodeResource(context.resources, R.drawable.ithingoemlogo)
            }.getOrNull()
            ithingBitmap = b
            b
        }
        rightBitmap?.let { bmp ->
            bottomY = maxOf(bottomY, logoTop + 4f + rightLogoH)
        }

        val metaTop = bottomY + 18f
        val lineH = 14f
        val titleY = metaTop + 3.6f * lineH
        val height = (titleY + 18f + marginPx).toInt().coerceAtLeast(1)

        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        val logoTop = marginPx
        val leftLogoH = 64f // OEM
        val rightLogoH = 48f // iThing

        oemBitmap?.let { bmp ->
            val width = scaledWidth(bmp, leftLogoH)
            val dst = RectF(marginPx, logoTop, marginPx + width, logoTop + leftLogoH)
            canvas.drawBitmap(bmp, null, dst, null)
        }

        (ithingBitmap ?: runCatching {
            BitmapFactory.decodeResource(context.resources, R.drawable.ithing_logo)
        }.getOrNull())?.let { bmp ->
            ithingBitmap = bmp
            val width = scaledWidth(bmp, rightLogoH)
            val left = pageWidthPx - marginPx - width
            val dst = RectF(left, logoTop + 4f, left + width, logoTop + 4f + rightLogoH)
            canvas.drawBitmap(bmp, null, dst, null)
        }

        val logosBottom = run {
            var bottom = logoTop
            oemBitmap?.let { bottom = maxOf(bottom, logoTop + leftLogoH) }
            ithingBitmap?.let {
                bottom = maxOf(bottom, logoTop + 4f + rightLogoH)
            }
            bottom
        }

        val metaTop = logosBottom + 18f
        val leftX = marginPx
        val rightX = pageWidthPx * 0.58f
        val lineH = 14f

        canvas.drawText("Customer:", leftX, metaTop, boldPaint)
        canvas.drawText(customerName, leftX + 74f, metaTop, valuePaint)

        canvas.drawText("Machine:", leftX, metaTop + lineH, boldPaint)
        canvas.drawText(machineName, leftX + 74f, metaTop + lineH, valuePaint)

        canvas.drawText("Device:", leftX, metaTop + 2 * lineH, boldPaint)
        canvas.drawText(deviceId, leftX + 74f, metaTop + 2 * lineH, valuePaint)

        canvas.drawText("Report:", rightX, metaTop, boldPaint)
        canvas.drawText("From:", rightX, metaTop + lineH, boldPaint)
        canvas.drawText(fromLabel, rightX + 48f, metaTop + lineH, valuePaint)

        canvas.drawText("To:", rightX, metaTop + 2 * lineH, boldPaint)
        canvas.drawText(toLabel, rightX + 48f, metaTop + 2 * lineH, valuePaint)

        val titleY = metaTop + 3.6f * lineH
        canvas.drawText("Performance Report", pageWidthPx / 2f, titleY, titlePaint)
    }

    private fun scaledWidth(bitmap: Bitmap, targetHeight: Float): Float =
        if (bitmap.height <= 0) targetHeight
        else (bitmap.width * (targetHeight / bitmap.height.toFloat())).coerceAtLeast(1f)
}
