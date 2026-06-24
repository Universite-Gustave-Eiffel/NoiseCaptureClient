package org.noiseplanet.noisecapture.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import kotlin.math.min

private val lightColorScheme = lightColorScheme(
    primary = Color.Noise.one.dark,
    onPrimary = Color.Noise.one.light,
    primaryContainer = Color.Noise.one.dark,
    onPrimaryContainer = Color.Noise.one.light,
    inversePrimary = Color.Noise.one.light,
    secondary = Color.Noise.three.dark,
    onSecondary = Color.Noise.three.light,
    secondaryContainer = Color.Noise.three.dark,
    onSecondaryContainer = Color.Noise.three.light,
    tertiary = Color.Noise.five.dark,
    onTertiary = Color.Noise.five.light,
    tertiaryContainer = Color.Noise.five.dark,
    onTertiaryContainer = Color.Noise.five.light,
    background = Color.Surface,
    onBackground = Color.OnSurface,
    surface = Color.Surface,
    onSurface = Color.OnSurface,
    surfaceVariant = Color.SurfaceContainer,
    onSurfaceVariant = Color.OnSurfaceVariant,
    surfaceContainer = Color.SurfaceContainer,
    surfaceTint = Color.Noise.one.light,
    inverseSurface = Color.OnSurface,
    inverseOnSurface = Color.Surface,
    error = Color.Noise.eight.dark,
    onError = Color.Noise.eight.light,
    errorContainer = Color.Noise.eight.light,
    onErrorContainer = Color.Noise.eight.dark,
)

val ColorScheme.accentBlue get() = Color.AccentBlue

private val darkColorScheme = lightColorScheme


@Composable
fun AppTheme(
    darkTheme: Boolean = false, // TODO: Enable dark theme when color scheme will be consistent
    content: @Composable() () -> Unit,
) {
    val rippleConfiguration = RippleConfiguration(color = Color.Noise.one.mediumLight)

    // Limit device font scaling to 130% to avoid layout breaks while keeping accessibility
    val density = Density(
        density = LocalDensity.current.density,
        fontScale = min(LocalDensity.current.fontScale, 1.3f)
    )

    val colorScheme = when {
        darkTheme -> darkColorScheme
        else -> lightColorScheme
    }

    CompositionLocalProvider(
        LocalRippleConfiguration provides rippleConfiguration,
        LocalDensity provides density,
        LocalContentColor provides colorScheme.onSurface,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = noiseCaptureTypography(),
            content = content,
        )
    }
}
