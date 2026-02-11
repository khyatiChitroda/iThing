package com.ithing.mobile.data.repository

import com.ithing.mobile.core.security.HashUtil
import com.ithing.mobile.data.local.datastore.AuthDataStore
import com.ithing.mobile.data.remote.api.AuthApiService
import com.ithing.mobile.data.remote.dto.LoginRequestDto
import com.ithing.mobile.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApiService: AuthApiService,
    private val authDataStore: AuthDataStore
) : AuthRepository {
//vikas@solegaonkar.com
    //iThing@2025
    override suspend fun login(
        username: String,
        password: String
    ) {
    val hashedPassword = HashUtil.sha1(password)

    val response = authApiService.loginAttempt(
        request = LoginRequestDto(
            id = username,
            password = hashedPassword
        )
    )

    val token = response.token

    // Persist token
    authDataStore.saveAccessToken(token)
    }

    override suspend fun logout() {
        authDataStore.clear()
    }
}
