package com.ithing.mobile.data.remote.api

import com.ithing.mobile.data.remote.dto.dashboard.ListRequestDto
import com.ithing.mobile.data.remote.dto.reports.DeviceMappingRequestDto
import com.ithing.mobile.data.remote.dto.reports.DeviceMappingResponseDto
import com.ithing.mobile.data.remote.dto.reports.DeviceOwnerDetailsRequestDto
import com.ithing.mobile.data.remote.dto.reports.DeviceOwnerDetailsResponseDto
import com.ithing.mobile.data.remote.dto.reports.ApiResponseDto
import com.ithing.mobile.data.remote.dto.reports.AlarmNotificationExportResponseDto
import com.ithing.mobile.data.remote.dto.reports.GenerateExcelPayloadDto
import com.ithing.mobile.data.remote.dto.reports.PdfReportConfigRequestDto
import com.ithing.mobile.data.remote.dto.reports.ReportScheduleDeleteRequestDto
import com.ithing.mobile.data.remote.dto.reports.ReportCreateRequestDto
import com.ithing.mobile.data.remote.dto.reports.ReportGetDataRequestDto
import com.ithing.mobile.data.remote.dto.reports.ReportScheduleListResponseDto
import kotlinx.serialization.json.JsonElement
import retrofit2.http.GET
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface ReportsApi {

    @POST("report-get-schedule-list")
    suspend fun getReportSchedules(
        @Body request: ListRequestDto
    ): ReportScheduleListResponseDto

    @POST("device-mapping-get")
    suspend fun getDeviceMapping(
        @Body request: DeviceMappingRequestDto
    ): DeviceMappingResponseDto

    @POST("device-owner-details")
    suspend fun getDeviceOwnerDetails(
        @Body request: DeviceOwnerDetailsRequestDto
    ): DeviceOwnerDetailsResponseDto

    @POST("report-create-schedule")
    suspend fun createReportSchedule(
        @Body request: ReportCreateRequestDto
    ): ApiResponseDto<JsonElement>

    @POST("report-delete-schedule")
    suspend fun deleteReportSchedule(
        @Body request: ReportScheduleDeleteRequestDto
    ): ApiResponseDto<JsonElement>

    @POST("report-generate-excel")
    suspend fun generateExcelReport(
        @Body request: ReportCreateRequestDto
    ): ApiResponseDto<GenerateExcelPayloadDto>

    @POST("pdf-report-create-config")
    suspend fun createPdfReportConfig(
        @Body request: PdfReportConfigRequestDto
    ): ApiResponseDto<JsonElement>

    @POST("report-get-data")
    suspend fun getReportData(
        @Body request: List<ReportGetDataRequestDto>
    ): ApiResponseDto<JsonElement>

    @GET
    suspend fun exportAlarmNotifications(
        @Url url: String
    ): AlarmNotificationExportResponseDto
}
