package com.ithing.mobile.domain.repository

import com.ithing.mobile.domain.model.DeviceOwnerDetails
import com.ithing.mobile.domain.model.ReportSchedule

interface ReportsRepository {
    suspend fun getReportSchedules(deviceId: String?): Result<List<ReportSchedule>>
    suspend fun getDeviceOwnerDetails(deviceId: String): Result<DeviceOwnerDetails>
}
