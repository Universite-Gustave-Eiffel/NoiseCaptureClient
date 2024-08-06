package org.noiseplanet.noisecapture.ui.features.measurement.spectrum

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import org.noiseplanet.noisecapture.ui.features.measurement.MeasurementScreen.Companion
import org.noiseplanet.noisecapture.ui.features.measurement.MeasurementScreen.Companion.makeXLabels
import org.noiseplanet.noisecapture.ui.features.measurement.MeasurementScreen.Companion.tickLength
import org.noiseplanet.noisecapture.ui.features.measurement.MeasurementScreen.Companion.tickStroke
import org.noiseplanet.noisecapture.ui.features.measurement.PlotBitmapOverlay
import org.noiseplanet.noisecapture.ui.features.measurement.SPECTRUM_PLOT_SQUARE_OFFSET
import org.noiseplanet.noisecapture.ui.features.measurement.SPECTRUM_PLOT_SQUARE_WIDTH
import org.noiseplanet.noisecapture.ui.features.measurement.formatFrequency
import org.noiseplanet.noisecapture.ui.features.measurement.spectrum.SpectrumPlotViewModel.Companion.DBA_MAX
import org.noiseplanet.noisecapture.ui.features.measurement.spectrum.SpectrumPlotViewModel.Companion.DBA_MIN
import kotlin.math.max
import kotlin.math.min

@Composable
fun SpectrumPlotView(
    viewModel: SpectrumPlotViewModel,
    modifier: Modifier = Modifier,
) {
    val surfaceColor = MaterialTheme.colorScheme.onSurface

    var preparedSpectrumOverlayBitmap = PlotBitmapOverlay(
        ImageBitmap(1, 1),
        Size(0F, 0F),
        Size(0F, 0F),
        Size(0F, 0F),
        0
    )

    val rawSpl: DoubleArray by viewModel.rawSplFlow
        .collectAsState(DoubleArray(0))
    val weightedSpl: DoubleArray by viewModel.weightedSplFlow
        .collectAsState(DoubleArray(0))
    val axisSettings: SpectrumPlotViewModel.AxisSettings by viewModel.axisSettingsFlow
        .collectAsState(
            SpectrumPlotViewModel.AxisSettings(0.0, 0.0, emptyList())
        )


    Canvas(modifier) {
        val pathEffect = PathEffect.dashPathEffect(
            floatArrayOf(
                SPECTRUM_PLOT_SQUARE_WIDTH.toPx(),
                SPECTRUM_PLOT_SQUARE_OFFSET.toPx()
            )
        )
        val weightedBarWidth = 10.dp.toPx()
        val maxYAxisWidth = preparedSpectrumOverlayBitmap.verticalLegendSize.width
        val barMaxWidth: Float = size.width - maxYAxisWidth
        val maxXAxisHeight = preparedSpectrumOverlayBitmap.horizontalLegendSize.height
        val chartHeight = (size.height - maxXAxisHeight - tickLength.toPx())
        val barHeight = chartHeight / rawSpl.size - SPECTRUM_PLOT_SQUARE_OFFSET.toPx()

        rawSpl.forEachIndexed { index, spl ->
            val barYOffset =
                (barHeight + SPECTRUM_PLOT_SQUARE_OFFSET.toPx()) * (rawSpl.size - 1 - index)
            val splRatio = (spl - DBA_MIN) / (DBA_MAX - DBA_MIN)
            val splWeighted = max(spl, weightedSpl[index])
            val splWeightedRatio = min(
                1.0,
                max(
                    0.0,
                    (splWeighted - DBA_MIN) / (DBA_MAX - DBA_MIN)
                )
            )
            val splGradient =
                Brush.horizontalGradient(
                    *viewModel.spectrumColorRamp,
                    startX = 0F,
                    endX = size.width
                )
            drawLine(
                brush = splGradient,
                start = Offset(maxYAxisWidth, barYOffset + barHeight / 2),
                end = Offset(
                    max(
                        maxYAxisWidth,
                        ((barMaxWidth * splRatio).toFloat() + maxYAxisWidth)
                    ),
                    barYOffset + barHeight / 2
                ),
                strokeWidth = barHeight,
                pathEffect = pathEffect
            )
            drawRect(
                color = surfaceColor,
                topLeft = Offset(
                    max(
                        maxYAxisWidth,
                        (barMaxWidth * splWeightedRatio).toFloat() - weightedBarWidth + maxYAxisWidth
                    ), barYOffset
                ),
                size = Size(weightedBarWidth, barHeight)
            )
        }
    }

    val colors = MaterialTheme.colorScheme
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = Modifier.fillMaxSize()) {
        if (preparedSpectrumOverlayBitmap.imageSize != size ||
            preparedSpectrumOverlayBitmap.plotSettingsHashCode != axisSettings.hashCode()
        ) {
            preparedSpectrumOverlayBitmap = buildSpectrumAxisBitmap(
                size,
                Density(density),
                axisSettings,
                textMeasurer,
                colors
            )
        }
        drawImage(preparedSpectrumOverlayBitmap.imageBitmap)
    }
}


