package ru.eafedorova.whatifapp.presentation.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
    surfaceContainer = md_theme_dark_surfaceContainer,
    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
    secondaryContainer = md_theme_dark_secondaryContainer,
    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
    outline = md_theme_dark_outline,
    error = md_theme_dark_error,
    onError = md_theme_dark_onError
)

private val LightColorScheme = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    surfaceContainer = md_theme_light_surfaceContainer,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    outline = md_theme_light_outline,
    error = md_theme_light_error,
    onError = md_theme_light_onError
)

private val buttonShape = RoundedCornerShape(16.dp)
private val containerShape = RoundedCornerShape(24.dp)

val Shapes = Shapes(
    extraSmall = containerShape,
    small = buttonShape,
    medium = buttonShape,
    large = containerShape,
    extraLarge = containerShape
)

data class CustomColors(
    val attention: androidx.compose.ui.graphics.Color
)

val DarkCustomColors = CustomColors(
    attention = md_theme_dark_attention
)

val LightCustomColors = CustomColors(
    attention = md_theme_light_attention
)

val LocalCustomColors = staticCompositionLocalOf { DarkCustomColors }

@Composable
fun WhatIfAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val customColors = if (darkTheme) DarkCustomColors else LightCustomColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalCustomColors provides customColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}