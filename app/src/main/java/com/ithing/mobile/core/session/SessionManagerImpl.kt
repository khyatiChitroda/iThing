package com.ithing.mobile.core.session

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManagerImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SessionManager {

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("jwt_token")
    }

    override suspend fun saveToken(token: String) {
        dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
        }
    }

    override suspend fun getToken(): String? {
        return dataStore.data
            .map { it[TOKEN_KEY] }
            .firstOrNull()
    }

    override fun observeToken(): Flow<String?> {
        return dataStore.data.map { it[TOKEN_KEY] }
    }

    override suspend fun clearSession() {
        dataStore.edit { it.remove(TOKEN_KEY) }
    }
}
