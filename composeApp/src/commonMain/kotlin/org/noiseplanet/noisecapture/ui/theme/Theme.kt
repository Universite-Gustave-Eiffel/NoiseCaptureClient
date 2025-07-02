package org.noiseplanet.noisecapture.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable


@Composable
fun AppTheme(
    content: @Composable() () -> Unit,
) {
    val colors = lightColorScheme()

    MaterialTheme(
        colorScheme = colors,
        typography = notoSansTypography(),
        content = content,
    )
}
