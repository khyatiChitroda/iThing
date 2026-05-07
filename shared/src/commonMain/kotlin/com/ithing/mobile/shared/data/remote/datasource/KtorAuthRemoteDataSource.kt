package com.ithing.mobile.shared.data.remote.datasource

import com.ithing.mobile.shared.data.remote.dto.auth.ChangePasswordRequestDto
import com.ithing.mobile.shared.data.remote.dto.auth.ChangePasswordResponseDto
import com.ithing.mobile.shared.data.remote.dto.auth.ForgotPasswordRequestDto
import com.ithing.mobile.shared.data.remote.dto.auth.ForgotPasswordResponseDto
import com.ithing.mobile.shared.data.remote.dto.auth.LoginRequestDto
import com.ithing.mobile.shared.data.remote.dto.auth.LoginResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class KtorAuthRemoteDataSource(
    private val httpClient: HttpClient
) : AuthRemoteDataSource {
    override suspend fun loginAttempt(request: LoginRequestDto): LoginResponseDto =
        httpClient.post("login-attempt") {
            setBody(request)
        }.body()

    override suspend fun forgotPassword(request: ForgotPasswordRequestDto): ForgotPasswordResponseDto =
        httpClient.post("forgot-password") {
            setBody(request)
        }.body()

    override suspend fun changePassword(request: ChangePasswordRequestDto): ChangePasswordResponseDto =
        httpClient.post("user-change-password") {
            setBody(request)
        }.body()
}
