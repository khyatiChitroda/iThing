package com.ithing.mobile.data.remote.api

import com.ithing.mobile.data.remote.dto.changepassword.ChangePasswordRequestDto
import com.ithing.mobile.data.remote.dto.changepassword.ChangePasswordResponseDto
import com.ithing.mobile.data.remote.dto.forgotpassword.ForgotPasswordRequestDto
import com.ithing.mobile.data.remote.dto.forgotpassword.ForgotPasswordResponseDto
import com.ithing.mobile.data.remote.dto.login.LoginRequestDto
import com.ithing.mobile.data.remote.dto.login.LoginResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("login-attempt")
    suspend fun loginAttempt(
        @Body request: LoginRequestDto
    ) : LoginResponseDto

    @POST("forgot-password")
    suspend fun forgotPassword(
        @Body request: ForgotPasswordRequestDto
    ): ForgotPasswordResponseDto

    @POST("user-change-password")
    suspend fun changePassword(
        @Body request: ChangePasswordRequestDto
    ): ChangePasswordResponseDto
}
