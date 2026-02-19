package com.ithing.mobile.domain.repository

import com.ithing.mobile.domain.model.User

interface AuthRepository {
    suspend fun login(
        username: String,
        password: String
    )

    suspend fun logout()

    suspend fun forgotPassword(email: String)
    suspend fun changePassword(newPassword: String)

}
