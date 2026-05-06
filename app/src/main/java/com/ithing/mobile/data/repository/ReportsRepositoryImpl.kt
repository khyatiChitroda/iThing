package com.ithing.mobile.data.repository

import com.ithing.mobile.data.remote.api.ReportsApi
import com.ithing.mobile.data.remote.dto.dashboard.ListRequestDto
import com.ithing.mobile.data.remote.dto.reports.DeviceMappingRequestDto
import com.ithing.mobile.data.remote.dto.reports.DeviceOwnerDetailsRequestDto
import com.ithing.mobile.data.remote.dto.reports.PdfChartConfigDto
import com.ithing.mobile.data.remote.dto.reports.PdfReportConfigRequestDto
import com.ithing.mobile.data.remote.dto.reports.ReportCreateRequestDto
import com.ithing.mobile.data.remote.dto.reports.ReportGetDataRequestDto
import com.ithing.mobile.data.remote.dto.reports.ReportScheduleDto
import com.ithing.mobile.domain.model.DeviceOwnerDetails
import com.ithing.mobile.domain.model.DeviceMappingFieldOption
import com.ithing.mobile.domain.model.PdfViewChartConfig
import com.ithing.mobile.domain.model.ReportSchedule
import com.ithing.mobile.domain.model.ReportSchedulePage
import com.ithing.mobile.domain.model.ReportDataRequest
import com.ithing.mobile.domain.repository.ReportsRepository
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import javax.inject.Inject

