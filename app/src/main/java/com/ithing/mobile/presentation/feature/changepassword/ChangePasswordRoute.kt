package com.ithing.mobile.presentation.feature.changepassword

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.ithing.mobile.presentation.root.AppContainer

@Composable
fun ChangePasswordRoute(
    navController: NavController,
    viewModel: ChangePasswordViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState

    AppContainer { modifier ->
        ChangePasswordScreen(
            modifier = modifier,
            uiState = uiState,
            onNewPasswordChange = viewModel::onNewPasswordChange,
            onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
            onSubmit = viewModel::submit,
            onBack = { navController.popBackStack() }
        )
    }
}
