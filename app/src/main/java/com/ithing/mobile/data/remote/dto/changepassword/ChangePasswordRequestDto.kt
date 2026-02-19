package com.ithing.mobile.data.remote.dto.changepassword

import kotlinx.serialization.Serializable

@Serializable
data class ChangePasswordRequestDto(
    val password: String
)
