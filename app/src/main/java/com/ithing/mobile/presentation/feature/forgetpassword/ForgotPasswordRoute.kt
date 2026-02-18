package com.ithing.mobile.presentation.feature.forgetpassword

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ithing.mobile.presentation.navigation.AppDestination

@Composable
fun ForgotPasswordRoute(
    navController: NavController,
    viewModel: ForgotPasswordViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            // After showing success, navigate back to login
            navController.popBackStack()
        }
    }

    ForgotPasswordScreen(
        uiState = uiState,
        onEmailChange = viewModel::onEmailChange,
        onSubmit = viewModel::submit,
        onBackToLogin = {
            navController.popBackStack()
        }
    )
}
