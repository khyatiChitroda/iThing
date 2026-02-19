package com.ithing.mobile.data.remote.dto.login

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponseDto(
    val data: LoginDataDto
)