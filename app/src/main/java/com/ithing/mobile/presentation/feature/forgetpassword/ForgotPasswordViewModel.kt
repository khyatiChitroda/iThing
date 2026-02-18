package com.ithing.mobile.presentation.feature.forgetpassword

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ithing.mobile.domain.usecase.ForgotPasswordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val forgotPasswordUseCase: ForgotPasswordUseCase
) : ViewModel() {

    var uiState by mutableStateOf(ForgotPasswordUiState())
        private set

    fun onEmailChange(email: String) {
        uiState = uiState.copy(
            email = email,
            emailError = null,
            errorMessage = null,
            successMessage = null
        )
    }

    fun submit() {
        var hasError = false

        if (uiState.email.isBlank()) {
            uiState = uiState.copy(emailError = "Email cannot be empty")
            hasError = true
        } else if (!android.util.Patterns.EMAIL_ADDRESS
                .matcher(uiState.email)
                .matches()
        ) {
            uiState = uiState.copy(emailError = "Invalid email format")
            hasError = true
        }

        if (hasError) return

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)

            try {
                forgotPasswordUseCase(uiState.email)

                uiState = uiState.copy(
                    isLoading = false,
                    successMessage = "Reset link sent to your email"
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Something went wrong"
                )
            }
        }
    }
}
