package com.ithing.mobile.presentation.feature.login

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.ithing.mobile.presentation.navigation.AppDestination
import com.ithing.mobile.presentation.root.AppContainer


@Composable
fun LoginRoute(
    viewModel: LoginViewModel,
    navController: NavController
) {
    val uiState = viewModel.uiState

    LaunchedEffect(uiState.isLoginSuccessful) {
        if (uiState.isLoginSuccessful) {
            navController.navigate(AppDestination.AppShell.route) {
                popUpTo(AppDestination.Login.route) {
                    inclusive = true
                }
            }
        }
    }
    AppContainer { modifier ->
        LoginScreen(
            modifier = modifier,
            uiState = uiState,
            onUsernameChange = viewModel::onUsernameChange,
            onPasswordChange = viewModel::onPasswordChange,
            onLoginClick = viewModel::login,
            onTogglePasswordVisibility = viewModel::togglePasswordVisibility,
            onRememberMeChange = viewModel::onRememberMeChange,
            onForgotPasswordClick = {
                navController.navigate(AppDestination.ForgotPassword.route)
            }
        )
    }
}
