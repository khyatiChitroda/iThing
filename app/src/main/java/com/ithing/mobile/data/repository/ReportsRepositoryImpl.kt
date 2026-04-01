package com.ithing.mobile.data.repository

import com.ithing.mobile.data.remote.api.ReportsApi
import com.ithing.mobile.data.remote.dto.dashboard.ListRequestDto
import com.ithing.mobile.data.remote.dto.reports.DeviceOwnerDetailsRequestDto
import com.ithing.mobile.data.remote.dto.reports.ReportScheduleDto
import com.ithing.mobile.domain.model.DeviceOwnerDetails
import com.ithing.mobile.domain.model.ReportSchedule
import com.ithing.mobile.domain.model.ReportSchedulePage
import com.ithing.mobile.domain.repository.ReportsRepository
import javax.inject.Inject

class ReportsRepositoryImpl @Inject constructor(
    private val reportsApi: ReportsApi
) : ReportsRepository {

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
}
