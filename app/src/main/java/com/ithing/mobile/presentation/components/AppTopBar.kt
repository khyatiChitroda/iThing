package com.ithing.mobile.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ithing.mobile.R
import com.ithing.mobile.core.session.SessionManager
import com.ithing.mobile.presentation.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    sessionManager: SessionManager,
    onChangePassword: () -> Unit,
    onHelpClick: () -> Unit,
    onLogout: () -> Unit,
    onNotificationClick: () -> Unit = {}
) {
    var profileMenuExpanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Image(
                painter = painterResource(id = R.drawable.ithing_logo),
                contentDescription = "iThing",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .height(40.dp)
                    .padding(start = 8.dp, end = 8.dp)
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent,
            titleContentColor = White,
            actionIconContentColor = White
        ),
        actions = {
            IconButton(onClick = onNotificationClick) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Notifications"
                )
            }

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
        }
    )
}
