package com.ithing.mobile.data.remote.dto.dashboard

import kotlinx.serialization.Serializable

@Serializable
data class DeviceDto(
    val id: String,
    val name: String,
    val customer: String? = null,
    val oem: String? = null,
    val industry: String? = null,
    val machine: String? = null,
    val configuration: String? = null,
    val mode: String? = null,
    val imei: String? = null,
    val phone: String? = null,
    val context: String? = null,
    val lastEvent: Long? = null,
    val createdAt: Long? = null,
    val updatedAt: Long? = null
)