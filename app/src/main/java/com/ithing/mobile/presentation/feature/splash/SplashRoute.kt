package com.ithing.mobile.presentation.feature.splash

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.ithing.mobile.presentation.navigation.AppDestination

@Composable
fun SplashRoute(
    navController: NavController,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val destination = viewModel.destination

    LaunchedEffect(destination) {
        when (destination) {
            is SplashDestination.Authenticated -> {
                destination.role

                // For now all roles go to Dashboard
                navController.navigate(AppDestination.AppShell.route) {
                    popUpTo(AppDestination.Splash.route) {
                        inclusive = true
                    }
                }
            }


            SplashDestination.Unauthenticated -> {
                navController.navigate(AppDestination.Login.route) {
                    popUpTo(AppDestination.Splash.route) {
                        inclusive = true
                    }
                }
            }

            null -> Unit
        }
    }

    SplashScreen()
}
