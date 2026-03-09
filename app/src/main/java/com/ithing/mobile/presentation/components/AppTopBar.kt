package com.ithing.mobile.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ithing.mobile.core.session.SessionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    sessionManager: SessionManager,
    onMenuClick: () -> Unit,
    onChangePassword: () -> Unit,
    onHelpClick: () -> Unit,
    onLogout: () -> Unit,
    onNotificationClick: () -> Unit = {}
) {

    var profileMenuExpanded by remember { mutableStateOf(false) }
    var oemLogoUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        oemLogoUrl = sessionManager.getOemLogo()
    }

    TopAppBar(
        title = {
            Text(
                text = "iThing",
                style = MaterialTheme.typography.titleLarge
            )
        },
        actions = {

            // 🔔 Notification Icon
            IconButton(onClick = onNotificationClick) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Notifications"
                )
            }

            // 🖼 OEM Logo
            if (!oemLogoUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = oemLogoUrl,
                    contentDescription = "OEM Logo",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .padding(end = 8.dp),
                    contentScale = ContentScale.Crop
                )
            }

            // 👤 Profile Icon
            Box {
                IconButton(onClick = { profileMenuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = "Profile"
                    )
                }

                DropdownMenu(
                    expanded = profileMenuExpanded,
                    onDismissRequest = { profileMenuExpanded = false }
                ) {

                    DropdownMenuItem(
                        text = { Text("Change Password") },
                        onClick = {
                            profileMenuExpanded = false
                            onChangePassword()
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Help") },
                        onClick = {
                            profileMenuExpanded = false
                            onHelpClick()
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Logout") },
                        onClick = {
                            profileMenuExpanded = false
                            onLogout()
                        }
                    )
                }
            }

            // ≡ Drawer Menu Icon
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu"
                )
            }
        }
    )
}