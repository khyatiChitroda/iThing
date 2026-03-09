package com.ithing.mobile.core.session

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class SessionManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("jwt_token")
        private val ROLE_KEY = stringPreferencesKey("user_role")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val OEM_LOGO_KEY = stringPreferencesKey("oem_logo")

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


    suspend fun saveUserId(userId: String) {
        dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
        }
    }

    suspend fun getUserId(): String? {
        return dataStore.data
            .map { it[USER_ID_KEY] }
            .firstOrNull()
    }

    suspend fun saveOemLogo(oemLogo: String?) {
        dataStore.edit { preferences ->
            if (oemLogo != null) {
                preferences[OEM_LOGO_KEY] = oemLogo
            } else {
                preferences.remove(OEM_LOGO_KEY)
            }
        }
    }

    suspend fun getOemLogo(): String? {
        return dataStore.data
            .map { it[OEM_LOGO_KEY] }
            .firstOrNull()
    }

    fun observeToken(): Flow<String?> {
        return dataStore.data.map { it[TOKEN_KEY] }
    }

    suspend fun clearSession() {
        dataStore.edit {
            it.remove(TOKEN_KEY)
            it.remove(ROLE_KEY)
            it.remove(USER_ID_KEY)
            it.remove(OEM_LOGO_KEY)
        }
    }
}

