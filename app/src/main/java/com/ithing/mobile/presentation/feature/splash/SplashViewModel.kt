package com.ithing.mobile.presentation.feature.splash

import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ithing.mobile.core.session.SessionManager
import com.ithing.mobile.data.local.datastore.AuthDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authDataStore: AuthDataStore,
    private val sessionManager: SessionManager
) : ViewModel() {

    var destination by mutableStateOf<SplashDestination?>(null)
        private set

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            val startTime = SystemClock.elapsedRealtime()
            val token = sessionManager.getToken()
            val role = sessionManager.getUserRole()
            val isExpired = sessionManager.isSessionExpired()

            if (isExpired) {
                sessionManager.clearSession()
            }

//            val remainingSplashTime = MIN_SPLASH_DURATION_MS - (SystemClock.elapsedRealtime() - startTime)
//            if (remainingSplashTime > 0) {
//                delay(remainingSplashTime)
//            }
            val remainingSplashTime =
                MIN_SPLASH_DURATION_MS - (SystemClock.elapsedRealtime() - startTime)
            if (remainingSplashTime > 0) {
                delay(remainingSplashTime)
            }

            destination = if (!token.isNullOrBlank() && role != null && !isExpired) {
                SplashDestination.Authenticated(role)
            } else {
                SplashDestination.Unauthenticated
            }
        }
    }

    private companion object {
        const val MIN_SPLASH_DURATION_MS = 1200L
    }
}
