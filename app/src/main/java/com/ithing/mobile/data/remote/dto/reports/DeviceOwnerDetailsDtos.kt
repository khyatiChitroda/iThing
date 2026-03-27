package com.ithing.mobile.data.remote.dto.reports

import com.ithing.mobile.data.remote.dto.dashboard.CustomerDto
import com.ithing.mobile.data.remote.dto.dashboard.DeviceDto
import com.ithing.mobile.data.remote.dto.dashboard.OemDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeviceOwnerDetailsRequestDto(
    @SerialName("id")
    val id: String
)

@Serializable
data class DeviceOwnerDetailsResponseDto(
    val data: DeviceOwnerDetailsDataDto
)

@Serializable
data class DeviceOwnerDetailsDataDto(
    val success: Boolean,
    val message: String,
    val data: DeviceOwnerDetailsPayloadDto? = null
)

@Serializable
data class DeviceOwnerDetailsPayloadDto(
    val device: DeviceDto,
    val customer: CustomerDto,
    val oem: OemDto
)
