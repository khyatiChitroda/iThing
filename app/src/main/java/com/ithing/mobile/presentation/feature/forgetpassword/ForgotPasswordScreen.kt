package com.ithing.mobile.presentation.feature.forgetpassword

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ForgotPasswordScreen(
    uiState: ForgotPasswordUiState,
    onEmailChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onBackToLogin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Request Reset Link",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.email,
            onValueChange = onEmailChange,
            label = { Text("Email") },
            isError = uiState.emailError != null,
            modifier = Modifier.fillMaxWidth()
        )

        uiState.emailError?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading
        ) {
            Text("Request Reset Link")
        }

        Spacer(Modifier.height(16.dp))

        TextButton(onClick = onBackToLogin) {
            Text("Back to Login")
        }

        uiState.errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error
            )
        }

        uiState.successMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
