package com.ithing.mobile.presentation.navigation


sealed class AppDestination(val route: String) {

    object Splash : AppDestination("splash")
    object Login : AppDestination("login")

    object Dashboard : AppDestination("dashboard")

    object Home : AppDestination("home")

    object ForgotPassword : AppDestination("forgot_password")

    // Future examples:
    // object Reports : AppDestination("reports")
    // object Settings : AppDestination("settings")
}