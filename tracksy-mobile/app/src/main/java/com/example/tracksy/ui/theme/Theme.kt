package com.example.tracksy.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFE2B7F3),
    onPrimary = Color(0xFF231730),
    secondary = Color(0xFFC1A4D7),
    onSecondary = Color(0xFF231730),
    tertiary = Color(0xFFD8C0EA),
    background = Color(0xFF231730),
    onBackground = Color(0xFFF6ECFF),
    surface = Color(0xFF342244),
    onSurface = Color(0xFFF6ECFF),
    error = TracksyErrorRed
)

private val LightColorScheme = lightColorScheme(
    primary = TracksyPrimaryPurple,
    onPrimary = Color.White,
    secondary = TracksyTextSecondary,
    onSecondary = Color.White,
    tertiary = TracksyTextMuted,
    background = TracksyBackgroundLight,
    onBackground = TracksyTextPrimary,
    surface = TracksySurface,
    onSurface = TracksyTextPrimary,
    error = TracksyErrorRed
)

@Composable
fun TracksyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val tracksyColors = if (darkTheme) darkTracksyColors() else lightTracksyColors()

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    CompositionLocalProvider(LocalTracksyColors provides tracksyColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
