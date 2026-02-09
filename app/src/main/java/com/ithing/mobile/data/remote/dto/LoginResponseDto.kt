package com.ithing.mobile.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class LoginResponseDto(
    @SerialName("token")
    val token: String,
    @SerialName("expires_at")
    val expiresAt: Long,
    @SerialName("user")
    val user: UserDto
)

@Serializable
data class UserDto(
    @SerialName("id")
    val id: String,
    @SerialName("name")
    val name: String,
    @SerialName("email")
    val email: String,
    @SerialName("profile_image_url")
    val profileImageUrl: String?
)