package org.noiseplanet.noisecapture.ui.features.recording.plot.spectrogram

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle


@Composable
fun SpectrogramPlotView(
    viewModel: SpectrogramPlotViewModel,
    modifier: Modifier = Modifier,
) {

    // - Properties

    val spectrogramBitmap: ImageBitmap? by viewModel.bitmapFlow.collectAsStateWithLifecycle()
    val density: Density = LocalDensity.current


    // - Layout

    SpectrogramPlotContainer(
        xAxisTicks = SpectrogramPlotViewModel.X_AXIS_TICKS,
        yAxisTicks = viewModel.yAxisTicks,
        modifier = modifier
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
                .onPlaced { coordinates ->
                    viewModel.updateCanvasSize(coordinates.size, newDensity = density)
                }
                .blur(radius = 1.dp) // Apply a 1dp blur to the image to antialias to current density
        ) {
            spectrogramBitmap?.let {
                drawImage(it)
            }
        }
    }
}


// TODO: Move this to a shared component
@Composable
private fun SpectrogramPlotContainer(
    xAxisTicks: List<String>,
    yAxisTicks: List<String>,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    // - Properties

    val xAxisTicksHeight = 12.dp


    // - Layout

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier.weight(1f)
            ) {
                content()
            }

            // X axis
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.height(xAxisTicksHeight).fillMaxWidth()
            ) {
                for (tick in xAxisTicks) {
                    AxisTickLabel(text = tick)
                }
            }
        }

        // Y axis
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.fillMaxHeight()
                .padding(bottom = xAxisTicksHeight + 4.dp)
        ) {
            for (tick in yAxisTicks.reversed()) {
                AxisTickLabel(text = tick)
            }
        }
    }
}


// TODO: Move this to a shared component
@Composable
private fun AxisTickLabel(text: String) = Text(
    text = text,
    style = MaterialTheme.typography.labelSmall,
    fontWeight = FontWeight.SemiBold,
    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
)
