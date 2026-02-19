package com.ithing.mobile.presentation.feature.changepassword

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ithing.mobile.domain.usecase.ChangePasswordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val changePasswordUseCase: ChangePasswordUseCase
) : ViewModel() {

    var uiState by mutableStateOf(ChangePasswordUiState())
        private set

    fun onNewPasswordChange(value: String) {
        uiState = uiState.copy(
            newPassword = value,
            newPasswordError = null,
            errorMessage = null,
            successMessage = null
        )
    }

    fun onConfirmPasswordChange(value: String) {
        uiState = uiState.copy(
            confirmPassword = value,
            confirmPasswordError = null,
            errorMessage = null,
            successMessage = null
        )
    }

    fun submit() {
        var hasError = false

        if (uiState.newPassword.isBlank()) {
            uiState = uiState.copy(newPasswordError = "Password cannot be empty")
            hasError = true
        }

        if (uiState.confirmPassword != uiState.newPassword) {
            uiState = uiState.copy(confirmPasswordError = "Passwords do not match")
            hasError = true
        }

        if (hasError) return

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)

            try {
                changePasswordUseCase(uiState.newPassword)

                uiState = uiState.copy(
                    isLoading = false,
                    successMessage = "Password changed successfully"
                )
            } catch (e: Exception) {
                e.printStackTrace()
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Something went wrong"
                )
            }
        }
    }
}
