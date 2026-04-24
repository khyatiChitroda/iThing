package com.ithing.mobile.presentation.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = NavyBlue,
    onPrimary = White,
    primaryContainer = LightBlue,
    onPrimaryContainer = White,
    secondary = AccentBlue,
    onSecondary = White,
    secondaryContainer = LightBlue.copy(alpha = 0.2f),
    onSecondaryContainer = NavyBlue,
    background = NavyBlue,
    onBackground = White,
    surface = CardBackground,
    onSurface = DarkText,
    surfaceVariant = BorderColor,
    onSurfaceVariant = MutedText,
    outline = BorderColor,
    outlineVariant = BorderColor.copy(alpha = 0.5f),
    error = ErrorRed,
    onError = White,
    errorContainer = ErrorRed.copy(alpha = 0.1f),
    onErrorContainer = ErrorRed
)

private val DarkColorScheme = darkColorScheme(
    primary = AccentBlue,
    onPrimary = White,
    primaryContainer = NavyBlue,
    onPrimaryContainer = White,
    secondary = LightBlue,
    onSecondary = DarkText,
    secondaryContainer = NavyBlue.copy(alpha = 0.3f),
    onSecondaryContainer = White,
    background = DarkBackground,
    onBackground = White,
    surface = DarkCardBg,
    onSurface = White,
    surfaceVariant = DarkBorder,
    onSurfaceVariant = LightText,
    outline = DarkBorder,
    outlineVariant = DarkBorder.copy(alpha = 0.5f),
    error = ErrorRed,
    onError = White,
    errorContainer = ErrorRed.copy(alpha = 0.2f),
    onErrorContainer = ErrorRed
)

@Composable
fun IThingMobileTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}