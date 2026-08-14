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
        private val DASHBOARD_INDUSTRY_KEY = stringPreferencesKey("dashboard_industry")
        private val DASHBOARD_OEM_KEY = stringPreferencesKey("dashboard_oem")
        private val DASHBOARD_CUSTOMER_KEY = stringPreferencesKey("dashboard_customer")
        private val DASHBOARD_DEVICE_KEY = stringPreferencesKey("dashboard_device")
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

    suspend fun saveDashboardFilters(
        industryId: String?,
        oemId: String?,
        customerId: String?,
        deviceId: String?
    ) {
        dataStore.edit { preferences ->
            preferences.setOrRemove(DASHBOARD_INDUSTRY_KEY, industryId)
            preferences.setOrRemove(DASHBOARD_OEM_KEY, oemId)
            preferences.setOrRemove(DASHBOARD_CUSTOMER_KEY, customerId)
            preferences.setOrRemove(DASHBOARD_DEVICE_KEY, deviceId)
        }
    }

    suspend fun getDashboardFilters(): DashboardFilterIds = dataStore.data
        .map { preferences ->
            DashboardFilterIds(
                industryId = preferences[DASHBOARD_INDUSTRY_KEY],
                oemId = preferences[DASHBOARD_OEM_KEY],
                customerId = preferences[DASHBOARD_CUSTOMER_KEY],
                deviceId = preferences[DASHBOARD_DEVICE_KEY]
            )
        }
        .firstOrNull() ?: DashboardFilterIds()

    private fun androidx.datastore.preferences.core.MutablePreferences.setOrRemove(
        key: androidx.datastore.preferences.core.Preferences.Key<String>,
        value: String?
    ) {
        if (value.isNullOrBlank()) remove(key) else this[key] = value
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

data class DashboardFilterIds(
    val industryId: String? = null,
    val oemId: String? = null,
    val customerId: String? = null,
    val deviceId: String? = null
)
