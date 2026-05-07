package com.ithing.mobile.shared.domain.repository

import com.ithing.mobile.shared.domain.model.DeviceMappingFieldOption
import com.ithing.mobile.shared.domain.model.DeviceOwnerDetails
import com.ithing.mobile.shared.domain.model.PdfViewChartConfig
import com.ithing.mobile.shared.domain.model.ReportDataRequest
import com.ithing.mobile.shared.domain.model.ReportSchedulePage

interface ReportsRepository {
    suspend fun getReportSchedules(deviceId: String?, page: Int, pageSize: Int): Result<ReportSchedulePage>
    suspend fun getDeviceMappingFields(deviceId: String): Result<List<DeviceMappingFieldOption>>
    suspend fun getDeviceOwnerDetails(deviceId: String): Result<DeviceOwnerDetails>

    suspend fun createScheduleReport(
        deviceId: String,
        customerId: String,
        oemId: String,
        email: String,
        subject: String,
        body: String,
        schedule: String,
        fields: List<String>
    ): Result<Unit>

    suspend fun deleteScheduleReport(id: String): Result<Unit>

    suspend fun generateSummaryReport(
        deviceId: String,
        customerId: String,
        oemId: String,
        email: String,
        subject: String,
        body: String,
        fromTimestamp: Long,
        toTimestamp: Long,
        fields: List<String>
    ): Result<String?>

    suspend fun saveAnalyticsView(
        deviceId: String,
        customerId: String,
        oemId: String,
        userId: String,
        charts: List<PdfViewChartConfig>
    ): Result<Unit>

    suspend fun requestAnalyticsPdfData(
        requests: List<ReportDataRequest>
    ): Result<List<List<Map<String, String>>>>

    suspend fun exportExceptionReportUrl(): Result<String>
}
