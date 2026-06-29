package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = SportAccentLime,
    secondary = SportAccentCyan,
    tertiary = SportAccentOrange,
    background = DarkOnyxBackground,
    surface = DarkOnyxSurface,
    surfaceVariant = DarkOnyxSurfaceVariant,
    onPrimary = Color(0xFF000000), // Pure black for optimum contrast on neon lime
    onSecondary = Color(0xFF000000),
    onTertiary = Color(0xFFFFFFFF),
    onBackground = DarkOnyxTextPrimary,
    onSurface = DarkOnyxTextPrimary,
    onSurfaceVariant = DarkOnyxTextSecondary,
    error = Color(0xFFFF5252),
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = LightSportAccent,
    secondary = LightSportSecondary,
    tertiary = SportAccentOrange,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = LightTextPrimary,
    onSurface = LightTextPrimary,
    onSurfaceVariant = LightTextSecondary,
    error = Color(0xFFD32F2F),
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // We prefer our premium hand-crafted athletic color scheme over default system dynamic color palettes
    // to maintain the true energetic brand look essential for gym, bodybuilding, and fitness experiences.
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
