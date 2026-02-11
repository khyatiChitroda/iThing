package com.ithing.mobile.core.auth

import com.ithing.mobile.data.local.datastore.AuthDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RealAuthTokenProvider @Inject constructor(
    private val authDataStore: AuthDataStore
) : AuthTokenProvider {

    override fun getToken(): String? {
        return runBlocking {
            authDataStore.accessToken.first()
        }

    }
}
