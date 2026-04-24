package com.ithing.mobile.data.remote.dto.dashboard

import kotlinx.serialization.Serializable

@Serializable
data class FetchEventsRequestDto(
    val device: String,
    val lastTimeStamp: Long = 0
)