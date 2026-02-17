package com.ithing.mobile.core.session

import kotlinx.coroutines.flow.Flow

interface SessionManager {

    suspend fun saveToken(token: String)

    suspend fun getToken(): String?

    fun observeToken(): Flow<String?>

    suspend fun clearSession()
}
