package com.ithing.mobile.shared.core.network

fun interface AuthTokenProvider {
    fun accessToken(): String?
}

object NoAuthTokenProvider : AuthTokenProvider {
    override fun accessToken(): String? = null
}
