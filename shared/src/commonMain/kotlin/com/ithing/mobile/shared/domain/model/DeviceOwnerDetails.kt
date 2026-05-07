package com.ithing.mobile.shared.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class DeviceOwnerDetails(
    val deviceId: String,
    val deviceName: String,
    val machineName: String?,
    val customerId: String,
    val customerName: String,
    val oemId: String,
    val oemName: String
)
