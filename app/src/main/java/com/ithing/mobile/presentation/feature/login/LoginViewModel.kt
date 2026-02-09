package com.ithing.mobile.presentation.feature.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ithing.mobile.domain.usecase.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
) : ViewModel() {

    private var _uiState = LoginUiState()
    val uiState: LoginUiState
        get() = _uiState


    fun onUsernameChange(username: String) {
        _uiState = _uiState.copy(username = username)
    }

    fun onPasswordChange(password: String) {
        _uiState = _uiState.copy(password = password)
    }

    fun login() {
        if (_uiState.username.isBlank() || _uiState.password.isBlank()) {
            _uiState = _uiState.copy(
                errorMessage = "Username and password cannot be empty"
            )
            return
        }

        viewModelScope.launch {
            _uiState = _uiState.copy(
                isLoading = true,
                errorMessage = null
            )

            try {
                loginUseCase(
                    username = _uiState.username,
                    password = _uiState.password
                )

                _uiState = _uiState.copy(
                    isLoading = false,
                    isLoginSuccessful = true
                )
            } catch (e: Exception) {
                _uiState = _uiState.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Login failed"
                )
            }
        }
    }
}