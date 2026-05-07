package com.ithing.mobile.shared.data.remote.datasource

import com.ithing.mobile.shared.data.remote.dto.auth.ChangePasswordRequestDto
import com.ithing.mobile.shared.data.remote.dto.auth.ChangePasswordResponseDto
import com.ithing.mobile.shared.data.remote.dto.auth.ForgotPasswordRequestDto
import com.ithing.mobile.shared.data.remote.dto.auth.ForgotPasswordResponseDto
import com.ithing.mobile.shared.data.remote.dto.auth.LoginRequestDto
import com.ithing.mobile.shared.data.remote.dto.auth.LoginResponseDto

interface AuthRemoteDataSource {
    suspend fun loginAttempt(request: LoginRequestDto): LoginResponseDto
    suspend fun forgotPassword(request: ForgotPasswordRequestDto): ForgotPasswordResponseDto
    suspend fun changePassword(request: ChangePasswordRequestDto): ChangePasswordResponseDto
}
