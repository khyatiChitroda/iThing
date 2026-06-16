package com.ithing.mobile.presentation.feature.reports.analyticsReport

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.runtime.rememberCoroutineScope
import com.ithing.mobile.presentation.theme.White
import com.ithing.mobile.presentation.theme.NavyBlue
import com.ithing.mobile.presentation.theme.MutedText
import com.ithing.mobile.presentation.theme.BorderColor
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun AnalyticsReportDatePickerDialog(
    selectedPreset: AnalyticsDatePreset,
    startMillis: Long?,
    endMillis: Long?,
    onPresetSelected: (AnalyticsDatePreset) -> Unit,
    onCustomClick: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = White)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Select Time Span",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF52637E)
                )

                AnalyticsDatePreset.values().forEach { preset ->
                    val isSelected = preset == selectedPreset
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (preset == AnalyticsDatePreset.CUSTOM) {
                                    onCustomClick()
                                } else {
                                    onPresetSelected(preset)
                                }
                            },
                        color = if (isSelected) Color(0xFFF1F5FB) else Color.Transparent,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = preset.label,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = Color(0xFF233A69)
                        )
                    }
                }

                Text(
                    text = analyticsDateRangeLabel(startMillis, endMillis),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF5A6880)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0xFFE4EBF4))
                )

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Done")
                }
            }
        }
    }
}

@Composable
fun AnalyticsCustomDateRangeDialog(
    startMillis: Long?,
    endMillis: Long?,
    onDismiss: () -> Unit,
    onConfirm: (Long, Long) -> Unit
) {
    val zoneId = remember { ZoneId.systemDefault() }
    var startDate by remember { mutableStateOf(startMillis?.toLocalDate(zoneId)) }
    var endDate by remember { mutableStateOf(endMillis?.toLocalDate(zoneId)) }

    val todayMonth = remember { YearMonth.now() }
    val minMonth = remember { YearMonth.of(1970, 1) }
    val maxMonth = remember { YearMonth.of(2100, 12) }
    val calendarState = rememberCalendarState(
        startMonth = minMonth,
        endMonth = maxMonth,
        firstVisibleMonth = (startDate?.let(YearMonth::from) ?: todayMonth),
        firstDayOfWeek = DayOfWeek.SUNDAY
    )
    val coroutineScope = rememberCoroutineScope()

    val visibleMonth by remember {
        derivedStateOf { calendarState.firstVisibleMonth.yearMonth }
    }

    var showMonthYearPicker by remember { mutableStateOf(false) }
    val locale = LocalLocale.current.platformLocale

    val rangeText = remember(startDate, endDate, locale) {
        formatRangeLabel(startDate, endDate, locale)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(0.94f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = White)
        ) {
            Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp)) {
                Text(
                    text = "Select dates",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MutedText
                )
                DividerLine()
                Text(
                    text = rangeText,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = NavyBlue,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                DividerLine()

                MonthNavHeader(
                    month = visibleMonth,
                    onPrev = {
                        coroutineScope.launch {
                            calendarState.scrollToMonth(visibleMonth.minusMonths(1))
                        }
                    },
                    onNext = {
                        coroutineScope.launch {
                            calendarState.scrollToMonth(visibleMonth.plusMonths(1))
                        }
                    },
                    onTitleClick = { showMonthYearPicker = true }
                )
                DividerLine()

                WeekDaysRow()

                // Fixed-height container so UI doesn't jump between 5/6-week months.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                        .padding(top = 4.dp)
                ) {
                    HorizontalCalendar(
                        state = calendarState,
                        userScrollEnabled = false,
                        monthHeader = {},
                        dayContent = { day ->
                            DayCell(
                                day = day,
                                startDate = startDate,
                                endDate = endDate,
                                onClick = { clicked ->
                                    if (clicked.position != DayPosition.MonthDate) return@DayCell
                                    val date = clicked.date
                                    when {
                                        startDate == null -> startDate = date
                                        endDate == null -> {
                                            val start = startDate ?: date
                                            if (date.isBefore(start)) {
                                                startDate = date
                                            } else {
                                                endDate = date
                                            }
                                        }

                                        else -> {
                                            startDate = date
                                            endDate = null
                                        }
                                    }
                                }
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            val start = startDate
                            val end = endDate
                            if (start != null && end != null) {
                                onConfirm(start.toEpochMillis(zoneId), end.toEpochMillis(zoneId))
                            }
                        },
                        enabled = startDate != null && endDate != null,
                        colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
                    ) {
                        Text("Apply")
                    }
                }
            }
        }
    }

    if (showMonthYearPicker) {
        MonthYearPickerDialog(
            initialMonth = visibleMonth,
            onDismiss = { showMonthYearPicker = false },
            onMonthSelected = { selectedMonth ->
                showMonthYearPicker = false
                coroutineScope.launch { calendarState.scrollToMonth(selectedMonth) }
            }
        )
    }
}

@Composable
private fun DividerLine() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .height(1.dp)
            .background(BorderColor)
    )
}

