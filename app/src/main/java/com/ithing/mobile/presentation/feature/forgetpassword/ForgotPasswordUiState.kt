package com.ithing.mobile.presentation.feature.forgetpassword

data class ForgotPasswordUiState(
    val email: String = "",
    val emailError: String? = null,
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)
