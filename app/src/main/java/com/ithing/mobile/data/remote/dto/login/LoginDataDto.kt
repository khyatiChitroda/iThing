package com.ithing.mobile.data.remote.dto.login

import com.ithing.mobile.data.remote.dto.login.UserDto
import kotlinx.serialization.Serializable

@Serializable
data class LoginDataDto(
    val token: String,
    val user: UserDto,
    val oemLogo: String? = null
)

