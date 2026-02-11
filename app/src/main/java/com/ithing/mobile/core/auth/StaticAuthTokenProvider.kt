package com.ithing.mobile.core.auth

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StaticAuthTokenProvider @Inject constructor() : AuthTokenProvider {
    val PRE_AUTH_JWT = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6InZpa2FzQHNvbGVnYW9ua2FyLmNvbSIsImN1c3RvbWVyIjoiIiwib2VtIjoiIiwiaW5kdXN0cnkiOiIiLCJjdXN0b21lcmFkbWluIjpmYWxzZSwib2VtYWRtaW4iOmZhbHNlLCJzdXBlcmFkbWluIjp0cnVlLCJybmQiOiJJQmpjdXFVUXhfTFB2YXpoUExOeTciLCJpYXQiOjE3NzA3MTMzNTUsImV4cCI6MTc3MDc5OTc1NX0.EBiAtVb3y70jcbmgfmU570Rxuo7wJU1K2M_4Sg9cXPE"
    override fun getToken(): String {
        return PRE_AUTH_JWT
    }
}
