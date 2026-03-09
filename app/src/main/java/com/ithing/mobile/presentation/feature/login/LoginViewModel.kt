package com.ithing.mobile.presentation.feature.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ithing.mobile.domain.usecase.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
) : ViewModel() {

    var uiState by mutableStateOf(LoginUiState())

    fun onUsernameChange(username: String) {
        uiState = uiState.copy(
            username = username,
            usernameError = null,
            errorMessage = null
        )
    }

    fun onPasswordChange(password: String) {
        uiState = uiState.copy(
            password = password,
            passwordError = null,
            errorMessage = null
        )
    }

    fun togglePasswordVisibility() {
        uiState = uiState.copy(
            isPasswordVisible = !uiState.isPasswordVisible
        )
    }

    fun onRememberMeChange(value: Boolean) {
        uiState = uiState.copy(rememberMe = value)
    }

    fun login() {
        if (uiState.isLoading) return

        var hasError = false

        if (uiState.username.isBlank()) {
            uiState = uiState.copy(usernameError = "Email cannot be empty")
            hasError = true
        } else if (!android.util.Patterns.EMAIL_ADDRESS
                .matcher(uiState.username)
                .matches()
        ) {
            uiState = uiState.copy(usernameError = "Invalid email format")
            hasError = true
        }

        if (uiState.password.isBlank()) {
            uiState = uiState.copy(passwordError = "Password cannot be empty")
            hasError = true
        }

        if (hasError) return

        viewModelScope.launch {
            uiState = uiState.copy(
                isLoading = true,
                errorMessage = null
            )

            try {
                loginUseCase(uiState.username, uiState.password)

                uiState = uiState.copy(
                    isLoading = false,
                    isLoginSuccessful = true
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = "Invalid credentials"
                )
            }
        }
    }
}