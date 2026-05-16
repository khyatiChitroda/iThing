package com.ithing.mobile.presentation.feature.reports

import androidx.activity.result.launch
import androidx.compose.animation.core.copy
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.concurrent.atomics.update

enum class AnalyticsChartType(val label: String) {
    LINE("Line Chart"),
    BAR("Bar Chart"),
    HEAT_MAP("Heat map"),
    AREA("Area Chart")
}

enum class AnalyticsFrequency(val label: String) {
    HOUR_1("1 hour"),
    HOUR_2("2 hour"),
    HOUR_3("3 hour"),
    HOUR_5("5 hour")
}

enum class ScheduleDeliveryFrequency(val label: String) {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    BI_WEEKLY("Bi-weekly"),
    MONTHLY("Monthly")
}

enum class AnalyticsDatePreset(val label: String) {
    TODAY("Today"),
    YESTERDAY("Yesterday"),
    THIS_WEEK("This Week"),
    LAST_WEEK("Last Week"),
    LAST_5_DAYS("Last 5 Days"),
    LAST_10_DAYS("Last 10 Days"),
    LAST_15_DAYS("Last 15 Days"),
    CUSTOM("Custom")
}

data class AnalyticsChartConfigUi(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "Analytic Report",
    val chartType: AnalyticsChartType? = null,
    val selectedFields: List<String> = emptyList(),
    val frequency: AnalyticsFrequency? = null,
    val titleError: String? = null,
    val chartTypeError: String? = null,
    val fieldError: String? = null,
    val frequencyError: String? = null
)

private val analyticsDateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

fun analyticsDateRangeLabel(startMillis: Long?, endMillis: Long?): String {
    if (startMillis == null || endMillis == null) return ""
    return "${analyticsDateFormatter.format(Date(startMillis))} to ${analyticsDateFormatter.format(Date(endMillis))}"
}

fun analyticsRangeForPreset(preset: AnalyticsDatePreset, nowMillis: Long = System.currentTimeMillis()): Pair<Long, Long>? {
    val calendar = Calendar.getInstance().apply { timeInMillis = nowMillis }
    return when (preset) {
        AnalyticsDatePreset.TODAY -> startAndEndOfDay(calendar)
        AnalyticsDatePreset.YESTERDAY -> {
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            startAndEndOfDay(calendar)
        }

        AnalyticsDatePreset.THIS_WEEK -> {
            val start = calendar.clone() as Calendar
            start.firstDayOfWeek = Calendar.SUNDAY
            start.set(Calendar.DAY_OF_WEEK, start.firstDayOfWeek)
            start.set(Calendar.HOUR_OF_DAY, 0)
            start.set(Calendar.MINUTE, 0)
            start.set(Calendar.SECOND, 0)
            start.set(Calendar.MILLISECOND, 0)

            val end = calendar.clone() as Calendar
            end.set(Calendar.HOUR_OF_DAY, 23)
            end.set(Calendar.MINUTE, 59)
            end.set(Calendar.SECOND, 59)
            end.set(Calendar.MILLISECOND, 999)
            start.timeInMillis to end.timeInMillis
        }

        AnalyticsDatePreset.LAST_WEEK -> {
            val start = calendar.clone() as Calendar
            start.firstDayOfWeek = Calendar.SUNDAY
            start.set(Calendar.DAY_OF_WEEK, start.firstDayOfWeek)
            start.add(Calendar.WEEK_OF_YEAR, -1)
            start.set(Calendar.HOUR_OF_DAY, 0)
            start.set(Calendar.MINUTE, 0)
            start.set(Calendar.SECOND, 0)
            start.set(Calendar.MILLISECOND, 0)

            val end = start.clone() as Calendar
            end.add(Calendar.DAY_OF_YEAR, 6)
            end.set(Calendar.HOUR_OF_DAY, 23)
            end.set(Calendar.MINUTE, 59)
            end.set(Calendar.SECOND, 59)
            end.set(Calendar.MILLISECOND, 999)
            start.timeInMillis to end.timeInMillis
        }

        AnalyticsDatePreset.LAST_5_DAYS -> rollingDaysRange(calendar, 5)
        AnalyticsDatePreset.LAST_10_DAYS -> rollingDaysRange(calendar, 10)
        AnalyticsDatePreset.LAST_15_DAYS -> rollingDaysRange(calendar, 15)
        AnalyticsDatePreset.CUSTOM -> null
    }
}

fun analyticsIsRangeWithin15Days(startMillis: Long, endMillis: Long): Boolean {
    if (startMillis > endMillis) return false
    val millisInDay = 24L * 60 * 60 * 1000
    val inclusiveDays = ((endMillis - startMillis) / millisInDay) + 1
    // Web app allows up to 45 days for analytics reports.
    return inclusiveDays <= 45
}

fun analyticsNormalizeStartOfDay(millis: Long): Long {
    return Calendar.getInstance().apply {
        timeInMillis = millis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

fun analyticsNormalizeEndOfDay(millis: Long): Long {
    return Calendar.getInstance().apply {
        timeInMillis = millis
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }.timeInMillis
}

private fun startAndEndOfDay(source: Calendar): Pair<Long, Long> {
    val start = source.clone() as Calendar
    start.set(Calendar.HOUR_OF_DAY, 0)
    start.set(Calendar.MINUTE, 0)
    start.set(Calendar.SECOND, 0)
    start.set(Calendar.MILLISECOND, 0)

    val end = source.clone() as Calendar
    end.set(Calendar.HOUR_OF_DAY, 23)
    end.set(Calendar.MINUTE, 59)
    end.set(Calendar.SECOND, 59)
    end.set(Calendar.MILLISECOND, 999)
    return start.timeInMillis to end.timeInMillis
}

private fun rollingDaysRange(source: Calendar, days: Int): Pair<Long, Long> {
    val end = source.clone() as Calendar
    end.set(Calendar.HOUR_OF_DAY, 23)
    end.set(Calendar.MINUTE, 59)
    end.set(Calendar.SECOND, 59)
    end.set(Calendar.MILLISECOND, 999)

    val start = source.clone() as Calendar
    start.add(Calendar.DAY_OF_YEAR, -(days - 1))
    start.set(Calendar.HOUR_OF_DAY, 0)
    start.set(Calendar.MINUTE, 0)
    start.set(Calendar.SECOND, 0)
    start.set(Calendar.MILLISECOND, 0)
    return start.timeInMillis to end.timeInMillis
}
