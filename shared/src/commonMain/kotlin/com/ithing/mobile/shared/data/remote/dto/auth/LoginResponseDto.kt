package com.ithing.mobile.shared.data.remote.dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponseDto(
    val data: LoginDataDto
)

@Serializable
data class LoginDataDto(
    val token: String,
    val user: UserDto,
    val oemLogo: String? = null
)

@Serializable
data class UserDto(
    val id: String,
    val name: String,
    val address: String,
    val phone: String,
    val organization: String,
    val context: String,
    val admin: Boolean = false,
    val superadmin: Boolean = false,
    val oemadmin: Boolean = false,
    val customeradmin: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long,
    val lastPasswordChange: Long,
    val password: String
)
