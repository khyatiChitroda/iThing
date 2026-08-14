package com.ithing.mobile.core.network

import com.ithing.mobile.core.session.SessionManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val sessionManager: SessionManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = runBlocking { sessionManager.getToken() }
            ?.removePrefix("Bearer ")
            ?.trim()

        val requestBuilder = originalRequest.newBuilder()

        if (!token.isNullOrBlank()) {
            requestBuilder.header("Authorization", token)
        }

        val response = chain.proceed(requestBuilder.build())

        if (response.code == 401 && !token.isNullOrBlank()) {
            runBlocking {
                sessionManager.expireSession()
            }
        }

        return response
    }
}
