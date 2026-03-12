package com.ithing.mobile.data.remote.dto.dashboard

import kotlinx.serialization.Serializable

@Serializable
data class DeviceListResponseDto(
    val data: DeviceListDataDto
)

@Serializable
data class DeviceListDataDto(
    val totalCount: Int,
    val totalPages: Int,
    val list: List<DeviceDto>,
    val currentPage: Int,
    val pageSize: Int
)