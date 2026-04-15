package com.ithing.mobile.presentation.feature.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ithing.mobile.R
import com.ithing.mobile.presentation.theme.DarkText
import com.ithing.mobile.presentation.theme.MutedText
import com.ithing.mobile.presentation.theme.NavyBlue
import com.ithing.mobile.presentation.theme.White

private val LoginCardShape = RoundedCornerShape(12.dp)
private val LoginFieldShape = RoundedCornerShape(8.dp)
private val FieldBorder = Color(0xFFE4E9F2)
private val LinkBlue = Color(0xFF2E4EA3)
private val LoginBackgroundEnd = Color(0xFF172554)
private val WebFontFamily = FontFamily.SansSerif

@Composable
fun LoginScreen(
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
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(NavyBlue, LoginBackgroundEnd)
                )
            )
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 10.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IThingLogo(
                modifier = Modifier
                    .padding(top = 26.dp, bottom = 28.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = LoginCardShape,
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Hi, Welcome Back",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = WebFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = NavyBlue,
                            fontSize = 20.sp
                        )
                    )

                    Text(
                        text = "Enter Credentials to continue",
                        modifier = Modifier.padding(top = 10.dp, bottom = 24.dp),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = WebFontFamily,
                            color = MutedText,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    )

                    LoginField(
                        value = uiState.username,
                        placeholder = "Email",
                        onValueChange = onUsernameChange,
                        isError = uiState.usernameError != null,
                        keyboardType = KeyboardType.Email
                    )

                    uiState.usernameError?.let {
                        ErrorText(
                            text = it,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp, start = 4.dp)
                        )
                    }

                    LoginField(
                        value = uiState.password,
                        placeholder = "Password",
                        onValueChange = onPasswordChange,
                        isError = uiState.passwordError != null,
                        visualTransformation = if (uiState.isPasswordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        keyboardType = KeyboardType.Password,
                        trailingIcon = {
                            IconButton(onClick = onTogglePasswordVisibility) {
                                Icon(
                                    imageVector = if (uiState.isPasswordVisible) {
                                        Icons.Default.VisibilityOff
                                    } else {
                                        Icons.Default.Visibility
                                    },
                                    contentDescription = "Toggle password visibility",
                                    tint = Color(0xFF9AA6BF)
                                )
                            }
                        },
                        modifier = Modifier.padding(top = 18.dp)
                    )

                    uiState.passwordError?.let {
                        ErrorText(
                            text = it,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp, start = 4.dp)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 14.dp),
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
                                modifier = Modifier.size(18.dp),
                                colors = CheckboxDefaults.colors(
                                    checkedColor = LinkBlue,
                                    uncheckedColor = FieldBorder,
                                    checkmarkColor = White
                                )
                            )
                            Text(
                                text = "Remember me",
                                modifier = Modifier.padding(start = 8.dp),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = WebFontFamily,
                                    color = NavyBlue,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                            )
                        }

                        Text(
                            text = "Forgot Password?",
                            modifier = Modifier.clickable(onClick = onForgotPasswordClick),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = WebFontFamily,
                                color = NavyBlue,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        )
                    }

                    uiState.errorMessage?.let {
                        ErrorText(
                            text = it,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 14.dp)
                        )
                    }

                    Button(
                        onClick = onLoginClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 22.dp)
                            .height(52.dp),
                        enabled = enabled,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NavyBlue,
                            contentColor = White,
                            disabledContainerColor = NavyBlue.copy(alpha = 0.5f),
                            disabledContentColor = White.copy(alpha = 0.8f)
                        )
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = White
                            )
                        } else {
                            Text(
                                text = "Login",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = WebFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                            )
                        }
                    }
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
        contentScale = ContentScale.Fit
    )
}

@Composable
private fun LoginField(
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    isError: Boolean,
    modifier: Modifier = Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardType: KeyboardType = KeyboardType.Text,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        shape = LoginFieldShape,
        isError = isError,
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        placeholder = {
            Text(
                text = placeholder,
                color = Color(0xFFB6C0D3),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = WebFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            )
        },
        trailingIcon = trailingIcon,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = White,
            unfocusedContainerColor = White,
            errorContainerColor = White,
            focusedBorderColor = LinkBlue,
            unfocusedBorderColor = FieldBorder,
            errorBorderColor = MaterialTheme.colorScheme.error,
            focusedTextColor = DarkText,
            unfocusedTextColor = DarkText,
            cursorColor = LinkBlue
        )
    )
}

@Composable
private fun ErrorText(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodySmall
    )
}
