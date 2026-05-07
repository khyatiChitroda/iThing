package com.ithing.mobile.presentation.navigation


sealed class AppDestination(val route: String) {

    object Splash : AppDestination("splash")
    object Login : AppDestination("login")
    object AppShell : AppDestination("app_shell")
    object Reports : AppDestination("reports")
    object Dashboard : AppDestination("dashboard")

    object Home : AppDestination("home")

    object ForgotPassword : AppDestination("forgot_password")
    object ChangePassword : AppDestination("change_password")
    object Help : AppDestination("help")

}
