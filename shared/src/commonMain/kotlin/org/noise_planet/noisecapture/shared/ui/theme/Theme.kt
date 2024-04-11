package org.noise_planet.noisecapture.shared.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

private val DarkColorPalette = darkColors(
    primary = md_yellow_A700,
    primaryVariant = md_yellow_A400,
    secondary = md_yellow_A400,
    background = md_blue_grey_900,
    surface = md_blue_grey_900,
    onPrimary = md_yellow_50,
    onSecondary = md_yellow_50,
    onBackground = md_yellow_50,
    onSurface = md_yellow_50,
)

private val LightColorPalette = lightColors(
    primary = md_blue_grey_gwendall_darker,
    primaryVariant = md_yellow_A400,
    secondary = md_yellow_A400,
    background = md_blue_grey_gwendall,
    surface = md_blue_grey_gwendall,
    onPrimary = md_blue_grey_900,
    onSecondary = md_blue_grey_900,
    onBackground = md_blue_grey_900,
    onSurface = md_blue_grey_900,
)

@Composable
fun AppyxStarterKitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
