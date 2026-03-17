package com.ithing.mobile.data.repository

import com.ithing.mobile.core.security.HashUtil
import com.ithing.mobile.core.session.SessionManager
import com.ithing.mobile.core.session.UserRole
import com.ithing.mobile.data.local.datastore.AuthDataStore
import com.ithing.mobile.data.remote.api.AuthApiService
import com.ithing.mobile.data.remote.dto.changepassword.ChangePasswordRequestDto
import com.ithing.mobile.data.remote.dto.forgotpassword.ForgotPasswordRequestDto
import com.ithing.mobile.data.remote.dto.login.LoginRequestDto
import com.ithing.mobile.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApiService: AuthApiService,
    private val authDataStore: AuthDataStore,
    private val sessionManager: SessionManager
) : AuthRepository {
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

        val user = response.data.user

        val role = when {
            user.superadmin -> UserRole.SUPER_ADMIN
            user.oemadmin -> UserRole.OEM_ADMIN
            user.customeradmin -> UserRole.CUSTOMER_ADMIN
            user.admin -> UserRole.ADMIN
            else -> UserRole.USER
        }
        sessionManager.saveUserRole(role)
        sessionManager.saveUserId(user.id)
        sessionManager.saveOemLogo(response.data.oemLogo)
        sessionManager.saveToken(response.data.token)
        println(
            "AuthRepository: Login succeeded for $username; saved token=${response.data.token.take(16)}..."
        )
    }

    override suspend fun logout() {
        sessionManager.clearSession()
        authDataStore.clear()
    }

    override suspend fun forgotPassword(email: String) {
        val response = authApiService.forgotPassword(
            ForgotPasswordRequestDto(id = email)
        )

        if (!response.success) {
            throw RuntimeException(response.message)
        }
    }

    override suspend fun changePassword(newPassword: String) {

        val hashedPassword = HashUtil.sha1(newPassword)

        val response = authApiService.changePassword(
            ChangePasswordRequestDto(password = hashedPassword)
        )

        if (!response.data.success) {
            throw RuntimeException(response.data.message)
        }
    }

}
