package com.ithing.mobile.shared.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class DeviceMappingFieldOption(
    val value: String,
    val label: String
)
