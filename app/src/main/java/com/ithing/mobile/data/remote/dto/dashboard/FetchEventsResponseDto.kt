package com.ithing.mobile.data.remote.dto.dashboard

import kotlinx.serialization.Serializable

@Serializable
data class FetchEventsResponseDto(
    val data: FetchEventsPayloadDto,
    val token: String? = null
)

@Serializable
data class FetchEventsPayloadDto(
    val hasMore: Boolean = false,
    val logs: List<DashboardEventLogDto> = emptyList(),
    val lastTimeStamp: Long = 0
)

@Serializable
data class DashboardEventLogDto(
    val timeStamp: Long = 0,
    val data: Map<String, String> = emptyMap(),
    val source: String? = null,
    val ttl: Long? = null,
    val device: String? = null
)
