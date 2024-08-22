package org.noiseplanet.noisecapture.ui.features.measurement.plot.spectrogram

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.toSize
import org.noiseplanet.noisecapture.services.DefaultMeasurementService.Companion.FFT_HOP
import org.noiseplanet.noisecapture.ui.features.measurement.DEFAULT_SAMPLE_RATE
import org.noiseplanet.noisecapture.ui.features.measurement.plot.PlotAxisBuilder
import org.noiseplanet.noisecapture.ui.features.measurement.plot.PlotBitmapOverlay
import org.noiseplanet.noisecapture.ui.features.measurement.plot.spectrogram.SpectrogramPlotViewModel.Companion.REFERENCE_LEGEND_TEXT
import org.noiseplanet.noisecapture.ui.features.measurement.plot.spectrogram.SpectrogramPlotViewModel.Companion.SPECTROGRAM_STRIP_WIDTH
import org.noiseplanet.noisecapture.util.toFrequencyString
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min

@Composable
fun SpectrogramPlotView(
    viewModel: SpectrogramPlotViewModel,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val textMeasurer = rememberTextMeasurer()

    var preparedSpectrogramOverlayBitmap =
        PlotBitmapOverlay(
            ImageBitmap(1, 1),
            Size(0F, 0F),
            Size(0F, 0F),
            Size(0F, 0F),
            0
        )

    val sampleRate: Double by viewModel.sampleRateFlow
        .collectAsState(DEFAULT_SAMPLE_RATE)
    val spectrogramBitmaps: List<SpectrogramBitmap> by viewModel.spectrogramBitmapFlow
        .collectAsState(emptyList())

    Canvas(modifier = Modifier.fillMaxSize()) {
        val spectrogramCanvasSize = IntSize(
            (size.width - preparedSpectrogramOverlayBitmap.verticalLegendSize.width).toInt(),
            (size.height - preparedSpectrogramOverlayBitmap.horizontalLegendSize.height).toInt()
        )
        viewModel.updateCanvasSize(spectrogramCanvasSize)

        drawRect(
            color = SpectrogramBitmap.colorRamp[0],
            size = spectrogramCanvasSize.toSize()
        )
        viewModel.currentStripData?.let { currentStripData ->
            val offset = currentStripData.offset
            spectrogramBitmaps.reversed().forEachIndexed { index, spectrogramBitmap ->
                val bitmapX = size.width -
                    preparedSpectrogramOverlayBitmap.verticalLegendSize.width -
                    (index * SPECTROGRAM_STRIP_WIDTH + offset).toFloat()
                drawImage(
                    spectrogramBitmap.toImageBitmap(),
                    topLeft = Offset(bitmapX, 0F)
                )
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        if (preparedSpectrogramOverlayBitmap.imageSize != size) {
            preparedSpectrogramOverlayBitmap = buildSpectrogramAxisBitmap(
                size,
                Density(density),
                viewModel.scaleMode,
                sampleRate,
                textMeasurer,
                colors
            )
        }
        drawImage(preparedSpectrogramOverlayBitmap.imageBitmap)
    }
}


@Suppress("LongParameterList", "LongMethod")
private fun buildSpectrogramAxisBitmap(
    size: Size,
    density: Density,
    scaleMode: SpectrogramBitmap.ScaleMode,
    sampleRate: Double,
    textMeasurer: TextMeasurer,
    colors: ColorScheme,
): PlotBitmapOverlay {
    val drawScope = CanvasDrawScope()
    val bitmap = ImageBitmap(size.width.toInt(), size.height.toInt())
    val canvas = androidx.compose.ui.graphics.Canvas(bitmap)

    var frequencyLegendPosition = when (scaleMode) {
        SpectrogramBitmap.ScaleMode.SCALE_LOG -> SpectrogramBitmap.frequencyLegendPositionLog
        else -> SpectrogramBitmap.frequencyLegendPositionLinear
    }
    frequencyLegendPosition = frequencyLegendPosition
        .filter { f -> f < sampleRate / 2 }
        .toIntArray()
    val timeXLabelMeasure = textMeasurer.measure(REFERENCE_LEGEND_TEXT)
    val timeXLabelHeight = timeXLabelMeasure.size.height
    val maxYLabelWidth = frequencyLegendPosition.maxOf { frequency ->
        val text = frequency.toFrequencyString()
        textMeasurer.measure(text).size.width
    }
    var bottomLegendSize = Size(0F, 0F)
    var rightLegendSize = Size(0F, 0F)
    drawScope.draw(
        density = density,
        layoutDirection = LayoutDirection.Ltr,
        canvas = canvas,
        size = size,
    ) {
        val axisBuilder = PlotAxisBuilder()
        val legendHeight = timeXLabelHeight + axisBuilder.tickLength.toPx()
        val legendWidth = maxYLabelWidth + axisBuilder.tickLength.toPx()
        bottomLegendSize = Size(size.width - legendWidth, legendHeight)
        rightLegendSize = Size(legendWidth, size.height - legendHeight)
        if (sampleRate > 1) {
            // draw Y axe labels
            val fMax = sampleRate / 2
            val fMin = frequencyLegendPosition[0].toDouble()
            val sheight = (size.height - legendHeight).toInt()
            frequencyLegendPosition.forEach { frequency ->
                val text = buildAnnotatedString {
                    withStyle(style = SpanStyle()) {
                        append(frequency.toFrequencyString())
                    }
                }
                val textSize = textMeasurer.measure(text)
                val tickHeightPos = when (scaleMode) {
                    SpectrogramBitmap.ScaleMode.SCALE_LOG -> {
                        sheight - (log10(frequency / fMin) / ((log10(fMax / fMin) / sheight))).toInt()
                    }

                    else -> (sheight - frequency / fMax * sheight).toInt()
                }
                drawLine(
                    color = colors.onSurfaceVariant, start = Offset(
                        size.width - legendWidth,
                        tickHeightPos.toFloat() - axisBuilder.tickStroke.toPx() / 2
                    ),
                    end = Offset(
                        size.width - legendWidth + axisBuilder.tickLength.toPx(),
                        tickHeightPos.toFloat() - axisBuilder.tickStroke.toPx() / 2
                    ),
                    strokeWidth = axisBuilder.tickStroke.toPx()
                )
                val textPos = min(
                    (size.height - textSize.size.height).toInt(),
                    max(0, tickHeightPos - textSize.size.height / 2)
                )
                drawText(
                    textMeasurer,
                    text,
                    topLeft = Offset(
                        size.width - legendWidth + axisBuilder.tickLength.toPx(),
                        textPos.toFloat()
                    )
                )
            }
            val xLegendWidth = (size.width - legendWidth)
            val legendElements = axisBuilder.makeXLabels(
                textMeasurer,
                (FFT_HOP / sampleRate) * xLegendWidth, 0.0,
                xLegendWidth,
                axisBuilder::timeAxisFormater
            )
            legendElements.forEach { legendElement ->
                val tickPos =
                    max(
                        axisBuilder.tickStroke.toPx() / 2F,
                        min(
                            xLegendWidth - axisBuilder.tickStroke.toPx(),
                            legendElement.xPos - axisBuilder.tickStroke.toPx() / 2F
                        )
                    )
                drawLine(
                    color = colors.onSurfaceVariant, start = Offset(
                        tickPos,
                        sheight.toFloat()
                    ),
                    end = Offset(
                        tickPos,
                        sheight + axisBuilder.tickLength.toPx()
                    ),
                    strokeWidth = axisBuilder.tickStroke.toPx()
                )
                drawText(
                    legendElement.text,
                    topLeft = Offset(
                        legendElement.textPos,
                        sheight.toFloat() + axisBuilder.tickLength.toPx()
                    )
                )
            }
        }
    }
    return PlotBitmapOverlay(
        bitmap,
        size,
        bottomLegendSize,
        rightLegendSize,
        scaleMode.hashCode()
    )
}
