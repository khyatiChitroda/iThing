package com.ithing.mobile.shared.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ReportSchedulePage(
    val totalCount: Int,
    val totalPages: Int,
    val schedules: List<ReportSchedule>,
    val currentPage: Int,
    val pageSize: Int
)

@Serializable
data class ReportSchedule(
    val id: String,
    val deviceId: String,
    val customerId: String,
    val oemId: String,
    val fields: List<String>,
    val email: String,
    val subject: String,
    val body: String,
    val schedule: String,
    val createdAt: Long,
    val updatedAt: Long
)
