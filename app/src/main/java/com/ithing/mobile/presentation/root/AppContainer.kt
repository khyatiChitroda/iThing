package com.ithing.mobile.presentation.root

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ithing.mobile.presentation.components.AppTopBar
import com.ithing.mobile.presentation.theme.Theme1Blue
import com.ithing.mobile.presentation.theme.Theme2Navy

@Composable
fun AppContainer(
    topBar: (@Composable () -> Unit)? = null,
    bottomBar: (@Composable () -> Unit)? = null,
    content: @Composable (Modifier) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Theme1Blue, Theme2Navy)
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Column {
                    topBar?.invoke()
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .shadow(4.dp)
                    )
                }
            },
            bottomBar = { bottomBar?.invoke() }
        ) { innerPadding ->
            content(Modifier.padding(innerPadding))
        }
    }
}