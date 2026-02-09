package com.ithing.mobile.data.repository

import com.ithing.mobile.data.local.datastore.AuthDataStore
import com.ithing.mobile.data.mapper.toDomain
import com.ithing.mobile.data.remote.api.AuthApiService
import com.ithing.mobile.data.remote.dto.LoginRequestDto
import com.ithing.mobile.domain.model.User
import com.ithing.mobile.domain.repository.AuthRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApiService: AuthApiService,
    private val authDataStore: AuthDataStore
) : AuthRepository {

    override suspend fun login(
        username: String,
        password: String
    ): User {
        val response = authApiService.login(
            LoginRequestDto(
                username = username,
                password = password
            )
        )

        // Save token
        authDataStore.saveAccessToken(response.token)

        // Return domain user
        return response.user.toDomain()
    }

    override suspend fun logout() {
        authDataStore.clear()
    }

    override suspend fun getLoggedInUser(): User? {
        val token = authDataStore.accessToken.first()
        return if (token != null) {
            // For now, user info comes from login response only
            // This will be expanded later (e.g. /me API)
            null
        } else {
            null
        }
    }
}