class ReportsRepositoryImpl @Inject constructor(
    private val reportsApi: ReportsApi
) : ReportsRepository {

    private companion object {
        const val ALARM_NOTIFICATION_EXPORT_URL =
            "https://xqs6mwcmgfu4pfazmdzyqswfye0qxvvt.lambda-url.ap-south-1.on.aws/alarm-notification-export"
    }

    override suspend fun getReportSchedules(
        deviceId: String?,
        page: Int,
        pageSize: Int
    ): Result<ReportSchedulePage> = runCatching {
        val response = reportsApi.getReportSchedules(
            ListRequestDto(
                page = page,
                pageSize = pageSize,
                sort = "asc",
                filter = buildMap {
                    if (!deviceId.isNullOrBlank()) {
                        put("deviceId", deviceId)
                    }
                }
            )
        )
        ReportSchedulePage(
            totalCount = response.data.totalCount,
            totalPages = response.data.totalPages,
            schedules = response.data.list.map { it.toDomain() },
            currentPage = response.data.currentPage,
            pageSize = response.data.pageSize
        )
    }

    override suspend fun getDeviceOwnerDetails(deviceId: String): Result<DeviceOwnerDetails> = runCatching {
        val response = reportsApi.getDeviceOwnerDetails(DeviceOwnerDetailsRequestDto(id = deviceId))
        val payload = requireNotNull(response.data.data) {
            response.data.message.ifBlank { "Device owner details not found" }
        }

        DeviceOwnerDetails(
            deviceId = payload.device.id,
            deviceName = payload.device.name,
            machineName = payload.device.machine,
            customerId = payload.customer.id,
            customerName = payload.customer.name,
            oemId = payload.oem.id,
            oemName = payload.oem.name
        )
    }

    override suspend fun getDeviceMappingFields(deviceId: String): Result<List<DeviceMappingFieldOption>> = runCatching {
        val response = reportsApi.getDeviceMapping(DeviceMappingRequestDto(id = deviceId))
        val payload = requireNotNull(response.data.data) {
            response.data.message.ifBlank { "Device mapping not found" }
        }

        val blocked = setOf("machine id", "industry", "device id")
        payload.mapping.mapNotNull { field ->
            val name = field.registerName.trim()
            if (name.isBlank()) return@mapNotNull null
            if (name.lowercase() in blocked) return@mapNotNull null
            val slaveId = field.slaveId?.trim().orEmpty()
            val label = if (slaveId.isBlank()) name else "$name ($slaveId)"
            DeviceMappingFieldOption(value = name, label = label)
        }.distinctBy { it.value }
    }

    private fun ReportScheduleDto.toDomain() = ReportSchedule(
        id = id,
        deviceId = deviceId,
        customerId = customerId,
        oemId = oemId,
        fields = excelConfig?.fields.orEmpty(),
        email = delivery.email,
        subject = delivery.subject,
        body = delivery.body,
        schedule = delivery.schedule,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    override suspend fun createScheduleReport(
        deviceId: String,
        customerId: String,
        oemId: String,
        email: String,
        subject: String,
        body: String,
        schedule: String,
        fields: List<String>
    ): Result<Unit> = runCatching {
        reportsApi.createReportSchedule(
            ReportCreateRequestDto(
                deviceId = deviceId,
                customerId = customerId,
                oemId = oemId,
                excelConfig = com.ithing.mobile.data.remote.dto.reports.ExcelConfigDto(
                    fields = fields,
                    filter = emptyMap(),
                    fromTimestamp = 0,
                    toTimestamp = 0
                ),
                pdfConfig = emptyMap(),
                delivery = com.ithing.mobile.data.remote.dto.reports.DeliveryDto(
                    email = email,
                    subject = subject,
                    body = body,
                    schedule = schedule
                )
            )
        )
        Unit
    }

    override suspend fun deleteScheduleReport(id: String): Result<Unit> = runCatching {
        reportsApi.deleteReportSchedule(
            com.ithing.mobile.data.remote.dto.reports.ReportScheduleDeleteRequestDto(id = id)
        )
        Unit
    }

    override suspend fun generateSummaryReport(
        deviceId: String,
        customerId: String,
        oemId: String,
        email: String,
        subject: String,
        body: String,
        fromTimestamp: Long,
        toTimestamp: Long,
        fields: List<String>
    ): Result<String?> = runCatching {
        val response = reportsApi.generateExcelReport(
            ReportCreateRequestDto(
                deviceId = deviceId,
                customerId = customerId,
                oemId = oemId,
                excelConfig = com.ithing.mobile.data.remote.dto.reports.ExcelConfigDto(
                    fields = fields,
                    filter = emptyMap(),
                    fromTimestamp = fromTimestamp,
                    toTimestamp = toTimestamp
                ),
                pdfConfig = emptyMap(),
                delivery = com.ithing.mobile.data.remote.dto.reports.DeliveryDto(
                    email = email,
                    subject = subject,
                    body = body,
                    schedule = ""
                )
            )
        )
        response.data.data?.url
    }

    override suspend fun saveAnalyticsView(
        deviceId: String,
        customerId: String,
        oemId: String,
        userId: String,
        charts: List<PdfViewChartConfig>
    ): Result<Unit> = runCatching {
        reportsApi.createPdfReportConfig(
            PdfReportConfigRequestDto(
                deviceId = deviceId,
                customerId = customerId,
                oemId = oemId,
                userId = userId,
                data = charts.map {
                    PdfChartConfigDto(
                        title = it.title,
                        chartType = it.chartType,
                        field = it.fields,
                        step = it.step
                    )
                }
            )
        )
        Unit
    }

    override suspend fun requestAnalyticsPdfData(
        requests: List<ReportDataRequest>
    ): Result<List<List<Map<String, String>>>> = runCatching {
        val response = reportsApi.getReportData(
            requests.map {
                ReportGetDataRequestDto(
                    deviceId = it.deviceId,
                    from = it.from,
                    to = it.to,
                    step = it.step,
                    fields = it.fields
                )
            }
        )

        val payload = response.data.data
        require(response.data.success) { response.data.message.ifBlank { "Failed to fetch report data." } }
        val root = requireNotNull(payload) { response.data.message.ifBlank { "Report data not found" } }
        parseReportData(root)
    }

    override suspend fun exportExceptionReportUrl(): Result<String> = runCatching {
        val response = reportsApi.exportAlarmNotifications(ALARM_NOTIFICATION_EXPORT_URL)
        requireNotNull(response.url) { "Export URL not found" }
    }

    private fun parseReportData(element: JsonElement): List<List<Map<String, String>>> {
        // Expected shape (from web): data = List<List<Record<string, any>>>
        val outer = element as? JsonArray ?: return emptyList()
        return outer.map { innerElement ->
            val inner = innerElement as? JsonArray ?: JsonArray(emptyList())
            inner.mapNotNull { rowElement ->
                val row = rowElement as? JsonObject ?: return@mapNotNull null
                row.entries.associate { (key, value) ->
                    key to jsonPrimitiveToString(value)
                }
            }
        }
    }

    private fun jsonPrimitiveToString(value: JsonElement): String {
        return when (value) {
            is JsonNull -> ""
            is JsonPrimitive -> value.contentOrNull ?: ""
            is JsonObject -> value.toString()
            is JsonArray -> value.toString()
            else -> value.toString()
        }
    }
}
