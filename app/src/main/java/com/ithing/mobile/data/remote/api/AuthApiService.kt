package com.ithing.mobile.data.remote.api

import com.ithing.mobile.data.remote.dto.LoginRequestDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("login-attempt")
    suspend fun loginAttempt(
        @Body request: LoginRequestDto
    )
}
