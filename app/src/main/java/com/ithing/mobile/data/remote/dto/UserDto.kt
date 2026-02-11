package com.ithing.mobile.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: String,
    val name: String,
    val address: String,
    val phone: String,
    val organization: String,
    val context: String,
    val admin: Boolean,
    val superadmin: Boolean,
    val oemadmin: Boolean,
    val customeradmin: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val lastPasswordChange: Long,
    val password: String
)
