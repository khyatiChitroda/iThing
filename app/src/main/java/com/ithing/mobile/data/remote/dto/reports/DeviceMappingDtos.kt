package com.ithing.mobile.data.remote.dto.reports

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeviceMappingRequestDto(
    @SerialName("id")
    val id: String
)

@Serializable
data class DeviceMappingResponseDto(
    val data: DeviceMappingEnvelopeDto
)

@Serializable
data class DeviceMappingEnvelopeDto(
    val success: Boolean,
    val message: String,
    val data: DeviceMappingPayloadDto? = null
)

@Serializable
data class DeviceMappingPayloadDto(
    val mapping: List<DeviceMappingFieldDto> = emptyList()
)

@Serializable
data class DeviceMappingFieldDto(
    val registerName: String,
    val slaveId: String? = null
)
