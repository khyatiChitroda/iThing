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
            println(
                "AuthInterceptor: Sending ${originalRequest.method} ${originalRequest.url.encodedPath} with token=${token.take(16)}..."
            )
        } else {
            println(
                "AuthInterceptor: Sending ${originalRequest.method} ${originalRequest.url.encodedPath} without auth token"
            )
        }

        val response = chain.proceed(requestBuilder.build())

        if (response.code == 401 && !token.isNullOrBlank()) {
            println(
                "AuthInterceptor: 401 from ${originalRequest.url.encodedPath}; authHeaderPresent=${!token.isNullOrBlank()}"
            )
            runBlocking {
                sessionManager.expireSession()
            }
        }

        return response
    }
}
