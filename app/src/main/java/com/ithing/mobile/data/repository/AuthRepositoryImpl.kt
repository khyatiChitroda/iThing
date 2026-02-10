package com.ithing.mobile.data.repository

import com.ithing.mobile.core.security.HashUtil
import com.ithing.mobile.data.remote.api.AuthApiService
import com.ithing.mobile.data.remote.dto.LoginRequestDto
import com.ithing.mobile.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApiService: AuthApiService
) : AuthRepository {
     val PRE_AUTH_JWT = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6InZpa2FzQHNvbGVnYW9ua2FyLmNvbSIsImN1c3RvbWVyIjoiIiwib2VtIjoiIiwiaW5kdXN0cnkiOiIiLCJjdXN0b21lcmFkbWluIjpmYWxzZSwib2VtYWRtaW4iOmZhbHNlLCJzdXBlcmFkbWluIjp0cnVlLCJybmQiOiJJQmpjdXFVUXhfTFB2YXpoUExOeTciLCJpYXQiOjE3NzA3MTMzNTUsImV4cCI6MTc3MDc5OTc1NX0.EBiAtVb3y70jcbmgfmU570Rxuo7wJU1K2M_4Sg9cXPE"
    override suspend fun login(
        username: String,
        password: String
    ) {
        val hashedPassword = HashUtil.sha1(password)

        authApiService.loginAttempt(
            authToken = PRE_AUTH_JWT, // explained below
            request = LoginRequestDto(
                id = username,
                password = hashedPassword
            )
        )
    }
}
