package com.ithing.mobile.presentation.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomNavBar(navController: NavHostController) {

    val items = listOf(
        BottomNavItem.Dashboard,
        BottomNavItem.Reports
    )

    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    NavigationBar {

        items.forEach { item ->

            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title
                    )
                },

                label = { Text(item.title) },

                selected = currentRoute == item.route,

                onClick = {

                    navController.navigate(item.route) {

                        // Prevent multiple copies
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }

                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}