package com.ithing.mobile.shared.data.remote.dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class ChangePasswordResponseDto(
    val data: ChangePasswordResponseDataDto
)

@Serializable
data class ChangePasswordResponseDataDto(
    val success: Boolean,
    val message: String
)
