package com.ithing.mobile.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ithing.mobile.presentation.feature.login.LoginRoute
import com.ithing.mobile.presentation.feature.login.LoginViewModel
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = AppDestination.Login.route
    ) {
        composable(AppDestination.Login.route) {
            val loginViewModel: LoginViewModel = hiltViewModel()
            LoginRoute(
                viewModel = loginViewModel,
                navController = navController
            )
        }

        composable(AppDestination.Dashboard.route) {
            // Temporary placeholder
            androidx.compose.material3.Text("Dashboard Screen")
        }
    }
}