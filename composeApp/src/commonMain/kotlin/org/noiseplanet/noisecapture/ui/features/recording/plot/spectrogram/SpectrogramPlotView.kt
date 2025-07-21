package org.noiseplanet.noisecapture.ui.features.recording.plot.spectrogram

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.noiseplanet.noisecapture.ui.components.plot.PlotContainer


@Composable
fun SpectrogramPlotView(
    viewModel: SpectrogramPlotViewModel,
    modifier: Modifier = Modifier,
) {

    // - Properties

    val spectrogramBitmap: ImageBitmap? by viewModel.bitmapFlow.collectAsStateWithLifecycle()
    val density: Density = LocalDensity.current


    // - Layout

    PlotContainer(
        axisSettings = viewModel.axisSettings,
        modifier = modifier,
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
                .onPlaced { coordinates ->
                    viewModel.updateCanvasSize(coordinates.size, newDensity = density)
                }
                .blur(radius = 2.dp) // Apply a 1dp blur to the image to antialias to current density
        ) {
            spectrogramBitmap?.let {
                drawImage(it)
            }
        }
    }
}
