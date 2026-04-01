package com.ithing.mobile.data.remote.dto.reports

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class ReportScheduleListResponseDto(
    val data: ReportScheduleListDataDto
)

@Serializable
data class ReportScheduleListDataDto(
    val totalCount: Int,
    val totalPages: Int,
    val list: List<ReportScheduleDto>,
    val currentPage: Int,
    val pageSize: Int
)

@Serializable
data class ReportScheduleDto(
    val id: String,
    val deviceId: String,
    val customerId: String,
    @SerialName("oem")
    val oemId: String,
    val excelConfig: ExcelConfigDto? = null,
    val delivery: DeliveryDto,
    val createdAt: Long,
    val updatedAt: Long
)

@Serializable
data class ExcelConfigDto(
    val fields: List<String> = emptyList(),
    val filter: Map<String, String> = emptyMap(),
    val fromTimestamp: Long = 0,
    val toTimestamp: Long = 0
)

@Serializable
data class DeliveryDto(
    val email: String,
    val subject: String,
    val body: String,
    val schedule: String
)
