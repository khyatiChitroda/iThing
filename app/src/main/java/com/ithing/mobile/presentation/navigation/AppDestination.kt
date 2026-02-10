package com.ithing.mobile.presentation.navigation


sealed class AppDestination(val route: String) {

    object Login : AppDestination("login")

    object Dashboard : AppDestination("dashboard")

    // Future examples:
    // object Reports : AppDestination("reports")
    // object Settings : AppDestination("settings")
}