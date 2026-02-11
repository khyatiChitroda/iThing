package com.ithing.mobile.presentation.feature.dashboard

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun DashboardScreen(onLogoutClick: () -> Unit) {

    Button(onClick = onLogoutClick) {
        Text("Logout")
    }
}
