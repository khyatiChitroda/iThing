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
    val mapping: List<DeviceMappingFieldDto> = emptyList(),
    val slaveConfig: List<DeviceSlaveConfigDto> = emptyList()
)

@Serializable
data class DeviceMappingFieldDto(
    val registerName: String,
    val slaveId: String? = null,
    val registerUnit: String? = null,
    val dataType: String? = null,
    val dividingFactor: String? = null,
    val additionFactor: String? = null,
    val dName: List<String> = emptyList(),
    val modbusAddress: List<String> = emptyList(),
    val config: String? = null,
    val canAddress: List<DeviceCanAddressDto> = emptyList()
)

@Serializable
data class DeviceSlaveConfigDto(
    val slaveIdNumber: String? = null,
    val slaveId: String? = null,
    val port: String? = null,
    val ip: String? = null
)

@Serializable
data class DeviceCanAddressDto(
    val c: String? = null,
    val b: String? = null
)
