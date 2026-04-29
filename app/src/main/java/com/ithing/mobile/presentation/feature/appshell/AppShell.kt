package com.ithing.mobile.presentation.feature.appshell

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ithing.mobile.presentation.components.AppShellViewModel
import com.ithing.mobile.presentation.components.AppTopBar
import com.ithing.mobile.presentation.components.BottomNavBar
import com.ithing.mobile.presentation.feature.dashboard.DashboardRoute
import com.ithing.mobile.presentation.feature.home.HomeScreen
import com.ithing.mobile.presentation.feature.reports.ReportsRoute
import com.ithing.mobile.presentation.navigation.AppDestination
import com.ithing.mobile.presentation.root.AppContainer
import com.ithing.mobile.presentation.theme.Transparent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AppShell(
    navController: NavHostController
) {
    val viewModel: AppShellViewModel = hiltViewModel()
    val sessionManager = viewModel.sessionManager
    val innerNavController = rememberNavController()
    val scope = rememberCoroutineScope()

    LaunchedEffect(sessionManager) {
        sessionManager.sessionExpiredEvents.collect {
            navController.navigate(AppDestination.Login.route) {
                popUpTo(0)
            }
        }
    }

    LaunchedEffect(sessionManager) {
        val expiryMillis = sessionManager.getTokenExpiry() ?: return@LaunchedEffect
        val delayMillis = expiryMillis - System.currentTimeMillis()
        if (delayMillis <= 0) {
            sessionManager.expireSession()
            return@LaunchedEffect
        }
        delay(delayMillis)
        sessionManager.expireSession()
    }

    AppContainer(
        showTopBar = true,
        topBar = {
            AppTopBar(
                sessionManager = sessionManager,
                onChangePassword = {
                    navController.navigate("change_password")
                },
                onHelpClick = {
                    navController.navigate("help")
                },
                onLogout = {
                    scope.launch {
                        sessionManager.clearSession()
                        navController.navigate(AppDestination.Login.route) {
                            popUpTo(0)
                        }
                    }
                }
            )
        },
        bottomBar = {
            BottomNavBar(innerNavController)
        }
    ) { modifier ->

        NavHost(
            navController = innerNavController,
            startDestination = "home",
            modifier = modifier
        ) {
            composable("home") {
                HomeScreen()
            }

            composable("dashboard") {
                DashboardRoute(
                    viewModel = hiltViewModel(),
                    navController = navController
                )
            }

            composable("reports") {
                ReportsRoute(
                    viewModel = hiltViewModel(),
                    navController = navController
                )
            }
        }
    }
}
