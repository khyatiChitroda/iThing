package com.ithing.mobile.shared.data.remote.dto.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(
    @SerialName("id")
    val id: String,
    @SerialName("password")
    val password: String
)
