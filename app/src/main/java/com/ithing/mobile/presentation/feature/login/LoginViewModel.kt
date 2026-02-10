package com.ithing.mobile.presentation.feature.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ithing.mobile.domain.usecase.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
) : ViewModel() {

    var uiState by mutableStateOf(LoginUiState())

    fun onUsernameChange(username: String) {
        uiState = uiState.copy(
            username = username,
            errorMessage = null
        )
    }

    fun onPasswordChange(password: String) {
        uiState = uiState.copy(
            password = password,
            errorMessage = null
        )
    }
    fun login() {
        if (uiState.isLoading) return

        if (uiState.username.isBlank() || uiState.password.isBlank()) {
            uiState = uiState.copy(
                errorMessage = "Username and password cannot be empty"
            )
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(
                isLoading = true,
                errorMessage = null
            )

            try {
                loginUseCase(
                    username = uiState.username,
                    password = uiState.password
                )

                uiState = uiState.copy(
                    isLoading = false,
                    isLoginSuccessful = true
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Login failed"
                )
            }
        }
    }
}