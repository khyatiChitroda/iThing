package com.ithing.mobile.presentation.feature.changepassword

import androidx.compose.foundation.Image
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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ithing.mobile.R
import com.ithing.mobile.presentation.components.IThingCard
import com.ithing.mobile.presentation.components.IThingPrimaryButton
import com.ithing.mobile.presentation.components.IThingTextField
import com.ithing.mobile.presentation.theme.DarkText
import com.ithing.mobile.presentation.theme.Slate400
import com.ithing.mobile.presentation.theme.Slate600
import com.ithing.mobile.presentation.theme.WebsiteBlue

@Composable
fun ChangePasswordScreen(
    uiState: ChangePasswordUiState,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit
) {
    val enabled =
        !uiState.isLoading &&
        uiState.newPasswordError == null &&
        uiState.confirmPasswordError == null &&
        uiState.newPassword.isNotBlank() &&
        uiState.confirmPassword.isNotBlank()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.ithing_logo),
                contentDescription = "iTHING",
                modifier = Modifier
                    .padding(top = 32.dp, bottom = 32.dp)
                    .width(224.dp),
                contentScale = androidx.compose.ui.layout.ContentScale.Fit
            )

            IThingCard(
                modifier = Modifier.fillMaxWidth(),
                elevation = 6,
                cornerRadius = 16.dp
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 32.dp),
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Change Password",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 20.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                        ),
                        color = WebsiteBlue,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Text(
                        text = "Enter a new password to continue",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                        color = Slate600,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                    )

                    IThingTextField(
                        value = uiState.newPassword,
                        onValueChange = onNewPasswordChange,
                        label = null,
                        placeholder = "New Password",
                        isPassword = true,
                        isError = uiState.newPasswordError != null,
                        errorMessage = uiState.newPasswordError,
                        cornerRadius = 12.dp,
                        textColor = DarkText,
                        placeholderColor = Slate400
                    )

                    IThingTextField(
                        value = uiState.confirmPassword,
                        onValueChange = onConfirmPasswordChange,
                        label = null,
                        placeholder = "Confirm Password",
                        isPassword = true,
                        isError = uiState.confirmPasswordError != null,
                        errorMessage = uiState.confirmPasswordError,
                        cornerRadius = 12.dp,
                        textColor = DarkText,
                        placeholderColor = Slate400
                    )

                    uiState.errorMessage?.let {
                        Text(
                            text = it,
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }

                    uiState.successMessage?.let {
                        Text(
                            text = it,
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }

                    IThingPrimaryButton(
                        text = "Change Password",
                        onClick = onSubmit,
                        enabled = enabled,
                        isLoading = uiState.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                            .height(48.dp),
                        cornerRadius = 12.dp
                    )

                    TextButton(onClick = onBack) {
                        Text("Back", color = WebsiteBlue)
                    }
                }
            }
        }
    }
}
