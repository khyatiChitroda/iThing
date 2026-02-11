package com.ithing.mobile.presentation.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ithing.mobile.domain.usecase.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            logoutUseCase()
            onLoggedOut()
        }
    }
}
