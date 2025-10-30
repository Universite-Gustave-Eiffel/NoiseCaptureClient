package org.noiseplanet.noisecapture.ui.features.recording.plot.spectrogram

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.noiseplanet.noisecapture.ui.components.plot.PlotContainer


@Composable
fun SpectrogramPlotView(
    modifier: Modifier = Modifier,
) {

    // - Properties

    val viewModel: SpectrogramPlotViewModel = koinViewModel()

    val spectrogramBitmap: SpectrogramBitmap? by viewModel.bitmapFlow.collectAsStateWithLifecycle()
    val density: Density = LocalDensity.current
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current


    // - Lifecycle

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    viewModel.startSpectrogram()
                }

                Lifecycle.Event.ON_STOP -> {
                    viewModel.stopSpectrogram()
                }

                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }


    // - Layout

    // TODO: Use koalaplot to draw axes instead
    PlotContainer(
        axisSettings = viewModel.axisSettings,
        tintColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
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
                drawImage(it.bitmap)
            }
        }
    }
}
