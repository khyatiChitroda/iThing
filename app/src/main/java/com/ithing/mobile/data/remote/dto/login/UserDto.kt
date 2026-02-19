package com.ithing.mobile.data.remote.dto.login

@kotlinx.serialization.Serializable
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