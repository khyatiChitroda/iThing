package com.ithing.mobile.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class LoginRequestDto(
    @SerialName("id")
    val id: String,
    @SerialName("password")
    val password: String
)