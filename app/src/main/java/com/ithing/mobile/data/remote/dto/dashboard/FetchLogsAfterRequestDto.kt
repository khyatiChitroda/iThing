package com.ithing.mobile.data.remote.dto.dashboard

import kotlinx.serialization.Serializable

@Serializable
data class FetchLogsAfterRequestDto(
    val device: String,
    val timestamp: Long,
    val limit: Int
)

