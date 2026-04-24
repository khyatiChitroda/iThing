package com.ithing.mobile.presentation.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ithing.mobile.presentation.components.IThingCard
import com.ithing.mobile.presentation.components.IThingPrimaryButton
import com.ithing.mobile.presentation.components.SectionHeader
import com.ithing.mobile.presentation.theme.AccentBlue
import com.ithing.mobile.presentation.theme.NavyBlue

// Data models
data class UserProfile(
    val name: String,
    val email: String,
    val role: String,
    val company: String,
    val avatarUrl: String? = null
)

data class SettingsItem(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val category: SettingsCategory
)

enum class SettingsCategory {
    ACCOUNT, PREFERENCES, SECURITY, ABOUT
}

// Sample data
fun getSampleProfile(): UserProfile {
    return UserProfile(
        name = "John Doe",
        email = "john.doe@company.com",
        role = "Administrator",
        company = "ABC Manufacturing"
    )
}

fun getSettingsItems(): List<SettingsItem> {
    return listOf(
        SettingsItem(
            id = "profile",
            title = "Edit Profile",
            description = "Update your personal information",
            icon = Icons.Default.Person,
            category = SettingsCategory.ACCOUNT
        ),
        SettingsItem(
            id = "notifications",
            title = "Notifications",
            description = "Manage notification preferences",
            icon = Icons.Default.Notifications,
            category = SettingsCategory.PREFERENCES
        ),
        SettingsItem(
            id = "language",
            title = "Language",
            description = "Change app language",
            icon = Icons.Default.Language,
            category = SettingsCategory.PREFERENCES
        ),
        SettingsItem(
            id = "dark_mode",
            title = "Dark Mode",
            description = "Toggle dark theme",
            icon = Icons.Default.DarkMode,
            category = SettingsCategory.PREFERENCES
        ),
        SettingsItem(
            id = "security",
            title = "Security",
            description = "Password and security settings",
            icon = Icons.Default.Security,
            category = SettingsCategory.SECURITY
        ),
        SettingsItem(
            id = "privacy",
            title = "Privacy Policy",
            description = "View privacy policy",
            icon = Icons.Default.PrivacyTip,
            category = SettingsCategory.ABOUT
        ),
        SettingsItem(
            id = "logout",
            title = "Logout",
            description = "Sign out of your account",
            icon = Icons.Default.Logout,
            category = SettingsCategory.ABOUT
        )
    )
}

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    profile: UserProfile = getSampleProfile(),
    settingsItems: List<SettingsItem> = getSettingsItems(),
    onItemClick: (SettingsItem) -> Unit = {},
    onLogout: () -> Unit = {}
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ProfileHeader(profile = profile)
        }
        
        item {
            SettingsList(
                settingsItems = settingsItems,
                onItemClick = onItemClick,
                onLogout = onLogout
            )
        }
        
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ProfileHeader(profile: UserProfile) {
    IThingCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = 2
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = profile.name.split(" ").map { it.firstOrNull()?.uppercase() ?: "" }.joinToString(""),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Name and Role
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = profile.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = profile.role,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = profile.company,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Email
            Text(
                text = profile.email,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
            
            // Edit Profile Button
            IThingPrimaryButton(
                text = "Edit Profile",
                onClick = { /* TODO: Navigate to profile edit */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(40.dp)
            )
        }
    }
}

@Composable
private fun SettingsList(
    settingsItems: List<SettingsItem>,
    onItemClick: (SettingsItem) -> Unit,
    onLogout: () -> Unit
) {
    val groupedItems = settingsItems.groupBy { it.category }
    
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SettingsCategory.entries.forEach { category ->
            groupedItems[category]?.let { items ->
                SettingsCategorySection(
                    title = getCategoryTitle(category),
                    items = items,
                    onItemClick = onItemClick,
                    onLogout = if (category == SettingsCategory.ABOUT) onLogout else { {} }
                )
            }
        }
    }
}

@Composable
private fun SettingsCategorySection(
    title: String,
    items: List<SettingsItem>,
    onItemClick: (SettingsItem) -> Unit,
    onLogout: () -> Unit
) {
    IThingCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = 1
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SectionHeader(
                title = title,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            items.forEachIndexed { index, item ->
                SettingsItemRow(
                    item = item,
                    onItemClick = onItemClick,
                    onLogout = onLogout,
                    showDivider = index < items.lastIndex
                )
            }
        }
    }
}

@Composable
private fun SettingsItemRow(
    item: SettingsItem,
    onItemClick: (SettingsItem) -> Unit,
    onLogout: () -> Unit,
    showDivider: Boolean
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    if (item.id == "logout") {
                        onLogout()
                    } else {
                        onItemClick(item)
                    }
                }
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon and Text
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = if (item.id == "logout") {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    modifier = Modifier.size(24.dp)
                )
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = if (item.id == "logout") {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                    
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Arrow Icon
            Icon(
                imageVector = Icons.Default.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
        
        if (showDivider) {
            Divider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 1.dp,
                modifier = Modifier.padding(start = 40.dp)
            )
        }
    }
}

@Composable
private fun getCategoryTitle(category: SettingsCategory): String {
    return when (category) {
        SettingsCategory.ACCOUNT -> "Account"
        SettingsCategory.PREFERENCES -> "Preferences"
        SettingsCategory.SECURITY -> "Security"
        SettingsCategory.ABOUT -> "About"
    }
}
