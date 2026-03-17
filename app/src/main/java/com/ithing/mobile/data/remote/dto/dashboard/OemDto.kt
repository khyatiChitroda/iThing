package com.ithing.mobile.data.remote.dto.dashboard

import kotlinx.serialization.Serializable

@Serializable
data class OemDto(
    val id: String,
    val name: String,
    val industry: String,
    val email: String? = null,
    val phone: String? = null,
    val logo: String? = null,
    val country: String? = null,
    val state: String? = null,
    val city: String? = null,
    val context: String? = null,
    val createdAt: Long? = null,
    val updatedAt: Long? = null,
    val companies: List<String>? = null,
    val users: List<String>? = null
)