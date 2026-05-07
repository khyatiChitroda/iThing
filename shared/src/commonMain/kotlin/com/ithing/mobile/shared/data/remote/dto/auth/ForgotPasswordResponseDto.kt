package com.ithing.mobile.shared.data.remote.dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class ForgotPasswordResponseDto(
    val success: Boolean,
    val message: String
)
