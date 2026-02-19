package com.ithing.mobile.presentation.feature.dashboard

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.ithing.mobile.presentation.navigation.AppDestination

@Composable
fun DashboardRoute(
    viewModel: DashboardViewModel = hiltViewModel(),
    navController: NavController
) {

    DashboardScreen(
        onLogoutClick = {
            viewModel.logout {
                navController.navigate(AppDestination.Login.route) {
                    popUpTo(AppDestination.Dashboard.route) {
                        inclusive = true
                    }
                }
            }
        },
        onChangePasswordClick = {
            navController.navigate(AppDestination.ChangePassword.route)
        }
    )
}