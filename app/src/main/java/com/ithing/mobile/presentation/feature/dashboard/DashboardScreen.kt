package com.ithing.mobile.presentation.feature.dashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun DashboardScreen(onLogoutClick: () -> Unit,
                    onChangePasswordClick : () -> Unit) {

    Column(modifier = Modifier.fillMaxSize()) {

        Button(onClick = onLogoutClick) {
            Text("Logout")
        }

        Button(onClick = onChangePasswordClick) {
            Text("Change Password")
        }
    }


}
