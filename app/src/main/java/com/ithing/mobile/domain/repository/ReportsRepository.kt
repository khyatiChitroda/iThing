package com.ithing.mobile.domain.repository

import com.ithing.mobile.domain.model.DeviceOwnerDetails
import com.ithing.mobile.domain.model.ReportSchedulePage

interface ReportsRepository {
    suspend fun getReportSchedules(deviceId: String?, page: Int, pageSize: Int): Result<ReportSchedulePage>
    suspend fun getDeviceMappingFields(deviceId: String): Result<List<String>>
    suspend fun getDeviceOwnerDetails(deviceId: String): Result<DeviceOwnerDetails>
}
