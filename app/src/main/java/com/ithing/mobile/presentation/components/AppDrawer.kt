package com.ithing.mobile.presentation.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AppDrawer(
    onNavigate: (String) -> Unit,
    onHelpClick: () -> Unit,
    onLogout: () -> Unit
) {

    ModalDrawerSheet {

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "iThing",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(start = 16.dp, bottom = 16.dp)
        )

        HorizontalDivider()

        DrawerItem(
            icon = Icons.Filled.Dashboard,
            label = "Dashboard",
            onClick = { onNavigate("dashboard") }
        )

        DrawerItem(
            icon = Icons.Filled.Assessment,
            label = "Reports",
            onClick = { onNavigate("reports") }
        )

        DrawerItem(
            icon = Icons.AutoMirrored.Filled.HelpOutline,
            label = "Help",
            onClick = onHelpClick
        )

        Spacer(modifier = Modifier.weight(1f))

        HorizontalDivider()

        DrawerItem(
            icon = Icons.AutoMirrored.Filled.Logout,
            label = "Logout",
            onClick = onLogout
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}
