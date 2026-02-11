package com.ithing.mobile.presentation.feature.login

import com.ithing.mobile.BuildConfig

data class LoginUiState(
    val username: String = if (BuildConfig.DEBUG) "vikas@solegaonkar.com" else "",
    val password: String = if (BuildConfig.DEBUG) "iThing@2025" else "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoginSuccessful: Boolean = false
)