@Composable
private fun MonthNavHeader(
    month: YearMonth,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onTitleClick: () -> Unit
) {
    val locale = LocalLocale.current.platformLocale

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPrev) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Previous month",
                tint = NavyBlue
            )
        }

        Row(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onTitleClick),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${
                    month.month.getDisplayName(
                        TextStyle.FULL,
                        locale
                    )
                } ${month.year}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = NavyBlue,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        IconButton(onClick = onNext) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Next month",
                tint = NavyBlue
            )
        }
    }
}

@Composable
private fun WeekDaysRow() {
    val locale = LocalLocale.current.platformLocale

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val days = listOf(
            DayOfWeek.SUNDAY,
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY,
            DayOfWeek.SATURDAY
        )
        days.forEach { day ->
            Text(
                text = day.getDisplayName(TextStyle.NARROW, locale),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleSmall,
                color = MutedText
            )
        }
    }
}

@Composable
private fun DayCell(
    day: CalendarDay,
    startDate: LocalDate?,
    endDate: LocalDate?,
    onClick: (CalendarDay) -> Unit
) {
    val date = day.date
    val isInMonth = day.position == DayPosition.MonthDate
    val isStart = startDate != null && date == startDate
    val isEnd = endDate != null && date == endDate
    val isInRange =
        startDate != null && endDate != null && (date.isAfter(startDate) || date == startDate) && (date.isBefore(
            endDate
        ) || date == endDate)

    val bg = when {
        !isInMonth -> Color.Transparent
        isStart || isEnd -> NavyBlue
        isInRange -> NavyBlue.copy(alpha = 0.18f)
        else -> Color.Transparent
    }
    val textColor = when {
        !isInMonth -> MutedText.copy(alpha = 0.3f)
        isStart || isEnd -> White
        else -> NavyBlue
    }

    Box(
        modifier = Modifier
            .size(44.dp)
            .background(bg, shape = RoundedCornerShape(22.dp))
            .clickable(enabled = isInMonth) { onClick(day) },
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = if (isStart || isEnd) FontWeight.SemiBold else FontWeight.Normal,
            color = textColor
        )
    }
}

@Composable
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
private fun MonthYearPickerDialog(
    initialMonth: YearMonth,
    onDismiss: () -> Unit,
    onMonthSelected: (YearMonth) -> Unit
) {
    var selectedYear by remember(initialMonth) { mutableStateOf(initialMonth.year) }
    var selectedMonthValue by remember(initialMonth) { mutableStateOf(initialMonth.monthValue) }
    var yearExpanded by remember { mutableStateOf(false) }
    val locale = LocalLocale.current.platformLocale

    val monthFormatter = remember(locale) { DateTimeFormatter.ofPattern("MMM", locale) }
    val years = remember { (1970..2100).toList() }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(0.94f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = White)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Select month",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = NavyBlue
                )

                ExposedDropdownMenuBox(
                    expanded = yearExpanded,
                    onExpandedChange = { yearExpanded = !yearExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedYear.toString(),
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(
                                type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                                enabled = true
                            ),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = yearExpanded)
                        }
                    )
                    ExposedDropdownMenu(
                        expanded = yearExpanded,
                        onDismissRequest = { yearExpanded = false },
                        modifier = Modifier.heightIn(max = 320.dp)
                    ) {
                        years.forEach { year ->
                            DropdownMenuItem(
                                text = { Text(year.toString()) },
                                onClick = {
                                    selectedYear = year
                                    yearExpanded = false
                                }
                            )
                        }
                    }
                }

                // 4 rows x 3 cols month grid, labels JAN/FEB...
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (rowStart in 1..10 step 3) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (m in rowStart until (rowStart + 3)) {
                                val ym = YearMonth.of(selectedYear, m)
                                val label = ym.atDay(1).format(monthFormatter)
                                    .uppercase(locale)
                                val isSelected =
                                    (selectedYear == ym.year && selectedMonthValue == ym.monthValue)
                                val modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)

                                if (isSelected) {
                                    Button(
                                        onClick = {
                                            selectedMonthValue = m
                                        },
                                        modifier = modifier,
                                        colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
                                    ) {
                                        Text(
                                            label,
                                            color = White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Clip
                                        )
                                    }
                                } else {
                                    OutlinedButton(
                                        onClick = {
                                            selectedMonthValue = m
                                        },
                                        modifier = modifier
                                    ) {
                                        Text(
                                            label,
                                            color = NavyBlue,
                                            maxLines = 1,
                                            overflow = TextOverflow.Clip
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            onMonthSelected(YearMonth.of(selectedYear, selectedMonthValue))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
                    ) {
                        Text("Done")
                    }
                }
            }
        }
    }
}

private fun Long.toLocalDate(zoneId: ZoneId): LocalDate {
    return Instant.ofEpochMilli(this).atZone(zoneId).toLocalDate()
}

private fun LocalDate.toEpochMillis(zoneId: ZoneId): Long {
    return this.atStartOfDay(zoneId).toInstant().toEpochMilli()
}

private fun formatRangeLabel(start: LocalDate?, end: LocalDate?, locale: Locale): String {
    if (start == null && end == null) return ""
    val fmt = DateTimeFormatter.ofPattern("MMM dd, yyyy", locale)
    return when {
        start != null && end != null -> "${start.format(fmt)} – ${end.format(fmt)}"
        start != null -> start.format(fmt)
        else -> end?.format(fmt).orEmpty()
    }
}
