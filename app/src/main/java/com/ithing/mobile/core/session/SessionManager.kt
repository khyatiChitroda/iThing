package com.ithing.mobile.core.session
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class SessionManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val TOKEN_KEY = stringPreferencesKey("jwt_token")
        private val TOKEN_EXPIRY_KEY = longPreferencesKey("jwt_token_expiry")
        private val ROLE_KEY = stringPreferencesKey("user_role")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val OEM_LOGO_KEY = stringPreferencesKey("oem_logo")
    }

    @Volatile
    private var cachedToken: String? = null

    @Volatile
    private var cachedRole: UserRole? = null

    @Volatile
    private var cachedTokenExpiry: Long? = null

    @Volatile
    private var cachedUserId: String? = null

    @Volatile
    private var cachedOemLogo: String? = null

    private val _sessionExpiredEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val sessionExpiredEvents: SharedFlow<Unit> = _sessionExpiredEvents

    suspend fun saveToken(token: String) {
        val normalizedToken = token.trim()
        cachedToken = normalizedToken
        println("SessionManager: saveToken token=${normalizedToken.take(16)}... length=${normalizedToken.length}")
        dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = normalizedToken
        }
    }

    suspend fun getToken(): String? {
        cachedToken?.let {
            if (isSessionExpired()) {
                expireSession()
                return null
            }
            println("SessionManager: getToken cache hit token=${it.take(16)}... length=${it.length}")
            return it
        }

        return dataStore.data
            .map { it[TOKEN_KEY] }
            .firstOrNull()
            ?.also {
                cachedToken = it
                if (isSessionExpired()) {
                    expireSession()
                    return null
                }
                println("SessionManager: getToken datastore hit token=${it.take(16)}... length=${it.length}")
            }
    }

    suspend fun saveTokenExpiry(expiryMillis: Long) {
        cachedTokenExpiry = expiryMillis
        println("SessionManager: saveTokenExpiry expiryMillis=$expiryMillis")
        dataStore.edit { preferences ->
            preferences[TOKEN_EXPIRY_KEY] = expiryMillis
        }
    }

    suspend fun getTokenExpiry(): Long? {
        cachedTokenExpiry?.let { return it }

        return dataStore.data
            .map { it[TOKEN_EXPIRY_KEY] }
            .firstOrNull()
            ?.also { cachedTokenExpiry = it }
    }

    suspend fun isSessionExpired(nowMillis: Long = System.currentTimeMillis()): Boolean {
        val expiryMillis = getTokenExpiry() ?: return false
        return nowMillis >= expiryMillis
    }

    suspend fun saveUserRole(role: UserRole) {
        cachedRole = role
        dataStore.edit { preferences ->
            preferences[ROLE_KEY] = role.name
        }
    }

    suspend fun getUserRole(): UserRole? {
        cachedRole?.let { return it }

        return dataStore.data
            .map { prefs ->
                prefs[ROLE_KEY]?.let { UserRole.valueOf(it) }
            }
            .firstOrNull()
            ?.also { cachedRole = it }
    }


    suspend fun saveUserId(userId: String) {
        cachedUserId = userId
        dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
        }
    }

    suspend fun getUserId(): String? {
        cachedUserId?.let { return it }

        return dataStore.data
            .map { it[USER_ID_KEY] }
            .firstOrNull()
            ?.also { cachedUserId = it }
    }

    suspend fun saveOemLogo(oemLogo: String?) {
        cachedOemLogo = oemLogo
        dataStore.edit { preferences ->
            if (oemLogo != null) {
                preferences[OEM_LOGO_KEY] = oemLogo
            } else {
                preferences.remove(OEM_LOGO_KEY)
            }
        }
    }

    suspend fun getOemLogo(): String? {
        cachedOemLogo?.let { return it }

        return dataStore.data
            .map { it[OEM_LOGO_KEY] }
            .firstOrNull()
            ?.also { cachedOemLogo = it }
    }

    fun observeToken(): Flow<String?> {
        return dataStore.data.map { it[TOKEN_KEY] }
    }

    suspend fun clearSession() {
        println("SessionManager: clearSession")
        cachedToken = null
        cachedTokenExpiry = null
        cachedRole = null
        cachedUserId = null
        cachedOemLogo = null
        dataStore.edit {
            it.remove(TOKEN_KEY)
            it.remove(TOKEN_EXPIRY_KEY)
            it.remove(ROLE_KEY)
            it.remove(USER_ID_KEY)
            it.remove(OEM_LOGO_KEY)
        }
    }

    suspend fun expireSession() {
        println("SessionManager: expireSession")
        clearSession()
        _sessionExpiredEvents.emit(Unit)
    }
}
