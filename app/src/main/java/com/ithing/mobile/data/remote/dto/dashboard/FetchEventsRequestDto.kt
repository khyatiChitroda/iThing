package com.ithing.mobile.data.remote.dto.dashboard

data class FetchEventsRequestDto(
    val device: String,
    val lastTimeStamp: Long = 0
)