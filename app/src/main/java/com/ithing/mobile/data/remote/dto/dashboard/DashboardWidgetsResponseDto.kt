package com.ithing.mobile.data.remote.dto.dashboard

import kotlinx.serialization.Serializable

@Serializable
data class DashboardWidgetsResponseDto(
    val data: DashboardWidgetsDataDto
)

@Serializable
data class DashboardWidgetsDataDto(
    val totalCount: Int,
    val totalPages: Int,
    val list: List<DashboardWidgetDto>,
    val currentPage: Int,
    val pageSize: Int
)