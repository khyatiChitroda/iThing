package com.ithing.mobile.shared.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Industry(
    val id: String,
    val name: String
)
