package com.ithing.mobile.presentation.feature.splash

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ithing.mobile.R

@Composable
fun SplashScreen() {

    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    val background = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn() + slideInVertically { -it / 2 }
        ) {
            Image(
                painter = painterResource(R.drawable.ithing_logo),
                contentDescription = "iThing Logo",
                modifier = Modifier.height(70.dp)
            )
        }

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn() + slideInVertically { it / 2 }
        ) {
            Image(
                painter = painterResource(R.drawable.splash_illustration),
                contentDescription = "Illustration",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            )
        }

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "Empowering Businesses with\nSmarter IoT Insights!",
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Device management, data collection, processing and visualization for your IoT solution",
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    lineHeight = 18.sp
                )
            }
        }
    }
}