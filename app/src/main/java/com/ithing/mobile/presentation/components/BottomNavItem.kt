package com.ithing.mobile.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {

    object Dashboard : BottomNavItem(
        route = "dashboard",
        title = "Dashboard",
        icon = Icons.Filled.Dashboard
    )

    object Reports : BottomNavItem(
        route = "reports",
        title = "Reports",
        icon = Icons.Filled.Assessment
    )
}