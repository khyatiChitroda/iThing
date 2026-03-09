package com.ithing.mobile.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private const val AUTH_DATASTORE_NAME = "auth_datastore"

private val Context.authDataStore by preferencesDataStore(
    name = AUTH_DATASTORE_NAME
)

@Singleton
class AuthDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private object Keys {
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
    }

    val accessToken: Flow<String?> =
        context.authDataStore.data.map { preferences ->
            preferences[Keys.ACCESS_TOKEN]
        }


    suspend fun clear() {
        context.authDataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
