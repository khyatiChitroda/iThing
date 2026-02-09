package com.ithing.mobile.data.remote.api

import com.ithing.mobile.data.remote.dto.LoginRequestDto
import com.ithing.mobile.data.remote.dto.LoginResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequestDto
    ): LoginResponseDto
}