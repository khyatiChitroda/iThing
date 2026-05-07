package com.ithing.mobile.shared.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Device(
    val id: String,
    val name: String,
    val customerId: String?,
    val oemId: String?,
    val industry: String?
)
