package com.ithing.mobile.data.remote.api

import com.ithing.mobile.data.remote.dto.dashboard.ListRequestDto
import com.ithing.mobile.data.remote.dto.reports.DeviceOwnerDetailsRequestDto
import com.ithing.mobile.data.remote.dto.reports.DeviceOwnerDetailsResponseDto
import com.ithing.mobile.data.remote.dto.reports.ReportScheduleListResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

interface ReportsApi {

    @POST("report-get-schedule-list")
    suspend fun getReportSchedules(
        @Body request: ListRequestDto
    ): ReportScheduleListResponseDto

    @POST("device-owner-details")
    suspend fun getDeviceOwnerDetails(
        @Body request: DeviceOwnerDetailsRequestDto
    ): DeviceOwnerDetailsResponseDto
}
