package com.ithing.mobile.presentation.feature.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import com.ithing.mobile.presentation.navigation.AppDestination


@Composable
fun LoginRoute(
    viewModel: LoginViewModel,
    navController: NavController
) {
    val uiState = viewModel.uiState

    LaunchedEffect(uiState.isLoginSuccessful) {
        if (uiState.isLoginSuccessful) {
            navController.navigate(AppDestination.Dashboard.route) {
                popUpTo(AppDestination.Login.route) {
                    inclusive = true
                }
            }
        }
    }
    LoginScreen(
        uiState = uiState,
        onUsernameChange = viewModel::onUsernameChange,
        onPasswordChange = viewModel::onPasswordChange,
        onLoginClick = viewModel::login
    )
}