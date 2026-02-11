package com.ithing.mobile.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponseDto(
    val token: String,
    val user: UserDto,
    val oemLogo: String? = null
)