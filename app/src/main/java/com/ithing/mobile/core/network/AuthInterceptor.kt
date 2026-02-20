package com.ithing.mobile.core.network

import android.util.Log
import com.ithing.mobile.core.session.AuthEvent
import com.ithing.mobile.core.session.AuthEventBus
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

        val token = runBlocking { sessionManager.getToken() }

        val request = chain.request().newBuilder().apply {
            if (!token.isNullOrBlank()) {
                addHeader("Authorization", token)
            }
        }.build()

        val response = chain.proceed(request)

        if (response.code == 401) {
            runBlocking {
                sessionManager.clearSession()
                AuthEventBus.emit(AuthEvent.Logout)
            }
        }

        return response
    }
}
