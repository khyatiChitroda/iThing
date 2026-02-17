package com.ithing.mobile.core.session

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import androidx.datastore.preferences.core.Preferences


@Singleton
class SessionManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("jwt_token")
    }

    suspend fun saveToken(token: String) {
        dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
        }
    }

    suspend fun getToken(): String? {
//        return "invalid_token_for_testing"
        return dataStore.data
            .map { it[TOKEN_KEY] }
            .firstOrNull()
    }

    fun observeToken(): Flow<String?> {
        return dataStore.data.map { it[TOKEN_KEY] }
    }

    suspend fun clearSession() {
        dataStore.edit { it.remove(TOKEN_KEY) }
    }
}

