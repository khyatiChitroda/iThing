package com.ithing.mobile.data.remote.dto.dashboard

import kotlinx.serialization.Serializable

@Serializable
data class CustomerDto(
    val id: String,
    val name: String,
    val oem: String,
    val industry: String,
    val email: String? = null,
    val phone: String? = null,
    val country: String? = null,
    val state: String? = null,
    val city: String? = null,
    val context: String? = null,
    val createdAt: Long? = null,
    val updatedAt: Long? = null,
    val devices: List<String>? = null,
    val users: List<String>? = null
)