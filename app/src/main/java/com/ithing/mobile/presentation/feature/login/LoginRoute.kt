package com.ithing.mobile.presentation.feature.login

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel


@Composable
fun LoginRoute(
    viewModel: LoginViewModel
) {
    val uiState = viewModel.uiState

    LoginScreen(
        uiState = uiState,
        onUsernameChange = viewModel::onUsernameChange,
        onPasswordChange = viewModel::onPasswordChange,
        onLoginClick = viewModel::login
    )

    if (uiState.isLoginSuccessful) {
        // Temporary: log or show a placeholder
        // Later: navigate to dashboard
    }
}