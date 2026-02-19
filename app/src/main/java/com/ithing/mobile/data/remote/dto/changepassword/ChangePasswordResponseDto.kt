package com.ithing.mobile.data.remote.dto.changepassword

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
