package com.ithing.mobile.data.repository

import com.ithing.mobile.data.remote.dto.dashboard.DashboardEventLogDto
import com.ithing.mobile.data.remote.dto.reports.DeviceMappingFieldDto
import com.ithing.mobile.data.remote.dto.reports.DeviceCanAddressDto
import com.ithing.mobile.data.remote.dto.reports.DeviceMappingPayloadDto
import com.ithing.mobile.domain.model.DashboardWidget
import com.ithing.mobile.domain.model.DashboardWidgetPoint
import com.ithing.mobile.domain.model.DashboardWidgetSeries
import com.ithing.mobile.domain.model.DashboardWidgetSource
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.mock

class DashboardTelemetryTest {
    private val repository = DashboardRepositoryImpl(mock(), mock())
    private val mapping = DeviceMappingPayloadDto(
        mapping = listOf(
            DeviceMappingFieldDto(registerName = "Temperature", dName = listOf("D1")),
            DeviceMappingFieldDto(registerName = "Status", dName = listOf("D2"))
        )
    )
    private val widget = DashboardWidget(
        id = "overview",
        title = "Overview",
        type = "metric",
        deviceId = "device-a",
        dashboardName = "Chiller",
        sources = listOf(DashboardWidgetSource(fields = listOf("Temperature", "Status")))
    )

    @Test
    fun `latest reading for each field survives separate out of order records`() = runTest {
        val result = repository.applyDashboardTelemetry(
            widgets = listOf(widget),
            mappingPayload = mapping,
            latestLogs = listOf(
                DashboardEventLogDto(timeStamp = 300_000L, data = mapOf("D2" to "0")),
                DashboardEventLogDto(timeStamp = 1_000L, data = mapOf("D1" to "18")),
                DashboardEventLogDto(timeStamp = 150_000L, data = mapOf("D1" to "20"))
            ),
            chartLogs = emptyList()
        ).getOrThrow()

        assertEquals(mapOf("Temperature" to 20.0, "Status" to 0.0), result.widgets.single().valuesByField)
        assertEquals(20.0, result.widgets.single().currentValue)
        assertEquals(300_000L, result.lastUpdatedAt)
    }

    @Test
    fun `partial and empty refresh retain readings and chart history`() = runTest {
        val series = listOf(DashboardWidgetSeries("Temperature", listOf(DashboardWidgetPoint(1L, "start", 18.0))))
        val previous = widget.copy(
            type = "charts",
            valuesByField = mapOf("Temperature" to 18.0, "Status" to 1.0),
            currentValue = 18.0,
            chartSeries = series
        )
        val updated = repository.applyDashboardTelemetry(
            widgets = listOf(previous),
            mappingPayload = mapping,
            latestLogs = listOf(DashboardEventLogDto(timeStamp = 200_000L, data = mapOf("D2" to "0"))),
            chartLogs = emptyList()
        ).getOrThrow().widgets.single()

        assertEquals(mapOf("Temperature" to 18.0, "Status" to 0.0), updated.valuesByField)
        assertEquals(series, updated.chartSeries)

        val empty = repository.applyDashboardTelemetry(listOf(updated), mapping, emptyList(), emptyList()).getOrThrow()
        assertEquals(updated, empty.widgets.single())
    }

    @Test
    fun `missing CAN frame does not fabricate a zero status`() = runTest {
        val canMapping = DeviceMappingPayloadDto(mapping = listOf(
            DeviceMappingFieldDto(
                registerName = "Status",
                canAddress = listOf(DeviceCanAddressDto(c = "C1", b = "B1"))
            )
        ))
        val previous = widget.copy(valuesByField = mapOf("Status" to 1.0))
        val result = repository.applyDashboardTelemetry(
            listOf(previous), canMapping,
            listOf(DashboardEventLogDto(timeStamp = 1L, data = mapOf("Comm Type" to "CAN"))),
            emptyList()
        ).getOrThrow()

        assertEquals(1.0, result.widgets.single().valuesByField["Status"])
        assertEquals(null, result.lastUpdatedAt)
    }

    @Test
    fun `partial multi register reading does not overwrite previous value`() = runTest {
        val multiRegisterMapping = DeviceMappingPayloadDto(mapping = listOf(
            DeviceMappingFieldDto(registerName = "Temperature", dName = listOf("D1", "D2"))
        ))
        val result = repository.applyDashboardTelemetry(
            listOf(widget.copy(valuesByField = mapOf("Temperature" to 18.0))),
            multiRegisterMapping,
            listOf(DashboardEventLogDto(timeStamp = 1L, data = mapOf("D1" to "0"))),
            emptyList()
        ).getOrThrow()

        assertEquals(18.0, result.widgets.single().currentValue)
    }

    @Test
    fun `initial missing fields remain unknown`() = runTest {
        val result = repository.applyDashboardTelemetry(listOf(widget), mapping, emptyList(), emptyList()).getOrThrow()
        assertTrue(result.widgets.single().valuesByField.isEmpty())
        assertEquals(null, result.widgets.single().currentValue)
    }
}
