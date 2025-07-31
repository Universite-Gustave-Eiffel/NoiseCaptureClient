package org.noiseplanet.noisecapture.ui.components.plot

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import io.github.koalaplot.core.style.LineStyle


object PlotGridLineStyle {

    val majorHorizontal
        @Composable get() = LineStyle(
            brush = SolidColor(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)),
            strokeWidth = 1.dp,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f)),
        )

    val minorHorizontal
        @Composable get() = LineStyle(
            brush = SolidColor(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)),
            strokeWidth = 1.dp,
        )

    val majorVertical
        @Composable get() = LineStyle(
            brush = SolidColor(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)),
            strokeWidth = 1.dp,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f)),
        )

    val minorVertical
        @Composable get() = LineStyle(
            brush = SolidColor(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)),
            strokeWidth = 1.dp,
        )
}
