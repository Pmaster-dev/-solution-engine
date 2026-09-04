package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = IndigoPrimaryLight,
    onPrimary = OnDarkTextPrimary,
    secondary = CyanSecondary,
    onSecondary = OnDarkTextPrimary,
    tertiary = AmberTertiary,
    background = DarkBackground,
    onBackground = OnDarkTextPrimary,
    surface = DarkSurface,
    onSurface = OnDarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = OnDarkTextSecondary
)

private val LightColorScheme = lightColorScheme(
    primary = IndigoPrimary,
    onPrimary = LightSurface,
    secondary = CyanSecondary,
    onSecondary = LightSurface,
    tertiary = AmberTertiary,
    background = LightBackground,
    onBackground = OnLightTextPrimary,
    surface = LightSurface,
    onSurface = OnLightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = OnLightTextSecondary
)

@Composable
fun SolutionsEngineTheme(
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

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    SolutionsEngineTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}

