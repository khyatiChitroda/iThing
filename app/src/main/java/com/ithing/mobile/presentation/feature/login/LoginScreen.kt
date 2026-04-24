package com.ithing.mobile.presentation.feature.login

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ithing.mobile.R
import com.ithing.mobile.presentation.components.IThingPrimaryButton
import com.ithing.mobile.presentation.components.IThingTextField
import com.ithing.mobile.presentation.components.IThingCard
import com.ithing.mobile.presentation.theme.DarkText
import com.ithing.mobile.presentation.theme.MutedText
import com.ithing.mobile.presentation.theme.NavyBlue
import com.ithing.mobile.presentation.theme.Slate400
import com.ithing.mobile.presentation.theme.Slate600
import com.ithing.mobile.presentation.theme.Theme1Blue
import com.ithing.mobile.presentation.theme.Theme2Navy
import com.ithing.mobile.presentation.theme.WebsiteBlue

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    uiState: LoginUiState,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onRememberMeChange: (Boolean) -> Unit,
    onForgotPasswordClick: () -> Unit
) {
    val enabled = !uiState.isLoading &&
        uiState.usernameError == null &&
        uiState.passwordError == null &&
        uiState.username.isNotBlank() &&
        uiState.password.isNotBlank()

    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IThingLogo(
                modifier = Modifier
                    .padding(top = 32.dp, bottom = 32.dp)
            )

            IThingCard(
                modifier = Modifier.fillMaxWidth(),
                elevation = 6,
                cornerRadius = 16.dp
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Hi, Welcome Back",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = WebsiteBlue,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Enter Credentials to continue",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                        color = Slate600,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 20.dp, bottom = 8.dp)
                    )

                    IThingTextField(
                        value = uiState.username,
                        onValueChange = onUsernameChange,
                        label = null,
                        placeholder = "Email",
                        isError = uiState.usernameError != null,
                        errorMessage = uiState.usernameError,
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next,
                        cornerRadius = 12.dp,
                        textColor = DarkText,
                        placeholderColor = Slate400
                    )

                    IThingTextField(
                        value = uiState.password,
                        onValueChange = onPasswordChange,
                        label = null,
                        placeholder = "Password",
                        isPassword = !uiState.isPasswordVisible,
                        isError = uiState.passwordError != null,
                        errorMessage = uiState.passwordError,
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                        trailingIcon = if (uiState.isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        onTrailingIconClick = onTogglePasswordVisibility,
                        cornerRadius = 12.dp,
                        textColor = DarkText,
                        placeholderColor = Slate400
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { onRememberMeChange(!uiState.rememberMe) }
                        ) {
                            Checkbox(
                                checked = uiState.rememberMe,
                                onCheckedChange = onRememberMeChange,
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MaterialTheme.colorScheme.primary,
                                    uncheckedColor = WebsiteBlue,
                                    checkmarkColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Remember me",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                                color = WebsiteBlue
                            )
                        }

                        Text(
                            text = "Forgot Password?",
                            modifier = Modifier.clickable(onClick = onForgotPasswordClick),
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                            color = WebsiteBlue,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    uiState.errorMessage?.let {
                        Text(
                            text = it,
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                    }

                    IThingPrimaryButton(
                        text = "Login",
                        onClick = onLoginClick,
                        enabled = enabled,
                        isLoading = uiState.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                            .height(48.dp),
                        cornerRadius = 12.dp
                    )
                }
            }
        }
    }
}

@Composable
private fun IThingLogo(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.ithing_logo),
        contentDescription = "iTHING",
        modifier = modifier.width(224.dp),
        contentScale = androidx.compose.ui.layout.ContentScale.Fit
    )
}