/**
 * Generate bitmap of Axis (as it does not change between redraw of values)
 */
@Suppress("LongParameterList", "LongMethod")
private fun buildSpectrumAxisBitmap(
    size: Size,
    density: Density,
    settings: SpectrumPlotViewModel.AxisSettings,
    textMeasurer: TextMeasurer,
    colors: ColorScheme,
): PlotBitmapOverlay {
    val drawScope = CanvasDrawScope()
    val bitmap = ImageBitmap(size.width.toInt(), size.height.toInt())
    val canvas = androidx.compose.ui.graphics.Canvas(bitmap)
    val legendTexts = List(settings.nominalFrequencies.size) { frequencyIndex ->
        val textLayoutResult = textMeasurer.measure(buildAnnotatedString {
            withStyle(
                SpanStyle(
                    fontSize = TextUnit(
                        10F,
                        TextUnitType.Sp
                    )
                )
            )
            { append(formatFrequency(settings.nominalFrequencies[frequencyIndex].toInt())) }
        })
        textLayoutResult
    }

    var horizontalLegendSize = Size(0F, 0F)
    var verticalLegendSize = Size(0F, 0F)
    drawScope.draw(
        density = density,
        layoutDirection = LayoutDirection.Ltr,
        canvas = canvas,
        size = size,
    ) {
        val maxYAxisWidth = (legendTexts.maxOfOrNull { it.size.width }) ?: 0
        verticalLegendSize = Size(maxYAxisWidth.toFloat(), size.height)
        val barMaxWidth: Float = size.width - maxYAxisWidth
        val legendElements = makeXLabels(
            textMeasurer, settings.minimumX, settings.maximumX, barMaxWidth,
            Companion::noiseLevelAxisFormater
        )
        val maxXAxisHeight = (legendElements.maxOfOrNull { it.text.size.height }) ?: 0
        horizontalLegendSize = Size(size.width, maxXAxisHeight.toFloat())
        val chartHeight = (size.height - maxXAxisHeight - tickLength.toPx())
        legendElements.forEach { legendElement ->
            val tickPos =
                maxYAxisWidth + max(
                    tickStroke.toPx() / 2F,
                    min(
                        barMaxWidth - tickStroke.toPx(),
                        legendElement.xPos - tickStroke.toPx() / 2F
                    )
                )
            drawLine(
                color = colors.onSurfaceVariant, start = Offset(
                    tickPos,
                    chartHeight
                ),
                end = Offset(
                    tickPos,
                    chartHeight + tickLength.toPx()
                ),
                strokeWidth = tickStroke.toPx()
            )
            drawText(
                legendElement.text,
                topLeft = Offset(
                    maxYAxisWidth + legendElement.textPos,
                    chartHeight + tickLength.toPx()
                )
            )
        }
        val barHeight =
            chartHeight / settings.nominalFrequencies.size - SPECTRUM_PLOT_SQUARE_OFFSET.toPx()
        legendTexts.forEachIndexed { index, legendText ->
            val barYOffset =
                (barHeight + SPECTRUM_PLOT_SQUARE_OFFSET.toPx()) * (settings.nominalFrequencies.size - 1 - index)
            drawText(
                textMeasurer,
                legendText.layoutInput.text,
                topLeft = Offset(
                    0F,
                    barYOffset + barHeight / 2 - legendText.size.height / 2F
                )
            )
        }
    }
    return PlotBitmapOverlay(
        bitmap,
        size,
        horizontalLegendSize,
        verticalLegendSize,
        settings.hashCode()
    )
}
