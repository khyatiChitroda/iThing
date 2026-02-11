package com.ithing.mobile.core.auth

import androidx.datastore.core.DataStore
import java.util.prefs.Preferences
import javax.inject.Inject

/**
 * Single source of truth for Authorization token.
 *
 * TEMPORARY:
 * Currently returns a static token for development.
 * Will be replaced by a bootstrap token flow in a future ticket.
 */
interface AuthTokenProvider {
    fun getToken(): String?
}
