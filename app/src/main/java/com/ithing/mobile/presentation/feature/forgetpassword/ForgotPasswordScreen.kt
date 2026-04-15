package com.ithing.mobile.presentation.feature.forgetpassword

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ithing.mobile.R
import com.ithing.mobile.presentation.theme.DarkText
import com.ithing.mobile.presentation.theme.MutedText
import com.ithing.mobile.presentation.theme.NavyBlue
import com.ithing.mobile.presentation.theme.White

private val ForgotCardShape = RoundedCornerShape(12.dp)
private val ForgotFieldShape = RoundedCornerShape(8.dp)
private val FieldBorder = Color(0xFFE4E9F2)
private val LoginBackgroundEnd = Color(0xFF172554)
private val WebFontFamily = FontFamily.SansSerif

@Composable
fun ForgotPasswordScreen(
    uiState: ForgotPasswordUiState,
    onEmailChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onBackToLogin: () -> Unit
) {
    val hasSuccess = uiState.successMessage != null

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
                modifier = Modifier.padding(top = 26.dp, bottom = 28.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = ForgotCardShape,
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (hasSuccess) "Done Done !!!" else "Request Reset Link",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = WebFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = NavyBlue,
                            fontSize = 20.sp
                        )
                    )

                    Text(
                        text = uiState.successMessage
                            ?: "Please enter the email address you'd like your password reset information sent to",
                        modifier = Modifier.padding(top = 10.dp, bottom = if (hasSuccess) 22.dp else 24.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = WebFontFamily,
                            color = MutedText,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    )

                    if (!hasSuccess) {
                        OutlinedTextField(
                            value = uiState.email,
                            onValueChange = onEmailChange,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = ForgotFieldShape,
                            isError = uiState.emailError != null,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            placeholder = {
                                Text(
                                    text = "Email",
                                    color = Color(0xFFB6C0D3),
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontFamily = WebFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = White,
                                unfocusedContainerColor = White,
                                errorContainerColor = White,
                                focusedBorderColor = NavyBlue,
                                unfocusedBorderColor = FieldBorder,
                                errorBorderColor = MaterialTheme.colorScheme.error,
                                focusedTextColor = DarkText,
                                unfocusedTextColor = DarkText,
                                cursorColor = NavyBlue
                            )
                        )

                        uiState.emailError?.let {
                            MessageText(
                                text = it,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 6.dp, start = 4.dp)
                            )
                        }

                        uiState.errorMessage?.let {
                            MessageText(
                                text = it,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 14.dp)
                            )
                        }

                        Button(
                            onClick = onSubmit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 22.dp, bottom = 18.dp)
                                .height(52.dp),
                            enabled = !uiState.isLoading && uiState.email.isNotBlank(),
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
                                    text = "Request Reset Link",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontFamily = WebFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                )
                            }
                        }
                    }

                    Text(
                        text = "Back to Login",
                        modifier = Modifier.clickable(onClick = onBackToLogin),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = WebFontFamily,
                            color = NavyBlue,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
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
        contentScale = ContentScale.Fit
    )
}

@Composable
private fun MessageText(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        style = MaterialTheme.typography.bodySmall.copy(fontFamily = WebFontFamily)
    )
}
