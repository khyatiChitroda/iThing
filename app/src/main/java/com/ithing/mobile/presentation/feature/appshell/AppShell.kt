package com.ithing.mobile.presentation.feature.appshell

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ithing.mobile.presentation.components.AppDrawer
import com.ithing.mobile.presentation.components.AppShellViewModel
import com.ithing.mobile.presentation.components.AppTopBar
import com.ithing.mobile.presentation.components.BottomNavBar
import com.ithing.mobile.presentation.feature.dashboard.DashboardRoute
import com.ithing.mobile.presentation.feature.reports.ReportsRoute
import com.ithing.mobile.presentation.navigation.AppDestination
import kotlinx.coroutines.launch

@Composable
fun AppShell(
    navController: NavHostController
) {
    val viewModel: AppShellViewModel = hiltViewModel()
    val sessionManager = viewModel.sessionManager

    val innerNavController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {

            AppDrawer(
                onNavigate = { route: String ->
                    scope.launch {
                        drawerState.close()
                        innerNavController.navigate(route)
                    }
                },

                onHelpClick = {
                    scope.launch {
                        drawerState.close()
                        navController.navigate("help")
                    }
                },

                onLogout = {
                    scope.launch {
                        drawerState.close()
                        sessionManager.clearSession()

                        navController.navigate(AppDestination.Login.route) {
                            popUpTo(0)
                        }
                    }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                AppTopBar(
                    sessionManager = sessionManager,
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onChangePassword = {
                        navController.navigate("change_password")
                    },
                    onHelpClick = {
                        navController.navigate("help")
                    },
                    onLogout = {
                        scope.launch {
                            sessionManager.clearSession()
                            navController.navigate("login") {
                                popUpTo(0)
                            }
                        }
                    }
                )
            },
            bottomBar = {
                BottomNavBar(innerNavController)
            }
        ) { padding ->

            NavHost(
                navController = innerNavController,
                startDestination = "dashboard",
                modifier = Modifier.padding(padding)
            ) {

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
}