package com.ithing.mobile.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ForgotPasswordResponseDto(
    val success: Boolean,
    val message: String
)
