package com.ithing.mobile.data.remote.dto.dashboard

import kotlinx.serialization.Serializable

@Serializable
data class DashboardWidgetsRequestDto(
    val customer: String,
    val pagination: PaginationDto = PaginationDto()
)

@Serializable
data class PaginationDto(
    val page: Int = 1,
    val pageSize: Int = -1,
    val sort: String = "asc",
    val filter: Map<String, String> = emptyMap()
)