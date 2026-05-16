package com.ithing.mobile.data.remote.dto.reports

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiResponseDto<T>(
    val data: ApiResponseDataDto<T>
)

@Serializable
data class ApiResponseDataDto<T>(
    val success: Boolean,
    val message: String = "",
    val data: T? = null
)

@Serializable
data class ReportCreateRequestDto(
    val deviceId: String,
    val customerId: String,
    @SerialName("oem")
    val oemId: String,
    val excelConfig: ExcelConfigDto,
    val pdfConfig: Map<String, String> = emptyMap(),
    val delivery: DeliveryDto
)

@Serializable
data class GenerateExcelPayloadDto(
    val url: String? = null
)

@Serializable
data class PdfReportConfigRequestDto(
    val deviceId: String,
    val customerId: String,
    @SerialName("oem")
    val oemId: String,
    val userId: String,
    val data: List<PdfChartConfigDto>
)

@Serializable
data class PdfChartConfigDto(
    val title: String,
    val chartType: String,
    val field: List<String>,
    val step: String
)

@Serializable
data class ReportGetDataRequestDto(
    val deviceId: String,
    val from: String,
    val to: String,
    val step: Long,
    val fields: List<String>
)

@Serializable
data class ReportScheduleDeleteRequestDto(
    val id: String
)

@Serializable
data class AlarmNotificationExportResponseDto(
    val url: String? = null
)

@Serializable
data class AlarmNotificationExportEnvelopeDto(
    val data: AlarmNotificationExportResponseDto? = null
)

@Serializable
data class AnalyticsPdfReportRequestDto(
    val deviceId: String,
    val customerName: String,
    val machineName: String? = null,
    val oemLogoUrl: String? = null,
    val fromTimestamp: Long,
    val toTimestamp: Long,
    val fromLabel: String,
    val toLabel: String,
    val charts: List<AnalyticsPdfChartConfigDto>
)

@Serializable
data class AnalyticsPdfChartConfigDto(
    val title: String,
    val chartType: String,
    val fields: List<String>,
    val stepMs: Long
)

@Serializable
data class AnalyticsPdfReportResponseDto(
    val url: String? = null
)
