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
        private val ROLE_KEY = stringPreferencesKey("user_role")

    }

    suspend fun saveToken(token: String) {
        dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
        }
    }

    suspend fun getToken(): String? {
        return dataStore.data
            .map { it[TOKEN_KEY] }
            .firstOrNull()
    }

    suspend fun saveUserRole(role: UserRole) {
        dataStore.edit { preferences ->
            preferences[ROLE_KEY] = role.name
        }
    }

    suspend fun getUserRole(): UserRole? {
        return dataStore.data
            .map { prefs ->
                prefs[ROLE_KEY]?.let { UserRole.valueOf(it) }
            }
            .firstOrNull()
    }

    fun observeToken(): Flow<String?> {
        return dataStore.data.map { it[TOKEN_KEY] }
    }

    suspend fun clearSession() {
        dataStore.edit { it.remove(TOKEN_KEY) }
    }
}

