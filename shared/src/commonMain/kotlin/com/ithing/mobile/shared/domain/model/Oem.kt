package com.ithing.mobile.shared.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Oem(
    val id: String,
    val name: String,
    val industry: String,
    val logoUrl: String? = null
)
