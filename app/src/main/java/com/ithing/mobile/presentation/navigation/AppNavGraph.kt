package com.ithing.mobile.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ithing.mobile.presentation.feature.login.LoginRoute
import com.ithing.mobile.presentation.feature.login.LoginViewModel
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ithing.mobile.presentation.feature.dashboard.DashboardRoute
import com.ithing.mobile.presentation.feature.dashboard.DashboardViewModel
import com.ithing.mobile.presentation.feature.forgetpassword.ForgotPasswordRoute
import com.ithing.mobile.presentation.feature.home.HomeScreen
import com.ithing.mobile.presentation.feature.splash.SplashRoute

@Composable
fun AppNavGraph(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = AppDestination.Splash.route
    ) {
        composable(AppDestination.Splash.route) {
            SplashRoute(navController = navController)
        }

        composable(AppDestination.Login.route) {
            val loginViewModel: LoginViewModel = hiltViewModel()
            LoginRoute(
                viewModel = loginViewModel,
                navController = navController
            )
        }
        composable(AppDestination.ForgotPassword.route) {
            ForgotPasswordRoute(
                viewModel = hiltViewModel(),
                navController = navController
            )
        }
        composable(AppDestination.Home.route) {
            HomeScreen()
        }
        composable(AppDestination.Dashboard.route) {
            val dashboardViewModel: DashboardViewModel = hiltViewModel()
            DashboardRoute(viewModel = dashboardViewModel,
                navController = navController)
        }
    }
}