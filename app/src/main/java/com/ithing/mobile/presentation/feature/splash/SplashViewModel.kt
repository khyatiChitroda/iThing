package com.ithing.mobile.presentation.feature.splash

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ithing.mobile.data.local.datastore.AuthDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authDataStore: AuthDataStore
) : ViewModel() {

    var destination by mutableStateOf<SplashDestination?>(null)
        private set

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            val token = authDataStore.accessToken.first()

            destination = if (!token.isNullOrBlank()) {
                SplashDestination.Authenticated
            } else {
                SplashDestination.Unauthenticated
            }
        }
    }
}