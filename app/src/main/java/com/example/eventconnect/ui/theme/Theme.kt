package com.example.eventconnect.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

val BluePrimary = Color(0xFF007BFF)
val WhitePrimary = Color(0xFFFFFFFF)
val GrayPrimary = Color(0xFF808080)
val BlackPrimary = Color(0xFF000000)
val BluePrimaryVariant = Color(0xFF005FCC)
private val LightColorScheme = lightColorScheme(
    primary = BluePrimary,
    onPrimary = WhitePrimary,
    primaryContainer = BluePrimaryVariant,
    onPrimaryContainer = WhitePrimary,
    secondary = GrayPrimary,
    onSecondary = WhitePrimary,
    secondaryContainer = GrayPrimary,
    onSecondaryContainer = WhitePrimary,
    tertiary = BluePrimary,
    onTertiary = WhitePrimary,
    background = WhitePrimary,
    onBackground = BlackPrimary,
    surface = WhitePrimary,
    onSurface = BlackPrimary,
    surfaceVariant = Color(0xFFE0E0E0),
    onSurfaceVariant = BlackPrimary,
    error = Color(0xFFB00020),
    onError = WhitePrimary,
    outline = GrayPrimary
)

private val DarkColorScheme = darkColorScheme(
    primary = BluePrimary,
    onPrimary = BlackPrimary,
    primaryContainer = BluePrimaryVariant,
    onPrimaryContainer = BlackPrimary,
    secondary = GrayPrimary,
    onSecondary = BlackPrimary,
    secondaryContainer = GrayPrimary,
    onSecondaryContainer = BlackPrimary,
    tertiary = BluePrimary,
    onTertiary = BlackPrimary,
    background = BlackPrimary,
    onBackground = WhitePrimary,
    surface = BlackPrimary,
    onSurface = WhitePrimary,
    surfaceVariant = Color(0xFF424242),
    onSurfaceVariant = WhitePrimary,
    error = Color(0xFFCF6679),
    onError = BlackPrimary,
    outline = GrayPrimary
)

@Composable
fun EventConnectTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
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
