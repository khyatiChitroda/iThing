package com.ithing.mobile.presentation.theme

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Responsive design utilities for different screen sizes
sealed class WindowSizeClass {
    object Compact : WindowSizeClass()
    object Medium : WindowSizeClass()
    object Expanded : WindowSizeClass()
}

@Composable
fun rememberWindowSizeClass(): WindowSizeClass {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp
    
    return when {
        screenWidthDp < 600.dp -> WindowSizeClass.Compact
        screenWidthDp < 840.dp -> WindowSizeClass.Medium
        else -> WindowSizeClass.Expanded
    }
}

// Responsive spacing values
object ResponsiveSpacing {
    @Composable
    fun paddingSmall(): Dp = when (rememberWindowSizeClass()) {
        is WindowSizeClass.Compact -> 8.dp
        is WindowSizeClass.Medium -> 12.dp
        is WindowSizeClass.Expanded -> 16.dp
    }
    
    @Composable
    fun paddingMedium(): Dp = when (rememberWindowSizeClass()) {
        is WindowSizeClass.Compact -> 12.dp
        is WindowSizeClass.Medium -> 16.dp
        is WindowSizeClass.Expanded -> 20.dp
    }
    
    @Composable
    fun paddingLarge(): Dp = when (rememberWindowSizeClass()) {
        is WindowSizeClass.Compact -> 16.dp
        is WindowSizeClass.Medium -> 20.dp
        is WindowSizeClass.Expanded -> 24.dp
    }
}

// Responsive modifiers
@Composable
fun Modifier.responsivePadding(): Modifier = this.padding(
    horizontal = ResponsiveSpacing.paddingMedium(),
    vertical = ResponsiveSpacing.paddingMedium()
)

@Composable
fun Modifier.responsiveMaxWidth(): Modifier = this.widthIn(
    max = when (rememberWindowSizeClass()) {
        is WindowSizeClass.Compact -> Dp.Infinity
        is WindowSizeClass.Medium -> 600.dp
        is WindowSizeClass.Expanded -> 800.dp
    }
)

// Responsive grid columns
@Composable
fun getGridColumns(): Int {
    return when (rememberWindowSizeClass()) {
        is WindowSizeClass.Compact -> 1
        is WindowSizeClass.Medium -> 2
        is WindowSizeClass.Expanded -> 3
    }
}
