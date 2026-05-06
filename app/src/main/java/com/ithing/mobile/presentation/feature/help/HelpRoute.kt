package com.ithing.mobile.presentation.feature.help

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController

@Composable
fun HelpRoute(
    navController: NavHostController
) {
    HelpScreen(
        onBack = { navController.popBackStack() }
    )
}

