package com.ithing.mobile.domain.repository

interface AuthRepository {
    suspend fun login(
        username: String,
        password: String
    )

    suspend fun logout()

    suspend fun forgotPassword(email: String)
    suspend fun changePassword(newPassword: String)

}
